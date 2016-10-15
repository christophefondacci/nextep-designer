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
package com.nextep.designer.vcs.ui.jface;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

public class ContainerInfoNotInViewContentProvider extends ContainerInfoContentProvider {

	private IWorkspace view;
	private boolean filterCheckOut = true;

	public ContainerInfoNotInViewContentProvider() {
		super(true, true);
	}

	/**
	 * Indicates whether the provider should remove non-checked in objects
	 * 
	 * @param filter boolean
	 */
	public void setFilterCheckOut(boolean filter) {
		this.filterCheckOut = filter;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getElements(Object inputElement) {
		List<ContainerInfo> containers = (List<ContainerInfo>) CorePlugin.getIdentifiableDao()
				.loadAll(ContainerInfo.class, HibernateUtil.getInstance().getSandBoxSession());
		List<IVersionable<?>> viewContainers = VersionHelper.getAllVersionables(view,
				IElementType.getInstance(IVersionContainer.TYPE_ID));
		for (IVersionable<?> c : viewContainers) {
			final ContainerInfo info = new ContainerInfo();
			info.setRelease(c.getVersion());
			if (containers.contains(info)) {
				containers.remove(info);
			}
			for (ContainerInfo i : new ArrayList<ContainerInfo>(containers)) {
				if (i.getRelease().getReference().getUID().rawId() == c.getVersion().getReference()
						.getUID().rawId()) {
					containers.remove(i);
				}
			}
		}
		if (filterCheckOut) {
			for (ContainerInfo i : new ArrayList<ContainerInfo>(containers)) {
				if (i.getRelease().isDropped()
						|| i.getRelease().getStatus() != IVersionStatus.CHECKED_IN
						|| i.getDBVendor() != view.getDBVendor()) {
					containers.remove(i);
				}
			}
		}
		return containers.toArray();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.view = (IWorkspace) newInput;
	}
}
