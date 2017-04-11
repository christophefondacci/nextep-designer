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

/**
 * This interface provides access to all information needed to connect to a database.
 * 
 * @author Christophe Fondacci
 */
public interface IDatabaseTarget {

	/**
	 * Database user to connect with
	 * 
	 * @return the database user
	 */
	String getUser();

	/**
	 * User password
	 * 
	 * @return the user password
	 */
	String getPassword();

	/**
	 * The database to connect to
	 * 
	 * @return the database id
	 */
	String getDatabase();

	/**
	 * The schema to connect to
	 * 
	 * @return the schema name
	 */
	String getSchema();

	/**
	 * The port number to use for connection
	 * 
	 * @return the port number
	 */
	String getPort();

	/**
	 * The host name where the database is located
	 * 
	 * @return the host name or ip address of the server database
	 */
	String getHost();

	/**
	 * The vendor of the database to connect to
	 * 
	 * @return the database vendor
	 */
	DBVendor getVendor();

	/**
	 * Retrieves the TNS alias of this target
	 * 
	 * @return the TNS alias to use
	 */
	String getTnsAlias();

	/**
	 * Defines the TNS alias of this target
	 * 
	 * @param tns the TNS alias to use for connection
	 */
	void setTnsAlias(String tns);

	boolean isSso();

}
