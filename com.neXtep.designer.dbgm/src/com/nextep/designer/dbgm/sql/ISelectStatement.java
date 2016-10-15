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
package com.nextep.designer.dbgm.sql;

import java.util.List;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.model.IObservable;

/**
 * The interface represents a select statement. It provides methods for a 2-way SQL manipulation:<br>
 * - Setting the SQL with the get / set SQL methods to parse the SQL will let the user extract
 * statement information.<br>
 * <br>
 * - Manipulating the statement by adding / removing elements with the API provided by this
 * interface and retrieve a SQL representation of the statement with the <code>getSQL()</code>
 * method. <br>
 * <br>
 * This interface should not be implemented directly. Clients should use the AbstractSelectStatement
 * as a base for extension.
 * 
 * @author Christophe Fondacci
 */
public interface ISelectStatement extends IObservable, IParseable {

	public static final String TYPE_ID = "SELECT"; //$NON-NLS-1$

	/**
	 * Defines the underlying SQL code of the statement
	 * 
	 * @param sql string representation of the SQL statement
	 */
	public void setSQL(String sql);

	/**
	 * @return the tables appearing in the FROM clause of the SQL statement.
	 */
	public List<TableAlias> getFromTables();

	/**
	 * Adds a table alias to the from table list
	 * 
	 * @param t table alias to add
	 */
	public void addFromTable(TableAlias t);

	/**
	 * Removes a table alias from the FROM table list
	 * 
	 * @param t table alias to remove
	 */
	public void removeFromTable(String tableName, String tableAlias);

	/**
	 * @return the list of selected column aliases
	 */
	public List<ColumnAlias> getSelectedColumns();

	/**
	 * Adds a column to the selected columns
	 * 
	 * @param c column alias to add to the selection
	 */
	public void addSelectedColumn(ColumnAlias c);

	/**
	 * Removes a column alias from the selected columns
	 * 
	 * @param c column alias to remove from the selection
	 */
	public void removeSelectedColumn(String columnName, String columnAlias);

	public void setPostFromFragment(String postFrom);

	public String getPostFromFragment();
}
