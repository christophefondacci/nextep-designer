/*******************************************************************************
 * Copyright (c) 2013 neXtep Software and contributors.
 * All rights reserved.
 *
 * This file is part of neXtep designer.
 *
 * NeXtep designer is free software: you can redistribute it 
 * and/or modify it under the terms of the GNU General Public 
 * License as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 *
 * NeXtep designer is distributed in the hope that it will be 
 * useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with neXtep.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.Domain;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.dbgm.model.IDomainVendorType;
import com.nextep.datadesigner.dbgm.model.LengthType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.services.IDatatypeService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 * 
 */
public class DatatypeService implements IDatatypeService {

	private static final Log LOGGER = LogFactory.getLog(DBGMHelper.class);

	private static final String EXPR_DATATYPE_ANY = "*"; //$NON-NLS-1$
	private static final String EXPR_DATATYPE_ANY_ALT = "%"; //$NON-NLS-1$
	private static final String EXTENSION_ID_DATATYPE_PROVIDER = "com.neXtep.designer.dbgm.datatypeProvider"; //$NON-NLS-1$
	private static final String EXTENSION_ID_SQL_TYPED_PARSER = "com.neXtep.designer.dbgm.sqlTypedParser"; //$NON-NLS-1$

	private final Map<DBVendor, IDatatypeProvider> datatypeProviders = new HashMap<DBVendor, IDatatypeProvider>();
	private final List<DBVendor> failedDatatypeProviderVendors = new ArrayList<DBVendor>();

	private Map<String, Collection<IDomain>> domainsMap = new HashMap<String, Collection<IDomain>>();
	private Collection<IDomain> allDomains = new ArrayList<IDomain>();

	@Override
	public Collection<IDomain> getDomains() {
		if (allDomains.isEmpty()) {

			// Loading domains
			Collection<? extends IDomain> domains = CorePlugin.getIdentifiableDao().loadAll(
					Domain.class);

			// Here we hash all domains by their datatype name for fast lookup
			// during a SQL generation
			for (IDomain domain : domains) {
				addDomain(domain);
			}
		}
		return allDomains;
	}

	@Override
	public void addDomain(IDomain domain) {
		// Getting domain datatype name
		final String domainName = domain.getName().toUpperCase().trim();

		// Getting the domain list
		Collection<IDomain> typedDomains = domainsMap.get(domainName);

		// If not existing we create it
		if (typedDomains == null) {
			typedDomains = new ArrayList<IDomain>();
			domainsMap.put(domainName, typedDomains);
		}

		// Appending our domain to the list
		typedDomains.add(domain);
		allDomains.add(domain);
	}

	@Override
	public void removeDomain(IDomain domain) {
		allDomains.remove(domain);
		Collection<IDomain> typedDomains = domainsMap.get(domain.getName());
		typedDomains.remove(domain);
	}

	@Override
	public void reset() {
		domainsMap = new HashMap<String, Collection<IDomain>>();
		allDomains = new ArrayList<IDomain>();
		// Forcing refresh
		getDomains();

	}

	@Override
	public IDatatype getDatatype(DBVendor generationVendor, IDatatype type) {
		final DBVendor currVendor = getCurrentVendor();
		// Forcing cache load if needed
		getDomains();
		// We make this check whenever the generation vendor differs from
		// current
		if (!currVendor.equals(generationVendor)) {
			final Collection<IDomain> domains = domainsMap.get(type.getName().toUpperCase().trim());
			if (domains != null && !domains.isEmpty()) {
				IDomain selectedDomain = null;
				final List<IDomain> selectedDomains = new ArrayList<IDomain>();
				for (IDomain domain : domains) {
					// First computing the most matching domain, which is the
					// domain with the most non wildcard expression.
					if (matchDatatype(type, domain)) {
						if (selectedDomain == null
								|| (selectedDomain != null && (isWildcard(selectedDomain
										.getLengthExpr()) && !isWildcard(domain.getLengthExpr())))
								|| (isWildcard(selectedDomain.getPrecisionExpr()) && !isWildcard(domain
										.getPrecisionExpr()))) {
							selectedDomain = domain;
							// Adding it first
							selectedDomains.add(0, domain);
						} else {
							// Adding it last
							selectedDomains.add(domain);
						}

					}
				}
				// Now we iterate over all selected domains (most relevant
				// first, most generic last), computing length and precision
				// expression from the matching domain.
				for (IDomain domain : selectedDomains) {

					// Now iterating over vendor mapping. When no mapping is
					// defined, for our current vendor it will automatically
					// jump to next domain until a mapping is found. Ultimately,
					// when no vendor-specific match, it will return the
					// datatype unchanged
					for (IDomainVendorType vendorType : domain.getVendorTypes()) {
						final IDatatype vt = vendorType.getDatatype();

						if (vt != null && vt.getName() != null
								&& vendorType.getDBVendor().equals(generationVendor)) {
							// Adjusting length (-1 means erase, 0 keep,
							// otherwise override)
							int length = 0;
							if (vt.getLength() == null) {
								length = type.getLength();
							} else {
								length = vt.getLength();
							}
							// Adjusting precision (null means keep, otherwise
							// override)
							int precision = 0;
							if (vt.getPrecision() == null) {
								precision = type.getPrecision();
							} else {
								precision = vt.getPrecision();
							}
							return new Datatype(vt.getName(), length, precision);
						}
					}
				}
			}
		}
		return type;
	}

