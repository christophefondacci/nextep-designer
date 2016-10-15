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
package com.nextep.designer.dbgm.ui.actions;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.vcs.ui.jface.TypedInnerReferencesContentProvider;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class ConstraintColumnsActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		final IKeyConstraint constraint = (IKeyConstraint) parent;
		final IBasicTable parentTable = constraint.getConstrainedTable();
		final IContentProvider provider = new TypedInnerReferencesContentProvider(
				constraint.getColumns(), IElementType.getInstance(IBasicColumn.TYPE_ID));
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);

		final ITypedObject column = uiService.findElement(Display.getDefault().getActiveShell(),
				"Select the table column to add to the constraint", parentTable, provider,
				new DbgmLabelProvider());
		if (column != null) {
			constraint.addColumn((IBasicColumn) column);
			return column;
		}
		return null;
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		final IKeyConstraint constraint = (IKeyConstraint) parent;
		final IBasicColumn column = (IBasicColumn) toRemove;
		constraint.removeColumn(column);
	}

	@Override
	public void up(ITypedObject parent, ITypedObject element) {
		final IKeyConstraint constraint = (IKeyConstraint) parent;
		final IBasicColumn column = (IBasicColumn) element;
		constraint.up(column);
	}

	@Override
	public void down(ITypedObject parent, ITypedObject element) {
		final IKeyConstraint constraint = (IKeyConstraint) parent;
		final IBasicColumn column = (IBasicColumn) element;
		constraint.down(column);
	}

	@Override
	public boolean isSortable() {
		return true;
	}
}
