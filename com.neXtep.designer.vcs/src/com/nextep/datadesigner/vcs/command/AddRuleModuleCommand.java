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
package com.nextep.datadesigner.vcs.command;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.impl.CommandWithProgress;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.impl.VersionContainer;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * Adds a module to a view. This command is designed for the view rules' editor to load module from
 * the {@link ContainerInfo} summary and injects it to the view.
 * 
 * @author Christophe Fondacci
 */
public class AddRuleModuleCommand extends CommandWithProgress {

	private ContainerInfo info;
	private IWorkspace view;

	public AddRuleModuleCommand(ContainerInfo container, IWorkspace view) {
		this.info = container;
		this.view = view;
	}

	@Override
	public Object execute(Object... parameters) {
		IVersionContainer c = (IVersionContainer) CorePlugin.getIdentifiableDao().load(
				VersionContainer.class, info.getRelease().getUID());
		if (c != null) {
			final IVersionable<IVersionContainer> v = VersionHelper.getVersionable(c);
			CorePlugin.getService(IReferenceManager.class).reference(v.getReference(), v, true);
			// Referencing all contents (because if object was already in Hibernate session it has
			// not been done)
			List<ITypedObject> toRelink = new ArrayList<ITypedObject>();
			for (IReferenceable r : v.getReferenceMap().values()) {
				CorePlugin.getService(IReferenceManager.class).reference(r.getReference(), r, true);
				if (r instanceof ITypedObject) {
					toRelink.add((ITypedObject) r);
				}
			}
			getProgressMonitor().worked(1);
			getProgressMonitor().subTask("Computing dependencies...");
			MultiValueMap invRefMap = CorePlugin.getService(IReferenceManager.class).getReverseDependenciesMap();
			getProgressMonitor().worked(1);
			getProgressMonitor().subTask("Linking imported module...");
			for (ITypedObject obj : toRelink) {
				VersionHelper.relink(obj, invRefMap);
			}
			view.addVersionable(VersionHelper.getVersionable(c), new ImportPolicyAddOnly());

		}
		return null;
	}

	@Override
	public String getName() {
		return VCSMessages.getString("viewRulesWizardLoadContainer");
	}

}