	/**
	 * Indicates whether the provided expression is a wildcard expression
	 * 
	 * @param expr
	 *            the expression to check
	 * @return <code>true</code> when the expression has wildcard (like * or %),
	 *         else <code>false</code>
	 */
	private boolean isWildcard(String expr) {
		return expr == null || EXPR_DATATYPE_ANY.equals(expr.trim())
				|| EXPR_DATATYPE_ANY_ALT.equals(expr.trim()) || expr.trim().isEmpty();
	}

	/**
	 * Matches the given datatype against a domain
	 * 
	 * @param datatype
	 *            the {@link IDatatype} to match
	 * @param domain
	 *            the {@link IDomain} to match with
	 * @return <code>true<code> when the domain matches the given datatype, thus indicating that the datatype might be converted
	 */
	private boolean matchDatatype(IDatatype datatype, IDomain domain) {
		final boolean matchLength = matchExpr(domain.getLengthExpr(), datatype.getLength());
		final boolean matchPrecision = matchExpr(domain.getPrecisionExpr(), datatype.getPrecision());
		return matchLength && matchPrecision;
	}

	private boolean matchExpr(String expr, int value) {
		if (isWildcard(expr)) {
			return true;
		} else {
			Integer val = null;
			try {
				val = Integer.valueOf(expr);
				if (val != null && value == val.intValue()) {
					return true;
				}
			} catch (NumberFormatException e) {
				LOGGER.error("Unable to parse domain expression '" + expr
						+ "' as a numeric expression: " + e.getMessage(), e);
			}
		}
		return false;
	}

	@Override
	public IDatatypeProvider getDatatypeProvider(DBVendor vendor) {
		IDatatypeProvider provider = datatypeProviders.get(vendor);
		if (null == provider) {
			if (!failedDatatypeProviderVendors.contains(vendor)) {
				IConfigurationElement elt = Designer.getInstance().getExtension(
						EXTENSION_ID_DATATYPE_PROVIDER, "dbVendor", vendor.name()); //$NON-NLS-1$
				if (elt != null) {
					try {
						provider = (IDatatypeProvider) elt.createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException e) {
						LOGGER.error("Error while instantiating datatype provider for vendor <"
								+ vendor.name() + ">"); //$NON-NLS-1$
						throw new ErrorException(e);
					}
					datatypeProviders.put(vendor, provider);
				}
			}
		}
		if (null == provider) {
			if (vendor != DBVendor.JDBC) {
				LOGGER.warn("No datatype provider found for vendor <" + vendor.name()
						+ ">, trying JDBC");
				if (!failedDatatypeProviderVendors.contains(vendor))
					failedDatatypeProviderVendors.add(vendor);
				return getDatatypeProvider(DBVendor.JDBC);
			}
			throw new ErrorException("No default JDBC datatype provider found");
		}
		return provider;
	}

	@Override
	public String getDatatypeLabel(IDatatype d, DBVendor vendor) {
		final StringBuffer buf = new StringBuffer(d.getName());
		final IDatatypeProvider provider = getDatatypeProvider(vendor);
		List<String> unsizableDatatypes = Collections.emptyList();
		if (vendor != null) {
			unsizableDatatypes = provider.getUnsizableDatatypes();
		}
		if (!unsizableDatatypes.contains(d.getName())) {
			if (d.getLength() > 0) {
				buf.append("(" + d.getLength()); //$NON-NLS-1$
				if (d.getPrecision() > 0) {
					buf.append("," + d.getPrecision()); //$NON-NLS-1$
				} else if (d.getLengthType() != LengthType.UNDEFINED) {

					// Checking support of datatype length type
					if (provider.isTypedLengthSupportedFor(d.getName())) {

						// If supported we append the proper type extension
						switch (d.getLengthType()) {
						case CHAR:
							buf.append(" CHAR"); //$NON-NLS-1$
							break;
						case BYTE:
							buf.append(" BYTE"); //$NON-NLS-1$
							break;
						}
					}
				}
				buf.append(")"); //$NON-NLS-1$
			}
		}
		return buf.toString();
	}

	@Override
	public String getDatatypeLabel(IDatatype d) {
		return getDatatypeLabel(d, DBGMHelper.getCurrentVendor());
	}

	private DBVendor getCurrentVendor() {
		final IWorkspaceService workspaceService = VCSPlugin.getService(IWorkspaceService.class);
		final IWorkspace workspace = workspaceService.getCurrentWorkspace();
		if (workspace != null) {
			return workspace.getDBVendor();
		} else {
			return DBVendor.getDefaultVendor();
		}
	}
}
