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
package com.nextep.designer.core.services;

import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;

public interface IRepositoryService {

	/**
	 * Retrieves the currently defined repository connector
	 * 
	 * @return a {@link IDatabaseConnector} to use to connect to repository.
	 */
	IDatabaseConnector getRepositoryConnector();

	/**
	 * Retrieves the vendor of the repository database.
	 * 
	 * @return the repository database vendor
	 */
	DBVendor getRepositoryVendor();

	/**
	 * Encrypts the specified password
	 * 
	 * @param password clear password string
	 * @return the encrypted string representation of this password text
	 */
	String encryptPassword(String password);

	/**
	 * Decrypts the secured password string back to a clear original password
	 * 
	 * @param encryptedPassword encrypted string representation of a password
	 * @return the original password
	 */
	String decrytPassword(String encryptedPassword);

	/**
	 * Retrieves the repository connection from the preferences
	 * 
	 * @return the current repository connection
	 */
	IConnection getRepositoryConnection();

	/**
	 * Defines the repository connection in the preferences
	 * 
	 * @param repositoryConnection the current repository connection
	 */
	void setRepositoryConnection(IConnection repositoryConnection);

	/**
	 * Retrieves a property stored in the repository
	 * 
	 * @param name name of the property to retrieve
	 * @return the property value or <code>null</code> if not found
	 */
	String getProperty(String name);

	/**
	 * Sets the property in the repository
	 * 
	 * @param name name of the property to set
	 * @param value the property value to store
	 */
	void setProperty(String name, String value);
}
