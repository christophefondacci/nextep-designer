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
package com.nextep.installer.model;

import java.sql.Connection;

/**
 * Represents a check. The check is also a command whose execute method should call the check
 * method.
 * 
 * @author Christophe Fondacci
 */
public interface ICheck {

	/**
	 * Checks the condition against the given database structure
	 * 
	 * @param the database structure to check
	 * @return <code>true</code> to indicate the structure check is a success, else
	 *         <code>false</code>
	 */
	public boolean check(IDatabaseStructure structure);

	/**
	 * Installs this check in the admin database for future use by the installer. The check is
	 * installed for the specified user and release number.
	 * 
	 * @param conn a connection to the neXtep admin schema
	 * @param user username of the owner of the installed module
	 */
	public void install(Connection conn, String user, IRelease rel);
}
