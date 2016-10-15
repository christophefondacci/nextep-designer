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
package com.nextep.designer.core.model;

import java.util.Collection;
import java.util.List;
import org.hibernate.Session;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.ReferenceContext;
import com.nextep.designer.core.model.impl.ReferenceManager;

/**
 * The reference manager is a centralized access point for references management. All initialized
 * references will be registered in the manager. Any object which has a reference to other
 * repository objects should use the reference manager
 * {@link IReferenceManager#getReferencedItems(IReference)} method to retrieve the currently
 * referenced object.<br>
 * This manager handles either volatile or persistent reference. Volatile references are the ones
 * which has not yet been persisted in the repository database.
 * 
 * @author Christophe Fondacci
 */
public interface IReferenceManager {

	/**
	 * Retrieves all {@link IReferenceable} instances which points to this reference. This method
	 * handles volatile and persistent references.
	 * 
	 * @param ref reference used by the searched instances
	 * @return a collection of {@link IReferenceable} instances which return the specified reference
	 *         when calling the {@link IReferenceable#getReference()} method.
	 */
	List<IReferenceable> getReferencedItems(IReference ref);

	/**
	 * References a new instance.
	 * 
	 * @param ref the absolute reference
	 * @param refInstance instance to reference
	 */
	void reference(IReference ref, IReferenceable refInstance);

	void reference(IReference ref, IReferenceable refInstance, boolean updateIfExists);

	/**
	 * Registers a new volatile instance reference. A volatile reference instance will not interfere
	 * with currently registered references and may have a short life time.
	 * 
	 * @param ref the absolute reference
	 * @param refInstance instance to register
	 */
	void volatileReference(IReference ref, IReferenceable refInstance, Session session);

	/**
	 * Clears all registered volatile references
	 */
	void flushVolatiles(Session session);

	void flush();

	/**
	 * Dereferences an object instance
	 * 
	 * @param instance instance to dereference
	 */
	void dereference(IReferenceable instance);

	/**
	 * Computing reverse dependencies to the specified referenceable elements. All dependencies will
	 * be computed.
	 * 
	 * @see ReferenceManager#getReverseDependencies(IReferenceable, IElementType)
	 * @param ref reference for which the dependencies will be retrieved
	 * @return a collection of all items referencing the specified element
	 */
	Collection<IReferencer> getReverseDependencies(IReferenceable ref);

	/**
	 * This method provide the reverse dependencies of a given IReferenceable object. Reverse
	 * dependencies are objects which have a reference to the specified {@link IReferenceable}
	 * object. Type of the returned reverse dependencies is {@link IReferencer} since they have a
	 * reference to the specified {@link IReferenceable} object. The following condition will be
	 * true for every item of the returned list:<br>
	 * obj.getReferenceDependencies().contains(ref)
	 * 
	 * @param ref reference for which we like to retrieve the reverse dependencies.
	 * @return a collection of {@link IReferencer} objects which have a reference to parameter ref.
	 */
	@SuppressWarnings("unchecked")
	Collection<IReferencer> getReverseDependencies(IReferenceable ref, IElementType type);

	/**
	 * Compiles the reverse dependencies map of the current view.
	 * 
	 * @return a multi valued map whose key are {@link IReference} instances and the value is a
	 *         collection of {@link IReferencer} depending on it
	 */
	DependenciesMap getReverseDependenciesMap();

	DependenciesMap getReverseDependenciesMapFor(IReference r);

	/**
	 * Compiles the reverse dependencies map of the current view, containing only dependencies
	 * matching the specified {@link IElementType}.
	 * 
	 * @return a multi valued map whose key are {@link IReference} instances and the value is a
	 *         collection of {@link IReferencer} depending on it
	 */
	DependenciesMap getReverseDependenciesMap(IElementType type);

	/**
	 * Searches in all registered references the instances matching the specified object type and
	 * name. This method will throw an {@link ErrorException} when there is more than 1 match.<br>
	 * Use with care as this might be time consuming.
	 * 
	 * @param type type of the item to look for
	 * @param name name of the item to look for
	 * @return the referenceable object (actually a {@link ITypedObject} and {@link INamedObject}
	 *         implementation)
	 */
	IReferenceable findByTypeName(IElementType type, String name) throws ReferenceNotFoundException;

	IReferenceable findByTypeName(IElementType type, String name, boolean ignoreCase)
			throws ReferenceNotFoundException;

	/**
	 * @param s session used to load references
	 * @return the reference context to use when loading objects from this session
	 */
	ReferenceContext getReferenceContext(Session s);

	void addReferencer(IReferencer referencer);

	void removeReferencer(IReferencer referencer);

	/**
	 * Indicates to the reference manager that we start to load a workspace
	 */
	void startWorkspaceLoad();

	/**
	 * Indicates to the reference manager that we finished to load a workspace
	 */
	void endWorkspaceLoad();
}
