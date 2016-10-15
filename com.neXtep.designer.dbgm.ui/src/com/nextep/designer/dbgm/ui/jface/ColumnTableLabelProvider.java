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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.jface;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.designer.helper.DatatypeHelper;

/**
 * @author Christophe Fondacci
 */
public class ColumnTableLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			return DatatypeHelper.getDatatypeIcon(((IBasicColumn) element).getDatatype());
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		final IBasicColumn c = (IBasicColumn) element;
		switch (columnIndex) {
		case 0:
			return c.getName();
		case 1:
			return c.getDatatype().getName();
		case 2:
			int length = c.getDatatype().getLength();
			if (length > 0) {
				return String.valueOf(length);
			}
			break;
		case 3:
			int precision = c.getDatatype().getPrecision();
			if (precision > 0) {
				return String.valueOf(precision);
			}
			break;
		case 4:
			return c.isNotNull() ? "X" : "";
		case 5:
			return c.getDefaultExpr() == null ? "" : c.getDefaultExpr();
		}
		return "";
	}

}
