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

public interface INamedObject {

	/**
	 * 
	 * @return the database object's name
	 */
	public abstract String getName();

	/**
	 * Defines the new name of a database object
	 * 
	 * @param name new database object's name
	 */
	public abstract void setName(String name);

	/**
	 * 
	 * @return the user description of this database object
	 */
	public abstract String getDescription();

	/**
	 * Defines a new user description for this database object
	 * 
	 * @param description description to define for this db object	
	 */
	public abstract void setDescription(String description);

}