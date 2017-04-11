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
 * @author Bruno Gautier
 */
public class JdbcStructureBuilder implements IDatabaseStructureBuilder {

	public IDatabaseStructure buildStructure(String schema, Connection conn) throws SQLException {
		final IDatabaseStructure structure = new DatabaseStructure();

		/*
		 * If specified schema name is empty, it is replaced by null so it won't be used to narrow
		 * the search of objects in the current database.
		 */
		String schemaPattern = ((schema != null && !schema.trim().equals("")) ? schema.trim() //$NON-NLS-1$
				: null);

		ResultSet rset = null;
		ResultSet rsetIdx = null;
		try {
			DatabaseMetaData md = conn.getMetaData();

			rset = md.getTables(null, schemaPattern, null, new String[] { "TABLE", "VIEW" }); //$NON-NLS-1$ //$NON-NLS-2$

			// Building an array of checked objects
			while (rset.next()) {
				DBObject dbObj = new DBObject(rset.getString("TABLE_TYPE"), //$NON-NLS-1$
						rset.getString("TABLE_NAME")); //$NON-NLS-1$
				structure.addObject(dbObj);
				try {
					rsetIdx = md.getIndexInfo(null, schemaPattern, dbObj.getName(), false, false);
					while (rsetIdx.next()) {
						structure.addObject(new DBObject(DBObject.TYPE_INDEX, rsetIdx
								.getString("INDEX_NAME"))); //$NON-NLS-1$
					}
				} catch (SQLException e) {
					// Unable to get the index, may we log anything?
				} finally {
					try {
						if (rsetIdx != null) {
							rsetIdx.close();
						}
					} catch (SQLException sqle) {
						// No logger so we do nothing
					}
				}
			}
			rset.close();
			// Fetching table columns
			rset = md.getColumns(null, schemaPattern, null, null);
			while (rset.next()) {
				final String tabName = rset.getString("TABLE_NAME"); //$NON-NLS-1$
				final String colName = rset.getString("COLUMN_NAME"); //$NON-NLS-1$
				structure.addObject(new DBObject("COLUMN", tabName + "." + colName)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// We return the database structure
			return structure;
		} finally {
			try {
				if (rset != null) {
					rset.close();
				}
			} catch (SQLException sqle) {
				// No logger so we do nothing
			}
			try {
				if (rsetIdx != null) {
					rsetIdx.close();
				}
			} catch (SQLException sqle) {
				// No logger so we do nothing
			}
		}
	}

}
