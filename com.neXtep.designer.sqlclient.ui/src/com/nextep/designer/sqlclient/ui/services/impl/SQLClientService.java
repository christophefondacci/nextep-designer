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
package com.nextep.designer.sqlclient.ui.services.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.gui.editors.ISQLEditorInput;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.sqlclient.ui.helpers.ExportHelper;
import com.nextep.designer.sqlclient.ui.model.INextepMetadata;
import com.nextep.designer.sqlclient.ui.model.IPinnable;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLQueryListener;
import com.nextep.designer.sqlclient.ui.model.ISQLResult;
import com.nextep.designer.sqlclient.ui.model.ISQLRowModificationStatus;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;
import com.nextep.designer.sqlclient.ui.model.impl.SQLQuery;
import com.nextep.designer.sqlclient.ui.model.impl.SQLRowModificationStatus;
import com.nextep.designer.sqlclient.ui.rcp.SQLClientEditorInput;
import com.nextep.designer.sqlclient.ui.rcp.SQLFullClientEditor;
import com.nextep.designer.sqlclient.ui.rcp.SQLResultsView;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class SQLClientService implements ISQLClientService {

	private final static Log LOGGER = LogFactory.getLog(SQLClientService.class);

	private final static boolean AUTOCOMMIT_DEFAULT = false;
	private Map<IEditorInput, List<IViewPart>> editorsViewsMap;
	private final static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$
	private static int secondaryId = 1;

	public SQLClientService() {
		editorsViewsMap = new HashMap<IEditorInput, List<IViewPart>>();
	}

	/**
	 * A cleanup listeners whose task is to unregister automatically added query listeners when the
	 * query is finished
	 */
	private class QueryFinishedListener implements ISQLQueryListener {

		private Collection<ISQLQueryListener> autoRegisteredListeners;

		public QueryFinishedListener(Collection<ISQLQueryListener> listeners) {
			autoRegisteredListeners = listeners;
		}

		@Override
		public void queryFinished(ISQLQuery query, long execTime, long fetchTime, int resultCount,
				boolean isResultSet) {
			if (!query.hasMoreRows()) {
				unregisterListeners(query);
			}
		}

		private void unregisterListeners(ISQLQuery query) {
			for (ISQLQueryListener l : autoRegisteredListeners) {
				query.removeQueryListener(l);
			}
		}

		@Override
		public void queryResultMetadataAvailable(ISQLQuery query, long executionTime,
				INextepMetadata md) {
		}

		@Override
		public void queryStarted(ISQLQuery query) {
			ExportHelper.initialize();
		}

		@Override
		public void queryFailed(ISQLQuery query, Exception e) {
			unregisterListeners(query);
		}
	}

	@Override
	public void runQuery(ISQLEditorInput<?> input, Connection conn, String sql) {
		// Automatically registering query listeners on our new query
		IWorkbenchPart sqlView = getSQLResultViewFor(input, true);
		IViewSite site = (IViewSite) sqlView.getSite();
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(site.getId(), site.getSecondaryId(), IWorkbenchPage.VIEW_VISIBLE);
		} catch (PartInitException e) {
			throw new ErrorException("Unable to show SQL result view: " + e.getMessage(), e);
		}
		// Resetting editable state
		if (sqlView instanceof SQLResultsView) {
			((SQLResultsView) sqlView).setEditableState(false);
		}
		// Unwrapping SQL query listener
		ISQLQueryListener listener = (ISQLQueryListener) sqlView
				.getAdapter(ISQLQueryListener.class);
		// Running query
		runQuery(conn, sql, listener);
	}

	@Override
	public void runQuery(Connection conn, String sql, ISQLQueryListener... listeners) {
		runQuery(conn, sql, -1, listeners);
	}

	@Override
	public void runQuery(Connection conn, String sql, int displayedColumnsCount,
			ISQLQueryListener... listeners) {
		final SQLQuery query = new SQLQuery(conn, sql);
		query.setDisplayedColumnsCount(displayedColumnsCount);
		// Registering listeners on query
		for (ISQLQueryListener l : listeners) {
			query.addQueryListener(l);
		}
		// Adding the cleanup listener
		query.addQueryListener(new QueryFinishedListener(Arrays.asList(listeners)));
		// Background query execution job
		Job j = new Job("Executing query") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				query.run(monitor);
				return Status.OK_STATUS;
			}
		};
		j.schedule();
	}

	@Override
	public ISQLRowModificationStatus updateQueryValue(ISQLQuery query, ISQLRowResult row,
			int modifiedColumnIndex, Object newValue) throws SQLException {
		final Connection conn = query.getConnection();
		final DatabaseMetaData md = conn.getMetaData();
		// For pending rows, we set the new column value
		if (row.isPending()) {
			row.setValue(modifiedColumnIndex, newValue);
		}
		final ISQLRowModificationStatus status = computeRowModificationStatus(query, row,
				modifiedColumnIndex);
		// Now processing database update if we can
		if (status.isModifiable()) {
			// Now we can build our query
			String sqlStatement = null;
			final boolean insertNeeded = status.isInsertNeeded();
			if (insertNeeded) {
				sqlStatement = buildInsertStatement(status, md.getIdentifierQuoteString());
			} else {
				sqlStatement = buildUpdateStatement(status, md.getIdentifierQuoteString());
			}
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(sqlStatement);
				fillPreparedStatement(stmt, !insertNeeded, insertNeeded, row, newValue);
				stmt.execute();
				// Everything was OK, we can unflag any pending row
				if (row.isPending()) {
					row.setPending(false);
				}
			} finally {
				if (stmt != null) {
					try {
						stmt.close();
					} catch (SQLException e) {
						LOGGER.error("Unable to close SQL update statement: " + e.getMessage(), e);
					}
				}
			}
		}
		return status;
	}

	private void fillPreparedStatement(PreparedStatement stmt, boolean isUpdate,
			boolean fillNullValues, ISQLRowResult row, Object newValue) throws SQLException {
		int i = 1;
		// For update statement, we set new value as first argument
		if (isUpdate) {
			if (newValue != null) {
				stmt.setObject(i++, newValue);
			} else {
				stmt.setNull(i++, getSqlTypeFor(newValue));
			}
		}
		// Passing all values
		final List<Object> values = row.getValues();
		final List<Integer> columnTypes = row.getSqlTypes();
		for (int rowIndex = 0; rowIndex < row.getValues().size(); rowIndex++) {
			Object o = values.get(rowIndex);
			if (o != null) {
				stmt.setObject(i++, o);
			} else {
				// If insert needed we explicitly set null values, otherwise it will be
				// written as "is null" in the statement
				if (fillNullValues) {
					stmt.setNull(i++, columnTypes.get(rowIndex));
				}
			}
		}
	}

	/**
	 * Builds a SQL UPDATE statement which can update one column value of the table.
	 * 
	 * @param status the {@link ISQLRowModificationStatus} previously computed by calling
	 *        {@link ISQLClientService#computeRowModificationStatus(ISQLQuery, ISQLRowResult, int)}
	 * @return the SQL UPDATE statement string
	 */
	private String buildUpdateStatement(ISQLRowModificationStatus status,
			String identifierQuoteString) {
		final StringBuilder buf = new StringBuilder(150);
		final String tableName = status.getUpdatedTableName();
		final String updatedColumnName = status.getUpdatedColumnName();
		buf.append("UPDATE ").append(tableName).append(" SET "); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(identifierQuoteString).append(updatedColumnName).append(identifierQuoteString)
				.append("=?"); //$NON-NLS-1$
		buf.append(buildWhereStatement(status, identifierQuoteString));
		return buf.toString();
	}

	/**
	 * Builds a where statement for use with JDBC prepared statements that restricts all column from
	 * the row.
	 * 
	 * @param status the pre-computed {@link ISQLRowModificationStatus} of the current query
	 * @param identifierQuoteString the string to use when escaping column names
	 * @return the "WHERE ...=... and ..." string
	 */
	private String buildWhereStatement(ISQLRowModificationStatus status,
			String identifierQuoteString) {
		final ISQLRowResult row = status.getSQLRow();
		final List<String> columnNames = status.getRowColumnNames();
		final StringBuilder buf = new StringBuilder(100);
		buf.append(" WHERE "); //$NON-NLS-1$
		String separator = ""; //$NON-NLS-1$
		int i = 0;
		for (String columnName : columnNames) {
			// We only need to retrieve the value to build a proper "is null" clause
			final Object value = row.getValues().get(i++);

			buf.append(separator).append(identifierQuoteString).append(columnName)
					.append(identifierQuoteString).append(value != null ? "=? " : " IS NULL "); //$NON-NLS-1$ //$NON-NLS-2$
			separator = "AND "; //$NON-NLS-1$
		}
		return buf.toString();
	}

	private String buildDeleteStatement(ISQLRowModificationStatus status,
			String identifierQuoteString) {
		final StringBuilder buf = new StringBuilder(150);
		final String tableName = status.getUpdatedTableName();
		buf.append("DELETE FROM ").append(tableName); //$NON-NLS-1$
		buf.append(buildWhereStatement(status, identifierQuoteString));
		return buf.toString();
	}

	/**
	 * Builds a SQL INSERT statement which can insert all row values into the table.
	 * 
	 * @param status the {@link ISQLRowModificationStatus} previously computed by calling
	 *        {@link ISQLClientService#computeRowModificationStatus(ISQLQuery, ISQLRowResult, int)}
	 * @return the SQL INSERT statement string
	 */
	private String buildInsertStatement(ISQLRowModificationStatus status,
			String identifierQuoteString) {
		final StringBuilder buf = new StringBuilder(150);
		final StringBuilder argBuf = new StringBuilder();
		final String tableName = status.getUpdatedTableName();
		final List<String> columnNames = status.getRowColumnNames();

		buf.append("INSERT INTO ").append(tableName).append(" ("); //$NON-NLS-1$ //$NON-NLS-2$
		String separator = ""; //$NON-NLS-1$
		for (String columnName : columnNames) {
			buf.append(separator).append(identifierQuoteString).append(columnName)
					.append(identifierQuoteString);
			argBuf.append(separator).append('?');
			separator = ","; //$NON-NLS-1$
		}
		buf.append(") VALUES (").append(argBuf).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		return buf.toString();
	}

	private ISQLRowModificationStatus computeRowModificationStatus(ISQLQuery query,
			ISQLRowResult row, int modifiedColumnIndex) {
		final INextepMetadata md = query.getMetadata();
		final Connection conn = query.getConnection();
		final SQLRowModificationStatus status = new SQLRowModificationStatus(row);
		if (md != null && conn != null) {
			Collection<String> selectedTableNames = new HashSet<String>();
			for (int i = 1; i <= md.getColumnCount(); i++) {
				selectedTableNames.add(md.getTableName(i));
				// Updating modification status with column name information
				final String columnName = md.getColumnName(i);
				status.addRowColumnName(columnName);
				if (i == (modifiedColumnIndex + 1)) {
					// Setting our modified column name
					status.setUpdatedColumnName(columnName);
				}
			}
			// Only supporting single table select
			if (selectedTableNames.size() != 1) {
				status.setMessage("Table data modification is only allowed on single-table SELECT.");
				return status;
			}
			// Checking that our query contains table primary key columns
			final String tableName = selectedTableNames.iterator().next();
			// Setting table in our modification status
			status.setUpdatedTableName(tableName);

			List<IBasicColumn> pkColumns = null;
			IBasicTable table = null;
			try {
				IColumnable columnable = null;
				columnable = (IColumnable) CorePlugin.getService(IReferenceManager.class)
						.findByTypeName(IElementType.getInstance(IBasicTable.TYPE_ID), tableName,
								true);
				// Only supporting table updates
				if (!(columnable instanceof IBasicTable)) {
					status.setMessage("Data modification is only allowed on tables.");
					return status;
				}
				// We got a table
				table = (IBasicTable) columnable;
				// Retrieving primary key columns. For table without PK, it will be all columns
				final UniqueKeyConstraint pk = DBGMHelper.getPrimaryKey(columnable);
				if (pk != null) {
					pkColumns = pk.getColumns();
				} else if (pkColumns == null) {
					pkColumns = table.getColumns();
				}
				// Checking that all not-null columns are filled
				if (row.isPending()) {
					boolean insertGranted = isGrantedForInsert(status, row, table);
					if (insertGranted) {
						status.setModifiable(true);
					}
				} else {
					// IF row is not pending, it is modifiable
					status.setModifiable(true);
				}
			} catch (ReferenceNotFoundException e1) {
				status.setMessage("Table definition is not found in current workspace, cannot modify data on unknown tables.");
				return status;
			}
			// Checking that our query contains all columns
			if (!checkColumnsCoverPK(status.getRowColumnNames(), pkColumns)) {
				status.setMessage("Data modification can only be performed when primary key columns are selected or on 'SELECT *' statements");
				return status;
			}
		}
		return status;
	}

	private boolean isGrantedForInsert(ISQLRowModificationStatus status, ISQLRowResult row,
			IBasicTable table) {
		final List<String> selectedColumnNames = new ArrayList<String>();
		// Upper-casing column names as JDBC metadata may alter case on column names
		for (String columnName : status.getRowColumnNames()) {
			selectedColumnNames.add(columnName.toUpperCase());
		}
		// Checking whether NOT NULL columns have correct not null row values
		for (IBasicColumn column : table.getColumns()) {
			if (column.isNotNull()) {
				int columnIndex = selectedColumnNames.indexOf(column.getName().toUpperCase());
				if (columnIndex >= 0) {
					final Object columnValue = row.getValues().get(columnIndex);
					// If value is null we stop, as the row is not yet granted
					if (columnValue == null) {
						return false;
					}
				}
			}
		}
		// Every column value passed the NOT NULL check
		return true;
	}

	private boolean checkColumnsCoverPK(List<String> columnNames, List<IBasicColumn> pkColumns) {
		List<String> pkColNames = NameHelper.buildNameList(pkColumns);
		return columnNames.containsAll(pkColNames);
	}

	@Override
	public void openSqlClientEditor(IConnection conn) {
		final ISQLScript queryScript = buildQueryScript();
		openSqlClientEditor(conn, queryScript);
	}

	/**
	 * Opens a SQL client editor on the specified connection using the provided sql script. This
	 * internal method returns the newly initialized connection that this editor uses so that
	 * callers can run queries with it.
	 * 
	 * @param conn the descriptor of the connection on which the client should be initialized
	 * @param queryScript the {@link ISQLScript} that the client editor will use
	 * @return the editor input
	 */
	private SQLClientEditorInput openSqlClientEditor(IConnection conn, ISQLScript queryScript) {
		// Initializing editor input
		final SQLClientEditorInput input = new SQLClientEditorInput(queryScript, conn);
		// Initiliazing connection
		IDatabaseConnector dbConnector = CorePlugin.getConnectionService().getDatabaseConnector(
				conn);
		try {
			Connection sqlConnection = dbConnector.connect(conn);
			sqlConnection.setAutoCommit(AUTOCOMMIT_DEFAULT);
			input.setSqlConnection(sqlConnection);
		} catch (SQLException e) {
			throw new ErrorException("Could not establish connection : " + e.getMessage(), e);
		}
		// Initializing result view
		final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage();
		try {
			page.openEditor(input, SQLFullClientEditor.EDITOR_ID);
			final IViewPart part = getSQLResultViewFor(input, false);
			final IViewSite site = part.getViewSite();
			page.showView(site.getId(), site.getSecondaryId(), IWorkbenchPage.VIEW_VISIBLE);
		} catch (PartInitException e) {
			throw new ErrorException("Unable to open SQL client: " + e.getMessage(), e);
		}
		return input;
	}

	/**
	 * Builds a new empty SQL script for the SQL client editor
	 * 
	 * @return a new empty {@link ISQLScript} to use with the SQL client editor
	 */
	private ISQLScript buildQueryScript() {
		// Initializing a SQL script for the new client
		final ISQLScript queryScript = new SQLScript(ScriptType.CUSTOM);
		queryScript.setExternal(true);
		queryScript.setDirectory(SQLGenUtil.getPreference(PreferenceConstants.TEMP_FOLDER));
		queryScript.setName("sqlquery_" + dateFormat.format(new Date())); //$NON-NLS-1$
		return queryScript;
	}

	@Override
	public void openSqlClientEditor(IConnection conn, String query) {
		final ISQLScript queryScript = buildQueryScript();
		queryScript.setSql(query);
		final SQLClientEditorInput input = openSqlClientEditor(conn, queryScript);
		runQuery(input, input.getSqlConnection(), query);
	}

	@Override
	public IViewPart getSQLResultViewFor(IEditorInput input, boolean avoidPinned) {
		List<IViewPart> sqlViews = editorsViewsMap.get(input);
		if (sqlViews == null) {
			sqlViews = new LinkedList<IViewPart>();
			editorsViewsMap.put(input, sqlViews);
		}
		// Trying to get an existing SQL result part
		for (IViewPart sqlView : sqlViews) {
			if (avoidPinned) {
				final IPinnable pinnable = (IPinnable) sqlView.getAdapter(IPinnable.class);
				if (pinnable != null && !pinnable.isPinned()) {
					return sqlView;
				}
			} else {
				return sqlView;
			}
		}
		// We fall here when no view matches or no view exists
		// Anyway we need to instantiate a new view
		try {
			IViewPart part = PlatformUI
					.getWorkbench()
					.getActiveWorkbenchWindow()
					.getActivePage()
					.showView(SQLResultsView.VIEW_ID, String.valueOf(secondaryId++),
							IWorkbenchPage.VIEW_VISIBLE);
			sqlViews.add(0, part);
			return part;
		} catch (PartInitException e) {
			throw new ErrorException("Unable to create a new SQL results view: " + e.getMessage(),
					e);
		}
	}

	@Override
	public void viewDisposed(IWorkbenchPart part) {
		// Browsing every registered client / view to dispose
		for (IEditorInput input : editorsViewsMap.keySet()) {
			final List<IViewPart> registeredParts = editorsViewsMap.get(input);
			if (registeredParts != null) {
				registeredParts.remove(part);
			}
		}
	}

	private int getSqlTypeFor(Object o) {
		if (o instanceof String) {
			return Types.VARCHAR;
		} else if (o instanceof Date) {
			return Types.DATE;
		} else if (o instanceof Integer) {
			return Types.INTEGER;
		} else if (o instanceof Double) {
			return Types.DOUBLE;
		} else if (o instanceof Float) {
			return Types.FLOAT;
		} else if (o instanceof BigInteger) {
			return Types.BIGINT;
		} else if (o instanceof BigDecimal) {
			return Types.NUMERIC;
		} else {
			return Types.OTHER;
		}
	}

	@Override
	public void deleteQueryValue(ISQLQuery query, ISQLRowResult row) throws SQLException {
		ISQLRowModificationStatus status = computeRowModificationStatus(query, row, -1);
		if (status.isModifiable()) {
			final ISQLResult result = query.getResult();
			final Connection conn = query.getConnection();
			final DatabaseMetaData md = conn.getMetaData();
			final String deleteStmt = buildDeleteStatement(status, md.getIdentifierQuoteString());
			PreparedStatement stmt = null;
			try {
				stmt = conn.prepareStatement(deleteStmt);
				fillPreparedStatement(stmt, false, false, row, null);
				stmt.execute();
				if (stmt.getUpdateCount() > 0) {
					result.removeRow(row);
				}
			} finally {
				if (stmt != null) {
					stmt.close();
				}
			}

		}

	}
}
