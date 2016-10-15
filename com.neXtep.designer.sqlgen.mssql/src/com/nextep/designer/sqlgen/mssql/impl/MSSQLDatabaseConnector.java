/*******************************************************************************
 * Copyright (c) 2010 neXtep Software and contributors.
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
package com.nextep.designer.sqlgen.mssql.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.model.base.AbstractDatabaseConnector;

/**
 * @author Darren Hartford
 * @author Bruno Gautier
 */
public final class MSSQLDatabaseConnector extends AbstractDatabaseConnector {

	private static final Log LOGGER = LogFactory.getLog(MSSQLDatabaseConnector.class);
	private static final String MSSQL_JDBC_DRIVER_CLASSNAME = "net.sourceforge.jtds.jdbc.Driver"; //$NON-NLS-1$

	@Override
	public Connection getConnection(IConnection conn) throws SQLException {
		final String connURL = getConnectionURL(conn);
		LOGGER.info("Connecting to SQL Server database from URL [" + connURL + "]..."); //$NON-NLS-2$
		Connection connection = null;
		try {
			connection = getDriver().connect(connURL, getConnectionInfo(conn));
		} catch (SQLException sqle) {
			LOGGER.error("Unable to connect to SQL Server database: " + sqle.getMessage(), sqle);
			throw sqle;
		}
		LOGGER.info("SQL Server connection established");
		return connection;
	}

	@Override
	public void doPostConnectionSettings(IConnection conn, Connection sqlConn) throws SQLException {
		/*
		 * FIXME [BGA] maybe we can use the statement
		 * "ALTER USER <login> WITH DEFAULT_SCHEMA=<schema_name>" but this is not session specific,
		 * so we would have to issue another "ALTER USER" statement at the end of the session.
		 */
	}

	@Override
	public String getConnectionURL(IConnection conn) {
		final String host = conn.getServerIP();
		final String port = conn.getServerPort();
		final String database = conn.getDatabase();
		return "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + database; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public Properties getConnectionInfo(IConnection conn) {
		final Properties info = super.getConnectionInfo(conn);
		final String instance = conn.getInstance();
		/*
		 * If an instance name has been specified in connection settings, try to connect to a
		 * "named instance". The "SQL Server Browser Service" must be running on the host machine.
		 */
		if (instance != null && !"".equals(instance.trim())) { //$NON-NLS-1$
			info.put("instance", conn.getDBVendor().getNameFormatter().format(instance)); //$NON-NLS-1$
		}
		return info;
	}

	@Override
	public String getJDBCDriverClassName() {
		return MSSQL_JDBC_DRIVER_CLASSNAME;
	}

	@Override
	public String getSchema(IConnection conn) {
		final String schema = conn.getSchema();
		if (schema != null && !"".equals(schema.trim())) { //$NON-NLS-1$
			// Schema name is trimmed by the vendor formatter
			return conn.getDBVendor().getNameFormatter().format(schema);
		}
		return null;
	}

}
