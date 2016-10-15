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
package com.nextep.designer.dbgm.ui.navigators;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.ui.impl.TypedNode;
import com.nextep.designer.vcs.ui.model.ITypedNode;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

public class TableContentProvider implements ITreeContentProvider, IEventListener {

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITypedNode) {
			return ((ITypedNode) parentElement).getChildren().toArray();
		} else if (parentElement instanceof IBasicTable) {
			final Collection<Object> elts = new ArrayList<Object>();
			final IBasicTable table = (IBasicTable) parentElement;
			elts.addAll(table.getColumns());
			elts.addAll(table.getConstraints());
			elts.addAll(table.getDataSets());
			elts.addAll(table.getIndexes());
			elts.addAll(table.getTriggers());
			return TypedNode.buildNodesFromCollection(table,
					(Collection<? extends ITypedObject>) elts, this).toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ITypedNode) {
			return ((ITypedNode) element).getParent();
		} else if (element instanceof IBasicColumn) {
			final IBasicColumn col = (IBasicColumn) element;
			return new TypedNode(col.getType(), (ITypedObject) col.getParent());
		} else if (element instanceof IKeyConstraint) {
			final IKeyConstraint key = (IKeyConstraint) element;
			return new TypedNode(key.getType(), key.getConstrainedTable());
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IBasicTable) {
			return true;
		} else if (element instanceof ITypedNode) {
			return true;
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		CorePlugin.getService(IWorkspaceUIService.class).refreshNavigatorFor(source);
	}
}
