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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

public class ContainerInfoViewContentProvider implements IStructuredContentProvider {

	private IWorkspace view;

	@Override
	public Object[] getElements(Object inputElement) {
		List<ContainerInfo> containers = new ArrayList<ContainerInfo>();

		for (IVersionable<?> v : view.getContents()) {
			if (v instanceof IVersionContainer) {
				final ContainerInfo info = new ContainerInfo();
				info.setRelease(v.getVersion());
				info.setName(v.getName());
				info.setUID(v.getVersion().getUID());
				containers.add(info);
			}
		}
		return containers.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.view = (IWorkspace) newInput;
	}

}
