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
package com.nextep.datadesigner.impl;

import java.text.MessageFormat;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.model.ICheckedObject;

/**
 * A convenient super class which defines standard behavior of naming and listening. Almost all
 * classes from the object model have these 2 behaviors and should derived from this abstract class,
 * unless it needs other extension. Moreover, this class provides a default IReferenceable
 * implementation for consistent integration with the ReferenceManager. Even though default
 * implementation is provided, this class does not declare any IReferenceable implementation.
 * Classes which need to implement IReferenceable should only declare it and do not bother with
 * implementation.
 * 
 * @author Christophe Fondacci
 */
public abstract class NamedObservable extends Observable implements INamedObject, IObservable,
		ICheckedObject {

	// Protected helper which should be initialized in child classes
	protected final NamedObjectHelper nameHelper = new NamedObjectHelper(null, null);
	protected Referenceable referenceable = new Referenceable(this);

	public String getDescription() {
		return nameHelper.getDescription();
	}

	public String getName() {
		// Could not be null
		return nameHelper.getName();
	}

	public final void setDescription(String description) {
		String currentDescription = nameHelper.getDescription();
		// Setting only on effective change
		if ((currentDescription != null && !currentDescription.equals(description))
				|| (currentDescription == null && description != null)) {

			nameHelper.setDescription(description);
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	public void setName(String name) {
		String currentName = nameHelper.getName();
		// Setting only on effective change
		if ((currentName != null && !currentName.equals(name))
				|| (currentName == null && name != null)) {

			nameHelper.setName(name != null ? name.trim() : ""); //$NON-NLS-1$
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * Alpha-Naming comparison
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if (o instanceof INamedObject) {
			INamedObject n = (INamedObject) o;
			return this.getName().compareTo(n.getName());
		}
		throw new ClassCastException("Cannot compare class <" + this.getClass().getName()
				+ "> with name \"" + getName() + "\" to an unnamed object");
	}

	public String toString() {
		if (nameHelper != null) {
			final String name = getName();
			return name == null ? "" : name; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Prebuilt implementation of the IReferenceable interface
	 * 
	 * @return
	 */
	public IReference getReference() {
		return referenceable.getReference();
	}

	/**
	 * Prebuilt implementation of the IReferenceable interface
	 * 
	 * @param ref
	 */
	public void setReference(IReference ref) {
		referenceable.setReference(ref);
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		if (getName() == null || "".equals(getName().trim())) { //$NON-NLS-1$
			String elementTypeName = ""; //$NON-NLS-1$
			if (this instanceof ITypedObject) {
				elementTypeName = ((ITypedObject) this).getType().getName().toLowerCase();
			}
			throw new InconsistentObjectException(MessageFormat.format(
					CoreMessages.getString("nameMustExist"), elementTypeName)); //$NON-NLS-1$
		}
	}

}
