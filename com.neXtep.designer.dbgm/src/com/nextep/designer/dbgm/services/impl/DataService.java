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
package com.nextep.designer.dbgm.services.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.dbgm.model.DeltaType;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataLine;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.model.impl.DataDelta;
import com.nextep.designer.dbgm.model.impl.DataSet;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.dbgm.services.IStorageService;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class DataService implements IDataService {

	private final static Log LOGGER = LogFactory.getLog(DataService.class);
	private final static int LINE_BUFFER_SIZE = 1024;
	private ITypedObjectFactory typedObjectFactory;
	private IRepositoryService repositoryService;
	private IStorageService storageService;

	@Override
	public void addDataline(IDataSet set, IDataLine... lines) {
		Connection conn = null;
		PreparedStatement stmt = null;
		IStorageHandle handle = set.getStorageHandle();
		if (handle == null) {
			storageService.createDataSetStorage(set);
			handle = set.getStorageHandle();
		}
		try {
			conn = storageService.getLocalConnection();
			final String insertStmt = handle.getInsertStatement();
			stmt = conn.prepareStatement(insertStmt);
			for (IDataLine line : lines) {
				int col = 1;
				// For repository handles, we specify the row id
				// if (handle.isRepositoryHandle()) {
				if (line.getRowId() == 0) {
					stmt.setNull(col++, Types.BIGINT);
				} else {
					stmt.setLong(col++, line.getRowId());
				}
				// }
				// Processing line data
				for (IReference r : set.getColumnsRef()) {
					final IColumnValue value = line.getColumnValue(r);
					Object valueObj = null;
					if (value != null) {
						valueObj = value.getValue();
						if (valueObj != null) {
							stmt.setObject(col, valueObj);
						} else {
							IBasicColumn c = (IBasicColumn) VersionHelper.getReferencedItem(r);
							int jdbcType = storageService.getColumnSqlType(set, c);
							stmt.setNull(col, jdbcType);
						}
					}
					// Incrementing column index
					col++;

				}
				stmt.addBatch();
			}
			stmt.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			LOGGER.error(
					DBGMMessages.getString("service.data.addDatalineFailed") + e.getMessage(), e); //$NON-NLS-1$
		} finally {
			safeClose(null, stmt, conn, false);
		}

	}

	private void safeClose(ResultSet rset, Statement stmt, Connection conn,
			boolean displayDisconnectMsg) {
		if (rset != null) {
			try {
				rset.close();
			} catch (SQLException e) {
				LOGGER.error(DBGMMessages.getString("service.data.closeResultSetFailed"), e); //$NON-NLS-1$
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error(DBGMMessages.getString("service.data.closeStatementFailed"), e); //$NON-NLS-1$
			}
		}
		if (conn != null) {
			try {
				if (displayDisconnectMsg) {
					final DatabaseMetaData md = conn.getMetaData();
					LOGGER.info("Disconnecting from " + md.getURL());
				}
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(DBGMMessages.getString("service.data.closeConnectionFailed"), e); //$NON-NLS-1$
			}
		}
	}

	@Override
	public IDataDelta computeDataSetDelta(IDataSet source, IDataSet target) {
		final IDataDelta delta = new DataDelta();
		// A few optimizations first
		final IStorageHandle srcHandle = source != null ? source.getStorageHandle() : null;
		final IStorageHandle tgtHandle = target != null ? target.getStorageHandle() : null;
		if (srcHandle == null && tgtHandle != null) {
			delta.setDeletedDataSet(target);
			delta.setReference(target.getReference());
			delta.computeDifferenceType(DifferenceType.MISSING_SOURCE);
		} else if (srcHandle != null && tgtHandle == null) {
			delta.setAddedDataSet(source);
			delta.setReference(source.getReference());
			delta.computeDifferenceType(DifferenceType.MISSING_TARGET);
		} else if (srcHandle != null && tgtHandle != null) {
			// Here is the hard work : full comparison
			// Preparing structures
			final IDataSet inserts = createEmptyDataSetFrom(source);
			// Handling structural updates (only the situation when a column is added or removed)
			IDataSet baseUpdateSet = source;
			if (target.getColumnsRef().size() > source.getColumnsRef().size()) {
				baseUpdateSet = target;
			}
			final IDataSet updates = createEmptyDataSetFrom(baseUpdateSet);
			final IDataSet deletes = createEmptyDataSetFrom(source);
			delta.setAddedDataSet(inserts);
			delta.setUpdatedDataSet(updates);
			delta.setDeletedDataSet(deletes);
			delta.setReference(source.getReference());
			// Preparing storage
			storageService.createDataSetStorage(inserts);
			storageService.createDataSetStorage(updates, true);
			storageService.createDataSetStorage(deletes);
			// Fetching comparison
			Connection conn = null;
			Statement stmt = null;
			ResultSet rset = null;
			String deltaSelect = ""; //$NON-NLS-1$
			try {
				conn = storageService.getLocalConnection();
				stmt = conn.createStatement();
				deltaSelect = buildSelectDataSetDeltaStatement(source, target);
				rset = stmt.executeQuery(deltaSelect);
				final ResultSetMetaData md = rset.getMetaData();
				final int datasetColumnCount = source.getColumnsRef().size();
				while (rset.next()) {
					// Checking mode (insert / update / delete) while fetching data
					List<Object> sourceResults = new ArrayList<Object>();

					final boolean sourceExists = fillResults(sourceResults, rset, 1,
							datasetColumnCount + 1);
					List<Object> targetResults = new ArrayList<Object>();
					final boolean targetExists = fillResults(targetResults, rset,
							datasetColumnCount + 2, md.getColumnCount());
					if (LOGGER.isTraceEnabled()) {
						LOGGER.warn("Source = " + sourceResults + " | Target = " + targetResults);
					}
					if (sourceExists && targetExists) {
						final int columnsDeltaCount = targetResults.size() - sourceResults.size();
						// Filling null columns when we got less source columns than target (since
						// the update dataset is mirrored). Note that the situation when target has
						// less columns than source is handled during the insert by filling null
						// values.
						for (int i = 0; i < columnsDeltaCount; i++) {
							sourceResults.add(null);
						}
						// We append our targets next to our results to inject them
						// as meta data that will be used
						sourceResults.addAll(targetResults);
						fillStorageValues(updates.getStorageHandle(), sourceResults);
						delta.computeDifferenceType(DifferenceType.DIFFER);
					} else if (sourceExists && !targetExists) {
						// fillDataLine(inserts, line, sourceResults, false);
						fillStorageValues(inserts.getStorageHandle(), sourceResults);
						delta.computeDifferenceType(DifferenceType.MISSING_TARGET);
					} else if (!sourceExists && targetExists) {
						// fillDataLine(deletes, line, targetResults, false);
						fillStorageValues(deletes.getStorageHandle(), targetResults);
						delta.computeDifferenceType(DifferenceType.MISSING_SOURCE);
					} else {
						throw new ErrorException(
								DBGMMessages.getString("service.data.dataComparisonError")); //$NON-NLS-1$
					}
				}
			} catch (SQLException e) {
				throw new ErrorException("Problems while computing data differences: " //$NON-NLS-1$
						+ e.getMessage(), e);
			} finally {
				safeClose(rset, stmt, conn, false);
			}

		}
		return delta;
	}

	private void fillStorageValues(IStorageHandle handle, Collection<Object> values)
			throws SQLException {
		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = storageService.getLocalConnection();
			final String insertStmt = handle.getInsertStatement();
			final int expectedArgCount = insertStmt.length() - insertStmt.replace("?", "").length();
			stmt = conn.prepareStatement(insertStmt);
			int i = 1;
			for (Object o : values) {
				// TODO : Testing nullity to workaround some derby jdbc problem (tmeporary)
				if (i <= expectedArgCount) {
					if (o == null) {
						stmt.setNull(i++, Types.VARCHAR);
					} else {
						stmt.setObject(i++, o);
					}
				} else {
					// Normally, this should append when source and target data sets have different
					// structure (more columns in target or in source), but this might also hide
					// some other bug (not sure about what will happen when column are swapped
					// between
					// source and target
					// So we log in debug mode
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("DataSet DEBUG : parameter " + (i++)
								+ " ignored while filling storage handle for query " + insertStmt
								+ " with values : " + values);
					}
				}
			}
			while (i <= expectedArgCount) {
				stmt.setNull(i++, Types.VARCHAR);
			}
			stmt.execute();
		} finally {
			safeClose(null, stmt, conn, false);
		}
	}

	// private void fillDataLine(IDataSet set, IDataLine line, List<Object> columnValues,
	// boolean isUpdated) {
	// int i = 0;
	// final List<IReference> colRefs = set.getColumnsRef();
	// // Injecting data line column values
	// for (Object o : columnValues) {
	// // Preparing column value
	// final IColumnValue colValue = typedObjectFactory.create(IColumnValue.class);
	// colValue.setDataLine(line);
	// if (i < colRefs.size()) {
	// colValue.setColumnRef(colRefs.get(i++));
	// }
	// colValue.setValue(o);
	// line.addColumnValue(colValue);
	// }
	// line.setDataSet(set);
	// addDataline(set, line);
	// }

	/**
	 * Fills the specified result collection from the starting result set column to the ending
	 * column. A boolean is returned to indicate whether all column values where null.
	 * 
	 * @param results
	 * @param rset
	 * @param startCol
	 * @param endCol
	 * @return
	 * @throws SQLException
	 */
	private boolean fillResults(Collection<Object> results, ResultSet rset, int startCol, int endCol)
			throws SQLException {
		boolean allNull = true;
		for (int i = startCol; i <= endCol; i++) {
			final Object o = rset.getObject(i);
			if (allNull) {
				// Avoiding startcol here as it will always be our rowid
				allNull = (o == null || i == startCol);
			}
			results.add(o);
		}
		return !allNull;
	}

	private IDataSet createEmptyDataSetFrom(IDataSet template) {
		final IVersionable<IDataSet> vSet = VersionableFactory.createVersionable(IDataSet.class);
		final IDataSet set = vSet.getVersionnedObject().getModel();
		for (IReference colRef : template.getColumnsRef()) {
			set.addColumnRef(colRef);
		}
		set.setTableReference(template.getTableReference());
		set.setCurrentRowId(template.getCurrentRowId());
		return set;
	}

	private String buildSelectDataSetDeltaStatement(IDataSet source, IDataSet target) {
		StringBuilder buf = new StringBuilder(500);
		buf.append("select "); //$NON-NLS-1$
		final String srcPrefix = "s"; //$NON-NLS-1$
		final String tgtPrefix = "t"; //$NON-NLS-1$
		final String srcColDecl = buildPrefixedColumnDeclaration(srcPrefix, tgtPrefix, source);
		final String tgtColDecl = buildPrefixedColumnDeclaration(tgtPrefix, srcPrefix, target);
		final IStorageHandle srcHandle = source.getStorageHandle();
		final IStorageHandle tgtHandle = target.getStorageHandle();
		buf.append(srcColDecl);
		buf.append(", "); //$NON-NLS-1$
		buf.append(tgtColDecl);
		buf.append(" from " + srcHandle.getStorageUnitName() + " " + srcPrefix); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(" left join " + tgtHandle.getStorageUnitName() + " " + tgtPrefix + " on "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		final String join = buildJoin(srcPrefix, source, tgtPrefix, target);
		buf.append(join);
		final String whereDifferent = buildWhereDifferentClause(srcPrefix, source, tgtPrefix,
				target);
		buf.append(whereDifferent);
		buf.append(" union all select "); //$NON-NLS-1$
		buf.append(srcColDecl);
		buf.append(", "); //$NON-NLS-1$
		buf.append(tgtColDecl);
		buf.append(" from " + srcHandle.getStorageUnitName() + " " + srcPrefix); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(" right join " + tgtHandle.getStorageUnitName() + " " + tgtPrefix + " on "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append(join);
		buf.append(" where "); //$NON-NLS-1$
		final String srcPkIsNull = buildPKColumnsIsNull(srcPrefix, source);
		buf.append(srcPkIsNull);
		return buf.toString();
	}

	/**
	 * Builds the primary key join between source and target
	 * 
	 * @param srcPrefix source table alias
	 * @param src source dataset
	 * @param tgtPrefix target table alias
	 * @param tgt target dataset
	 * @return
	 */
	private String buildJoin(String srcPrefix, IDataSet src, String tgtPrefix, IDataSet tgt) {
		final StringBuilder buf = new StringBuilder(100);
		// When we have 2 repository dataset to compare, we join on the repository rowid, else
		// we join on the PK of the table, and if not found we fallback by joining all columns
		if (src.getStorageHandle().isRepositoryHandle()
				&& tgt.getStorageHandle().isRepositoryHandle()) {
			// Joining on rowid
			buf.append(srcPrefix + "." + IStorageService.ROWID_COLUMN_NAME + "=" + tgtPrefix + "." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ IStorageService.ROWID_COLUMN_NAME);
		} else {
			List<IBasicColumn> joinCols = getPrimaryKeyColumns(src);
			String separator = ""; //$NON-NLS-1$
			// If we have no PK columns to join, we join on all table columns
			if (joinCols.isEmpty()) {
				final IBasicTable table = src.getTable();
				joinCols = table.getColumns();
			}
			// Generating the join clause
			for (IBasicColumn c : joinCols) {
				buf.append(separator + buildPrefixedCol(srcPrefix, c) + "=" //$NON-NLS-1$
						+ buildPrefixedCol(tgtPrefix, c));
				separator = " and "; //$NON-NLS-1$
			}
		}
		return buf.toString();
	}

	private String buildPKColumnsIsNull(String alias, IDataSet dataset) {
		final StringBuilder buf = new StringBuilder(100);
		List<IBasicColumn> joinCols = getPrimaryKeyColumns(dataset);
		// If no PK we need to consider every column of our table
		if (joinCols.isEmpty()) {
			final IBasicTable table = dataset.getTable();
			joinCols = table.getColumns();
		}
		List<String> joinColName = new ArrayList<String>();
		String separator = "("; //$NON-NLS-1$
		// First
		for (IBasicColumn c : joinCols) {
			buf.append(separator + buildPrefixedCol(alias, c) + " is null"); //$NON-NLS-1$
			separator = " and "; //$NON-NLS-1$
			joinColName.add(c.getName());
		}
		buf.append(")"); //$NON-NLS-1$
		return buf.toString();
	}

	private String buildWhereDifferentClause(String srcAlias, IDataSet src, String tgtAlias,
			IDataSet tgt) {
		final StringBuilder buf = new StringBuilder(100);
		buf.append(" where "); //$NON-NLS-1$
		List<IBasicColumn> pkCols = Collections.emptyList();
		final boolean isRepositoryComparison = src.getStorageHandle().isRepositoryHandle()
				&& tgt.getStorageHandle().isRepositoryHandle();
		if (isRepositoryComparison) {
			buf.append(tgtAlias + "." + IStorageService.ROWID_COLUMN_NAME + " is null"); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append(" or " + srcAlias + "." + IStorageService.ROWID_COLUMN_NAME + "<>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ tgtAlias + "." + IStorageService.ROWID_COLUMN_NAME); //$NON-NLS-1$
		} else {
			pkCols = getPrimaryKeyColumns(src);
			// Only generating "where different" clause when we have a PK
			final String targetPKIsNull = buildPKColumnsIsNull(tgtAlias, tgt);
			buf.append(targetPKIsNull);
		}
		if (!pkCols.isEmpty() || isRepositoryComparison) {
			List<String> joinColName = NameHelper.buildNameList(pkCols);
			String separator = " or "; //$NON-NLS-1$
			for (IBasicColumn c : src.getColumns()) {
				if (tgt.getColumnsRef().contains(c.getReference())) {
					if (!joinColName.contains(c.getName())) {
						buf.append(separator);
						buf.append('(');
						buf.append(buildPrefixedCol(srcAlias, c) + " is not null"); //$NON-NLS-1$
						buf.append(" and " + buildPrefixedCol(tgtAlias, c) + " is null"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append(") or ("); //$NON-NLS-1$
						buf.append(buildPrefixedCol(srcAlias, c) + " is null"); //$NON-NLS-1$
						buf.append(" and " + buildPrefixedCol(tgtAlias, c) + " is not null"); //$NON-NLS-1$ //$NON-NLS-2$
						buf.append(") or "); //$NON-NLS-1$
						buf.append(buildPrefixedCol(srcAlias, c)
								+ "<>" + buildPrefixedCol(tgtAlias, c)); //$NON-NLS-1$
						separator = " or "; //$NON-NLS-1$
					}
				} else {
					buf.append(separator);
					buf.append(buildPrefixedCol(srcAlias, c) + " is not null"); //$NON-NLS-1$
					separator = " or "; //$NON-NLS-1$
				}
			}
		}
		return buf.toString();
	}

	private String buildPrefixedCol(String prefix, IBasicColumn col) {
		return prefix + "." + storageService.escape(col.getName()); //$NON-NLS-1$ 
	}

	private List<IBasicColumn> getPrimaryKeyColumns(IDataSet src) {
		final IBasicTable table = src.getTable();
		IKeyConstraint pk = DBGMHelper.getPrimaryKey(table);
		// First checking that all columns in dataset
		final Collection<IReference> srcCols = src.getColumnsRef();
		if (pk != null) {
			for (IReference colRef : pk.getConstrainedColumnsRef()) {
				if (!srcCols.contains(colRef)) {
					throw new ErrorException(
							DBGMMessages.getString("service.data.PKNotInDatasetError")); //$NON-NLS-1$
				}
			}
			return pk.getColumns();
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Builds a string with a comma-separated list of the dataset columns, prefixed by the provided
	 * string. All columns are aliased by the prefix immediatly followed by a counter. For example,
	 * calling this method with a "s" prefix and a dataset with column A B and C would give :<br>
	 * <code>s.A s1, s.B s2, s.C s3</code>
	 * 
	 * @param prefix column name prefix
	 * @param set dataset to generate column list for
	 * @return a comma seperated string with prefixed and aliased columns
	 */
	private String buildPrefixedColumnDeclaration(String prefix, String otherPrefix, IDataSet set) {
		final StringBuilder buf = new StringBuilder(100);
		String separator = ""; //$NON-NLS-1$
		int i = 1;
		// If current table is not a repository table, we try to fetch other's table rowid which is
		// our only chance to get a rowid
		String rowIdPrefix = prefix;
		if (!set.getStorageHandle().isRepositoryHandle()) {
			rowIdPrefix = otherPrefix;
		}
		buf.append(rowIdPrefix + "." + IStorageService.ROWID_COLUMN_NAME); //$NON-NLS-1$
		separator = ", "; //$NON-NLS-1$
		// }
		for (IBasicColumn c : set.getColumns()) {
			buf.append(separator + buildPrefixedCol(prefix, c) + " " + prefix + i); //$NON-NLS-1$
			separator = ", "; //$NON-NLS-1$
			i++;
		}
		return buf.toString();
	}

	@Override
	public void loadDataLinesFromRepository(IDataSet dataSet, IProgressMonitor m) {
		SubMonitor monitor = SubMonitor.convert(m, 100000);
		// Check if already loaded
		if (dataSet.getStorageHandle() != null || dataSet.getUID() == null) {
			return;
		} else {
			storageService.createDataSetStorage(dataSet);
		}
		// Make sure that data set version is tagged
		Session session = HibernateUtil.getInstance().getSandBoxSession();
		final IVersionInfo version = VersionHelper.getVersionInfo(dataSet);
		Long versionTag = version.getVersionTag();
		long computedVersionTag = VersionHelper.computeVersion(version);
		if (versionTag == null || versionTag.longValue() != computedVersionTag) {
			version.setVersionTag(computedVersionTag);
			CorePlugin.getIdentifiableDao().save(version, true, session, true);
		}
		// Connecting explicitly to repository
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		// Building reference map to avoid instantiating references
		final Map<Long, IReference> colRefMap = new HashMap<Long, IReference>();
		for (IReference colRef : dataSet.getColumnsRef()) {
			colRefMap.put(colRef.getUID().rawId(), colRef);
		}
		// Building version tree id list
		List<Long> idList = buildVersionIdHistoryList(version);
		try {
			conn = getRepositoryConnection();
			monitor.subTask(DBGMMessages.getString("service.data.executingRepositoryQuery")); //$NON-NLS-1$
			final String selectStmt = buildSelectRepositoryValuesStmt(idList);
			stmt = conn.prepareStatement(selectStmt);
			// "SELECT dlc.dset_row_id, dlv.column_refid, dlv.column_value "
			// + "FROM dbgm_dset_rows dlc LEFT JOIN dbgm_dset_rows dln "
			// + "       ON dln.dset_refid = dlc.dset_refid "
			// + "      AND dln.dset_row_id = dlc.dset_row_id "
			// + "      AND dln.version_tag > dlc.version_tag "
			// + "      AND dln.version_tag <= ? "
			// + "  JOIN dbgm_dset_row_values dlv ON dlv.drow_id = dlc.drow_id "
			// + "WHERE dlc.dset_refid = ? AND dlc.version_tag <= ? "
			// + "AND dln.dset_refid IS NULL ");
			// "ORDER BY dlc.dlin_no, dlc.version_tag" );
			final long setRefId = version.getReference().getUID().rawId();
			int colIndex = 1;
			stmt.setLong(colIndex++, setRefId);
			for (Long id : idList) {
				stmt.setLong(colIndex++, id);
			}
			stmt.setLong(colIndex++, setRefId);
			// stmt.setLong(4, versionTag);
			rset = stmt.executeQuery();
			monitor.worked(1);
			IDataLine line = typedObjectFactory.create(IDataLine.class);
			long lineId;
			boolean isEmpty = true;
			// Preparing line buffer
			Collection<IDataLine> bufferedLines = new ArrayList<IDataLine>(LINE_BUFFER_SIZE);
			long counter = 0;
			while (rset.next()) {
				if (monitor.isCanceled()) {
					return;
				}
				lineId = rset.getLong(1);
				// If new row id, new line
				if (line.getRowId() != lineId && !isEmpty) {
					bufferedLines.add(line);
					if (bufferedLines.size() >= LINE_BUFFER_SIZE) {
						counter += LINE_BUFFER_SIZE;
						monitor.subTask(MessageFormat.format(
								DBGMMessages.getString("service.data.loadedLines"), counter)); //$NON-NLS-1$
						monitor.worked(LINE_BUFFER_SIZE);
						addDataline(dataSet,
								bufferedLines.toArray(new IDataLine[bufferedLines.size()]));
						bufferedLines.clear();
					}
					line = typedObjectFactory.create(IDataLine.class);
				}
				line.setRowId(lineId);
				isEmpty = false;
				final long colRefId = rset.getLong(2);
				final String strValue = rset.getString(3);
				final IReference colRef = colRefMap.get(colRefId);
				// We might have unresolved column reference when the column has been removed
				// from the dataset. In this case we simply ignore the value
				if (colRef != null) {
					final Object value = storageService.decodeValue(colRef, strValue);
					// Preparing column value
					final IColumnValue colValue = typedObjectFactory.create(IColumnValue.class);
					colValue.setDataLine(line);
					colValue.setColumnRef(colRef);
					colValue.setValue(value);
					line.addColumnValue(colValue);
				}
			}
			if (!isEmpty) {
				bufferedLines.add(line);
				addDataline(dataSet, bufferedLines.toArray(new IDataLine[bufferedLines.size()]));
			}
			monitor.done();
		} catch (SQLException e) {
			throw new ErrorException(
					DBGMMessages.getString("service.data.loadRepositoryDataSetError") + e.getMessage(), e); //$NON-NLS-1$
		} finally {
			safeClose(rset, stmt, conn, true);
		}
		handleDataSetStructuralChanges(dataSet);
	}

	private void handleDataSetStructuralChanges(final IDataSet dataSet) {
		Designer.getListenerService().registerListener(this, dataSet, new IEventListener() {

			@Override
			public void handleEvent(ChangeEvent event, IObservable source, Object data) {
				switch (event) {
				case COLUMN_ADDED:
				case COLUMN_REMOVED:
					Job j = new Job("Refreshing dataset contents") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							dataSet.setStorageHandle(null);
							loadDataLinesFromRepository(dataSet, monitor);
							return Status.OK_STATUS;
						}
					};
					j.schedule();
				}
			}
		});
	}

	/**
	 * Recursively builds the list of all version tags of the whole history tree of the provided
	 * version.
	 * 
	 * @param version the version to compute the history tag list for
	 * @return the tag list
	 */
	private List<Long> buildVersionIdHistoryList(IVersionInfo version) {
		final List<Long> idList = new ArrayList<Long>();
		Long tag = version.getUID().rawId();
		idList.add(tag);
		final IVersionInfo previousVersion = version.getPreviousVersion();
		if (previousVersion != null) {
			idList.addAll(buildVersionIdHistoryList(previousVersion));
		}
		final IVersionInfo mergedFromVersion = version.getMergedFromVersion();
		if (mergedFromVersion != null) {
			idList.addAll(buildVersionIdHistoryList(mergedFromVersion));
		}
		return idList;
	}

	private String buildSelectRepositoryValuesStmt(List<Long> idList) {
		final StringBuilder buf = new StringBuilder();
		buf.append("SELECT r.dset_row_id, rv.column_refid, rv.column_value" //$NON-NLS-1$
				+ " FROM (    SELECT ds.dset_row_id, MAX(rv.version_tag) max_tag" //$NON-NLS-1$
				+ "    FROM DBGM_DSET_ROWS ds, REP_VERSIONS rv" //$NON-NLS-1$
				+ "    WHERE ds.dset_version_id = rv.version_id" //$NON-NLS-1$
				+ "      AND rv.vref_id=? and rv.VERSION_ID in ("); //$NON-NLS-1$
		String separator = ""; //$NON-NLS-1$
		for (Long id : idList) {
			buf.append(separator + "?"); //$NON-NLS-1$
			separator = ","; //$NON-NLS-1$
		}
		buf.append(") and rv.IS_DROPPED='N'" + "    GROUP BY ds.dset_row_id" //$NON-NLS-1$ //$NON-NLS-2$
				+ "  ) t, REP_VERSIONS v, DBGM_DSET_ROWS r, DBGM_DSET_ROW_VALUES rv" //$NON-NLS-1$
				+ " WHERE v.vref_id = ?  AND v.version_tag = t.max_tag and v.IS_DROPPED='N'" //$NON-NLS-1$
				+ "  AND r.dset_row_id = t.dset_row_id" //$NON-NLS-1$
				+ "  AND r.dset_version_id = v.version_id  AND rv.drow_id = r.drow_id"); //$NON-NLS-1$
		return buf.toString();
	}

	@Override
	public void saveDataLinesToRepository(IDataSet dataSet, IProgressMonitor monitor) {
		IVersionable<IDataSet> vSet = VersionHelper.getVersionable(dataSet);
		final IVersionInfo previousVersion = vSet.getVersion().getPreviousVersion();
		final SubMonitor m = SubMonitor.convert(monitor, 10000);
		m.subTask(MessageFormat.format(
				DBGMMessages.getString("service.data.savingLines"), dataSet.getName())); //$NON-NLS-1$
		// If no previous version we store everything
		if (previousVersion == null) {
			resetDataset(dataSet);
			m.worked(100);
			saveDataLinesToRepository(dataSet, dataSet, DeltaType.INSERT, m.newChild(9900));
		} else {
			// Here we need to compute a delta so we first load previous version
			m.subTask(DBGMMessages.getString("service.data.loadingPreviousVersion")); //$NON-NLS-1$
			final Session session = HibernateUtil.getInstance().getSandBoxSession();
			IDataSet previousSet = (IDataSet) CorePlugin.getIdentifiableDao().load(DataSet.class,
					previousVersion.getUID(), session, true);
			m.worked(100);
			loadDataLinesFromRepository(previousSet, m.newChild(1500));
			loadDataLinesFromRepository(dataSet, m.newChild(1500));
			m.setWorkRemaining(6900);
			m.subTask(DBGMMessages.getString("service.data.computingDifferences")); //$NON-NLS-1$
			IDataDelta delta = computeDataSetDelta(dataSet, previousSet);
			m.worked(3000);
			saveDataDeltaToRepository(dataSet, delta, m.newChild(3900));
		}
	}

	@Override
	public void saveDataDeltaToRepository(IDataSet set, IDataDelta delta, IProgressMonitor monitor) {
		final SubMonitor m = SubMonitor.convert(monitor, 10000);
		m.subTask(DBGMMessages.getString("service.data.clearingData")); //$NON-NLS-1$
		resetDataset(set);
		m.worked(100);
		if (delta.getAddedDataSet() != null) {
			saveDataLinesToRepository(set, delta.getAddedDataSet(), DeltaType.INSERT,
					m.newChild(3300));
		}
		m.setWorkRemaining(6600);
		if (delta.getUpdatedDataSet() != null) {
			saveDataLinesToRepository(set, delta.getUpdatedDataSet(), DeltaType.UPDATE,
					m.newChild(3300));
		}
		m.setWorkRemaining(3300);
		if (delta.getDeletedDataSet() != null) {
			saveDataLinesToRepository(set, delta.getDeletedDataSet(), DeltaType.DELETE,
					m.newChild(3300));
		}
		m.done();
	}

	private void resetDataset(IDataSet set) {
		// Nothing to do for datasets which have not been already persisted
		if (set.getUID() == null || (set.getUID() != null && set.getUID().rawId() <= 0)) {
			return;
		}
		Connection repoConn = null;
		PreparedStatement stmt = null;
		try {
			repoConn = getRepositoryConnection();
			repoConn.setAutoCommit(false);
			stmt = repoConn
					.prepareStatement("delete from DBGM_DSET_ROW_VALUES where DROW_ID in (select r.DROW_ID from DBGM_DSET_ROWS r where DSET_VERSION_ID=?)"); //$NON-NLS-1$
			stmt.setLong(1, set.getUID().rawId());
			stmt.execute();
			stmt.close();
			// Now deleting lines
			stmt = repoConn.prepareStatement("delete from DBGM_DSET_ROWS where DSET_VERSION_ID=?"); //$NON-NLS-1$
			stmt.setLong(1, set.getUID().rawId());
			stmt.execute();
			stmt.close();
			repoConn.commit();

		} catch (SQLException e) {
			safeClose(null, stmt, repoConn, true);
		}

	}

	private Connection getRepositoryConnection() throws SQLException {
		final IConnection repoConnection = repositoryService.getRepositoryConnection();
		final Connection conn = repositoryService.getRepositoryConnector().connect(repoConnection);
		conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		return conn;
	}

	private void saveDataLinesToRepository(IDataSet dataSet, IDataSet dataSetContents,
			DeltaType deltaType, IProgressMonitor monitor) {
		final SubMonitor m = SubMonitor.convert(monitor, 10000);
		m.subTask(MessageFormat.format(
				DBGMMessages.getString("service.data.dataSetSaveInit"), dataSet.getName())); //$NON-NLS-1$
		IStorageHandle handle = dataSetContents.getStorageHandle();
		if (handle == null) {
			handle = storageService.createDataSetStorage(dataSet);
		}
		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		Connection repoConn = null;
		PreparedStatement insertStmt = null;
		Session s = null;
		Transaction t = null;
		long rowid = dataSet.getCurrentRowId() + 1;
		try {
			repoConn = getRepositoryConnection();
			repoConn.setAutoCommit(false);
			// We handle the hibernate session specifically to boost the import process
			s = HibernateUtil.getInstance().getSandBoxSession();
			s.clear();
			t = s.beginTransaction();
			// Getting our local derby connection
			conn = storageService.getLocalConnection();
			stmt = conn.createStatement();
			// Our prepared INSERT rows statement
			insertStmt = repoConn
					.prepareStatement("insert into DBGM_DSET_ROW_VALUES (DROW_ID,COLUMN_REFID,COLUMN_VALUE) values (?,?,?)"); //$NON-NLS-1$
			// Selecting data from derby local storage
			String selectStmt = handle.getSelectStatement();
			selectStmt = selectStmt.replace("select", "select " + IStorageService.ROWID_COLUMN_NAME //$NON-NLS-1$ //$NON-NLS-2$
					+ ","); //$NON-NLS-1$
			rset = stmt.executeQuery(selectStmt);
			final List<IReference> colRefs = dataSet.getColumnsRef();

			int lineBufferCount = 0;
			long counter = 0;
			while (rset.next()) {
				final IDataLine line = typedObjectFactory.create(IDataLine.class);
				line.setDataSet(dataSet);
				// If we got a repository rowid, we use it, else we affect a new available rowid
				final long selectedRowId = rset.getLong(1);
				if (selectedRowId != 0) {
					line.setRowId(selectedRowId);
				} else {
					line.setRowId(rowid++);
				}
				// Persisting line so that columns can use its ID
				s.save(line);
				if (deltaType != DeltaType.DELETE) {
					for (int i = 2; i < colRefs.size() + 2; i++) {
						final Object val = rset.getObject(i);
						// First column is our rowid, so we shift left by 1, starting at 0 => -2
						final IReference colRef = colRefs.get(i - 2);
						final IColumnValue colValue = typedObjectFactory.create(IColumnValue.class);
						colValue.setDataLine(line);
						colValue.setColumnRef(colRef);
						colValue.setValue(val);
						line.addColumnValue(colValue);
						insertStmt.setLong(1, line.getUID().rawId());
						insertStmt.setLong(2, colRef.getUID().rawId());
						insertStmt.setString(3, colValue.getStringValue());
						insertStmt.addBatch();
					}
				}
				if (lineBufferCount++ >= LINE_BUFFER_SIZE) {
					t.commit();
					insertStmt.executeBatch();
					s.clear();
					t = s.beginTransaction();
					counter += lineBufferCount;
					m.subTask(MessageFormat.format(
							DBGMMessages.getString("service.data.savedLines"), dataSet.getName(), //$NON-NLS-1$
							counter));
					m.worked(500);
					lineBufferCount = 0;
				}
			}
			if (lineBufferCount > 0) {
				t.commit();
				insertStmt.executeBatch();
				s.clear();
				lineBufferCount = 0;
			}
			repoConn.commit();
			dataSet.setCurrentRowId(rowid);
		} catch (SQLException e) {
			throw new ErrorException(
					DBGMMessages.getString("service.data.saveDatalineFailed") + e.getMessage(), e); //$NON-NLS-1$
		} finally {
			safeClose(rset, stmt, conn, false);
			safeClose(null, insertStmt, repoConn, true);
		}
	}

	public void setTypedObjectFactory(ITypedObjectFactory typedObjectFactory) {
		this.typedObjectFactory = typedObjectFactory;
	}

	public void setRepositoryService(IRepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void setStorageService(IStorageService storageService) {
		this.storageService = storageService;
	}
	/***
	 * SELECT dlc.dset_refid, dlc.dlin_no, dlc.version_tag, dlv.column_refid, dlv.column_value FROM
	 * dbgm_dset_lines dlc LEFT JOIN dbgm_dset_lines dln ON dln.dset_refid = dlc.dset_refid AND
	 * dln.dlin_no = dlc.dlin_no AND dln.version_tag > dlc.version_tag AND dln.version_tag <=
	 * 100000100 JOIN dbgm_dset_line_values dlv ON dlv.dln2_id = dlc.dln2_id WHERE dlc.dset_refid =
	 * 1 AND dlc.version_tag <= 100000100 AND dln.dset_refid IS NULL ORDER BY dlc.dlin_no,
	 * dlc.version_tag ;
	 */
}
