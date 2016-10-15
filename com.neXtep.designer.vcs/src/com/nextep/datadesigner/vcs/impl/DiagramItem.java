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
import java.util.List;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class DiagramItem extends Observable implements IDiagramItem, INamedObject,
		IParentable<IDiagram> {

	private int x;
	private int y;
	private int width;
	private int height;
	// private IVersionable<?> itemModel;
	private IReferenceable itemModel;
	private IReference itemRef;
	private IDiagram parent;
	private UID id;
	private IReference reference;

	public DiagramItem(IReferenceable itemModel, int x, int y) {
		this();
		this.itemModel = itemModel;
		setItemReference(itemModel.getReference());
		setXStart(x);
		setYStart(y);
		setWidth(150);
		setHeight(300);
	}

	public DiagramItem() {
		setItemReference(new Reference(getType(), "", this)); //$NON-NLS-1$
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getItemModel()
	 */
	public IReferenceable getItemModel() {
		if (itemModel == null || (!itemRef.isVolatile() && itemModel instanceof IVersionable<?>)) {
			try {
				return (IVersionable<?>) VersionHelper.getReferencedItem(itemRef);
			} catch (UnresolvedItemException e) {
				return null;
			}
		}
		return itemModel; //
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getXStart()
	 */
	public int getXStart() {
		return x;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getYStart()
	 */
	public int getYStart() {
		return y;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IDiagramItem#setItemModel(com.nextep.datadesigner.dbgm.model.IDatabaseObject)
	 */
	// public void setItemModel(IVersionable itemModel) {
	// this.itemModel=itemModel;
	// }
	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setXStart(int)
	 */
	public void setXStart(int x) {
		this.x = x;
		this.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setYStart(int)
	 */
	public void setYStart(int y) {
		this.y = y;
		this.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	public void setParentDiagram(IDiagram parent) {
		this.parent = parent;
	}

	public IDiagram getParentDiagram() {
		return parent;
	}

	protected void setId(long id) {
		setUID(new UID(id));
	}

	protected long getId() {
		if (id == null) {
			return 0;
		} else {
			return id.rawId();
		}
		// if(id == null) {
		// return 0;
		// } else {
		// return id.rawId();
		// }
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#getUID()
	 */
	public UID getUID() {
		return id;
	}

	/**
	 * @see com.nextep.datadesigner.model.IdentifiedObject#setUID(com.nextep.datadesigner.model.UID)
	 */
	public void setUID(UID id) {
		this.id = id;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getHeight()
	 */
	@Override
	public int getHeight() {
		return height;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getWidth()
	 */
	@Override
	public int getWidth() {
		return width;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setHeight(int)
	 */
	@Override
	public void setHeight(int height) {
		this.height = height;
		this.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setWidth(int)
	 */
	@Override
	public void setWidth(int width) {
		this.width = width;
		this.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#getItemReference()
	 */
	@Override
	public IReference getItemReference() {
		return itemRef;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IDiagramItem#setItemReference(IReference)
	 */
	@Override
	public void setItemReference(IReference itemRef) {
		this.itemRef = itemRef;
		adjustInternalReference();
	}

	private void adjustInternalReference() {
		if (itemRef.getUID() != null) {

			if (reference == null) {
				reference = new Reference(getType(), null, this);
			}
			UID itemRefId = itemRef.getUID();
			UID refId = new UID(-itemRefId.rawId());
			reference.setUID(refId);
			reference.setVolatile(itemRef.isVolatile());
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferencer#getReferenceDependencies()
	 */
	@Override
	public Collection<IReference> getReferenceDependencies() {
		List<IReference> refs = new ArrayList<IReference>(1);
		refs.add(itemRef);
		return refs;
	}

	/**
	 * @see com.nextep.datadesigner.model.IReferencer#updateReferenceDependencies(com.nextep.datadesigner.model.IReference,
	 *      com.nextep.datadesigner.model.IReference)
	 */
	@Override
	public boolean updateReferenceDependencies(IReference oldRef, IReference newRef) {
		if (itemRef.equals(oldRef)) {
			itemRef = newRef;
			return true;
		}
		return false;
	}

	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(IDiagramItem.TYPE_ID);
	}

	@Override
	public String getDescription() {
		IReferenceable r = getItemModel();
		if (r instanceof INamedObject) {
			return ((INamedObject) r).getDescription();
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getName() {
		IReferenceable r = getItemModel();
		if (r instanceof INamedObject) {
			return (getParentDiagram() != null ? getParentDiagram().getName() + "." : "") + ((INamedObject) r).getName(); //$NON-NLS-1$
		}
		return (getParentDiagram() != null ? getParentDiagram().getName() : "");
	}

	@Override
	public void setDescription(String description) {
		// Nonsense
	}

	@Override
	public void setName(String name) {
		// Nonsense
	}

	@Override
	public IDiagram getParent() {
		return getParentDiagram();
	}

	@Override
	public void setParent(IDiagram parent) {
		setParentDiagram(parent);
	}

	@Override
	public IReference getReference() {
		adjustInternalReference();
		return reference;
	}

	@Override
	public void setReference(IReference ref) {
	}
}
