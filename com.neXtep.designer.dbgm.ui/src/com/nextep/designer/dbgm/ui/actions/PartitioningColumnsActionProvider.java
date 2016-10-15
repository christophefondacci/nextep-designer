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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.swt.widgets.Display;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.dbgm.model.IPartitionableTable;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.vcs.ui.jface.TypedInnerReferencesContentProvider;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * @author Christophe Fondacci
 */
public class PartitioningColumnsActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		final IPartitionableTable partitionable = (IPartitionableTable) parent;
		IColumnable columnable = null;
		if (partitionable instanceof IParentable<?>) {
			columnable = ((IParentable<IColumnable>) partitionable).getParent();
		}
		// Building the column list
		final List<IBasicColumn> partitionedColumns = new ArrayList<IBasicColumn>();
		for (IReference colRef : partitionable.getPartitionedColumnsRef()) {
			try {
				final IBasicColumn c = (IBasicColumn) VersionHelper.getReferencedItem(colRef);
				if (c != null) {
					partitionedColumns.add(c);
				}
			} catch (ErrorException e) {
				// Simply ignoring, we are only building a filter...
			}
		}

		final IContentProvider provider = new TypedInnerReferencesContentProvider(
				partitionedColumns, IElementType.getInstance(IBasicColumn.TYPE_ID));
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);

		final ITypedObject column = uiService.findElement(Display.getDefault().getActiveShell(),
				"Select the table column to add to the partitioning columns", columnable, provider,
				new DbgmLabelProvider());
		if (column != null) {
			partitionable.addPartitionedColumnRef(((IBasicColumn) column).getReference());
			return column;
		}
		return null;
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		final IPartitionableTable index = (IPartitionableTable) parent;
		final IBasicColumn column = (IBasicColumn) toRemove;
		index.removePartitionedColumnRef(column.getReference());
	}

	@Override
	public void up(ITypedObject parent, ITypedObject element) {
		final IPartitionableTable partitionable = (IPartitionableTable) parent;
		final IBasicColumn column = (IBasicColumn) element;
		final IReference columnRef = column.getReference();
		final List<IReference> colRefs = partitionable.getPartitionedColumnsRef();
		int colIndex = colRefs.indexOf(columnRef);
		if (colIndex > 0) {
			// final IReference swappedCol = index.getPartitionedColumnsRef().get(colIndex - 1);
			Collections.swap(colRefs, colIndex, colIndex - 1);
			partitionable.notifyListeners(ChangeEvent.MODEL_CHANGED, column);
			column.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void down(ITypedObject parent, ITypedObject element) {
		final IPartitionableTable index = (IPartitionableTable) parent;
		final IBasicColumn column = (IBasicColumn) element;
		final IReference columnRef = column.getReference();
		final List<IReference> colRef = index.getPartitionedColumnsRef();
		int colIndex = colRef.indexOf(columnRef);
		if (colIndex < colRef.size() - 1) {
			// final IBasicColumn swappedCol = index.getPartitionedColumnsRef().get(colIndex + 1);
			Collections.swap(colRef, colIndex, colIndex + 1);
			index.notifyListeners(ChangeEvent.MODEL_CHANGED, column);
			column.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			// swappedCol.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public boolean isSortable() {
		return true;
	}
}
