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
package com.nextep.designer.testing.impl;

import java.sql.Connection;
import java.sql.SQLException;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.testing.model.TestStatus;

/**
 * A compatiblity test which uses a database connection to perform the compatibility check rather
 * than the repository.
 * 
 * @author Christophe
 */
public abstract class DatabaseCompatibilityTest extends CompatibilityTest {

	@Override
	public TestStatus run(IProgressMonitor monitor) {
		final IConnection conn = getConnection();
		IDatabaseConnector dbConnector = CorePlugin.getConnectionService().getDatabaseConnector(
				conn.getDBVendor());
		Connection c = null;
		try {
			c = dbConnector.connect(conn);
			return runDatabase(monitor, c);
		} catch (SQLException e) {
			throw new ErrorException(e);
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					throw new ErrorException("Error while trying to release SQL connection");
				}
			}
		}
	}

	/**
	 * Runs the tests using a SQL connection
	 * 
	 * @param conn
	 * @return
	 */
	protected abstract TestStatus runDatabase(IProgressMonitor monitor, Connection conn);
}
