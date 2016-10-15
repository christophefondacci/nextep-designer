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
package com.nextep.designer.sqlgen.postgre.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.base.AbstractDatabaseConnector;

/**
 * Database connector for PostGreSQL database vendor.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PostgreSqlDatabaseConnector extends AbstractDatabaseConnector {

	private static final Log LOGGER = LogFactory.getLog(PostgreSqlDatabaseConnector.class);
	private static final String POSTGRE_JDBC_DRIVER_CLASSNAME = "org.postgresql.Driver"; //$NON-NLS-1$
	private static final String POSTGRE_DEFAULT_SCHEMA = "public"; //$NON-NLS-1$

	@Override
	public Connection getConnection(IConnection conn) throws SQLException {
		final String connURL = getConnectionURL(conn);
		LOGGER.info("Connecting to PostgreSQL database from URL [" + connURL + "]..."); //$NON-NLS-2$
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(connURL, conn.getLogin(), conn.getPassword());
		} catch (SQLException sqle) {
			LOGGER.error("Unable to connect to PostgreSQL database: " + sqle.getMessage(), sqle);
			throw sqle;
		}
		LOGGER.info("PostgreSQL connection established");
		return connection;
	}

	@Override
	public void doPostConnectionSettings(IConnection conn, Connection sqlConn) throws SQLException {
		final String schema = conn.getSchema();

		if (schema != null && !"".equals(schema.trim())) { //$NON-NLS-1$
			Statement stmt = null;
			try {
				stmt = sqlConn.createStatement();
				stmt.execute("SET search_path TO " + schema + ",public"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (SQLException sqle) {
				LOGGER.error("Unable to set the search_path variable: " + sqle.getMessage(), sqle);
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
		final String db = conn.getDatabase();
		return "jdbc:postgresql://" + host + ":" + port + "/" + db; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public String getJDBCDriverClassName() {
		return POSTGRE_JDBC_DRIVER_CLASSNAME;
	}

	@Override
	public String getSchema(IConnection conn) {
		String schema = conn.getSchema();
		if (schema == null || "".equals(schema.trim())) { //$NON-NLS-1$
			return POSTGRE_DEFAULT_SCHEMA;
		} else {
			// Schema name is trimmed by the vendor formatter
			return schema;
		}
	}

}
