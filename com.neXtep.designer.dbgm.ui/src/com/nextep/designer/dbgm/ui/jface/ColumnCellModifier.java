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

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;

/**
 * @author Christophe Fondacci
 */
public class ColumnCellModifier implements ICellModifier {

	public static final String PROP_NAME = "NAME"; //$NON-NLS-1$
	public static final String PROP_DATATYPE = "DATATYPE"; //$NON-NLS-1$
	public static final String PROP_LENGTH = "LENGTH"; //$NON-NLS-1$
	public static final String PROP_PRECISION = "PRECISION"; //$NON-NLS-1$
	public static final String PROP_NOTNULL = "NOT_NULL"; //$NON-NLS-1$
	public static final String PROP_DEFAULT = "DEFAULT"; //$NON-NLS-1$

	@Override
	public void modify(Object element, String property, Object value) {
		final IBasicColumn c = (IBasicColumn) ((TableItem) element).getData();
		if (PROP_NAME.equals(property)) {
			c.setName((String) value);
		} else if (PROP_DATATYPE.equals(property)) {
			c.getDatatype().setName((String) value);
		} else if (PROP_LENGTH.equals(property)) {
			// Length converted to integer only if not null
			final String length = (String) value;
			if (length != null && !length.trim().isEmpty()) {
				c.getDatatype().setLength(Integer.valueOf(length));
			} else {
				c.getDatatype().setLength(0);
			}
		} else if (PROP_PRECISION.equals(property)) {
			// Precision converted to integer only if not null
			final String precision = (String) value;
			if (precision != null && !precision.trim().isEmpty()) {
				c.getDatatype().setPrecision(Integer.valueOf(precision));
			} else {
				c.getDatatype().setPrecision(0);
			}

		} else if (PROP_DEFAULT.equals(property)) {
			final String defaultExpr = (String) value;
			c.setDefaultExpr(defaultExpr);
		} else if (PROP_NOTNULL.equals(property)) {
			// The checkbox editor manipulates boolean so this is what we have
			c.setNotNull((Boolean) value);
		}
	}

	@Override
	public Object getValue(Object element, String property) {
		final IBasicColumn c = (IBasicColumn) element;
		// Converting requested properties to strings for text editor
		if (PROP_NAME.equals(property)) {
			return c.getName();
		} else if (PROP_DATATYPE.equals(property)) {
			return c.getDatatype().getName();
		} else if (PROP_LENGTH.equals(property)) {
			final int length = c.getDatatype().getLength();
			if (length > 0) {
				return String.valueOf(length);
			} else {
				return "";
			}
		} else if (PROP_PRECISION.equals(property)) {
			final int precision = c.getDatatype().getPrecision();
			if (precision > 0) {
				return String.valueOf(precision);
			} else {
				return "";
			}
		} else if (PROP_DEFAULT.equals(property)) {
			final String defaultExpr = c.getDefaultExpr();
			return defaultExpr == null ? "" : defaultExpr;
		} else if (PROP_NOTNULL.equals(property)) {
			// Retruning boolean for the checkbox editor
			return c.isNotNull();
		}
		return "";
	}

	@Override
	public boolean canModify(Object element, String property) {
		return !CorePlugin.getService(ICoreService.class).isLocked(element);
	}

}
