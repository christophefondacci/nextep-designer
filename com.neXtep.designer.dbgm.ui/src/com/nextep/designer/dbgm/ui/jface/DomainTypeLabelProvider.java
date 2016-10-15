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
package com.nextep.designer.dbgm.ui.jface;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

import com.nextep.datadesigner.dbgm.model.IDomainVendorType;

/**
 * 
 * @author Christophe Fondacci
 */
public class DomainTypeLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		final IDomainVendorType t = (IDomainVendorType) element;
		switch (columnIndex) {
		case 0:
			if (t.getDBVendor() != null) {
				return t.getDBVendor().toString();
			} else {
				return "";
			}
		case 1:
			if (t.getDatatype() != null) {
				return t.getDatatype().getName();
			} else {
				return "";
			}
		case 2:
			if (t.getDatatype() != null && t.getDatatype().getLength() != null) {
				return String.valueOf(t.getDatatype().getLength());
			} else {
				return "[Preserve]";
			}
		case 3:
			if (t.getDatatype() != null && t.getDatatype().getPrecision() != null) {
				return String.valueOf(t.getDatatype().getPrecision());
			} else {
				return "[Preserve]";
			}

		}
		// TODO Auto-generated method stub
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
