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
package com.nextep.designer.sqlgen.db2.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.base.AbstractDatabaseConnector;

/**
 * @author Bruno Gautier
 * @author Christophe Fondacci
 */
public final class DB2DatabaseConnector extends AbstractDatabaseConnector {

	private static final Log LOGGER = LogFactory.getLog(DB2DatabaseConnector.class);
	private static final String DB2_JDBC_DRIVER_CLASSNAME = "com.ibm.db2.jcc.DB2Driver"; //$NON-NLS-1$

	@Override
	public Connection getConnection(IConnection conn) throws SQLException {
		final String connURL = getConnectionURL(conn);
		LOGGER.info("Connecting to DB2 database from URL [" + connURL + "]..."); //$NON-NLS-2$
		Connection connection = null;
		try {
			connection = getDriver().connect(connURL, getConnectionInfo(conn));
		} catch (SQLException sqle) {
			LOGGER.error("Unable to connect to DB2 database: " + sqle.getMessage(), sqle);
			throw sqle;
		}
		LOGGER.info("DB2 connection established");
		return connection;
	}

	@Override
	public void doPostConnectionSettings(IConnection conn, Connection sqlConn) throws SQLException {
		final String schema = conn.getSchema();
		/*
		 * If schema is set to a non-empty value, we set the first value of the CURRENT PATH with
		 * the specified schema. We don't need to set the CURRENT SCHEMA variable as it is already
		 * set with the connection properties.
		 */
		if (schema != null && !"".equals(schema.trim())) { //$NON-NLS-1$
			Statement stmt = null;
			try {
				stmt = sqlConn.createStatement();
				stmt.execute("SET CURRENT PATH " + schema + ", CURRENT PATH"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (SQLException sqle) {
				LOGGER.error("Unable to set the DB2 current path: " + sqle.getMessage(), sqle);
				throw sqle;
			} finally {
				CaptureHelper.safeClose(null, stmt);
			}
		}
	}

	@Override
	public String getConnectionURL(IConnection conn) {
		final String host = conn.getServerIP();
		final String port = conn.getServerPort();
		final String database = conn.getDatabase();
		return "jdbc:db2://" + host + ":" + port + "/" + database; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public Properties getConnectionInfo(IConnection conn) {
		final Properties info = super.getConnectionInfo(conn);
		final String schema = conn.getSchema();
		/*
		 * If a non-empty schema name has been specified in connection settings, we set the
		 * currentSchema property.
		 */
		if (schema != null && !"".equals(schema.trim())) { //$NON-NLS-1$
			info.put("currentSchema", getSchema(conn)); //$NON-NLS-1$
		}
		return info;
	}

	@Override
	public String getJDBCDriverClassName() {
		return DB2_JDBC_DRIVER_CLASSNAME;
	}

	/**
	 * Returns the formatted schema name of the specified connection if available, the formatted
	 * user name otherwise.
	 * 
	 * @param conn a {@link IConnection}
	 * @return a <code>String</code> representing the schema name of the specified connection
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
