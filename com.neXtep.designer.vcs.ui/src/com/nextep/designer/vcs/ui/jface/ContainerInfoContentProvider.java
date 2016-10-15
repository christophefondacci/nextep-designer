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
import java.util.Collection;
import java.util.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;

public class ContainerInfoContentProvider implements IStructuredContentProvider {

	private boolean checkedInOnly = false;
	private boolean sandBoxed = true;

	public ContainerInfoContentProvider(boolean checkedInOnly, boolean sandBoxed) {
		this.checkedInOnly = checkedInOnly;
		this.sandBoxed = sandBoxed;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getElementsList(inputElement).toArray();
	}

	@SuppressWarnings("unchecked")
	protected List<ContainerInfo> getElementsList(Object inputElement) {
		List<ContainerInfo> containers = null;
		try {
			Observable.deactivateListeners();
			containers = (List<ContainerInfo>) CorePlugin.getIdentifiableDao().loadAll(
					ContainerInfo.class); // ,HibernateUtil.getInstance().getSandBoxSession() );
		} finally {
			Observable.activateListeners();
		}
		// (List<IDeliveryModule>)IdentifiableDAO.getInstance().loadAll(DeliveryModule.class,HibernateUtil.getInstance().getSandBoxSession());

		// Input may be a container, if so we restrict to its contents
		Collection<UID> restriction = null;
		if (inputElement instanceof IVersionContainer) {
			IVersionContainer c = (IVersionContainer) inputElement;
			List<IVersionable<?>> viewCont = VersionHelper.getAllVersionables(c,
					IElementType.getInstance(IVersionContainer.TYPE_ID));
			// Building a container reference list
			restriction = new ArrayList<UID>();
			for (IVersionable<?> v : viewCont) {
				restriction.add(v.getReference().getUID());
			}
		}
		// Filtering only non dropped and non checked out items
		for (ContainerInfo c : new ArrayList<ContainerInfo>(containers)) {
			if (c.getRelease().isDropped()) {
				containers.remove(c);
			}
			if (restriction != null && !restriction.contains(c.getReference().getUID())) {
				containers.remove(c);
			}
			if (c.getRelease().getStatus() != IVersionStatus.CHECKED_IN && checkedInOnly) {
				containers.remove(c);
			}
		}
		return containers;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
