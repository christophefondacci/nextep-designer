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
package com.nextep.installer.model.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.IDatabaseStructure;
import com.nextep.installer.model.IDatabaseStructureBuilder;
import com.nextep.installer.services.ILoggingService;

/**
 * The Oracle object checker.
 * 
 * @author Christophe Fondacci
 */
public class OracleStructureBuilder implements IDatabaseStructureBuilder {

	public IDatabaseStructure buildStructure(String schema, Connection conn) throws SQLException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final IDatabaseStructure structure = new DatabaseStructure();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			// Retrieving all defined objects for the specified user
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT object_type, object_name FROM user_objects"); //$NON-NLS-1$

			// Building an array of checked objects
			while (rset.next()) {
				DBObject dbObj = new DBObject(rset.getString(1), rset.getString(2));
				structure.addObject(dbObj);
			}
			rset.close();

			rset = stmt.executeQuery("SELECT master,log_table FROM user_mview_logs"); //$NON-NLS-1$
			while (rset.next()) {
				final String master = rset.getString(1);
				final String logTab = rset.getString(2);
				structure.removeObject(new DBObject("TABLE", logTab)); //$NON-NLS-1$
				structure.addObject(new DBObject("MVIEW_LOG", master + " (log)")); //$NON-NLS-1$ //$NON-NLS-2$
			}
			rset.close();

			rset = stmt.executeQuery("SELECT table_name||'.'||column_name FROM user_tab_columns"); //$NON-NLS-1$
			while (rset.next()) {
				final String colName = rset.getString(1);
				structure.addObject(new DBObject("COLUMN", colName)); //$NON-NLS-1$
			}

			// We return the checked objects list
			return structure;
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException sqle) {
				logger.log("Unable to close statement: " + sqle.getMessage()); //$NON-NLS-1$
			}
		}
	}

}
