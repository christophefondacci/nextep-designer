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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.core.services;

import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.core.model.IResourceLocator;

/**
 * The core services provides raw-level method to work with neXtep platform objects.
 * 
 * @author Christophe Fondacci
 */
public interface ICoreService {

	/**
	 * Raw-level service informing whether the specified object is locked or unlocked.
	 * 
	 * @param o object to check for lock status
	 * @return <code>true</code> when object is locked, else <code>false</code>
	 */
	boolean isLocked(Object o);

	/**
	 * Retrieves the lockable instance which controls the lock state of the specified object. The
	 * returned object may or may not be the object itself.
	 * 
	 * @param o the object to get the {@link ILockable} for
	 * @return the {@link ILockable} instance which controls the lock lifecycle of the specified
	 *         object
	 */
	ILockable<?> getLockable(Object o);

	/**
	 * Retrieves the first parent of the specified parentable object having the specified type.
	 * 
	 * @param p the {@link IParentable} to process
	 * @param parentClass the {@link Class} of the parent to look for
	 * @return the first found instance of the parentClass in the parent hierarchy, or
	 *         <code>null</code> if none found
	 */
	<T extends ITypedObject> T getFirstTypedParent(IParentable<?> p, Class<T> parentClass);

	/**
	 * Registers a resource locator under a resource key.
	 * 
	 * @param resourceKey key under which the resource locator will be stored
	 * @param locator the {@link IResourceLocator} to store
	 */
	void registerResource(String resourceKey, IResourceLocator locator);

	/**
	 * Retrieves a registered resource
	 * 
	 * @param resourceKey the key of the resource to retrieve
	 * @return the {@link IResourceLocator} locating the resource or <code>null</code> if none
	 */
	IResourceLocator getResource(String resourceKey);
}
