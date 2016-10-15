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
package com.nextep.designer.dbgm.postgre.ui.jface;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlInheritedTable;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * @author Christophe Fondacci
 */
public class InheritedTablesContentProvider implements IStructuredContentProvider, IEventListener,
		IModelOriented<IPostgreSqlTable> {

	private IPostgreSqlTable parent;
	private Viewer viewer;

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		IListenerService listenerService = Designer.getListenerService();
		// Unregistering any previous listener
		listenerService.unregisterListeners(this);
		// Keeping viewer's reference
		this.viewer = viewer;
		if (newInput instanceof IPostgreSqlTable) {
			parent = (IPostgreSqlTable) newInput;
			// Listening to everything
			bindListener(parent);
		} else {
			parent = null;
		}
	}

	private void bindListener(Object o) {
		if (o instanceof IObservable) {
			Designer.getListenerService().registerListener(this, (IObservable) o, this);
			Designer.getListenerService().registerListener(this, (IObservable) o,
					UIControllerFactory.getController(o));
		}
	}

	@Override
	public Object[] getElements(Object inputElement) {
		final List<IPostgreSqlInheritedTable> tables = new ArrayList<IPostgreSqlInheritedTable>();
		for (IReference r : parent.getInheritances()) {
			final IPostgreSqlTable t = (IPostgreSqlTable) VersionHelper.getReferencedItem(r);
			if (t != null) {
				bindListener(t);
				final IPostgreSqlInheritedTable inheritedTable = CorePlugin.getTypedObjectFactory()
						.create(IPostgreSqlInheritedTable.class);
				inheritedTable.setParent(t);
				inheritedTable.setTable(parent);
				tables.add(inheritedTable);
			}
		}
		return tables.toArray();
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (source instanceof IColumnable) {
			viewer.refresh();
		} else {
			if (viewer instanceof StructuredViewer) {
				((StructuredViewer) viewer).refresh(source);
			}
		}
	}

	@Override
	public void setModel(IPostgreSqlTable model) {
		inputChanged(viewer, parent, model);
		viewer.refresh();
	}

	@Override
	public IPostgreSqlTable getModel() {
		return parent;
	}

}
