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

import java.sql.Connection;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import com.nextep.datadesigner.dbgm.model.ISqlBased;

/**
 * This interface defines a sql query.
 * 
 * @author Christophe Fondacci
 */
public interface ISQLQuery extends ISqlBased {

	/**
	 * Retrieves the interface to access query results.
	 * 
	 * @return the {@link ISQLResult} where all fetched data is pushed
	 */
	ISQLResult getResult();

	/**
	 * Executes this SQL query
	 * 
	 * @param monitor a monitor to report progress and check for cancellation
	 * @return a {@link IStatus} indicating how the execution finished
	 */
	IStatus run(IProgressMonitor monitor);

	void fetchNextRows();

	/**
	 * Adds a listener to this query. Listeners will generally be automatically added and removed by
	 * the sql executor service. You may register additional specific listeners directly on the
	 * query but you will then need to unregister them explicitly.
	 * 
	 * @param listener a {@link ISQLQueryListener} which will be notified of the query events
	 */
	void addQueryListener(ISQLQueryListener listener);

	/**
	 * Removes a listener from this query. Listeners will generally be automatically added and
	 * removed by the sql executor service. You may register additional specific listeners directly
	 * on the query but you will then need to unregister them explicitly.
	 * 
	 * @param listener a {@link ISQLQueryListener} to remove from events notification
	 */
	void removeQueryListener(ISQLQueryListener listener);

	/**
	 * Indicates whether this SQL query is currently running or if it has finished
	 * 
	 * @return <code>true</code> when query is currently executing, else <code>false</code>
	 */
	boolean isRunning();

	/**
	 * Indicates whether this query have more rows that have not yet been fetched. When this method
	 * returns <code>true</code>, another call to {@link ISQLQuery#run(IProgressMonitor)} will fetch
	 * more rows.
	 * 
	 * @return whether this query have more rows waiting to be fetched
	 */
	boolean hasMoreRows();

	/**
	 * Disposes any resource which might be in use by this query. Disposing an already disposed
	 * query have no effect.
	 */
	void dispose();

	/**
	 * Retrieves the connection with which this query has been executed. Note that the connection
	 * may has been closed.
	 * 
	 * @return the Connection used to execute this query or <code>null</code> if query is disposed
	 */
	Connection getConnection();

	INextepMetadata getMetadata();

	/**
	 * Retrieves the number of displayed columns count or -1 if all columns should be displayed.
	 * 
	 * @return the number of columns of results to display
	 */
	int getDisplayedColumnsCount();
}
