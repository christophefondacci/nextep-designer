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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * This class provides columns of a partitionable table element
 * 
 * @author Christophe Fondacci
 */
public class PartitionsContentProvider implements IStructuredContentProvider, IEventListener,
		IModelOriented<IPartitionable> {

	private IPartitionable parent;
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
		if (newInput instanceof IPartitionable) {
			parent = (IPartitionable) newInput;
			// Listening to everything
			bindListener(parent);
		} else if (newInput instanceof IPhysicalObject) {
			final IPhysicalProperties props = ((IPhysicalObject) newInput).getPhysicalProperties();
			inputChanged(viewer, oldInput, props);
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
		if (parent != null && parent.getPartitions() != null) {
			for (IPartition o : parent.getPartitions()) {
				bindListener(o);
			}
			return parent.getPartitions().toArray();
		} else {
			return new Object[] {};
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (source instanceof IPartitionable) {
			viewer.refresh();
		} else {
			if (viewer instanceof StructuredViewer) {
				((StructuredViewer) viewer).refresh(source);
			}
		}
	}

	@Override
	public void setModel(IPartitionable model) {
		inputChanged(viewer, parent, model);
		viewer.refresh();
	}

	@Override
	public IPartitionable getModel() {
		return parent;
	}
}
