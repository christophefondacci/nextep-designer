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
package com.nextep.designer.core.services;

import java.sql.Connection;
import java.sql.SQLException;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;

/**
 * This interface provides methods to manage connections and connectors to a database.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface IConnectionService {

	/**
	 * Provides the database connector for the specified database vendor
	 * 
	 * @param vendor vendor to retrieve the connector for
	 * @return a {@link IDatabaseConnector} implementation able to connect to the specified database
	 *         vendor
	 */
	IDatabaseConnector<?> getDatabaseConnector(DBVendor vendor);

	/**
	 * Retrieves the database connector ready to connect to the specified connection definition.
	 * While the {@link IConnection} defines connection parameters, the {@link IDatabaseConnector}
	 * is the abstraction which can literally perform the connection to the database defined by the
	 * connection object.
	 * 
	 * @param c the {@link IConnection} which holds parameters to connect to the database
	 * @return the appropriate {@link IDatabaseConnector}, ready to connect to the database.
	 */
	IDatabaseConnector<?> getDatabaseConnector(IConnection c);

	/**
	 * Connects to the database defined by the settings in the specified <code>IConnection</code>.
	 * 
	 * @param conn a {@link IConnection} which holds parameters identifying the database to which we
	 *        must connect
	 * @return a {@link Connection} with the specified database
	 * @throws SQLException if a database access error occurs
	 */
	Connection connect(IConnection conn) throws SQLException;

}
