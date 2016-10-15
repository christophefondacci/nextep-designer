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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.ui.factories.ImageFactory;

public class DeliveryInfoTableLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			if (element instanceof IDeliveryInfo) {
				return ImageFactory.getImage(IElementType.getInstance(IDeliveryModule.TYPE_ID)
						.getIcon());
			} else if (element instanceof IDeliveryIncrement) {
				return ImageFactory.ICON_ERROR_TINY;
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IDeliveryIncrement) {
			final IDeliveryIncrement inc = (IDeliveryIncrement) element;
			switch (columnIndex) {
			case 0:
				return "Missing delivery chain";
			case 2:
				return inc.getFromRelease() == null ? "[Scratch]" : inc.getFromRelease().getLabel();
			case 3:
				return inc.getToRelease().getLabel();
			default:
				return ""; //$NON-NLS-1$
			}
		}

		IDeliveryInfo m = (IDeliveryInfo) element;
		switch (columnIndex) {
		case 0:
			return m.getName();
		case 1:
			// Loading module to display it...
			// IVersionContainer c =
			// (IVersionContainer)IdentifiableDAO.getInstance().load(IVersionable.class,
			// m.getTargetRelease().getUID(),HibernateUtil.getInstance().getSandBoxSession(),true);
			final String version = m.getTargetRelease().getLabel();
			return m.getName().substring(0, m.getName().length() - 1 - version.length())
					.replace('_', ' ');
		case 2:
			if (m.getSourceRelease() != null) {
				return m.getSourceRelease().getLabel();
			} else {
				return "[None]";
			}
		case 3:
			return m.getTargetRelease().getLabel();
		case 4:
			return m.getTargetRelease().getBranch().getName();
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
