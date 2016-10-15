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

import java.util.List;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.dao.IDeliveryDao;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.core.model.DBVendor;

/**
 * A JFace content provider which provides {@link IDeliveryInfo} (summarized delivery information)
 * for the deliveries matching a given {@link DBVendor} given as the input
 * 
 * @author Christophe Fondacci
 */
public class VendorDeliveriesContentProvider implements IStructuredContentProvider {

	private DBVendor vendor;

	@Override
	public Object[] getElements(Object inputElement) {
		// Loading all existing deliveries
		final IDeliveryDao deliveryDao = BengPlugin.getService(IDeliveryDao.class);
		List<IDeliveryInfo> deliveries = deliveryDao.getDeliveriesForVendor(vendor);

		return deliveries.toArray();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.vendor = (DBVendor) newInput;
	}

}
