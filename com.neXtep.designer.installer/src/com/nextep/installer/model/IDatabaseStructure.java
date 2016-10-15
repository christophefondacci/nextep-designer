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

/**
 * This interface represents a database structure. It represents the structural state of a target
 * database.
 * 
 * @author Christophe Fondacci
 */
public interface IDatabaseStructure {

	/**
	 * Adds a database structural object to this structure definition.
	 * 
	 * @param structuralObject the {@link IDBObject} to add
	 */
	void addObject(IDBObject structuralObject);

	/**
	 * Removes a database structural object from this structure definition
	 * 
	 * @param structuralObject the {@link IDBObject} to remove
	 */
	void removeObject(IDBObject structuralObject);

	/**
	 * Retrieves all structural objects of this database.
	 * 
	 * @return a collection of {@link IDBObject}
	 */
	Collection<IDBObject> getObjects();
}
