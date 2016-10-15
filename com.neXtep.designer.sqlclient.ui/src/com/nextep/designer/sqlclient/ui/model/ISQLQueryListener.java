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


/**
 * The interface for listeners of {@link ISQLQuery}
 * 
 * @author Christophe Fondacci
 */
public interface ISQLQueryListener {

	/**
	 * Informs that the specified query has started. This method is called only the very first time
	 * the query is executed.
	 * 
	 * @param query the query which has started execution
	 */
	void queryStarted(ISQLQuery query);

	/**
	 * Informs that the query execution finished. There might be more rows to fetch depending on the
	 * result of {@link ISQLQuery#hasMoreRows()} method call.
	 * 
	 * @param query the {@link ISQLQuery} which finished execution
	 * @param execTime execution time
	 * @param totalTime total time (execution + fetch + processing)
	 * @param resultCount number of results fetched
	 * @param isResultSet informs whether the result is a set of result or not
	 */
	void queryFinished(ISQLQuery query, long execTime, long totalTime, int resultCount,
			boolean isResultSet);

	/**
	 * Informs that the specified query failed during its execution
	 * 
	 * @param query the {@link ISQLQuery} whose execution failed
	 * @param e raised exception
	 */
	void queryFailed(ISQLQuery query, Exception e);

	/**
	 * Informs that result metadata is available but no results have yet been fetched. Metadata
	 * provides the structure of the results that will come. This method will only be called for
	 * queries returning results.
	 * 
	 * @param query the {@link ISQLQuery} whose metadata are available
	 * @param executionTime exectuion time on the server side
	 * @param md metadata of the results
	 */
	void queryResultMetadataAvailable(ISQLQuery query, long executionTime, INextepMetadata md);
}
