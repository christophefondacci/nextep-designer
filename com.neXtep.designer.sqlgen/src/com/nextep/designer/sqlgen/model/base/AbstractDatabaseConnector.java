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
package com.nextep.designer.sqlgen.model.base;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.sqlgen.helpers.ConnectorHelper;

/**
 * This interface is the connector for a database.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public abstract class AbstractDatabaseConnector extends Observable implements
		IDatabaseConnector<Connection> {

	private static final Log LOGGER = LogFactory.getLog(AbstractDatabaseConnector.class);
	private static final int LOGIN_TIMEOUT = 10;

	private Driver driver;

	/**
	 * This constructor manually load any JDBC drivers prior to version 4.0. As for JDBC 4.0
	 * drivers, if some are found in the class path, they are automatically loaded by the
	 * <code>DriverManager</code> class when it first attempts to establish a connection.
	 */
	public AbstractDatabaseConnector() {
		DriverManager.setLoginTimeout(LOGIN_TIMEOUT);
		String driverClassName = getJDBCDriverClassName();
		if (driverClassName != null) {
			try {
				driver = (Driver) Class.forName(driverClassName).newInstance();
			} catch (Exception e) {
				throw new ErrorException(e);
			}
		} else {
			LOGGER.info("This IDatabaseConnector implementation does not provide a JDBC driver, "
					+ "you may use a vendor specific IDatabaseConnector implementation in order to "
					+ "connect to the database");
		}
	}

	@Override
	public final Connection connect(IConnection conn) throws SQLException {
		return CorePlugin.getConnectionService().connect(conn);
	}

	@Override
	public Properties getConnectionInfo(IConnection conn) {
		Properties info = new Properties();
		if (!conn.isSsoAuthentication()) {
			info.put("user", conn.getLogin()); //$NON-NLS-1$
			info.put("password", ConnectorHelper.getEscapedPassword(conn)); //$NON-NLS-1$
		}
		return info;
	}

	@Override
	public String getCatalog(IConnection conn) {
		return null;
	}

	protected Driver getDriver() {
		return driver;
	}

}
