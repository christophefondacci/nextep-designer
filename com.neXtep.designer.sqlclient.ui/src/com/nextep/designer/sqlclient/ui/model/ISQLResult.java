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
package com.nextep.designer.sqlclient.ui.model;

import java.util.List;

/**
 * This interface represents the result of a SQL query. A result is available as soon as a query is
 * created and gets populated while the query is executed, allowing the executor to process data
 * while displaying them at the same time.
 * 
 * @author Christophe Fondacci
 */
public interface ISQLResult {

	/**
	 * Retrieves all rows that have already been fetched. Note that this method may return a
	 * non-empty even if all rows have not been fetched.
	 * 
	 * @return
	 */
	List<ISQLRowResult> getRows();

	/**
	 * Adds a row to this result
	 * 
	 * @param row the {@link ISQLRowResult} to add
	 */
	void addRow(ISQLRowResult row);

	/**
	 * Removes a row from this result
	 * 
	 * @param row the {@link ISQLRowResult} to remove
	 */
	void removeRow(ISQLRowResult row);

	/**
	 * Adds a listener to this result which will be notified about row additions
	 * 
	 * @param listener the {@link ISQLResultListener} to register
	 */
	void addListener(ISQLResultListener listener);

	/**
	 * Removes a listener from this result.
	 * 
	 * @param listener the {@link ISQLResultListener} to unregister
	 */
	void removeListener(ISQLResultListener listener);

	/**
	 * Retrieves the query for which this result has been created
	 * 
	 * @return the {@link ISQLQuery} which spawned this result
	 */
	ISQLQuery getSQLQuery();
}
