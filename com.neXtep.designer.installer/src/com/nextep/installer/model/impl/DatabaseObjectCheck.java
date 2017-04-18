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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.IDBObject;
import com.nextep.installer.model.IDatabaseObjectCheck;
import com.nextep.installer.model.IDatabaseStructure;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Christophe Fondacci
 */
public class DatabaseObjectCheck implements IDatabaseObjectCheck {

	/** The set of objects to check */
	private Set<IDBObject> objects = new HashSet<IDBObject>();
	/** Database schema */
	private List<IDBObject> missing = new ArrayList<IDBObject>();

	/**
	 * Adds an object to check in target database
	 * 
	 * @param obj database object to check
	 */
	public void addObject(IDBObject obj) {
		objects.add(obj);
	}

	public boolean check(IDatabaseStructure structure) {
		final Collection<IDBObject> tempObjects = new HashSet<IDBObject>(objects);
		if (!objects.isEmpty()) {
			Collection<IDBObject> checked = structure.getObjects();
			// Computing difference
			tempObjects.removeAll(checked);
			// Displaying if required

			for (IDBObject obj : tempObjects) {
				missing.add(obj);
			}
		}
		// Objects array size should be null if everything checked
		return (tempObjects.size() == 0);
	}

	public void install(Connection conn, String user, IRelease rel) {
		final ILoggingService logger = ServicesHelper.getLoggingService();

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement("INSERT INTO nadm_release_objects ( " //$NON-NLS-1$
					+ "  irel_id, object_type, object_name" //$NON-NLS-1$
					+ ") VALUES (" //$NON-NLS-1$
					+ "  ?, ?, ?" //$NON-NLS-1$
					+ ") "); //$NON-NLS-1$

			for (IDBObject dbObj : objects) {
				stmt.setLong(1, rel.getId());
				stmt.setString(2, dbObj.getType());
				stmt.setString(3, dbObj.getName());
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (SQLException e) {
			logger.error("Warning: Unable to add release validation info to admin database."); //$NON-NLS-1$
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * @return the list of missing objects
	 */
	public List<IDBObject> getMissingObjects() {
		return missing;
	}

	public Collection<IDBObject> getObjectsToCheck() {
		return objects;
	}

}
