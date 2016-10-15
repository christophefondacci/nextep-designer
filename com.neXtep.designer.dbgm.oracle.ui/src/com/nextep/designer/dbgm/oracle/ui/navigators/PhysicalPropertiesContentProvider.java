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
package com.nextep.designer.dbgm.oracle.ui.navigators;

import java.util.ArrayList;
import java.util.Collection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.vcs.ui.impl.TypedNode;
import com.nextep.designer.vcs.ui.model.ITypedNode;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

public class PhysicalPropertiesContentProvider implements ITreeContentProvider, IEventListener {

	@SuppressWarnings("unchecked")
	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IPhysicalObject) {
			final Collection<Object> elts = new ArrayList<Object>();
			final IPhysicalObject physObj = (IPhysicalObject) parentElement;
			if (physObj.getPhysicalProperties() != null) {
				elts.add(physObj.getPhysicalProperties());
			}
			return TypedNode.buildNodesFromCollection((ITypedObject) physObj,
					(Collection<? extends ITypedObject>) elts, this).toArray();
		} else if (parentElement instanceof ITypedNode) {
			return ((ITypedNode) parentElement).getChildren().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IPhysicalProperties) {
			final IPhysicalProperties props = (IPhysicalProperties) element;
			return new TypedNode(props.getType(), props.getParent());
		} else if (element instanceof ITypedNode) {
			return ((ITypedNode) element).getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IPhysicalObject) {
			return ((IPhysicalObject) element).getPhysicalProperties() != null;
		} else if (element instanceof ITypedNode) {
			return !((ITypedNode) element).getChildren().isEmpty();
		}
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public void dispose() {
		Designer.getListenerService().unregisterListeners(this);
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		CorePlugin.getService(IWorkspaceUIService.class).refreshNavigatorFor(source);
	}

}
