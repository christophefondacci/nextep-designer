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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.ui.model.ICategorizedType;
import com.nextep.designer.synch.ui.model.impl.CategorizedType;
import com.nextep.designer.synch.ui.navigators.ComparisonNavigatorRoot;
import com.nextep.designer.vcs.model.IComparisonItem;

public class ComparisonTypedTreeContentProvider implements ITreeContentProvider, IEventListener {

	private Viewer viewer;
	private Map<IElementType, ICategorizedType> categories;
	private ComparisonNavigatorRoot root;
	private IObservable observableInput;

	public ComparisonTypedTreeContentProvider() {
		categories = new HashMap<IElementType, ICategorizedType>();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ComparisonNavigatorRoot) {
			final ComparisonNavigatorRoot root = (ComparisonNavigatorRoot) parentElement; 
			if(root.getCurrentSynchronization()==null ) {
				return new Object[] {};
			}
			return new Object[] {root.getCurrentSynchronization()};
		} else if (parentElement instanceof ISynchronizationResult) {
			return categories.values().toArray();
		} else if (parentElement instanceof ICategorizedType) {
			return ((ICategorizedType) parentElement).getItems().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IComparisonItem) {
			return categories.get(((IComparisonItem) element).getType());
		} else if (element instanceof ICategorizedType) {
			return root;
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ISynchronizationResult) {
			return !categories.isEmpty();
		} else if (element instanceof ICategorizedType) {
			return !((ICategorizedType) element).getItems().isEmpty();
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
		this.viewer = viewer;
		if (oldInput instanceof IObservable) {
			((IObservable) oldInput).removeListener(this);
		}
		if (newInput instanceof IObservable) {
			observableInput = (IObservable) newInput;
			observableInput.addListener(this);
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (viewer != null && !viewer.getControl().isDisposed()) {
			if (data instanceof ComparisonNavigatorRoot) {
				this.root = (ComparisonNavigatorRoot) data;
				hashItemTypes(((ComparisonNavigatorRoot) data).getItems());
			}
			viewer.refresh();
			((TreeViewer)viewer).expandToLevel(3);
		}
	}

	@SuppressWarnings("unchecked")
	private void hashItemTypes(Collection<IComparisonItem> items) {
		categories.clear();
		MultiValueMap itemsTypeMap = new MultiValueMap();
		for (IComparisonItem item : items) {
			itemsTypeMap.put(item.getType(), item);
		}
		for (Object o : itemsTypeMap.keySet()) {
			final IElementType t = (IElementType) o;
			final Collection<IComparisonItem> typedItems = itemsTypeMap.getCollection(t);
			final ICategorizedType category = new CategorizedType(t, typedItems);
			categories.put(t, category);
		}
	}

}
