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
package com.nextep.designer.sqlgen.mysql.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.base.AbstractDatabaseConnector;
import com.nextep.designer.sqlgen.mysql.MySQLMessages;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MySQLDatabaseConnector extends AbstractDatabaseConnector {

	private static final Log LOGGER = LogFactory.getLog(MySQLDatabaseConnector.class);
	private static final String MYSQL_JDBC_DRIVER_CLASSNAME = "com.mysql.jdbc.Driver"; //$NON-NLS-1$

	@Override
	public Connection getConnection(IConnection conn) throws SQLException {
		final String connURL = getConnectionURL(conn);
		LOGGER.info(MessageFormat.format(MySQLMessages.getString("capturer.mysql.connecting"), //$NON-NLS-1$
				connURL));
		Connection connection = null;
		try {
			DriverManager.setLoginTimeout(15);
			connection = DriverManager.getConnection(connURL, conn.getLogin(), conn.getPassword());
		} catch (SQLException sqle) {
			LOGGER.error("Unable to connect to MySQL database: " + sqle.getMessage(), sqle);
			throw sqle;
		}
		LOGGER.info("MySQL connection established");
		return connection;
	}

	@Override
	public void doPostConnectionSettings(IConnection conn, Connection sqlConn) throws SQLException {
		Statement stmt = null;
		try {
			stmt = sqlConn.createStatement();
			stmt.execute("SET CHARACTER SET utf8"); //$NON-NLS-1$
		} catch (SQLException sqle) {
			LOGGER.error("Unable to set the character set: " + sqle.getMessage(), sqle);
			throw sqle;
		} finally {
			CaptureHelper.safeClose(null, stmt);
		}
	}

	@Override
	public String getConnectionURL(IConnection conn) {
		final String host = conn.getServerIP();
		final String port = conn.getServerPort();
		final String db = conn.getDatabase();
		return "jdbc:mysql://" + host + ":" + port + "/" + db //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				+ "?useUnicode=true&autoReconnect=true"; //$NON-NLS-1$
	}

	@Override
	public String getJDBCDriverClassName() {
		return MYSQL_JDBC_DRIVER_CLASSNAME;
	}

	@Override
	public String getSchema(IConnection conn) {
		return conn.getDatabase();
	}

}
