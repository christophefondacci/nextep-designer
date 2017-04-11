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
package com.nextep.designer.dbgm.connectors;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class LocalDatabaseConnector implements IDatabaseConnector<Connection> {

	static Map<String, EmbeddedConnectionPoolDataSource> connectionPoolMap = new HashMap<String, EmbeddedConnectionPoolDataSource>();
	private EmbeddedConnectionPoolDataSource connectionPoolDatasource;
	private String databaseName;

	@Override
	public Connection connect(IConnection conn) throws SQLException {
		String databaseName = conn.getDatabase();
		/*
		 * Looking at our data source map. We do this because database connectors are statefull and
		 * initialized each time we need them. Here we don't want to re-establish Derby connection
		 * each time we connect so we store a reference to the connection pool in a static map.
		 */
		connectionPoolDatasource = connectionPoolMap.get(databaseName);
		if (connectionPoolDatasource == null) {
			// No pool yet created, we instantiate it
			connectionPoolDatasource = new EmbeddedConnectionPoolDataSource();
			connectionPoolDatasource.setDatabaseName(databaseName);
			connectionPoolDatasource.setCreateDatabase("create"); //$NON-NLS-1$
			connectionPoolMap.put(databaseName, connectionPoolDatasource);
		}
		return connectionPoolDatasource.getConnection();
	}

	@Override
	public String getConnectionURL(IConnection conn) {
		return databaseName;
	}

	@Override
	public Properties getConnectionInfo(IConnection conn) {
		return new Properties();
	}

	@Override
	public String getJDBCDriverClassName() {
		return null;
	}

	@Override
	public String getSchema(IConnection conn) {
		return null;
	}

	@Override
	public String getCatalog(IConnection conn) {
		return null;
	}

	public void doPostConnectionSettings(IConnection conn, Connection sqlConn) throws SQLException {
		// Do nothing
	}

	public Connection getConnection(IConnection conn) throws SQLException {
		return connect(conn);
	}

}
