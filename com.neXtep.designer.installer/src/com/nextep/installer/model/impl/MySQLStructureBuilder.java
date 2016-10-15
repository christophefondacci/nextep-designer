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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.model.IDatabaseStructure;
import com.nextep.installer.model.IDatabaseStructureBuilder;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Christophe Fondacci
 */
public class MySQLStructureBuilder implements IDatabaseStructureBuilder {

	public IDatabaseStructure buildStructure(String schema, Connection conn) throws SQLException {
		final ILoggingService logger = NextepInstaller.getService(ILoggingService.class);
		final IDatabaseStructure structure = new DatabaseStructure();
		ResultSet rset = null;
		ResultSet rsetIdx = null;
		Statement stmt = null;
		try {
			DatabaseMetaData md = conn.getMetaData();

			// Listing tables
			rset = md.getTables(null, schema, null, new String[] { "TABLE", "VIEW" }); //$NON-NLS-1$ //$NON-NLS-2$

			// Building an array of checked objects
			while (rset.next()) {
				DBObject dbObj = new DBObject(rset.getString("TABLE_TYPE"), //$NON-NLS-1$
						rset.getString("TABLE_NAME")); //$NON-NLS-1$
				structure.addObject(dbObj);
				rsetIdx = md.getIndexInfo(null, schema, dbObj.getName(), false, false);
				while (rsetIdx.next()) {
					structure.addObject(new DBObject(DBObject.TYPE_INDEX, rsetIdx
							.getString("INDEX_NAME"))); //$NON-NLS-1$
				}
				rsetIdx.close();
			}
			rset.close();
			// Fetching table columns
			rset = md.getColumns(null, schema, null, null);
			while (rset.next()) {
				final String tabName = rset.getString("TABLE_NAME"); //$NON-NLS-1$
				final String colName = rset.getString("COLUMN_NAME"); //$NON-NLS-1$
				structure.addObject(new DBObject("COLUMN", tabName + "." + colName)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SHOW TRIGGERS"); //$NON-NLS-1$
			while (rset.next()) {
				String name = rset.getString("Trigger"); //$NON-NLS-1$
				DBObject trigger = new DBObject("TRIGGER", name); //$NON-NLS-1$
				structure.addObject(trigger);
			}
			rset.close();
			try {
				rset = stmt.executeQuery("select name from mysql.proc"); // where db='" + //$NON-NLS-1$
				// getSchema() + "'");
				while (rset.next()) {
					structure.addObject(new DBObject("PROCEDURE", rset.getString(1))); //$NON-NLS-1$
				}
			} catch (SQLException e) {
				logger.log("Warning: unable to retrieve stored procedure info, please grant mysql schema to this user."); //$NON-NLS-1$
			}

			// We return the checked objects list
			return structure;
		} finally {
			if (rset != null) {
				rset.close();
			}
			if (rsetIdx != null) {
				rsetIdx.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

}
