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
package com.nextep.designer.vcs.ui.navigators;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.ICommonViewerMapper;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.vcs.ui.model.ITypedNode;

public class VersionableNavigatorMapper implements ICommonViewerMapper {

	private MultiValueMap itemsMap;
	private CommonViewer viewer;

	public VersionableNavigatorMapper(CommonViewer viewer) {
		this.viewer = viewer;
		itemsMap = new MultiValueMap();
	}

	@Override
	public void addToMap(Object element, Item item) {
		itemsMap.put(element, item);
	}

	@Override
	public void clearMap() {
		itemsMap.clear();
	}

	@Override
	public boolean handlesObject(Object object) {
		if (object instanceof IReferenceable) {
			return (!((IReferenceable) object).getReference().isVolatile() && object instanceof ITypedObject)
					|| object instanceof ITypedNode;
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		return itemsMap.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void objectChanged(Object object) {
		final Collection<Item> items = (Collection<Item>) itemsMap.get(object);
		if (items != null && !items.isEmpty()) {
			for (Item item : new ArrayList<Item>(items)) {
				if (item != null && !item.isDisposed()) {
					viewer.doUpdateItem(item);
				}
			}
		}
	}

	@Override
	public void removeFromMap(Object element, Item item) {
		itemsMap.remove(element, item);
	}

}
