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
package com.nextep.designer.synch.services.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.core.services.IConnectionService;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.IDataLine;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.ISQLCommandWriter;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.synch.services.IDataCaptureService;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 */
public class DataCaptureService implements IDataCaptureService {

	private final static Log LOGGER = LogFactory.getLog(DataCaptureService.class);
	private final static int BUFFER_SIZE = 1000;

	private IConnectionService connectionService;
	private ITypedObjectFactory typedObjectFactory;
	private IDataService dataService;

	@Override
	public Collection<IVersionable<IDataSet>> captureTablesData(IConnection c,
			Collection<IBasicTable> tablesToCapture, IProgressMonitor m) {
		final SubMonitor monitor = SubMonitor.convert(m, "Capturing table data",
				tablesToCapture.size() * 1000 + 10);
		monitor.subTask("Initializing database connection");
		final IDatabaseConnector dbConnector = connectionService.getDatabaseConnector(c);
		final Collection<IVersionable<IDataSet>> datasets = new ArrayList<IVersionable<IDataSet>>();
		Connection conn = null;
		try {
			conn = dbConnector.connect(c);
			monitor.worked(10);
			for (IBasicTable t : tablesToCapture) {
				try {
					final IVersionable<IDataSet> dataset = fetchDataSet(conn, c.getDBVendor(), t,
							t.getColumns(), monitor.newChild(1000));
					// Checking for cancellation
					if (monitor.isCanceled()) {
						return datasets;
					}
					if (dataset != null) {
						datasets.add(dataset);
					}
				} catch (SQLException e) {
					LOGGER.warn(
							"Unable to capture data of table "
									+ t.getName()
									+ ". This might be caused by structural de-synchronization, try to synchronize structure first. ("
									+ e.getMessage() + ")", e); //$NON-NLS-1$
				}
			}
		} catch (SQLException e) {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					LOGGER.error("Problems while closing database connection: " + ex.getMessage(),
							ex);
				}
			}
			LOGGER.error("Problems while fetching data: " + e.getMessage(), e);
		}
		return datasets;
	}

	private IVersionable<IDataSet> fetchDataSet(Connection conn, DBVendor vendor, IBasicTable t,
			List<IBasicColumn> columns, IProgressMonitor m) throws SQLException {
		final String taskName = "Capturing " + t.getName() + " data";
		SubMonitor monitor = SubMonitor.convert(m, taskName, 100);
		monitor.subTask(taskName);
		final IVersionable<IDataSet> v = VersionableFactory.createVersionable(IDataSet.class);
		final IDataSet dataSet = v.getVersionnedObject().getModel();
		final Collection<IDataLine> datalineBuffer = new ArrayList<IDataLine>(BUFFER_SIZE);
		// Configuring dataset
		dataSet.setTable(t);
		// Aligning captured data set with repository dataset name
		if (!t.getDataSets().isEmpty()) {
			// Taking first one
			final IDataSet set = t.getDataSets().iterator().next();
			// Captured data set will be named just like the repository dataset to force name synch
			dataSet.setName(set.getName());
			// Captured columns are restricted to defined data set columns only
			columns = set.getColumns();
		} else {
			dataSet.setName(t.getName());
		}
		for (IBasicColumn c : columns) {
			dataSet.addColumn(c);
		}
		// Fetching data
		Statement stmt = null;
		ResultSet rset = null;
		long counter = 0;
		try {
			stmt = conn.createStatement();
			final String dataSelect = buildDataSelect(vendor, t, columns);
			monitor.subTask(taskName + " - querying data");
			rset = stmt.executeQuery(dataSelect);
			final ResultSetMetaData md = rset.getMetaData();
			int bufferCount = 0;
			while (rset.next()) {
				// Handling cancellation
				if (monitor.isCanceled()) {
					return v;
				} else {
					if (counter++ % 100 == 0) {
						monitor.worked(100);
					}
				}
				// Preparing dataline
				final IDataLine line = typedObjectFactory.create(IDataLine.class);

				// Iterating over result set columns
				for (int i = 1; i <= md.getColumnCount(); i++) {
					// Fetching result set column value
					Object value = null;
					try {
						value = rset.getObject(i);
					} catch (SQLException e) {
						LOGGER.error(
								"Data import problem on " + t.getName() + " column " + i
										+ " of line " + counter
										+ " failed to fetch data, NULL will be used instead ["
										+ e.getMessage() + "]", e); //$NON-NLS-1$
					}
					// Preparing column value
					final IColumnValue colValue = typedObjectFactory.create(IColumnValue.class);
					colValue.setDataLine(line);
					colValue.setColumn(columns.get(i - 1));
					colValue.setValue(value);
					line.addColumnValue(colValue);
				}
				datalineBuffer.add(line);
				if (++bufferCount >= BUFFER_SIZE) {
					dataService.addDataline(dataSet,
							datalineBuffer.toArray(new IDataLine[datalineBuffer.size()]));
					datalineBuffer.clear();
					bufferCount = 0;
					monitor.subTask(taskName + " - " + counter + " lines fetched"); //$NON-NLS-1$
				}
			}
			// Flushing end of buffer
			if (!datalineBuffer.isEmpty()) {
				dataService.addDataline(dataSet,
						datalineBuffer.toArray(new IDataLine[datalineBuffer.size()]));
			}
			LOGGER.info("Captured " + counter + " data lines from " + t.getName());
		} catch (SQLException e) {
			LOGGER.error("Unable to fetch data from table " + t.getName()
					+ ": this table may need structure synchronization: " + e.getMessage(), e);
		} finally {
			if (rset != null) {
				rset.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		monitor.done();
		// Only returning dataset if at least one row was fetched
		return counter == 0 ? null : v;
	}

	private String buildDataSelect(DBVendor vendor, IBasicTable t, List<IBasicColumn> columns) {
		final ISQLCommandWriter writer = SQLGenPlugin.getService(IGenerationService.class)
				.getSQLCommandWriter(vendor);
		// Preparing column list, comma seperated
		final List<String> colNames = buildNameList(writer, columns);
		String colNameList = colNames.toString();
		colNameList = colNameList.substring(1, colNameList.length() - 1);
		// Building SQL statement
		final StringBuilder buf = new StringBuilder(100);
		buf.append("SELECT "); //$NON-NLS-1$
		buf.append(colNameList).append(" FROM ").append(writer.escapeDbObjectName(t.getName())); //$NON-NLS-1$
		// buf.append(" order by " + colNameList);
		return buf.toString();
	}

	private List<String> buildNameList(ISQLCommandWriter writer,
			List<? extends INamedObject> objects) {
		final List<String> names = new ArrayList<String>(objects.size());
		for (INamedObject namedObj : objects) {
			names.add(writer.escapeDbObjectName(namedObj.getName()));
		}
		return names;
	}

	public void setConnectionService(IConnectionService connectionService) {
		this.connectionService = connectionService;
	}

	public void setTypedObjectFactory(ITypedObjectFactory typedFactory) {
		this.typedObjectFactory = typedFactory;
	}

	public void setDataService(IDataService dataService) {
		this.dataService = dataService;
	}

}
