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
import com.nextep.designer.beng.exception.UndeliverableIncrementException;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.services.IDeliveryService;

public class DeliveryFromToContentProvider implements IStructuredContentProvider {

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IDeliveryIncrement) {
			try {
				List<IDeliveryInfo> deliveries = BengPlugin.getService(IDeliveryService.class)
						.getDeliveries(((IDeliveryIncrement) inputElement));
				return deliveries.toArray();
			} catch (UndeliverableIncrementException e) {
				return new Object[] { e.getDeliveryIncrement() };
			}
		}
		return null;
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

}
