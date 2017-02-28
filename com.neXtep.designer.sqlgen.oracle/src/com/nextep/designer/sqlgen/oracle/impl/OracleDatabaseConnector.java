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
package com.nextep.designer.sqlgen.oracle.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.model.base.AbstractDatabaseConnector;
import com.nextep.designer.sqlgen.oracle.OracleMessages;

/**
 * This class is a database connector which provides the logic of connecting to an Oracle database
 * through the JDBC.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleDatabaseConnector extends AbstractDatabaseConnector {

	private static final Log LOGGER = LogFactory.getLog(OracleDatabaseConnector.class);
	private static final String ORACLE_JDBC_DRIVER_CLASSNAME = "oracle.jdbc.driver.OracleDriver"; //$NON-NLS-1$

	@Override
	public Connection getConnection(IConnection conn) throws SQLException {
		LOGGER.info(MessageFormat.format(OracleMessages.getString("connecting"), conn)); //$NON-NLS-1$
		Connection connection = null;
		try {
			connection = getDriver().connect(getConnectionURL(conn), getConnectionInfo(conn));
		} catch (SQLException sqle) {
			LOGGER.error("Unable to connect to Oracle database: " + sqle.getMessage(), sqle);
			throw sqle;
		}
		LOGGER.info(OracleMessages.getString("connectionEstablished")); //$NON-NLS-1$
		return connection;
	}

	@Override
	public void doPostConnectionSettings(IConnection conn, Connection sqlConn) throws SQLException {
		// Nothing to do
	}

	@Override
	public Properties getConnectionInfo(IConnection conn) {
		final Properties props = super.getConnectionInfo(conn);
		// props.put("internal_logon", "sysdba");
		return props;
	}

	@Override
	public String getJDBCDriverClassName() {
		return ORACLE_JDBC_DRIVER_CLASSNAME;
	}

	/**
	 * Retrieves the prefix of a string by extracting any prefixed string followed by "_"
	 * 
	 * @param name the prefix will be extracted from this string
	 * @return the string prefix or "" if none
	 */
	public String getPrefix(String name) {
		if (name.indexOf("_") > 0) { //$NON-NLS-1$
			String prefix = name.substring(0, name.indexOf("_")); //$NON-NLS-1$
			if (prefix != null && !prefix.equals(name) && prefix.length() > 0) {
				return prefix;
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getConnectionURL(IConnection conn) {
		final String alias = conn.getTnsAlias();

		StringBuilder sb = new StringBuilder("jdbc:oracle:thin:@"); //$NON-NLS-1$
		sb.append("//") //$NON-NLS-1$
				.append(conn.getServerIP()).append(":") //$NON-NLS-1$
				.append(conn.getServerPort()).append("/"); //$NON-NLS-1$

		sb.append((alias != null && !"".equals(alias.trim()) ? alias : conn.getDatabase())); //$NON-NLS-1$

		return sb.toString();
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
		String schema = conn.getSchema();
		if (schema == null || "".equals(schema.trim())) { //$NON-NLS-1$
			String user = conn.getLogin();
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
