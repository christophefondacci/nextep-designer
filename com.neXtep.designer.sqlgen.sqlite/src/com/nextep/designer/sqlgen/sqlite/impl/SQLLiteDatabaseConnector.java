/*******************************************************************************
 * Copyright (c) 2013 neXtep Software and contributors.
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
 * along with neXtep.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.sqlgen.sqlite.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.model.base.AbstractDatabaseConnector;
import com.nextep.designer.sqlgen.sqlite.SQLiteMessages;

/**
 * @author Christophe Fondacci
 * 
 */
public class SQLLiteDatabaseConnector extends AbstractDatabaseConnector {

	private static final Log LOGGER = LogFactory.getLog(SQLLiteDatabaseConnector.class);
	private static final String SQLITE_JDBC_DRIVER_CLASSNAME = "org.sqlite.JDBC"; //$NON-NLS-1$

	@Override
	public Connection getConnection(IConnection conn) throws SQLException {
		final String connURL = getConnectionURL(conn);
		LOGGER.info(MessageFormat.format(SQLiteMessages.getString("connector.sqlite.connecting"), //$NON-NLS-1$
				connURL));
		Connection connection = null;
		try {
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(connURL, conn.getLogin(), conn.getPassword());
		} catch (SQLException sqle) {
			LOGGER.error("Unable to connect to SQLite database: " + sqle.getMessage(), sqle);
			throw sqle;
		}
		LOGGER.info("SQLite connection established");
		return connection;
	}

	@Override
	public void doPostConnectionSettings(IConnection conn, Connection sqlConn) throws SQLException {

	}

	@Override
	public String getConnectionURL(IConnection conn) {
		// final String host = conn.getServerIP();
		// final String port = conn.getServerPort();
		final String database = conn.getDatabase();
		return "jdbc:sqlite:" + database; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public Properties getConnectionInfo(IConnection conn) {
		final Properties info = super.getConnectionInfo(conn);
		final String schema = conn.getSchema();
		/*
		 * If a non-empty schema name has been specified in connection settings,
		 * we set the currentSchema property.
		 */
		if (schema != null && !"".equals(schema.trim())) { //$NON-NLS-1$
			info.put("currentSchema", getSchema(conn)); //$NON-NLS-1$
		}
		return info;
	}

	@Override
	public String getJDBCDriverClassName() {
		return SQLITE_JDBC_DRIVER_CLASSNAME;
	}

	/**
	 * Returns the formatted schema name of the specified connection if
	 * available, the formatted user name otherwise.
	 * 
	 * @param conn
	 *            a {@link IConnection}
	 * @return a <code>String</code> representing the schema name of the
	 *         specified connection
	 */
	@Override
	public String getSchema(IConnection conn) {
		final String schema = conn.getSchema();
		if (schema == null || "".equals(schema.trim())) { //$NON-NLS-1$
			final String user = conn.getLogin();
			if (user != null && !"".equals(user.trim())) { //$NON-NLS-1$
				// User name is trimmed by the vendor formatter
				return conn.getDBVendor().getNameFormatter().format(user);
			}
			return null;
		}
		// Schema name is trimmed by the vendor formatter
		return conn.getDBVendor().getNameFormatter().format(schema);
	}

}
