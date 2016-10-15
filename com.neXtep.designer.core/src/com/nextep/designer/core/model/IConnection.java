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
/**
 *
 */
package com.nextep.designer.core.model;

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * Interface describing a database connection. A database connection defines all elements for the
 * application to connect to a database schema using a JDBC connection.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface IConnection extends INamedObject, ITypedObject, IObservable, IdentifiedObject {

	public static final String TYPE_ID = "CONNECTION"; //$NON-NLS-1$

	/**
	 * @return the database login of this connection
	 */
	String getLogin();

	/**
	 * Defines the database login of this connection
	 * 
	 * @param login
	 */
	void setLogin(String login);

	/**
	 * @return the database password of this connection
	 */
	String getPassword();

	/**
	 * Defines the database password of this connection
	 * 
	 * @param password database password
	 */
	void setPassword(String password);

	/**
	 * Indicates whether the password should be saved along with the connection or if it will be
	 * prompted each time we need to connect.
	 * 
	 * @return <code>true</code> if the password is saved, else <code>false</code>
	 */
	boolean isPasswordSaved();

	/**
	 * Defines whether he password should be saved along with the connection or if it will be
	 * prompted each time we need to connect.
	 * 
	 * @param saved <code>true</code> to indicate that the password is saved, else
	 *        <code>false</code>
	 */
	void setPasswordSaved(boolean saved);

	/**
	 * @return the database SID
	 */
	String getDatabase();

	/**
	 * Defines the database SID of this connection
	 * 
	 * @param database database SID
	 */
	void setDatabase(String database);

	/**
	 * @return the IP address of the database server
	 */
	String getServerIP();

	/**
	 * Defines the IP address of the database server
	 * 
	 * @param serverIP ip address of the database server
	 */
	void setServerIP(String serverIP);

	/**
	 * @return the port number of the database listener service
	 */
	String getServerPort();

	/**
	 * Defines the port number of the database listener service
	 * 
	 * @param port port number
	 */
	void setServerPort(String port);

	/**
	 * @return the vendor of this connection
	 */
	DBVendor getDBVendor();

	/**
	 * Defines the vendor of this connection
	 * 
	 * @param vendor vendor to use with this connection
	 */
	void setDBVendor(DBVendor vendor);

	/**
	 * Defines the TNS alias of this connection (for Oracle database connections)
	 * 
	 * @param tnsAlias TNS alias of the connection
	 */
	void setTnsAlias(String tnsAlias);

	/**
	 * @return the TNS alias of the connection
	 */
	String getTnsAlias();

	/**
	 * Defines the database schema to connect to
	 * 
	 * @param schema schema name
	 */
	void setSchema(String schema);

	/**
	 * @return the schema to connect to, can be <code>null</code>
	 */
	String getSchema();

	/**
	 * Returns the database instance name.
	 * 
	 * @return the database instance name
	 */
	String getInstance();

	/**
	 * Defines the database instance name.
	 * 
	 * @param instance the database instance name
	 */
	void setInstance(String instance);

	/**
	 * Indicates whether the authentication method is based on a platform specific Single Sign On
	 * mechanism or a standard authentication with a database user.
	 * 
	 * @return <code>true</code> if authentication method is SSO, <code>false</code> otherwise
	 */
	boolean isSsoAuthentication();

	/**
	 * Defines whether the authentication method is based on a platform specific Single Sign On
	 * mechanism or a standard authentication with a database user.
	 * 
	 * @param sso <code>true</code> if authentication method is SSO, <code>false</code> otherwise
	 */
	void setSsoAuthentication(boolean sso);

}
