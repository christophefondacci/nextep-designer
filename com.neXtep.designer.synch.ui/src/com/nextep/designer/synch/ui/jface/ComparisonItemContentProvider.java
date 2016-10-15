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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.synch.ui.model.ICategorizedType;
import com.nextep.designer.synch.ui.model.impl.CategorizedType;
import com.nextep.designer.vcs.model.IComparisonItem;

public class ComparisonItemContentProvider implements ITreeContentProvider {

	private IComparisonItem parent;
	private Map<Object, Collection<ICategorizedType>> parentCategories;

	public ComparisonItemContentProvider() {
		parentCategories = new HashMap<Object, Collection<ICategorizedType>>();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof IComparisonItem) {

			return getOrCreateCategories(parentElement).toArray();
		} else if (parentElement instanceof ICategorizedType) {
			return ((ICategorizedType) parentElement).getItems().toArray();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IComparisonItem) {
			return parentCategories.get(((IComparisonItem) element).getParent());
		} else if (element instanceof ICategorizedType) {
			return parent;
		}
		return null;
	}

	private Collection<ICategorizedType> getOrCreateCategories(Object parentElement) {
		Collection<ICategorizedType> categories = parentCategories.get(parentElement);
		if (categories == null) {
			categories = getHashedItemTypes(((IComparisonItem) parentElement).getSubItems());
			parentCategories.put(parentElement, categories);
		}
		return categories;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IComparisonItem) {
			return !getOrCreateCategories(element).isEmpty();
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
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@SuppressWarnings("unchecked")
	private Collection<ICategorizedType> getHashedItemTypes(Collection<IComparisonItem> items) {
		List<ICategorizedType> categories = new ArrayList<ICategorizedType>();
		MultiValueMap itemsTypeMap = new MultiValueMap();
		for (IComparisonItem item : items) {
			itemsTypeMap.put(item.getType(), item);
		}
		for (Object o : itemsTypeMap.keySet()) {
			final IElementType t = (IElementType) o;
			final Collection<IComparisonItem> typedItems = itemsTypeMap.getCollection(t);
			final ICategorizedType category = new CategorizedType(t, typedItems);
			categories.add(category);
		}
		return categories;
	}
}
