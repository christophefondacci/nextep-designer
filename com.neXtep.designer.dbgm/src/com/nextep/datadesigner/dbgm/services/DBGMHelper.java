/*******************************************************************************
 * Copyright (c) 2011 neXtep Software and contributors.
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
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
/**
 *
 */
package com.nextep.datadesigner.dbgm.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.impl.Domain;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.model.IDatabaseRawObject;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.services.IDatatypeService;
import com.nextep.designer.dbgm.services.IParsingService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ITargetSet;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class DBGMHelper {

	private static final Log LOGGER = LogFactory.getLog(DBGMHelper.class);

	private static final String EXTENSION_ID_DATATYPE_PROVIDER = "com.neXtep.designer.dbgm.datatypeProvider"; //$NON-NLS-1$
	private static final String EXTENSION_ID_SQL_TYPED_PARSER = "com.neXtep.designer.dbgm.sqlTypedParser"; //$NON-NLS-1$

	private static Map<DBVendor, IDatatypeProvider> datatypeProviders = new HashMap<DBVendor, IDatatypeProvider>();
	private static List<DBVendor> failedDatatypeProviderVendors = new ArrayList<DBVendor>();

	/**
	 * @return the target set defined for this view. If no target set exists for
	 *         the specified view it will be created and returned.
	 * @deprecated please use {@link IWorkspaceService#getCurrentViewTargets()}
	 *             instead
	 */
	@Deprecated
	public static ITargetSet getTargetSet() {
		return VCSPlugin.getViewService().getCurrentViewTargets();
	}

	/**
	 * Retrieves the collection of all foreign key constraints which reference
	 * the specified unique key constraint.
	 * 
	 * @param uk
	 *            the unique key constraint
	 * @return a collection of {@link ForeignKeyConstraint} <i>fk</i> which
	 *         match the following condition:<br>
	 *         fk.getRemoteConstraint() == uk
	 * @throws ReferenceNotFoundException
	 *             when the unique key cannot be located in the current view.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ForeignKeyConstraint> getForeignKeys(UniqueKeyConstraint uk,
			boolean repository) throws ReferenceNotFoundException {
		if (uk == null)
			return Collections.EMPTY_LIST;

		// Retrieving table in repository
		IBasicTable repositoryTable = null;
		if (repository) {
			repositoryTable = (IBasicTable) CorePlugin.getService(IReferenceManager.class)
					.findByTypeName(uk.getConstrainedTable().getType(),
							uk.getConstrainedTable().getName());
		} else {
			repositoryTable = uk.getConstrainedTable();
		}
		UniqueKeyConstraint repositoryUK = null;
		for (IKeyConstraint repoUK : repositoryTable.getConstraints()) {
			switch (repoUK.getConstraintType()) {
			case PRIMARY:
			case UNIQUE:
				// Name based match because we might have non-repository unique
				// keys here
				if (repoUK.getName().equals(uk.getName())) {
					repositoryUK = (UniqueKeyConstraint) repoUK;
				}
				break;
			}
		}

		if (repositoryUK != null) {
			Collection<IReferencer> dependencies = CorePlugin.getService(IReferenceManager.class)
					.getReverseDependencies(repositoryUK);
			List<ForeignKeyConstraint> foreignConstraints = new ArrayList<ForeignKeyConstraint>();
			for (IReferencer r : dependencies) {
				if (r instanceof ForeignKeyConstraint) {
					foreignConstraints.add((ForeignKeyConstraint) r);
				}
			}
			return foreignConstraints;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * This method is a helper which retrieves all foreign keys which are
	 * currently enforced by the specified index. TODO: handle backup indexes
	 * which would still enforce the constraint after an enforcing index removal
	 * 
	 * @param index
	 *            index which may enforce a foreign key constraint
	 * @return foreign keys enforced by the specified index
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ForeignKeyConstraint> getForeignKeysForIndex(IIndex index) {
		if (index == null)
			return Collections.EMPTY_LIST;
		IBasicTable t = index.getIndexedTable();
		List<ForeignKeyConstraint> fkeys = new ArrayList<ForeignKeyConstraint>();
		for (IKeyConstraint c : t.getConstraints()) {
			if (c.getConstraintType() == ConstraintType.FOREIGN) {
				ForeignKeyConstraint fk = (ForeignKeyConstraint) c;
				// If we have several enforcing index / constraints for this FK,
				// the index is not
				// the enforcing one (i.e. it could be removed without any
				// problem) so we only
				// consider the case of FK having one unique enforcing index /
				// constraint
				if (fk.getEnforcingIndex().size() == 1) {
					final IDatabaseRawObject enforcingIndex = fk.getEnforcingIndex().iterator()
							.next();
					// We need to be compatible with volatiles so we check
					// reference ids
					if (enforcingIndex == index) {
						fkeys.add(fk);
					} else if (enforcingIndex != null && enforcingIndex.getReference() != null
							&& enforcingIndex.getReference().equals(index.getReference())) {
						fkeys.add(fk);
					} else if (enforcingIndex != null
							&& enforcingIndex.getReference().getUID() != null
							&& enforcingIndex.getReference().getUID()
									.equals(index.getReference().getUID())) {
						fkeys.add(fk);
					}
				}
			}
		}
		return fkeys;
	}

	/**
	 * Retrieves the primary key of a table
	 * 
	 * @param table
	 *            table
	 * @return the table's primary key (or null if none)
	 */
	public static UniqueKeyConstraint getPrimaryKey(IColumnable columnable) {
		if (columnable instanceof IBasicTable) {
			final IBasicTable table = (IBasicTable) columnable;
			for (IKeyConstraint c : table.getConstraints()) {
				if (c.getConstraintType() == ConstraintType.PRIMARY) {
					return (UniqueKeyConstraint) c;
				}
			}
		}
		return null;
	}

	/**
	 * A helper method which returns the current database vendor. At the moment,
	 * the method returns the default database vendor. It may evolve later on to
	 * be plugged on a customizable property. Clients who have to call a method
	 * with a {@link DBVendor} parameter should always retrieve the DBVendor by
	 * calling this method.
	 * 
	 * @return the currently defined database vendor
	 */
	public static DBVendor getCurrentVendor() {
		final IWorkspaceService workspaceService = VCSPlugin.getService(IWorkspaceService.class);
		final IWorkspace workspace = workspaceService.getCurrentWorkspace();
		if (workspace != null) {
			return workspace.getDBVendor();
		} else {
			return DBVendor.getDefaultVendor();
		}
	}

	/**
	 * @deprecated please use
	 *             {@link IDatatypeService#getDatatypeProvider(DBVendor)}
	 *             instead
	 */
	@Deprecated
	public static IDatatypeProvider getDatatypeProvider(DBVendor vendor) {
		return CorePlugin.getService(IDatatypeService.class).getDatatypeProvider(vendor);
	}

	/**
	 * This method converts each "newline" characters to the current client
	 * newline character and trims (or removes for mysql) empty lines.<br>
	 * To use when importing multi-line text from db.
	 * 
	 * @param input
	 *            multi line text
	 * @return clean client-compatible multi line text
	 */
	public static String trimEmptyLines(String input) {
		if (input == null)
			return null;
		BufferedReader srcReader = new BufferedReader(new StringReader(input));
		String line;
		StringBuffer output = new StringBuffer(input.length());
		final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$
		try {
			while ((line = srcReader.readLine()) != null) {
				if ("".equals(line.trim())) { //$NON-NLS-1$
					if (getCurrentVendor() == DBVendor.ORACLE) {
						output.append(NEWLINE);
					}
				} else {
					output.append(line + NEWLINE);
				}
			}
		} catch (IOException e) {
			throw new ErrorException("Error while triming trigger : " + e.getMessage(), e);
		}
		return output.toString().trim();
	}

	@Deprecated
	public static String getDatatypeLabel(IDatatype d) {
		return getDatatypeLabel(d, DBGMHelper.getCurrentVendor());
	}

	/**
	 * @deprecated please use
	 *             {@link IDatatypeService#getDatatypeLabel(IDatatype, DBVendor)}
	 */
	@Deprecated
	public static String getDatatypeLabel(IDatatype d, DBVendor vendor) {
		return CorePlugin.getService(IDatatypeService.class).getDatatypeLabel(d, vendor);
	}

	/**
	 * Parses the specified parseable element. All parsers attached to this
	 * element through the sqlTypedParser extension point will be invoked.
	 * 
	 * @param p
	 *            parseable element to parse.
	 * @param contentsToParse
	 *            string to parse
	 * @deprecated use {@link IParsingService#parse(IParseable)} instead
	 */
	@Deprecated
	public static void parse(IParseable p) {
		DbgmPlugin.getService(IParsingService.class).parse(p);
	}

	/**
	 * PArses a specific source into the supplied {@link IParseable}.
	 * 
	 * @param p
	 *            the {@link IParseable} in which parse info should be stored
	 * @param parseSource
	 *            the source code to parse
	 * @deprecated use {@link IParsingService#parse(IParseable, String)} instead
	 */
	@Deprecated
	public static void parse(IParseable p, String parseSource) {
		DbgmPlugin.getService(IParsingService.class).parse(p, parseSource);
	}

	/**
	 * A helper method which returns a {@link DBVendor} which corresponds to a
	 * repository object. Repository view can contain a mix of several different
	 * vendor modules. For a given object in the view, this method tries to
	 * retrieve the appropriate vendor.<br>
	 * Result of this method may not be accurate for non versionable typed
	 * objects (like FKs, PK, columns).
	 * 
	 * @param o
	 *            the {@link ITypedObject} to evaluate the vendor of
	 * @return the corresponding {@link DBVendor}
	 */
	public static DBVendor getVendorFor(ITypedObject o) {
		if (o instanceof IVersionContainer) {
			return ((IVersionContainer) o).getDBVendor();
		} else if (o instanceof IVersionable<?>) {
			final IVersionContainer container = ((IVersionable<?>) o).getContainer();
			if (container != null) {
				return container.getDBVendor();
			}
		}
		return getCurrentVendor();
	}

	public static Collection<? extends IDomain> listDomains() {
		// Session s = HibernateUtil.getInstance().getSandBoxSession();
		// s.clear();
		return CorePlugin.getIdentifiableDao().loadAll(Domain.class);
	}

	/**
	 * Retrieves the remote table referenced by the specified foreign key
	 * 
	 * @param fk
	 *            foreign key
	 * @return the remote table which this FK references
	 */
	public static IBasicTable getRemoteTable(ForeignKeyConstraint fk) {
		IKeyConstraint remotePK = fk.getRemoteConstraint();
		return remotePK.getConstrainedTable();
	}

	/**
	 * @deprecated please use
	 *             {@link IDatatypeService#getDatatype(DBVendor, IDatatype)}
	 */
	@Deprecated
	public static IDatatype getDatatype(DBVendor generationVendor, IDatatype type) {
		return CorePlugin.getService(IDatatypeService.class).getDatatype(generationVendor, type);
	}

}
