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
package com.nextep.designer.core.services.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.services.IConnectionService;

/**
 * The {@link IConnectionService} default implementation.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class ConnectionService implements IConnectionService {

	/** Extension identified for database connector definitions */
	private static final String EXTENSION_ID = "com.neXtep.designer.core.dbConnector"; //$NON-NLS-1$

	@Override
	public IDatabaseConnector<?> getDatabaseConnector(DBVendor vendor) {
		IConfigurationElement elt = Designer.getInstance().getExtension(EXTENSION_ID,
				"dbVendor", vendor.name()); //$NON-NLS-1$
		if (elt == null)
			return null;
		try {
			IDatabaseConnector<?> conn = (IDatabaseConnector<?>) elt
					.createExecutableExtension("class"); //$NON-NLS-1$
			return conn;
		} catch (CoreException e) {
			throw new ErrorException(
					MessageFormat.format(CoreMessages
							.getString("connection.service.connectorNotFound"), vendor.name()), e); //$NON-NLS-1$
		}
	}

	@Override
	public IDatabaseConnector<?> getDatabaseConnector(IConnection conn) {
		if (conn == null) {
			throw new ErrorException(CoreMessages.getString("connection.service.noConnection")); ////$NON-NLS-1$
		}
		IDatabaseConnector<?> connector = getDatabaseConnector(conn.getDBVendor());
		return connector;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Connection connect(IConnection conn) throws SQLException {
		final IDatabaseConnector<Connection> connector = (IDatabaseConnector<Connection>) getDatabaseConnector(conn);
		Connection jdbcConn = connector.getConnection(conn);
		connector.doPostConnectionSettings(conn, jdbcConn);
		return jdbcConn;
	}

}
