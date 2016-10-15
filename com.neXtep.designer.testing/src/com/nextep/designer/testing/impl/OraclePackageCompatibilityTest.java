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
package com.nextep.designer.testing.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.vcs.impl.VersionContainer;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.testing.model.TestEvent;
import com.nextep.designer.testing.model.TestStatus;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

public class OraclePackageCompatibilityTest extends CompatibilityTest {

	@Override
	public String getName() {
		return "Stored procedures check";
	}

	@Override
	public TestStatus run(IProgressMonitor monitor) {
		// Retrieving compatiblity container
		final IVersionContainer compatContainer = (IVersionContainer) CorePlugin
				.getIdentifiableDao().load(VersionContainer.class,
						getCompatibilityRelease().getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), true);
		if (compatContainer == null) {
			return TestStatus.FAILED;
		}
		// Retrieving current container
		final IVersionContainer container = getContainer();
		// Compatible packages list
		final List<IVersionable<?>> compatPackages = VersionHelper.getAllVersionables(
				compatContainer, IElementType.getInstance(IPackage.TYPE_ID));
		final List<IVersionable<?>> packages = VersionHelper.getAllVersionables(container,
				IElementType.getInstance(IPackage.TYPE_ID));
		// Hashing current packages by their reference ID to easily retrieve them
		final Map<UID, IPackage> packageMap = hashByRefID(packages);
		// Processing old packages...
		for (IVersionable<?> v : compatPackages) {
			if (monitor.isCanceled())
				return TestStatus.FAILED;
			final IPackage oldPackage = (IPackage) v.getVersionnedObject().getModel();
			// Corresponding current package
			final IPackage currentPackage = packageMap.get(v.getReference().getUID());
			// If none, no compatibility
			if (currentPackage == null) {
				handle(v, TestEvent.COMPATIBILITY, TestStatus.FAILED, "Package does no more exist");
				continue;
			}
			// Parsing
			DBGMHelper.parse(oldPackage);
			DBGMHelper.parse(currentPackage);
			boolean passed = true;
			for (IProcedure proc : oldPackage.getProcedures()) {
				if (findCompatibleProcedure(proc, currentPackage.getProcedures()) == null) {
					handle(v, TestEvent.COMPATIBILITY, TestStatus.FAILED,
							"No compatibility found for: " + proc.getHeader());
					passed = false;
				}
			}
			if (passed) {
				handle(v, TestEvent.COMPATIBILITY, TestStatus.PASSED, null);
			}
		}

		// TODO Auto-generated method stub
		return null;
	}

	private Map<UID, IPackage> hashByRefID(Collection<IVersionable<?>> packages) {
		final Map<UID, IPackage> refMap = new HashMap<UID, IPackage>();
		for (IVersionable<?> v : packages) {
			refMap.put(v.getReference().getUID(), (IPackage) v.getVersionnedObject().getModel());
		}
		return refMap;
	}

	private IProcedure findCompatibleProcedure(IProcedure p, Collection<IProcedure> procs) {
		for (IProcedure eligibleProc : procs) {
			if (eligibleProc.getName().toUpperCase().equals(p.getName().toUpperCase())) {
				// Specific no-parameters case
				if (p.getParameters().size() == 0 && eligibleProc.getParameters().size() == 0) {
					return eligibleProc;
				}
				for (IProcedureParameter param : p.getParameters()) {
					Iterator<IProcedureParameter> eligibleParamIt = eligibleProc.getParameters()
							.iterator();
					boolean match = false;
					while (!match && eligibleParamIt.hasNext()) {
						final IProcedureParameter eligibleParam = eligibleParamIt.next();
						if (eligibleParam.getDatatype().getName()
								.equals(param.getDatatype().getName())) {
							match = true;
						} else if (eligibleParam.getDefaultExpr() != null
								&& !eligibleParam.getDefaultExpr().trim().isEmpty()) {
							break;
						}
					}
					// No parameter match, next procedure
					if (!match) {
						break;
					} else {
						return eligibleProc;
					}
				}
			}
		}
		// Falling here means no match has been found
		return null;
	}
}
