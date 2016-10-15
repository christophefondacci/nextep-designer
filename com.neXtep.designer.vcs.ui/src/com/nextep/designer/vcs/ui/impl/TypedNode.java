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
package com.nextep.designer.vcs.ui.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.ui.model.ITypedNode;

public class TypedNode implements ITypedNode {

	private Collection<ITypedObject> children;
	private ITypedObject parent;
	private IElementType type;

	public TypedNode(IElementType type, ITypedObject parent) {
		this.type = type;
		setParent(parent);
		children = new ArrayList<ITypedObject>();
	}

	@Override
	public void addChild(ITypedObject child) {
		children.add(child);
	}

	@Override
	public Collection<ITypedObject> getChildren() {
		return children;
	}

	@Override
	public ITypedObject getParent() {
		return parent;
	}

	@Override
	public void setChildren(Collection<ITypedObject> children) {
		this.children = children;
	}

	@Override
	public void setParent(ITypedObject parent) {
		this.parent = parent;
	}

	@Override
	public IElementType getNodeType() {
		return type;
	}

	@Override
	public String getName() {
		return getNodeType().getCategoryTitle();
	}

	public static Collection<?> buildNodesFromCollection(ITypedObject parent,
			Collection<? extends ITypedObject> elements, IEventListener listener) {
		Map<IElementType, ITypedNode> nodesMap = new HashMap<IElementType, ITypedNode>();
		Collection<Object> result = new ArrayList<Object>();
		for (ITypedObject v : elements) {
			final IElementType type = v.getType();
			if (type == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
				result.add(v);
			} else {
				ITypedNode node = nodesMap.get(type);
				if (node == null) {
					node = new TypedNode(type, parent);
					nodesMap.put(type, node);
				}
				node.addChild(v);
			}
			if (v instanceof IObservable && listener != null) {
				Designer.getListenerService().registerListener(listener, (IObservable) v, listener);
				// Adding the controller as a listener too
				if (v instanceof ITypedObject) {
					final ITypedObjectUIController controller = UIControllerFactory.getController(v
							.getType());
					Designer.getListenerService().registerListener(listener, (IObservable) v,
							controller);
				}
			}
		}
		result.addAll(nodesMap.values());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ITypedNode) {
			final ITypedNode n = (ITypedNode) obj;
			return getParent().equals(n.getParent()) && getNodeType().equals(n.getNodeType());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return (getParent() != null ? getParent().hashCode() : 0)
				+ (getNodeType() != null ? getNodeType().hashCode() : 0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IElementType.class) {
			return getNodeType();
		}
		return null;
	}
}
