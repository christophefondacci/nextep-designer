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
/**
 *
 */
package com.nextep.datadesigner.vcs.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.impl.Activity;

/**
 * @author Christophe Fondacci
 */
public class VersionedDiagram extends SelfControlVersionable<IDiagram> implements IDiagram {

	private Set<IDiagramItem> items;
	private Map<IVersionInfo, IDiagramItem> itemVersions;

	public VersionedDiagram(String name, String description) {
		this();
		setName(name);
		setDescription(description);
		setVersion(VersionFactory.getUnversionedInfo(new Reference(getType(), name, this), Activity
				.getDefaultActivity()));

	}

	public VersionedDiagram() {
		items = new HashSet<IDiagramItem>();
		itemVersions = new HashMap<IVersionInfo, IDiagramItem>();
		nameHelper.setFormatter(IFormatter.UPPERCASE);
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(IDiagram.TYPE_ID);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagram#addItem(com.nextep.designer.vcs.model.IDiagramItem)
	 */
	public void addItem(IDiagramItem item) {
		items.add(item);
		item.setParentDiagram(this);
		notifyListeners(ChangeEvent.ITEM_ADDED, item);
	}

	public void attachVersion(IDiagramItem item, IVersionInfo version) {
		itemVersions.put(version, item);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagram#getItem(com.nextep.datadesigner.dbgm.model.IDatabaseObject)
	 */
	public IDiagramItem getItem(IReferenceable itemModel) {
		if (itemModel instanceof IVersionInfo) {
			return itemVersions.get(itemModel);
		}
		IReference ref = itemModel.getReference();
		for (IDiagramItem i : items) {
			if (i.getItemReference().equals(ref)) {
				return i;
			}
		}
		return null;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagram#getItems()
	 */
	public Set<IDiagramItem> getItems() {
		return items;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagram#removeItem(com.nextep.designer.vcs.model.IDiagramItem)
	 */
	public void removeItem(IDiagramItem item) {
		if (items.remove(item)) {
			item.setParentDiagram(null);
			notifyListeners(ChangeEvent.ITEM_REMOVED, item);
		}
	}

	/**
	 * Loads the items set (hibernate)
	 * 
	 * @param items set of items contained in this diagram
	 */
	protected void setItems(Set<IDiagramItem> items) {
		this.items = items;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.SelfControlVersionable#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IReference> refs = new ArrayList<IReference>();
		for (IDiagramItem item : getItems()) {
			refs.addAll(item.getReferenceDependencies());
		}
		return refs;
	}

	@Override
	public Map<IReference, IReferenceable> getReferenceMap() {
		Map<IReference, IReferenceable> refMap = new HashMap<IReference, IReferenceable>();
		for (IDiagramItem item : getItems()) {
			refMap.put(item.getReference(), item);
		}
		return refMap;
	}
}
