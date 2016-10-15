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
package com.nextep.designer.dbgm.policies;

import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.VersionPolicy;
import com.nextep.designer.vcs.policies.DefaultVersionPolicy;

/**
 * A specific version policy for data sets which ensures proper synchronization of the local storage
 * with the repository during versioning events.
 * 
 * @author Christophe Fondacci
 */
public class VersionPolicyDataSet implements VersionPolicy {

	private VersionPolicy defaultPolicy = DefaultVersionPolicy.getInstance();

	@Override
	public void checkIn(IVersionable<?> v, IVersioningOperationContext context) {
		final IDataService dataService = DbgmPlugin.getService(IDataService.class);
		// dataService.saveDataLinesToRepository((IDataSet) v.getVersionnedObject().getModel(),
		// new NullProgressMonitor());
		defaultPolicy.checkIn(v, context);
	}

	@Override
	public <V> IVersionable<V> checkOut(IVersionable<V> source, IVersioningOperationContext context) {
		final IVersionable<V> checkedOutObj = defaultPolicy.checkOut(source, context);
		// Transferring storage handle from source to checked out object
		final IDataSet sourceSet = (IDataSet) source.getVersionnedObject().getModel();
		final IDataSet targetSet = (IDataSet) checkedOutObj.getVersionnedObject().getModel();
		final IStorageHandle handle = sourceSet.getStorageHandle();
		targetSet.setStorageHandle(handle);
		// Removing storage from initial element, in case we need comparison it will be refetched
		// and it must not point on our working copy
		sourceSet.setStorageHandle(null);
		return checkedOutObj;

	}

	@Override
	public void debranch(IVersionable<?> v, IVersionBranch branch) {

	}

}
