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
package com.nextep.installer.model;

import java.util.Collection;
import java.util.List;
import com.nextep.installer.model.impl.DBObject;

/**
 * This interface represents a database object check which can check whether a collection of
 * elements are present or not in a database using a JDBC connection.
 * 
 * @author Christophe Fondacci
 */
public interface IDatabaseObjectCheck extends ICheck {

	/**
	 * Adds an element to the collection of elements to check.
	 * 
	 * @param obj a {@link DBObject} to check
	 */
	void addObject(IDBObject obj);

	/**
	 * Retrieves the list of all objects that needs to be checked.
	 * 
	 * @return a collection of {@link DBObject} to check
	 */
	Collection<IDBObject> getObjectsToCheck();

	/**
	 * @return the list of missing objects
	 */
	List<IDBObject> getMissingObjects();

}
