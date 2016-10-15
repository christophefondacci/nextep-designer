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
package com.nextep.designer.beng.ui.jface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.jface.ContainerInfoContentProvider;

public class ContainerInfoNotInDependenciesContentProvider extends
		ContainerInfoContentProvider {

	private boolean filterObsoleteDeps = false;
	public ContainerInfoNotInDependenciesContentProvider() {
		super(true,false);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement) {
		List<ContainerInfo> containers = null;
		if(!filterObsoleteDeps) {
			containers = getElementsList(VersionHelper.getCurrentView());
		} else {
			// When filtering obsolete dependencies we only consider current view contents
			List<IVersionable<?>> viewContents = VersionHelper.getAllVersionables(VersionHelper.getCurrentView(), IElementType.getInstance(IVersionContainer.TYPE_ID));
			containers = new ArrayList<ContainerInfo>();
			// Building list from view contents
			for(IVersionable<?> v : viewContents) {
				ContainerInfo info = new ContainerInfo();
				info.setUID(v.getUID());
				info.setName(v.getName());
				info.setRelease(v.getVersion());
				if(inputElement instanceof IDeliveryModule) {
					if(!((IDeliveryModule) inputElement).getModuleRef().equals(v.getReference())) {
						containers.add(info);
					} 
				} else {
					containers.add(info);
				}
			}
		}
		List<IVersionInfo> moduleDeps = Collections.EMPTY_LIST;
		if(inputElement instanceof IDeliveryModule) {
			moduleDeps = ((IDeliveryModule)inputElement).getDependencies();
		}
		for(IVersionInfo v : moduleDeps) {
			for(ContainerInfo i : new ArrayList<ContainerInfo>(containers)) {
				if(i.getRelease().getReference().getUID().rawId() == v.getReference().getUID().rawId()) {
					containers.remove(i);
				}
				if(i.getRelease().isDropped() || i.getRelease().getStatus()!=IVersionStatus.CHECKED_IN) {
					containers.remove(i);
				}
			}
		}
		
		return containers.toArray();
	}
	
	public void setFilterObsoleteDeps(boolean filter) {
		filterObsoleteDeps = filter;
	}
}
