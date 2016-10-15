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
import com.nextep.installer.model.IDatabaseStructure;
import com.nextep.installer.model.IDatabaseStructureBuilder;

/**
 * @author Christophe Fondacci
 */
public class JdbcStructureBuilder implements IDatabaseStructureBuilder {

	public IDatabaseStructure buildStructure(String schema, Connection conn) throws SQLException {
		final IDatabaseStructure structure = new DatabaseStructure();
		ResultSet rset = null;
		ResultSet rsetIdx = null;
		try {
			DatabaseMetaData md = conn.getMetaData();

			// Listing tables
			rset = md.getTables(null, schema, null, new String[] { "TABLE", "VIEW" });

			// Building an array of checked objects
			while (rset.next()) {
				DBObject dbObj = new DBObject(rset.getString("TABLE_TYPE"),
						rset.getString("TABLE_NAME"));
				structure.addObject(dbObj);
				try {
					rsetIdx = md.getIndexInfo(null, schema, dbObj.getName(), false, false);
					while (rsetIdx.next()) {
						structure.addObject(new DBObject(DBObject.TYPE_INDEX, rsetIdx
								.getString("INDEX_NAME")));
					}
				} catch (SQLException e) {
					// Unable to get the index, may we log anything?
				} finally {
					if (rsetIdx != null) {
						rsetIdx.close();
					}
				}
			}
			rset.close();
			// Fetching table columns
			rset = md.getColumns(null, schema, null, null);
			while (rset.next()) {
				final String tabName = rset.getString("TABLE_NAME");
				final String colName = rset.getString("COLUMN_NAME");
				structure.addObject(new DBObject("COLUMN", tabName + "." + colName));
			}
			// We return the database structure
			return structure;
		} finally {
			if (rset != null) {
				rset.close();
			}
			if (rsetIdx != null) {
				rsetIdx.close();
			}
		}
	}

}
