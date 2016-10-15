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
package com.nextep.datadesigner.model;

import com.nextep.designer.core.model.impl.ReferenceManager;

/**
 * Defines a soft reference to a repository object.
 * 
 * @author Christophe Fondacci
 * 
 */
public interface IReference extends ITypedObject, IReferenceable, IdentifiedObject {

	public static final String TYPE_ID = "REFERENCE"; //$NON-NLS-1$

	/**
	 * @return the unique identifier of this reference
	 */
	public abstract UID getReferenceId();

	/**
	 * The arbitrary name of a reference is the last name used in the last
	 * checkin operation of an instance of this reference. It is used to be able
	 * to "name" the reference before knowing which version instance of this
	 * reference we will work on. Since the name depend on the version, nobody
	 * can rely on this name. It is only used for a user reference selection.
	 * 
	 * @return the arbitrary name of this reference
	 */
	public abstract String getArbitraryName();

	/**
	 * Sets the new arbitrary name of this reference. This should happen on
	 * checkin to update the reference "last known" name.
	 * 
	 * @param arbitraryName
	 *            the new arbitrary name
	 */
	public abstract void setArbitraryName(String arbitraryName);

	/**
	 * Defines the object instance registered in the workspace for this
	 * reference. The reference instance is used by the reference manager to
	 * correctly match "volatile" references, that means not-yet persisted
	 * references which have no ID
	 * 
	 * @param referenceable
	 *            the reference instance
	 */
	public abstract void setInstance(IReferenceable referenceable);

	/**
	 * @return the instance registered for this reference. This method should
	 *         only be used when manipulating volatile references
	 */
	public abstract IReferenceable getInstance();

	/**
	 * @return whether this reference is volatile. Volatile references are
	 *         references of objects which does not belong to the current view's
	 *         contents. That is to say references which have been loaded
	 *         <b>after</b> the view by direct load calls in a sandbox hibernate
	 *         session.
	 */
	public abstract boolean isVolatile();

	/**
	 * Defines whether this reference is volatile or not. At creation time,
	 * references are non-volatile by default so this method should be called to
	 * define volatile references. <b>This method should NOT be used
	 * directly</b> (let the framework do), as this would lead to unpredictable
	 * behaviour of the {@link ReferenceManager}. This method is automatically
	 * called when loading objects outside the main session.
	 * 
	 * @param isVolatile
	 *            define the new volatile status
	 */
	public abstract void setVolatile(boolean isVolatile);

	/**
	 * Defines the reference context of this reference. This context is used by
	 * the {@link ReferenceManager} for volatile references to isolate sets of
	 * references. This method should not be called directly.
	 * 
	 * @param context
	 */
	public abstract void setReferenceContext(ReferenceContext context);

	/**
	 * @return the reference context of this reference
	 * @see IReference#setReferenceContext(ReferenceContext)
	 */
	public abstract ReferenceContext getReferenceContext();
}