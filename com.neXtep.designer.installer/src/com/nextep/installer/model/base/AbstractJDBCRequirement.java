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
package com.nextep.installer.model.base;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IRequirement;

public abstract class AbstractJDBCRequirement implements IRequirement {

	/**
	 * Opens a connection on the provided {@link IDatabaseTarget} or raise an exception if no
	 * connection could be established. Note that the returned connection is still opened.
	 * 
	 * @param target the {@link IDatabaseTarget} information on the connection to establish
	 * @return the opened JDBC {@link Connection}
	 * @throws InstallerException whenever the connection could not be established
	 */
	protected Connection getConnectionFor(IDatabaseTarget target) throws InstallerException {
		final DBVendor vendor = target.getVendor();
		String connectionUrl = vendor.buildConnectionURL(target.getHost(), target.getPort(), target
				.getDatabase());
		try {
			Driver driver = (Driver) Class.forName(target.getVendor().getDriverClass())
					.newInstance();
			Properties connectionInfo = new Properties();
			connectionInfo.put("user", target.getUser());
			if (target.getPassword() != null && !"".equals(target.getPassword())) {
				connectionInfo.put("password", target.getPassword());
			}
			Connection connection = driver.connect(connectionUrl, connectionInfo);
			return connection;
		} catch (SQLException e) {
			throw new InstallerException("Unable to connect to database URL [" + connectionUrl
					+ "]", e);
		} catch (ClassNotFoundException e) {
			throw new InstallerException("Unable to load JDBC driver, verify your classpath.", e);
		} catch (InstantiationException e) {
			throw new InstallerException("Problems while initializing JDBC driver.", e);
		} catch (IllegalAccessException e) {
			throw new InstallerException("Illegal access.", e);
		}
	}
}
