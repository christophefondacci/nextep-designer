/*******************************************************************************
 * Copyright (c) 2012 neXtep Software and contributors.
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
package com.nextep.installer.model.base;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseConnector;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Bruno Gautier
 */
public abstract class AbstractJDBCDatabaseConnector implements IDatabaseConnector {

	/**
	 * Opens a connection on the provided {@link IDatabaseTarget} or raise an exception if no
	 * connection could be established. Note that the returned connection is still opened.
	 * 
	 * @param target the {@link IDatabaseTarget} information on the connection to establish
	 * @return the opened JDBC {@link Connection}
	 */
	public Connection getConnection(IDatabaseTarget target) throws SQLException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final DBVendor vendor = target.getVendor();

		String connectionUrl = vendor.buildConnectionURL(target.getHost(), target.getPort(),
				target.getDatabase(), target.getTnsAlias());

		try {
			Driver driver = (Driver) Class.forName(target.getVendor().getDriverClass())
					.newInstance();

			Properties connectionInfo = new Properties();
			connectionInfo.put("user", target.getUser()); //$NON-NLS-1$
			if (target.getPassword() != null && !"".equals(target.getPassword())) { //$NON-NLS-1$
				connectionInfo.put("password", getEscapedPassword(target)); //$NON-NLS-1$
			}

			Connection connection = driver.connect(connectionUrl, connectionInfo);

			return connection;
		} catch (ClassNotFoundException cnfe) {
			throw new SQLException("Unable to load JDBC driver, verify your classpath.", cnfe);
		} catch (InstantiationException ie) {
			throw new SQLException("Problems while initializing JDBC driver.", ie);
		} catch (IllegalAccessException iae) {
			throw new SQLException("Illegal access.", iae);
		} catch (SQLException sqle) {
			logger.error("Unable to connect to database URL [" + connectionUrl + "]", sqle); //$NON-NLS-2$
			throw sqle;
		}
	}

	/**
	 * Returns the password of the specified target with reserved characters escaped when necessary.
	 * 
	 * @param target the database target for which we need to return an escaped password
	 * @return the escaped password
	 */
	private String getEscapedPassword(IDatabaseTarget target) {
		final DBVendor vendor = target.getVendor();
		String escapedPassword = target.getPassword();

		switch (vendor) {
		case ORACLE:
			escapedPassword = "\"" + escapedPassword + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}

		return escapedPassword;
	}

}
