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
package com.nextep.designer.sqlclient.ui.model.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import com.nextep.designer.sqlclient.ui.helpers.SQLHelper;
import com.nextep.designer.sqlclient.ui.model.INextepMetadata;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLQueryListener;
import com.nextep.designer.sqlclient.ui.model.ISQLResult;

public class SQLQuery implements ISQLQuery {

	private final static Log LOGGER = LogFactory.getLog(SQLQuery.class);

	/** Maximum number of rows to fetch per execution of the query */
	private final static int FETCH_SIZE = 10000;

	private String sqlQuery;
	private ISQLResult result;
	private Connection connection;
	private List<ISQLQueryListener> listeners;
	private boolean isRunning = false;
	private Statement stmt = null;
	private ResultSet rset = null;
	private INextepMetadata md;
	private int rowCount = 0;
	private boolean isDisposed;
	private int displayedColumnsCount = -1;

	public SQLQuery(Connection connection, String sql) {
		this.sqlQuery = sql;
		result = new SQLResult(this);
		this.connection = connection;
		listeners = Collections.synchronizedList(new ArrayList<ISQLQueryListener>());
		isDisposed = false;
	}

	@Override
	public ISQLResult getResult() {
		return result;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		isRunning = true;
		notifyQueryStarted();
		boolean isResultSet = false;
		try {
			stmt = connection.createStatement();
			final long startTime = System.currentTimeMillis();
			isResultSet = stmt.execute(sqlQuery);
			final long afterExecutionTime = System.currentTimeMillis();
			final long execTime = afterExecutionTime - startTime;
			rowCount = Math.max(0, stmt.getUpdateCount());
			if (isResultSet) {
				rset = stmt.getResultSet();
				// Retrieving metadata
				md = SQLHelper.createOfflineMetadata(rset.getMetaData(), sqlQuery);
				notifyStructureReady(execTime, md);
				fetchRows(monitor);
			}
			final long endTime = System.currentTimeMillis();
			notifyQueryFinished(execTime, endTime - startTime, rowCount, isResultSet);
		} catch (SQLException e) {
			notifyQueryFailed(e);
		} finally {
			if (!isResultSet) {
				dispose();
			}
			isRunning = false;
		}
		return Status.OK_STATUS;
	}

	private synchronized IStatus fetchRows(IProgressMonitor monitor) {
		if (hasMoreRows()) {
			int localRowCount = 0;
			try {
				while (rset != null && rset.next()) {
					rowCount++;
					localRowCount++;
					// Handling cancellation while fetching SQL lines
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					// Preparing our row
					final SQLRowResult row = new SQLRowResult();
					for (int i = 1; i <= md.getColumnCount(); i++) {
						try {
							final Object o = rset.getObject(i);
							row.addValue(o);
							row.addSqlType(md.getColumnType(i));
						} catch (SQLException e) {
							row.addValue(null);
							LOGGER.error(
									"Cannot fetch value for column " + i + " : " + e.getMessage(),
									e);
						}
					}
					result.addRow(row);
					// We need to break the loop that way because each time rset.next() is called
					// one
					// row is fetched - see bug DES-644
					if (localRowCount >= FETCH_SIZE) {
						break;
					}
				}
			} catch (SQLException e) {
				notifyQueryFailed(e);
			} finally {
				if (localRowCount < FETCH_SIZE) {
					dispose();
				}
			}
		}
		return Status.OK_STATUS;
	}

	@Override
	public void fetchNextRows() {
		if (hasMoreRows() && !isRunning()) {
			Job j = new Job("Fetching more rows...") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					isRunning = true;
					// notifyQueryStarted();
					try {
						final long startTime = System.currentTimeMillis();
						IStatus status = fetchRows(monitor);
						final long time = System.currentTimeMillis() - startTime;
						notifyQueryFinished(0, time, rowCount, false);
						return status;
					} finally {
						isRunning = false;
					}
				}

			};

			j.schedule();
		}
	}

	@Override
	public void addQueryListener(ISQLQueryListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeQueryListener(ISQLQueryListener listener) {
		listeners.remove(listener);
	}

	private void notifyStructureReady(long execTime, INextepMetadata md) {
		for (ISQLQueryListener l : new ArrayList<ISQLQueryListener>(listeners)) {
			l.queryResultMetadataAvailable(this, execTime, md);
		}
	}

	private void notifyQueryFinished(long execTime, long totalTime, int resultCount,
			boolean isResultSet) {
		for (ISQLQueryListener l : new ArrayList<ISQLQueryListener>(listeners)) {
			l.queryFinished(this, execTime, totalTime, resultCount, isResultSet);
		}
	}

	private void notifyQueryStarted() {
		for (ISQLQueryListener l : new ArrayList<ISQLQueryListener>(listeners)) {
			l.queryStarted(this);
		}
	}

	private void notifyQueryFailed(Exception e) {
		for (ISQLQueryListener l : new ArrayList<ISQLQueryListener>(listeners)) {
			l.queryFailed(this, e);
		}
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public boolean hasMoreRows() {
		return rset != null;
	}

	@Override
	public void dispose() {
		if (rset != null) {
			try {
				rset.close();
			} catch (SQLException e) {
				LOGGER.error("Unable to close result set: " + e.getMessage(), e);
			} finally {
				rset = null;
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error("Unable to close statement: " + e.getMessage(), e);
			} finally {
				stmt = null;
			}
		}
		isDisposed = true;
	}

	@Override
	public String getSql() {
		return sqlQuery;
	}

	@Override
	public void setSql(String sql) {
		// Unsupported
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public INextepMetadata getMetadata() {
		return md;
	}

	@Override
	public int getDisplayedColumnsCount() {
		return displayedColumnsCount;
	}

	public void setDisplayedColumnsCount(int displayedColumnsCount) {
		this.displayedColumnsCount = displayedColumnsCount;
	}
}
