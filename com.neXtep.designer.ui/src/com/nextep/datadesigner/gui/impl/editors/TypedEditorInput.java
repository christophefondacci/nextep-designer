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
package com.nextep.datadesigner.gui.impl.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.impl.rcp.PersisterFactory;
import com.nextep.datadesigner.gui.model.ITypePersister;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * A generic editor input for all typed based objects.
 * 
 * @author Christophe Fondacci
 */
public class TypedEditorInput implements IEditorInput, IModelOriented<ITypedObject>, IEventListener {

	/** Our typed object model */
	private ITypedObject model;
	/** Our persister */
	private ITypePersister persister;

	public TypedEditorInput(ITypedObject model) {
		this.model = model;
		if (model != null) {
			persister = PersisterFactory.getPersister(model);
			if (model instanceof IObservable) {
				Designer.getListenerService().registerListener(this, (IObservable) model, this);
			}
		}
	}

	public ITypedObject getModel() {
		return model;
	}

	public void setModel(ITypedObject model) {
		this.model = model;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	@Override
	public boolean exists() {
		return false;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageFactory.getImageDescriptor(model.getType().getIcon());
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	@Override
	public String getName() {
		if (model instanceof INamedObject) {
			String name = ((INamedObject) model).getName();
			return name == null ? "" : name;
		} else {
			return "<Unknown>";
		}
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	@Override
	public IPersistableElement getPersistable() {
		return persister;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	@Override
	public String getToolTipText() {
		return getName();
	}

	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IReferenceable.class && getModel() instanceof IReferenceable) {
			return (IReferenceable) getModel();
		}
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TypedEditorInput) {
			return isAssignable() && ((TypedEditorInput) obj).isAssignable()
					&& (getModel() == ((TypedEditorInput) obj).getModel());
		}
		return false;
	}

	/**
	 * This method allows subclasses to define whether they are assignable to this input or not.
	 * Extend to return false for typed input which should be considered different from a
	 * superclass.
	 * 
	 * @return
	 */
	protected boolean isAssignable() {
		return true;
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (persister != null) {
			persister.handleEvent(event, source, data);
		}
	}
}
