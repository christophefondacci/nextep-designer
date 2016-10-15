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
package com.nextep.designer.sqlgen.ui.model;

import java.sql.Connection;
import com.nextep.designer.core.model.IConnection;

/**
 * This interface defines the ability for an UI element to be connected to a database using a JDBC
 * connection.
 * 
 * @author Christophe Fondacci
 */
public interface IConnectable {

	/**
	 * A SQL editor input is connectable to a database in order to allow instant query execution. As
	 * soon as the editor needs to run a query, the JDBC connection is injected in the input and
	 * becomes available through this method.
	 * 
	 * @return the {@link Connection} with which the editor is associated, or <code>null</code> when
	 *         this input has not yet been connected.
	 */
	Connection getSqlConnection();

	/**
	 * Defines the JDBC {@link Connection} with which this input is associated. A SQL editor input
	 * is connectable to a database in order to allow instant query execution. As soon as the editor
	 * needs to run a query, the JDBC connection is injected in the input by calling this method.
	 * 
	 * @param sqlConnection the JDBC {@link Connection} to which this input is associated
	 */
	void setSqlConnection(Connection sqlConnection);

	IConnection getConnection();

	void setConnection(IConnection connection);
}
