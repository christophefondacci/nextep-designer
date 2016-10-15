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
package com.nextep.datadesigner.vcs.impl;

import java.util.Collection;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * This class is a wrapper for a diagram item allowing it to be referenceable. This adapter will
 * return the referenced item model as the reference of this diagram item.<br>
 * Such an adapter has been designed to use during merge operations where the merger expects
 * {@link IReferenceable} objects but the diagram item cannot be referenceable as it would generate
 * duplicate references.
 * 
 * @author Christophe Fondacci
 */
public class ReferenceableDiagramItemAdapter implements IDiagramItem, IReferenceable {

	private IDiagramItem item;
	private IReference ref;

	public ReferenceableDiagramItemAdapter(IDiagramItem item) {
		this(item, false);
	}

	public ReferenceableDiagramItemAdapter(IDiagramItem item, boolean isVolatile) {
		this.item = item;
		ref = new Reference(IElementType.getInstance(IDiagramItem.TYPE_ID), null, null);
		ref.setUID(item.getItemReference().getUID());
		ref.setVolatile(isVolatile);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getHeight()
	 */
	@Override
	public int getHeight() {
		return item.getHeight();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getItemModel()
	 */
	@Override
	public IReferenceable getItemModel() {
		return item.getItemModel();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getItemReference()
	 */
	@Override
	public IReference getItemReference() {
		return item.getItemReference();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getParentDiagram()
	 */
	@Override
	public IDiagram getParentDiagram() {
		return item.getParentDiagram();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getWidth()
	 */
	@Override
	public int getWidth() {
		return item.getWidth();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getXStart()
	 */
	@Override
	public int getXStart() {
		return item.getXStart();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getYStart()
	 */
	@Override
	public int getYStart() {
		return item.getYStart();
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setHeight(int)
	 */
	@Override
	public void setHeight(int height) {
		item.setHeight(height);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setItemReference(com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public void setItemReference(IReference itemRef) {
		item.setItemReference(itemRef);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setParentDiagram(com.nextep.designer.vcs.model.IDiagram)
	 */
	@Override
	public void setParentDiagram(IDiagram parent) {
		item.setParentDiagram(parent);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setWidth(int)
	 */
	@Override
	public void setWidth(int width) {
		item.setWidth(width);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setXStart(int)
	 */
	@Override
	public void setXStart(int x) {
		item.setXStart(x);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setYStart(int)
	 */
	@Override
	public void setYStart(int y) {
		item.setYStart(y);
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#getUID()
	 */
	@Override
	public UID getUID() {
		return item.getUID();
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#setUID(com.nextep.datadesigner.model.UID)
	 */
	@Override
	public void setUID(UID id) {
		item.setUID(id);
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#addListener(com.nextep.datadesigner.model.IEventListener)
	 */
	@Override
	public void addListener(IEventListener listener) {
		item.addListener(listener);
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#getListeners()
	 */
	@Override
	public Collection<IEventListener> getListeners() {
		return item.getListeners();
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#notifyListeners(com.nextep.datadesigner.model.ChangeEvent,
	 *      java.lang.Object)
	 */
	@Override
	public void notifyListeners(ChangeEvent event, Object o) {
		item.notifyListeners(event, o);
	}

	/**
	 * @see com.nextep.datadesigner.model.IObservable#removeListener(com.nextep.datadesigner.model.IEventListener)
	 */
	@Override
	public void removeListener(IEventListener listener) {
		item.removeListener(listener);
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferencer#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		return item.getReferenceDependencies();
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferencer#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		return item.updateReferenceDependencies(oldRef, newRef);
	}

	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return item.getType();
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferenceable#getReference()
	 */
	@Override
	public IReference getReference() {
		return ref;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferenceable#setReference(com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public void setReference(IReference ref) {
		// Doing nothing
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IDiagramItem) {
			final IDiagramItem item = (IDiagramItem) obj;
			return getItemReference().equals(item.getItemReference());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return 1;
	}

}
