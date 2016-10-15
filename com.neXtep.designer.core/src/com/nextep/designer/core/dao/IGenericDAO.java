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
package com.nextep.designer.core.dao;

import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.model.UID;

/**
 * This interface describes a generic Data Accessor
 * Object (DAO).<br>
 * A DAO is described by basic functionnalities :<br>
 * - Save operation<br>
 * - load operation<br>
 * - delete operation<br>
 * - loadAll operation<br>
 * - getIdMap operation<br>
 *
 * @author Christophe Fondacci
 *
 * @param <T> is the class handled by the DAO object
 */
public interface IGenericDAO<T> {
	/**
	 * Saves the object to database
	 *
	 * @param object object to save to database
	 */
	public void save(T object);
	/**
	 * Loads the object from database given its unique
	 * identifier
	 *
	 * @param clazz class of the object to load
	 * @param id the unique identifier of the object
	 * @return the loaded object or null if no object has been found with the provided ID
	 */
	public T load(Class<?> clazz, UID id);
	/**
	 * Loads objects from database given its parent ID instead
	 * of the object unique key. Since the result can be more
	 * than one object, the returned type is a list.
	 *
	 * @param clazz the class to load
	 * @param parentID parent ID to use to load objects
	 *
	 * @return a List of the objects loaded from db
	 */
	//public List<T> loadFromParentID(Class clazz, UID parentID);
	/**
	 * Deletes the object from database.
	 *
	 * @param object object to delete from db
	 */
	public void delete(T object);
	/**
	 * Loads all object of the specified class and returns
	 * the loaded list.
	 *
	 * @param clazz class of the object collection to load
	 * @return the loaded List
	 */
	public <U> List<? extends U> loadAll(Class<U> clazz);
	/**
	 * Loads all object of the specified class and returns
	 * the loaded collection as a Map hashed by each object
	 * unique identifiers
	 *
	 * @param clazz class of the object to load
	 * @return a Map of the loaded objects hashed by their ID
	 */
	public Map<UID,T> getIdMap(Class<?> clazz);
}
