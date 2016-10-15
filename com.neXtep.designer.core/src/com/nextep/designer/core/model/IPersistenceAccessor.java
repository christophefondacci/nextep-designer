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
package com.nextep.designer.core.model;

import java.util.Collection;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface defines accessor methods to persist an element in a store.
 * 
 * @author Christophe Fondacci
 * @param <T> a typed object class. Generally this parameter will also be an extension of a
 *        {@link IdentifiedObject}
 */
public interface IPersistenceAccessor<T extends ITypedObject> {

	/**
	 * Saves the specified element
	 * 
	 * @param element element to save
	 */
	void save(T element);

	/**
	 * Loads all elements from the given type.
	 * 
	 * @param typeToLoad a {@link IElementType} to load
	 * @return a collection of all existing elements for this type
	 */
	Collection<T> loadAll(IElementType typeToLoad);

	/**
	 * Loads elements from the specified type considering the input parameters passed as a list of
	 * {@link ITypedObject}. The behaviour will depend on the implementation.
	 * 
	 * @param typeToLoad a {@link IElementType} to load compatible objects. Resulting objects will
	 *        always validate the following clause :<br>
	 *        <code>elt.getType() == typeToLoad</code>
	 * @param parents a collection of "parent" elements to use when looking for the typed objects.
	 *        The way those parameters are interpreted depend on the implementation and may change
	 *        from one persistance accessor to another
	 * @return a collection of loaded elements, all of the same <code>typeToLoad</code> type
	 */
	Collection<T> load(IElementType typeToLoad, ITypedObject... parents);

	/**
	 * Delettes the specified element from the persistance storage.
	 * 
	 * @param element element to delete
	 */
	void delete(T element);

	/**
	 * Indicates whether the specified input arguments are valid for this persistance accessor.
	 * Every caller willing to load elements through the
	 * {@link IPersistenceAccessor#load(IElementType, ITypedObject...)} method should check the
	 * validity of their input parameters by calling this method first. This will allow callers to
	 * distinguish empty results from misunderstood requests.
	 * 
	 * @param parents parent arguments to check validity for load call
	 * @return <code>true</code> if the load method can handle this load arguments
	 */
	boolean isHandledForLoad(IElementType typeToLoad, ITypedObject... parents);
}
