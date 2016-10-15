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
package com.nextep.designer.dbgm.model;

/**
 * A storage handle defines a pointer to a local storage. For example with a local embedded
 * database, a {@link IStorageHandle} will typically point to a table in this local database.
 * 
 * @author Christophe Fondacci
 */
public interface IStorageHandle {

	/**
	 * Retrieves the name of the local storage unit.
	 * 
	 * @return the name of the storage unit
	 */
	String getStorageUnitName();

	/**
	 * Retrieves the information needed to insert data in the local storage
	 * 
	 * @return an insert statement
	 */
	String getInsertStatement();

	/**
	 * Retrieves the select statement which retrieves the information from the local storage.
	 * 
	 * @return the select statement
	 */
	String getSelectStatement();

	/**
	 * Indicates the number of columns which should be displayed when displaying this storage unit
	 * to the user. Any other column will be considered as metadata information
	 * 
	 * @return the number of columns to display to the user
	 */
	int getDisplayedColumnsCount();

	/**
	 * Indicates whether this storage corresponds to a repository object in which case it will
	 * contain some internal repository information like the rowid. The rowid is used by the data
	 * services in order to locate a row within a dataset during updates / comparisons.
	 * 
	 * @return <code>true</code> when this storage handle has been created for a repository data
	 *         set, else <code>false</code>
	 */
	boolean isRepositoryHandle();
}
