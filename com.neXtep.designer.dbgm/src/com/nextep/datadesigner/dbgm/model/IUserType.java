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
package com.nextep.datadesigner.dbgm.model;

import java.util.List;

/**
 * This interface represents a database user type.
 * 
 * @author Christophe Fondacci
 *
 */
public interface IUserType extends IDatabaseObject<IUserType> {
	
	public static final String TYPE_ID = "USER_TYPE";
	
	/**
	 * @return the collection of columns defining this type
	 */
	public List<ITypeColumn> getColumns();
	/**
	 * Defines the collection of columns which consitute this
	 * type.
	 * @param columns
	 */
	public void setColumns(List<ITypeColumn> columns);
	/**
	 * Adds a column to this type 
	 * 
	 * @param column the type column to add
	 */
	public void addColumn(ITypeColumn column);
	/**
	 * Removes a column from this type
	 * @param column column to remove from this type
	 */
	public void removeColumn(ITypeColumn column);

}
