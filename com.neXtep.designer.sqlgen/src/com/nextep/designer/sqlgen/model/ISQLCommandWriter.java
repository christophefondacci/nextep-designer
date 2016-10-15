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
package com.nextep.designer.sqlgen.model;

/**
 * @author Bruno Gautier
 */
public interface ISQLCommandWriter {

	/**
	 * Returns a SQL command to prompt the specified message.
	 * 
	 * @param message the message to prompt to the user
	 * @return a <code>String</code> representing the SQL command to prompt the specified message
	 */
	String promptMessage(String message);

	/**
	 * Returns a SQL command to put a comment with the specified comment.
	 * 
	 * @param comment the comment to put in the script
	 * @return a <code>String</code> representing the SQL command to put a comment
	 */
	String comment(String comment);

	/**
	 * Returns a SQL command to exit the SQL script.
	 * 
	 * @return a <code>String</code> representing the SQL command to exit the SQL script
	 */
	String exit();

	/**
	 * Returns a SQL command to close a SQL statement.
	 * 
	 * @return a <code>String</code> representing the SQL command to close a SQL statement
	 */
	String closeStatement();

	/**
	 * Returns an escaped column name.
	 * 
	 * @param columnName the name of the column to escape
	 * @return a <code>String</code> representing the specified column name escaped with appropriate
	 *         escape character
	 */
	String escapeDbObjectName(String columnName);

}
