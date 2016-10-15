package com.nextep.designer.capture.services.impl;

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
import com.nextep.datadesigner.model.IDatabaseConnector;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.designer.capture.services.IDataCaptureService;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.core.services.IConnectionService;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.IDataLine;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class DataCaptureService implements IDataCaptureService {

	private final static int BUFFER_SIZE = 1000;
	private final static Log LOGGER = LogFactory.getLog(DataCaptureService.class);
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
			conn = dbConnector.connect();
			monitor.worked(10);
			for (IBasicTable t : tablesToCapture) {
				final IVersionable<IDataSet> dataset = fetchDataSet(conn, t, t.getColumns(),
						monitor.newChild(1000));
				// Checking for cancellation
				if (monitor.isCanceled()) {
					return datasets;
				}
				datasets.add(dataset);
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

	private IVersionable<IDataSet> fetchDataSet(Connection conn, IBasicTable t,
			List<IBasicColumn> columns, IProgressMonitor m) throws SQLException {
		SubMonitor monitor = SubMonitor.convert(m, "Capturing " + t.getName() + " data", 100);
		monitor.subTask("Capturing " + t.getName() + " data");
		final IVersionable<IDataSet> v = VersionableFactory.createVersionable(IDataSet.class);
		final IDataSet dataSet = v.getVersionnedObject().getModel();
		final Collection<IDataLine> datalineBuffer = new ArrayList<IDataLine>(BUFFER_SIZE);
		// Configuring dataset
		dataSet.setTable(t);
		for (IBasicColumn c : columns) {
			dataSet.addColumn(c);
		}
		dataSet.setName(t.getName());
		// Fetching data
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			final String dataSelect = buildDataSelect(t, columns);
			rset = stmt.executeQuery(dataSelect);
			final ResultSetMetaData md = rset.getMetaData();
			long counter = 0;
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
										+ e.getMessage() + "]", e);
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
					+ ": this table may need structure synchronization", e);
		} finally {
			if (rset != null) {
				rset.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		monitor.done();
		return v;
	}

	private String buildDataSelect(IBasicTable t, List<IBasicColumn> columns) {
		// Preparing column list, comma seperated
		final List<String> colNames = buildNameList(columns);
		String colNameList = colNames.toString();
		colNameList = colNameList.substring(1, colNameList.length() - 1);
		// Building SQL statement
		final StringBuilder buf = new StringBuilder(100);
		buf.append("select ");
		buf.append(colNameList + " from " + t.getName());
		// buf.append(" order by " + colNameList);
		return buf.toString();

	}

	private List<String> buildNameList(List<? extends INamedObject> objects) {
		final List<String> names = new ArrayList<String>(objects.size());
		for (INamedObject namedObj : objects) {
			names.add(namedObj.getName());
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
