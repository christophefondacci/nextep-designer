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
package com.nextep.designer.dbgm.oracle.ui.jface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.ui.model.IColumnBinding;
import com.nextep.designer.dbgm.ui.model.impl.ColumnBinding;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * @author Christophe Fondacci
 */
public class ClusteredTableColumnsContentProvider implements IStructuredContentProvider,
		IModelOriented<IOracleClusteredTable>, IEventListener {

	private IOracleClusteredTable parent;
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
		if (newInput instanceof IOracleClusteredTable) {
			parent = (IOracleClusteredTable) newInput;
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
		final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
		// The list we are about to build containing column bindings
		final List<IColumnBinding> bindings = new ArrayList<IColumnBinding>();
		// The clustered table mappings
		final IOracleClusteredTable table = getModel();
		final Map<IReference, IReference> columnMappings = table.getColumnMappings();
		// Building our list of column bindings
		for (IBasicColumn c : table.getCluster().getColumns()) {
			final IReference mappedRef = columnMappings.get(c.getReference());
			try {
				IBasicColumn mappedCol = null;
				// Resolving our mapped column and adding the binding when successful
				if (mappedRef != null) {
					mappedCol = (IBasicColumn) VersionHelper.getReferencedItem(mappedRef);
				}
				final IColumnBinding binding = new ColumnBinding(c, mappedCol);
				bindings.add(binding);
			} catch (ErrorException e) {
				// If we fail to resolve our column we add a marker binding
				final IOracleCluster cluster = table.getCluster();
				final IMarker marker = coreFactory.createMarker(cluster, MarkerType.ERROR,
						e.getMessage());
				final IColumnBinding binding = new ColumnBinding(c, null, marker);
				bindings.add(binding);
			}
		}
		// Returning our list of column bindings
		return bindings.toArray();
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (source instanceof IOracleClusteredTable) {
			viewer.refresh();
		} else {
			if (viewer instanceof StructuredViewer) {
				((StructuredViewer) viewer).refresh(source);
			}
		}
	}

	@Override
	public void setModel(IOracleClusteredTable model) {
		inputChanged(viewer, parent, model);
		viewer.refresh();
	}

	@Override
	public IOracleClusteredTable getModel() {
		return parent;
	}

}
