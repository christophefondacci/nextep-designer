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
package com.nextep.designer.synch.ui.jface;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.synch.ui.navigators.ComparisonNavigatorRoot;


public class ComparisonTreeContentProvider implements ITreeContentProvider, IEventListener {

	private Viewer viewer;
	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof ComparisonNavigatorRoot) {
			return ((ComparisonNavigatorRoot) parentElement).getItems().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return element instanceof ComparisonNavigatorRoot;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof ComparisonNavigatorRoot) {
			return ((ComparisonNavigatorRoot) inputElement).getItems().toArray();
		}
		return null;
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		if(oldInput instanceof IObservable) {
			((IObservable)oldInput).removeListener(this);
		}
		if(newInput instanceof IObservable) {
			((IObservable) newInput).addListener(this);
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if(viewer !=null && !viewer.getControl().isDisposed()) {
			viewer.refresh();
		}
	}

}
