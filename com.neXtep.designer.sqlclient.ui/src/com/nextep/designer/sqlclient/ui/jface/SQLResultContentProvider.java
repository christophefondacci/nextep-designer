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
package com.nextep.designer.sqlclient.ui.jface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;
import com.nextep.designer.sqlclient.ui.model.INextepMetadata;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLQueryListener;
import com.nextep.designer.sqlclient.ui.model.ISQLResult;
import com.nextep.designer.sqlclient.ui.model.ISQLResultListener;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;

public class SQLResultContentProvider implements ILazyContentProvider, ISQLResultListener {

	private final static Log LOGGER = LogFactory.getLog(SQLResultContentProvider.class);
	private ISQLResult result;
	private TableViewer viewer;
	private List<ISQLRowResult> pendingRows;
	private List<ISQLRowResult> sortedRows;

	/**
	 * This job is in charge of refreshing the UI sql results table while query is running.
	 */
	private class UpdateResultsTableJob extends UIJob {

		public UpdateResultsTableJob() {
			super("Refreshing SQL results table contents");
			setSystem(true);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			boolean isRunning = false;
			if (result != null && result.getSQLQuery() != null) {
				isRunning = result.getSQLQuery().isRunning();
			}
			updatePendingRows();
			if (isRunning) {
				schedule(500);
			}
			return Status.OK_STATUS;
		}
	}

	/**
	 * This listener handles the refresh of a specific row of the viewer after a query is finished.
	 * It is meant to handle the situation when last row was not yet available and required a new
	 * query fetch to get it.
	 */
	private class RefreshItemQueryListener implements ISQLQueryListener {

		private int index;

		public RefreshItemQueryListener(int index) {
			this.index = index;
		}

		@Override
		public void queryStarted(ISQLQuery query) {
		}

		@Override
		public void queryResultMetadataAvailable(ISQLQuery query, long executionTime,
				INextepMetadata md) {
		}

		@Override
		public void queryFinished(final ISQLQuery query, long execTime, long fetchTime,
				int resultCount, boolean isResultSet) {
			if (resultCount >= index) {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						viewer.replace(query.getResult().getRows().get(index), index);
					}
				});
			}
			query.removeQueryListener(this);
		}

		@Override
		public void queryFailed(ISQLQuery query, Exception e) {
		}
	}

	@Override
	public void dispose() {
		if (result != null) {
			result.removeListener(this);
		}
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Removing previous listener
		if (result != null) {
			result.removeListener(this);
		}
		// Registering new input
		if (newInput instanceof ISQLResult) {
			this.result = (ISQLResult) newInput;
			this.result.addListener(this);
			this.viewer = (TableViewer) viewer;
			pendingRows = new LinkedList<ISQLRowResult>();
			sortedRows = new ArrayList<ISQLRowResult>();
			// Scheduling UI update job
			Job updateUIJob = new UpdateResultsTableJob();
			updateUIJob.schedule();
		} else {
			this.result = null;
		}
	}

	// @Override
	// public Object[] getElements(Object inputElement) {
	// if (result != null) {
	// return result.getRows().toArray();
	// } else {
	// return null;
	// }
	// }

	@Override
	public void rowsAdded(ISQLResult result, ISQLRowResult... rows) {
		final ISQLQuery query = result.getSQLQuery();
		synchronized (pendingRows) {
			for (ISQLRowResult row : rows) {
				pendingRows.add(row);
			}
		}
		if (!query.isRunning()) {
			updatePendingRows();
		}
	}

	protected synchronized void updatePendingRows() {
		List<ISQLRowResult> toProcess = null;
		// Synchronized switch of buffered rows
		synchronized (pendingRows) {
			toProcess = pendingRows;
			pendingRows = new LinkedList<ISQLRowResult>();
		}
		// Now processing safely
		synchronized (sortedRows) {
			sortedRows.addAll(toProcess);
		}
		// viewer.add(toProcess.toArray());
		if (result != null) {
			final ISQLQuery query = result.getSQLQuery();
			int rows = result.getRows().size();
			if (query.hasMoreRows() && !query.isRunning()) {
				rows++;
			}
			viewer.setItemCount(rows);
		} else {
			viewer.setItemCount(0);
		}
	}

	@Override
	public void updateElement(final int index) {
		if (result != null) {
			try {
				final ISQLRowResult row = sortedRows.get(index);
				viewer.replace(row, index);
			} catch (IndexOutOfBoundsException e) {
				final ISQLQuery query = result.getSQLQuery();
				if (query.hasMoreRows()) {
					// Adding a listener which will update the requested row when available
					query.addQueryListener(new RefreshItemQueryListener(index));
					// Fetching next rows
					query.fetchNextRows();
					Job updateUIJob = new UpdateResultsTableJob();
					updateUIJob.schedule();
				} else {
					LOGGER.error("SQL results viewer requested a non existing element: " + index);
				}
			}
		}
	}

	public void sortRows(Comparator<ISQLRowResult> comparator) {
		synchronized (sortedRows) {
			Collections.sort(sortedRows, comparator);
		}
	}

	@Override
	public void rowsRemoved(ISQLResult result, ISQLRowResult... rows) {
		synchronized (sortedRows) {
			for (ISQLRowResult row : rows) {
				sortedRows.remove(row);
			}
		}
		viewer.setItemCount(sortedRows.size());
		viewer.refresh();
	}
}
