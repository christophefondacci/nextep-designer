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
package com.nextep.designer.core.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import com.nextep.designer.core.services.IConnectionService;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface IDatabaseConnector<T> {

	/**
	 * Connects to the database defined by the settings in the specified <code>IConnection</code>.
	 * 
	 * @param conn a {@link IConnection} which holds parameters identifying the database to which we
	 *        must connect
	 * @return a {@link Connection} with the specified database
	 * @throws SQLException if a database access error occurs
	 * @deprecated use the {@link IConnectionService#connect(IConnection)} method instead
	 */
	@Deprecated
	Connection connect(IConnection conn) throws SQLException;

	/**
	 * Returns a {@link Connection} with the database defined by the settings in the specified
	 * <code>IConnection</code> and perform any necessary pre-connection settings, such as setting
	 * the connection timeout for example.
	 * 
	 * @param conn a {@link IConnection} which holds parameters identifying the database to which we
	 *        must connect
	 * @return a connection with the database defined by the specified {@link IConnection}
	 * @throws SQLException if a database access error occurs
	 */
	T getConnection(IConnection conn) throws SQLException;

	/**
	 * Perform any necessary post-connection settings on the specified <code>Connection</code>, such
	 * as setting the current schema or the client character set for example.
	 * 
	 * @param conn a {@link IConnection} representing the connection settings defined by the user
	 * @param sqlConn a connection with the database defined by the specified {@link IConnection}
	 * @throws SQLException if a database access error occurs
	 */
	void doPostConnectionSettings(IConnection conn, T sqlConn) throws SQLException;

	/**
	 * @return the connection URL built after initialization
	 */
	String getConnectionURL(IConnection conn);

	/**
	 * Returns the properties to be used when connecting to the specified <code>IConnection</code>.
	 * 
	 * @param conn a {@link IConnection} for which we must return a set of properties
	 * @return a {@link Properties} object representing the list of properties to be used when
	 *         connecting to the specified <code>IConnection</code>, must not be <code>null</code>
	 */
	Properties getConnectionInfo(IConnection conn);

	/**
	 * Returns the fully qualified name of the JDBC Driver class.
	 * 
	 * @return a <code>String</code> representing the fully qualified name of the JDBC Driver class
	 *         if available, <code>null</code> otherwise
	 */
	String getJDBCDriverClassName();

	/**
	 * Returns the schema name to which this connector would be connected when using the specified
	 * <code>IConnection</code>.
	 * 
	 * @param conn a {@link IConnection} for which we must return the connection schema name
	 * @return a schema name, <code>null</code> if no schema is available in the specified
	 *         <code>IConnection</code> or if the schema concept is not handled by the vendor of
	 *         this connector implementation
	 */
	String getSchema(IConnection conn);

	/**
	 * Returns the catalog name to which this connector would be connected when using the specified
	 * <code>IConnection</code>.
	 * 
	 * @param conn a {@link IConnection} for which we must return the connection catalog name
	 * @return a catalog name, <code>null</code> if no catalog is available in the specified
	 *         <code>IConnection</code> or if the catalog concept is not handled by the vendor of
	 *         this connector implementation
	 */
	String getCatalog(IConnection conn);

}
