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
import org.hibernate.classic.Session;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;

/**
 * @author Christophe Fondacci
 */
public interface IIdentifiableDAO extends IGenericDAO<IdentifiedObject> {

	/**
	 * Loads all elements of the specified class using the provided session
	 * 
	 * @param clazz class of the objects to load from db
	 * @param session Hibernate session to use
	 * @return a list of all objects from this type
	 */
	<T> List<? extends T> loadAll(Class<T> clazz, Session session);

	void delete(IdentifiedObject object, Session session);

	IdentifiedObject load(Class<?> clazz, UID id, Session session, boolean clearSession);

	List<?> loadForeignKey(Class<?> clazz, UID id, String fkName);

	List<?> loadForeignKey(Class<?> clazz, UID id, String fkName, boolean sandBox);

	List<?> loadForeignKey(Class<?> clazz, UID id, String fkName, boolean sandBox,
			boolean clearSession);

	List<?> loadWhere(Class<?> clazz, String columnName, String columnValue);

	void save(IdentifiedObject object, boolean forceSave);

	/**
	 * Saves the specified object to the database.
	 * 
	 * @param object object to save
	 * @param forceSave
	 */
	void save(IdentifiedObject object, boolean forceSave, Session session, boolean clearSession);

	boolean isPersisting();

	void refresh(IdentifiedObject o);

	void clearException();

}
