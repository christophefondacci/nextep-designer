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
package com.nextep.designer.sqlclient.ui.services;

import java.sql.Connection;
import java.sql.SQLException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLQueryListener;
import com.nextep.designer.sqlclient.ui.model.ISQLRowModificationStatus;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;

public interface ISQLClientService {

	/**
	 * Notifies that the specified SQL view is disposed and should no longer be eligible to receive
	 * any new SQL query.
	 * 
	 * @param part the SQL results part which is disposed
	 */
	void viewDisposed(IWorkbenchPart part);

	/**
	 * Runs a new SQL query coming from the specified editor input. The input is used to determine
	 * which SQL result view should be used to display query results. Any SQL results view is
	 * specific to a SQL client editor.
	 * 
	 * @param input the editor input from which the query has been run
	 * @param conn the SQL connection to use
	 * @param sql the SQL query to execute
	 */
	void runQuery(ISQLEditorInput<?> input, Connection conn, String sql);

	/**
	 * Runs the specified SQL query against the given connection. An optional list of query
	 * listeners could be specified so they will be notified of query execution.
	 * 
	 * @param conn the JDBC sql connection
	 * @param sql the SQL statement to execute
	 * @param listeners any {@link ISQLQueryListener} which will be notified of query execution
	 */
	void runQuery(Connection conn, String sql, ISQLQueryListener... listeners);

	/**
	 * Runs the specified SQL query against the given connection. An optional list of query
	 * listeners could be specified so they will be notified of query execution. Only the first
	 * <code>displayedColumnsCount</code> columns will be shown in the resulting SQL table, any
	 * exceeding column will be treated as metadata.
	 * 
	 * @param conn the JDBC sql connection
	 * @param sql the SQL statement to execute
	 * @param displayedColumnsCount number of columns to display
	 * @param listeners any {@link ISQLQueryListener} which will be notified of query execution
	 */
	void runQuery(Connection conn, String sql, int displayedColumnsCount,
			ISQLQueryListener... listeners);

	/**
	 * Opens the SQL client editor.
	 * 
	 * @param conn the {@link IConnection} which should be opened for this client editor.
	 */
	void openSqlClientEditor(IConnection conn);

	/**
	 * Opens a new SQL client editor on the specified connection with a prepared SQL query that will
	 * be initialized in the editor's pane and which will be executed immediatly after the editor
	 * has opened.
	 * 
	 * @param conn the {@link IConnection} which should be opened for this client editor.
	 * @param query the query to pre-initialize and run
	 */
	void openSqlClientEditor(IConnection conn, String query);

	/**
	 * Updates a column value for the specified row of the given query.
	 * 
	 * @param query the {@link ISQLQuery} on which the user performs an update
	 * @param row the row which is being edited
	 * @param valueIndex the index of the column which has been modified
	 * @param newValue new value for this column
	 * @return a {@link ISQLRowModificationStatus} containing information about what has been done
	 */
	ISQLRowModificationStatus updateQueryValue(ISQLQuery query, ISQLRowResult row,
			int modifiedColumnIndex, Object newValue) throws SQLException;

	/**
	 * Deletes the specified row.
	 * 
	 * @param query the {@link ISQLQuery} on which the user performs the deletion
	 * @param row the row to delete
	 * @throws SQLException whenever a database problem prevented the deletion
	 */
	void deleteQueryValue(ISQLQuery query, ISQLRowResult row) throws SQLException;

	/**
	 * Retrieves the workbench part which is associated with the specified editor input. When the
	 * input has multiple SQL results view attached, the last recently used will be returned. If the
	 * avoidPinned flag is set, then this method will return the last view used which is not pinned.
	 * 
	 * @param input the {@link IEditorInput} to retrieve the SQL results pane for
	 * @param avoidPinned whether or not we should return pinned views.
	 * @return the workbench part
	 */
	IWorkbenchPart getSQLResultViewFor(IEditorInput input, boolean avoidPinned);

}
