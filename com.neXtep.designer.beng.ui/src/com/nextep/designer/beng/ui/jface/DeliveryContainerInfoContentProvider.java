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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.dao.IDeliveryDao;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionInfo;

public class DeliveryContainerInfoContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		final IDeliveryDao deliveryDao = BengPlugin.getService(IDeliveryDao.class);
		if (inputElement instanceof IDeliveryInfo) {
			// Loading
			final IDeliveryInfo info = (IDeliveryInfo) inputElement;
			IDeliveryModule m = deliveryDao.loadModule(info);
			List<ContainerInfo> depContainers = new ArrayList<ContainerInfo>();
			// A reference map of imported items in case a same module would appear twice, we would
			// choose the highest release
			Map<IReference, ContainerInfo> refMap = new HashMap<IReference, ContainerInfo>();
			// Adding delivered container
			ContainerInfo initCont = getContainerInfo(m.getTargetRelease().getUID());
			depContainers.add(initCont);
			refMap.put(initCont.getRelease().getReference(), initCont);
			// Adding dependencies
			for (IVersionInfo i : m.getDependencies()) {
				ContainerInfo cont = getContainerInfo(i.getUID());
				final IReference ref = cont.getRelease().getReference();
				// Adding if not already added
				if (refMap.get(ref) == null) {
					depContainers.add(cont);
					refMap.put(ref, cont);
				} else {
					// If already added, we choose the highest release
					if (refMap.get(ref).getRelease().compareTo(cont.getRelease()) < 0) {
						depContainers.remove(refMap.get(ref));
						depContainers.add(cont);
						refMap.put(ref, cont);
					}
				}
			}
			return depContainers.toArray();
		}
		return null;
	}

	private ContainerInfo getContainerInfo(UID id) {
		return (ContainerInfo) CorePlugin.getIdentifiableDao().load(ContainerInfo.class, id,
				HibernateUtil.getInstance().getSandBoxSession(), false);
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
