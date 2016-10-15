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
package com.nextep.designer.synch.policies;

import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

public class ImportPolicyAddDataSet extends ImportPolicyAddOnly {

	@Override
	protected boolean unexistingObject(IVersionable<?> importing, IVersionContainer targetContainer) {
		// We inject the version tag to make sure it is set
		final IVersionInfo version = importing.getVersion();
		version.setVersionTag(VersionHelper.computeVersion(version));
		// Adding data set to workspace
		super.unexistingObject(importing, targetContainer);
		// Saving data lines
		IDataSet set = (IDataSet) importing.getVersionnedObject().getModel();
		final IDataService dataService = DbgmPlugin.getService(IDataService.class);
		dataService.saveDataLinesToRepository(set, new NullProgressMonitor());
		// Since we import the dataset in the workspace, we need to make it a regular repository
		// dataset, we do this by emptying the handle which will force neXtep to refetch it from
		// the repository
		set.setStorageHandle(null);
		return true;
	}

}
