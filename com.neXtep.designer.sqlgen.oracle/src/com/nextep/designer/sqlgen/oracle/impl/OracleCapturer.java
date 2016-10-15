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
package com.nextep.designer.sqlgen.oracle.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.nextep.datadesigner.dbgm.impl.BasicColumn;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.TypeColumn;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.dbgm.model.LengthType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.CustomProgressMonitor;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.dbgm.model.CollectionType;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.IIndexPartition;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.model.IUserCollection;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.oracle.impl.IndexPartition;
import com.nextep.designer.dbgm.oracle.impl.OracleClusteredTable;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueConstraint;
import com.nextep.designer.dbgm.oracle.impl.TablePartition;
import com.nextep.designer.dbgm.oracle.impl.external.MaterializedViewLogPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.external.OracleIndexPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.external.OracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.external.PartitionPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.BuildType;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.oracle.model.IOracleIndex;
import com.nextep.designer.dbgm.oracle.model.IOracleSynonym;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;
import com.nextep.designer.dbgm.oracle.model.RefreshMethod;
import com.nextep.designer.dbgm.oracle.model.RefreshTime;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.ErrorInfo;
import com.nextep.designer.sqlgen.model.ICaptureContext;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;
import com.nextep.designer.sqlgen.model.base.AbstractCapturer;
import com.nextep.designer.sqlgen.oracle.OracleMessages;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * TODO Kept the 1.0.4 capturer style, need to refactor properly to ICapturer
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleCapturer extends AbstractCapturer {

	private static final Log LOGGER = LogFactory.getLog(OracleDatabaseConnector.class);
	private static final int PROGRESS_RANGE = 100;
	private static final String NEWLINE = System.getProperty("line.separator"); //$NON-NLS-1$

	private final Map<String, IMaterializedViewLog> mviewLogs = new HashMap<String, IMaterializedViewLog>();
	private final List<IOracleCluster> clusters = new ArrayList<IOracleCluster>();
	private final Collection<IView> views = new ArrayList<IView>();
	private final Collection<ITrigger> triggers = new ArrayList<ITrigger>();
	private final List<IIndex> indexes = new ArrayList<IIndex>();

	private PreparedStatement tabPartStmt = null;
	private PreparedStatement indPartStmt = null;

	private final ITypedObjectFactory typedObjectFactory;

	public OracleCapturer() {
		typedObjectFactory = CorePlugin.getTypedObjectFactory();
	}

	@Override
	public void initialize(IConnection conn, IMutableCaptureContext context) {
		super.initialize(conn, context);
		Connection c = (Connection)context.getConnectionObject();
		try {
			initPreparedStatements(c);
		} catch (SQLException e) {
			LOGGER.warn("Unable to initialize Oracle capturer: " + e.getMessage(), e);
		}
	}

	@Override
	public void release(IMutableCaptureContext context) {
		closePreparedStatements();
		super.release(context);
	}

	@Override
	public Collection<IView> getViews(ICaptureContext context, IProgressMonitor m) {
		return views;
	}

	@Override
	public Collection<IIndex> getIndexes(ICaptureContext context, IProgressMonitor monitor) {
		return indexes;
	}

	@Override
	public Collection<ITrigger> getTriggers(ICaptureContext context, IProgressMonitor monitor) {
		return triggers;
	}

	@Override
	public Collection<IBasicTable> getTables(ICaptureContext context, IProgressMonitor m) {
		final Map<String, IBasicColumn> columns = new HashMap<String, IBasicColumn>();
		final Map<String, IKeyConstraint> uniqueKeys = new HashMap<String, IKeyConstraint>();
		final Map<String, IVersionable<?>> tablesMap = new HashMap<String, IVersionable<?>>();
		final Collection<IBasicTable> tables = new ArrayList<IBasicTable>();
		final Map<String, IVersionable<?>> viewsMap = new HashMap<String, IVersionable<?>>();
		final Map<String, IVersionable<?>> matViews = new HashMap<String, IVersionable<?>>();
		final Map<String, IOracleIndex> indexesMap = new HashMap<String, IOracleIndex>();
		final Connection conn = (Connection)context.getConnectionObject();

		final IProgressMonitor monitor = new CustomProgressMonitor(SubMonitor.convert(m, 1000),
				PROGRESS_RANGE);

		final List<String> unsizableDatatypes = DBGMHelper.getDatatypeProvider(DBVendor.ORACLE).getUnsizableDatatypes();
		// Specific list to dynamically exclude tables from the capture
		final List<String> excludedTables = new ArrayList<String>();
		Statement stmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			stmt = conn.createStatement();

			// Starting by materialized views as we will fill them later with
			// table data
			monitor.worked(1);
			monitor.subTask("Retrieving materialized view summary...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT v.mview_name, v.query, v.rewrite_enabled, v.refresh_mode, v.refresh_method, v.build_mode, j.interval " //$NON-NLS-1$
						+ "FROM user_mviews v, user_jobs j " //$NON-NLS-1$
						+ "WHERE j.what(+) LIKE 'dbms_refresh.refresh(%'||v.mview_name||'%);'"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Materialized views] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				while (rset.next()) {
					monitor.worked(1);
					final String name = rset.getString(1);
					final String query = rset.getString(2);
					final String queryRewrite = rset.getString(3);
					final String refreshMode = rset.getString(4);
					final String refreshMethod = rset.getString(5);
					final String buildMode = rset.getString(6);
					final String nextExpr = rset.getString(7);

					IVersionable<IMaterializedView> viewV = VersionableFactory.createVersionable(IMaterializedView.class);
					IMaterializedView view = viewV.getVersionnedObject().getModel();
					view.setName(name);
					view.setSql(query);
					view.setQueryRewriteEnabled("Y".equals(queryRewrite)); //$NON-NLS-1$
					view.setRefreshTime(RefreshTime.valueOf(refreshMode));
					view.setRefreshMethod(RefreshMethod.valueOf(refreshMethod));
					view.setBuildType(BuildType.valueOf(buildMode));
					if (nextExpr != null) {
						view.setRefreshTime(RefreshTime.SPECIFY);
						view.setNextExpr(nextExpr);
					}
					matViews.put(name, viewV);
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Materialized views] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving tables...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT t.table_name, c.comments, t.tablespace_name, t.pct_free, t.pct_used, " //$NON-NLS-1$
						+ "	 t.ini_trans, t.max_trans, t.logging, t.compression, t.iot_type, t.partitioned, t.temporary " //$NON-NLS-1$
						+ "FROM user_tables t, user_tab_comments c " //$NON-NLS-1$
						+ "WHERE c.table_name(+) = t.table_name " //$NON-NLS-1$
						+ "  AND t.iot_type IS NULL AND t.secondary = 'N' " //$NON-NLS-1$
						+ "UNION " //$NON-NLS-1$
						+ "SELECT t.table_name, c.comments, i.tablespace_name, i.pct_free, i.pct_increase, " //$NON-NLS-1$
						+ "  i.ini_trans, i.max_trans, i.logging, i.compression, t.iot_type, t.partitioned, t.temporary " //$NON-NLS-1$
						+ "FROM user_tables t, user_tab_comments c, user_indexes i " //$NON-NLS-1$
						+ "WHERE c.table_name = t.table_name " //$NON-NLS-1$
						+ "  AND i.table_name = t.table_name " //$NON-NLS-1$
						+ "  AND i.index_type = 'IOT - TOP' " //$NON-NLS-1$
						+ "  AND t.iot_type = 'IOT' AND t.secondary = 'N' " //$NON-NLS-1$
						+ "UNION " //$NON-NLS-1$
						+ "SELECT c.cluster_name, '##CLUSTER##', c.tablespace_name, c.pct_free, c.pct_used," //$NON-NLS-1$
						+ "	 c.ini_trans, c.max_trans, null, null, null, null,null " //$NON-NLS-1$
						+ "FROM user_clusters c"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Tables] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				while (rset.next()) {
					monitor.worked(50);
					final String tableName = rset.getString(1);
					final String tableComments = rset.getString(2);
					final String tablespace = rset.getString(3);
					final int pctFree = rset.getInt(4);
					final int pctUsed = rset.getInt(5);
					final int initTrans = rset.getInt(6);
					final int maxTrans = rset.getInt(7);
					final String logging = rset.getString(8);
					final String compression = rset.getString(9);
					final String iot = rset.getString(10);
					final boolean partTable = "YES".equals(rset.getString(11)); //$NON-NLS-1$
					final boolean temporary = "Y".equals(rset.getString(12)); //$NON-NLS-1$
					// final String partType = rset.getString(12);

					// IBasicTable t =
					// (IBasicTable)ControllerFactory.getController(IElementType.getInstance("TABLE")).emptyInstance(null);////new
					// VersionedTable(tableName,tableComments,activity);
					IVersionable<?> v = null;
					if ("##CLUSTER##".equals(tableComments)) { //$NON-NLS-1$
						v = VersionableFactory.createVersionable(IOracleCluster.class);
						clusters.add((IOracleCluster)v.getVersionnedObject().getModel());
					} else {
						// This table might be a materialized view
						v = matViews.get(tableName);
						if (v == null) {
							// If not, this is a table
							v = VersionableFactory.createVersionable(IBasicTable.class);
						}
					}
					IBasicTable t = (IBasicTable)v.getVersionnedObject().getModel();
					t.setName(tableName);
					t.setTemporary(temporary);
					if (!(t instanceof IOracleCluster)) {
						t.setDescription(tableComments);
					}
					// Initializing physical properties
					OracleTablePhysicalProperties props = new OracleTablePhysicalProperties();
					if (!partTable) {
						props.setTablespaceName(tablespace);
						props.setAttribute(PhysicalAttribute.PCT_FREE, pctFree);
						if (pctUsed > 0) props.setAttribute(PhysicalAttribute.PCT_USED, pctUsed);
						props.setAttribute(PhysicalAttribute.INIT_TRANS, initTrans);
						props.setAttribute(PhysicalAttribute.MAX_TRANS, maxTrans);
						props.setPhysicalOrganisation("IOT".equals(iot) ? PhysicalOrganisation.INDEX //$NON-NLS-1$
								: PhysicalOrganisation.HEAP);
						props.setLogging("YES".equals(logging)); //$NON-NLS-1$
						props.setCompressed("ENABLED".equals(compression)); //$NON-NLS-1$
						props.setPartitioningMethod(PartitioningMethod.NONE);
					} else {
						// We do not set any physical information for
						// partitioned tables.
						// Instead, we fetch partitions information and add it
						// to the physical
						// properties
						props.setTablespaceName(tablespace);
						List<IPartition> tabParts = getTablePartitions(
								props, tableName, conn, monitor);
						props.setPartitions(tabParts);
						// props.setPartitioningMethod(PartitioningMethod.valueOf(partType));
					}
					((IOracleTable)t).setPhysicalProperties(props);
					// Registering table
					tablesMap.put(tableName, VersionHelper.getVersionable(t));
					tables.add(t);
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Tables] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving materialized views logs...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT master, rowids, primary_key, sequence, include_new_values, log_table, " //$NON-NLS-1$
						+ "  t.tablespace_name, t.pct_free, t.pct_used, t.ini_trans, t.max_trans, t.logging, t.compression " //$NON-NLS-1$
						+ "FROM user_mview_logs l, user_tables t " //$NON-NLS-1$
						+ "WHERE t.table_name = l.log_table"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Materialized views logs] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				while (rset.next()) {
					monitor.worked(1);
					final String table = rset.getString(1);
					final boolean rowids = "YES".equals(rset.getString(2)); //$NON-NLS-1$
					final boolean pk = "YES".equals(rset.getString(3)); //$NON-NLS-1$
					final boolean sequence = "YES".equals(rset.getString(4)); //$NON-NLS-1$
					final boolean newVals = "YES".equals(rset.getString(5)); //$NON-NLS-1$
					final String logTable = rset.getString(6);
					final String tablespace = rset.getString(7);
					final int pctFree = rset.getInt(8);
					final int pctUsed = rset.getInt(9);
					final int initTrans = rset.getInt(10);
					final int maxTrans = rset.getInt(11);
					final String logging = rset.getString(12);
					final String compression = rset.getString(13);

					IVersionable<IMaterializedViewLog> viewV = VersionableFactory.createVersionable(IMaterializedViewLog.class);
					IMaterializedViewLog view = viewV.getVersionnedObject().getModel();
					view.setTableReference(tablesMap.get(table).getReference());
					view.setPrimaryKey(pk);
					view.setRowId(rowids);
					view.setSequence(sequence);
					view.setIncludingNewValues(newVals);
					// Removing underlying physical table from capture
					excludedTables.add(logTable);

					// Physical properties
					MaterializedViewLogPhysicalProperties props = new MaterializedViewLogPhysicalProperties();
					props.setTablespaceName(tablespace);
					props.setAttribute(PhysicalAttribute.PCT_FREE, pctFree);
					if (pctUsed > 0) props.setAttribute(PhysicalAttribute.PCT_USED, pctUsed);
					props.setAttribute(PhysicalAttribute.INIT_TRANS, initTrans);
					props.setAttribute(PhysicalAttribute.MAX_TRANS, maxTrans);
					props.setLogging("YES".equals(logging)); //$NON-NLS-1$
					props.setCompressed("ENABLED".equals(compression)); //$NON-NLS-1$

					mviewLogs.put(viewV.getName(), view);
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Materialized views logs] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving table columns...");
			// Temporary storing columns into a map, hashed by
			// table_name.column_name
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT /*+ STAR_TRANSFORMATION */ " //$NON-NLS-1$
						+ "  c.table_name, c.column_name, c.column_id, c.data_type, decode(c.data_type,'NUMBER',c.data_precision,c.data_length), " //$NON-NLS-1$
						+ "  c.data_scale, c.nullable, c.data_default, cmt.comments, nvl(null,'N'), c.char_col_decl_length, c.char_length, c.char_used, c.virtual_column " //$NON-NLS-1$
						+ "FROM user_tab_cols c, user_col_comments cmt " //$NON-NLS-1$
						+ "WHERE cmt.table_name = c.table_name " //$NON-NLS-1$
						+ "  AND cmt.column_name = c.column_name " //$NON-NLS-1$
						+ "  AND c.table_name IN ( " //$NON-NLS-1$
						+ "      SELECT table_name " //$NON-NLS-1$
						+ "      FROM user_tables " //$NON-NLS-1$
						+ "      WHERE secondary = 'N' " //$NON-NLS-1$
						+ "   ) " //$NON-NLS-1$
						+ "ORDER BY c.table_name, c.column_id" //$NON-NLS-1$
				);
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Table columns] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				// Fetching results and creating related columns (separate loop
				// to avoid the n*m sql
				// calls)
				while (rset.next()) {
					monitor.worked(1);
					final String tableName = rset.getString(1);
					final String columnName = rset.getString(2);
					final int rank = rset.getInt(3);
					String datatypeName = rset.getString(4);
					int dataLength = rset.getInt(5);
					int dataPrecision = rset.getInt(6);
					final String nullable = rset.getString(7);
					final String dataDefault = rset.getString(8);
					final String colComments = rset.getString(9);
					final String partFlag = rset.getString(10);
					final Integer declCharLength = rset.getInt(11);
					final Integer charLength = rset.getInt(12);
					final String charUsed = rset.getString(13);
					final String isVirtual = rset.getString(14);

					// Altering datatype (bug for Timestamps containing
					// "(scale)")
					if (datatypeName.startsWith("TIMESTAMP")) { //$NON-NLS-1$
						datatypeName = "TIMESTAMP"; //$NON-NLS-1$
						dataLength = 0;
						dataPrecision = 0;
					}
					if (datatypeName.startsWith("CLOB") || datatypeName.startsWith("DATE") //$NON-NLS-1$ //$NON-NLS-2$
							|| datatypeName.startsWith("BLOB")) { //$NON-NLS-1$
						dataLength = 0;
					}
					// FIXME: Ugly VSC-specific varray compatibility (why is
					// there a column length
					// for VARRAYS ???)
					if (datatypeName.contains("VARRAY_TYP")) { //$NON-NLS-1$
						dataLength = 0;
					}

					// Retrieving referenced table
					IBasicTable t = (IBasicTable)tablesMap.get(tableName).getVersionnedObject().getModel();
					IBasicColumn c = typedObjectFactory.create(IBasicColumn.class);
					c.setName(columnName);
					c.setDescription(colComments);
					c.setRank(rank - 1);
					c.setNotNull("N".equals(nullable)); //$NON-NLS-1$
					c.setDefaultExpr(dataDefault == null ? "" : dataDefault.trim()); //$NON-NLS-1$
					final IDatatype datatype = new Datatype(datatypeName);
					if (!unsizableDatatypes.contains(datatype.getName())) {
						if ("C".equals(charUsed)) { //$NON-NLS-1$
							// Setting char length as default length
							datatype.setLength(charLength.intValue());
							datatype.setLengthType(LengthType.CHAR);
							// Setting BYTE length as alternate length
							datatype.setAlternateLength(dataLength);
						} else if ("B".equals(charUsed)) { //$NON-NLS-1$
							// Setting the BYTE length as regular length
							datatype.setLength(dataLength);
							datatype.setLengthType(LengthType.BYTE);
							// Setting the CHAR length
							datatype.setAlternateLength(charLength);
						} else {
							// Setting the BYTE length as regular length
							datatype.setLength(declCharLength != null && declCharLength > 0 ? declCharLength
									: dataLength);
							datatype.setLengthType(LengthType.UNDEFINED);
							datatype.setAlternateLength(charLength);
						}
						datatype.setPrecision(dataPrecision);
					}
					c.setDatatype(datatype);
					c.setVirtual("YES".equals(isVirtual)); //$NON-NLS-1$
					// TODO Warning: might cause save problems / column list
					// duplicates because
					// adding unsaved columns
					c.setParent(t);
					t.addColumn(c);
					// If we got the flag, we have a partitioning column
					if (!"N".equals(partFlag)) { //$NON-NLS-1$
						IOracleTablePhysicalProperties p = ((IOracleTable)t).getPhysicalProperties();
						p.addPartitionedColumn(c);
					}
					// Storing columns for later use
					columns.put(t.getName() + "." + c.getName(), c); //$NON-NLS-1$
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Table columns] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving clusters...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT cluster_name, clu_column_name, table_name, tab_column_name " //$NON-NLS-1$
						+ "FROM user_clu_columns " //$NON-NLS-1$
						+ "WHERE table_name NOT LIKE 'BIN$%' " //$NON-NLS-1$
						+ "ORDER BY cluster_name, table_name, clu_column_name"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Clusters] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				IOracleCluster currentCluster = null;
				// IOracleClusteredTable currentClusteredTable = null;
				while (rset.next()) {
					monitor.worked(1);
					final String clName = rset.getString(1);
					final String clColName = rset.getString(2);
					final String tbName = rset.getString(3);
					final String tbColName = rset.getString(4);

					if (currentCluster == null || !clName.equals(currentCluster.getName())) {
						final IVersionable<?> v = tablesMap.get(clName);
						if (v == null) {
							LOGGER.warn("Cluster '" + clName
									+ "' not found while importing cluster column '" + clColName
									+ "', skipping...");
							continue;
						}
						currentCluster = (IOracleCluster)v.getVersionnedObject().getModel();
					}
					final IVersionable<?> v = tablesMap.get(tbName);
					if (v == null) {
						LOGGER.warn("Table '" + tbName + "' not found while importing cluster '"
								+ clName + "', skipping...");
						continue;
					}
					IOracleTable table = (IOracleTable)v.getVersionnedObject().getModel();
					// Removing physical properties of that table
					table.setPhysicalProperties(null);
					IBasicColumn tableColumn = columns.get(tbName + "." + tbColName); //$NON-NLS-1$
					if (tableColumn == null) {
						LOGGER.warn("Table column '" + tbName + "." + tbColName //$NON-NLS-2$
								+ "' not found, skipping clustered table column...");
						continue;
					}
					// Does this column already exist in cluster ?
					IBasicColumn clusterCol = null;
					for (IBasicColumn c : currentCluster.getColumns()) {
						if (c.getName().equals(clColName)) {
							clusterCol = c;
							break;
						}
					}
					// If not we create it
					if (clusterCol == null) {
						final IDatatype colType = tableColumn.getDatatype();
						clusterCol = new BasicColumn(clColName, null, new Datatype(
								colType.getName(), colType.getLength(), colType.getPrecision()),
								currentCluster.getColumns().size());
						currentCluster.addColumn(clusterCol);
						columns.put(currentCluster.getName() + "." + clColName, clusterCol); //$NON-NLS-1$
					}
					// Does the table already exists in the cluster ?
					IOracleClusteredTable clusteredTab = null;
					for (IOracleClusteredTable t : currentCluster.getClusteredTables()) {
						if (table.getReference().equals(t.getReference())) {
							clusteredTab = t;
							break;
						}
					}
					if (clusteredTab == null) {
						clusteredTab = new OracleClusteredTable();
						clusteredTab.setTableReference(table.getReference());
						clusteredTab.setCluster(currentCluster);
						currentCluster.getClusteredTables().add(clusteredTab);
					}
					clusteredTab.setColumnReferenceMapping(
							clusterCol.getReference(), tableColumn.getReference());
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Clusters] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving partitions informations...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT name, column_name " //$NON-NLS-1$
						+ "FROM user_part_key_columns " //$NON-NLS-1$
						+ "WHERE object_type = 'TABLE' " //$NON-NLS-1$
						+ "  AND name NOT LIKE 'BIN$%'"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Partitions informations] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				while (rset.next()) {
					monitor.worked(1);
					final String tabName = rset.getString(1);
					final String colName = rset.getString(2);

					IOracleTable t = (IOracleTable)tablesMap.get(tabName);
					if (t != null) {
						try {
							Observable.deactivateListeners();
							(t.getPhysicalProperties()).addPartitionedColumn(columns.get(tabName
									+ "." + colName)); //$NON-NLS-1$
						} finally {
							Observable.activateListeners();
						}
					}
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Partitions informations] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving constraints...");
			// Temporary map to retrieve unique constraints later
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT " //$NON-NLS-1$
						+ "  con.constraint_name, con.constraint_type, con.table_name, con.r_constraint_name, con.delete_rule, " //$NON-NLS-1$
						+ "  col.column_name, idx.tablespace_name, idx.pct_free, idx.pct_increase, idx.ini_trans, idx.max_trans, " //$NON-NLS-1$
						+ "  idx.partitioned, idx.logging, idx.compression, idx.index_name, col.position " //$NON-NLS-1$
						+ "FROM ( " //$NON-NLS-1$
						+ "    SELECT c.constraint_name, c.constraint_type, c.table_name, c.r_constraint_name, c.index_name, c.delete_rule " //$NON-NLS-1$
						+ "    FROM user_constraints c " //$NON-NLS-1$
						+ "    WHERE c.constraint_type IN ('P', 'U', 'R') " //$NON-NLS-1$
						+ "      AND c.table_name IN ( " //$NON-NLS-1$
						+ "        SELECT t.table_name "//$NON-NLS-1$
						+ "        FROM user_tables t " //$NON-NLS-1$
						+ "        WHERE t.secondary = 'N' " //$NON-NLS-1$
						+ "      ) " //$NON-NLS-1$
						+ "  ) con " //$NON-NLS-1$
						+ "  LEFT JOIN user_indexes idx " //$NON-NLS-1$
						+ "  	ON idx.index_name = con.index_name AND con.constraint_type IN ('P', 'U'), " //$NON-NLS-1$
						+ "  user_cons_columns col " //$NON-NLS-1$
						+ "WHERE col.constraint_name = con.constraint_name " //$NON-NLS-1$
						+ "  AND col.table_name = con.table_name " //$NON-NLS-1$
						+ "ORDER BY DECODE(con.constraint_type, 'P', 1, 'U', 2, 3), con.table_name, con.constraint_name, col.position"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled()) LOGGER.debug("[Tables][Constraints] query time: " //$NON-NLS-1$
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				IKeyConstraint lastConstraint = null;
				while (rset.next()) {
					monitor.worked(1);
					final String name = rset.getString("constraint_name"); //$NON-NLS-1$
					final String type = rset.getString("constraint_type"); //$NON-NLS-1$
					final String table = rset.getString("table_name"); //$NON-NLS-1$
					final String remoteConstraint = rset.getString("r_constraint_name"); //$NON-NLS-1$
					final String onDeleteRule = rset.getString("delete_rule"); //$NON-NLS-1$
					final String col = rset.getString("column_name"); //$NON-NLS-1$

					final IVersionable<?> versionable = tablesMap.get(table);
					if (versionable == null) {
						LOGGER.warn("SKIPPING '" + name
								+ "': This constraint references the non-captured table '" + table
								+ "'");
					} else {
						final IBasicTable t = (IBasicTable)versionable.getVersionnedObject().getModel();
						IBasicColumn c = columns.get(table + "." + col); //$NON-NLS-1$
						if (lastConstraint == null || (!lastConstraint.getName().equals(name))) { // &&
							// !(lastConstraint.getConstrainedTable()
							// ==
							// t)))
							// {
							if ("P".equals(type) || "U".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
								lastConstraint = new OracleUniqueConstraint(); // name,"",t);
								// //(IKeyConstraint)ControllerFactory.getController(IElementType.getInstance("UNIQUE_KEY")).emptyInstance(t);
								// //
								lastConstraint.setName(name);
								lastConstraint.setConstrainedTable(t);
								uniqueKeys.put(name, lastConstraint);
								// Using unique key prefix as table short name
								t.setShortName(getPrefix(name));

								// Initializing physical properties
								final String ts = rset.getString("tablespace_name"); //$NON-NLS-1$
								final int pctFree = rset.getInt("pct_free"); //$NON-NLS-1$
								// final int pctInc = rset.getInt("pct_increase"); //$NON-NLS-1$
								final int initTrans = rset.getInt("ini_trans"); //$NON-NLS-1$
								final int maxTrans = rset.getInt("max_trans"); //$NON-NLS-1$
								final boolean partIndex = "YES".equals(rset.getString("partitioned")); //$NON-NLS-1$ //$NON-NLS-2$
								final String logging = rset.getString("logging"); //$NON-NLS-1$
								final String compression = rset.getString("compression"); //$NON-NLS-1$
								final String indexName = rset.getString("index_name"); //$NON-NLS-1$
								// final int colPosition = rset.getInt("position");

								OracleIndexPhysicalProperties props = new OracleIndexPhysicalProperties();
								if (!partIndex) {
									props.setTablespaceName(ts);
									props.setAttribute(PhysicalAttribute.PCT_FREE, pctFree);
									// if(pctInc>0)
									// props.setAttribute(PhysicalAttribute.PCT_, pctUsed);
									props.setAttribute(PhysicalAttribute.INIT_TRANS, initTrans);
									props.setAttribute(PhysicalAttribute.MAX_TRANS, maxTrans);
									props.setLogging("YES".equals(logging)); //$NON-NLS-1$
									props.setCompressed("ENABLED".equals(compression)); //$NON-NLS-1$
								} else {
									// if(!local) {
									// LOGGER.warn("Index '" + name +
									// "' is partitioned globally. Global partitions not supported, ignoring physical properties.");
									// } else {
									props.setTablespaceName(ts);
									props.setPartitioningMethod(PartitioningMethod.INDEX_LOCAL);
									List<IPartition> partitions = getIndexPartitions(
											indexName, conn, monitor);
									props.setPartitions(partitions);
									((OracleUniqueConstraint)lastConstraint).setPhysicalProperties(props);
									// }
								}
								((OracleUniqueConstraint)lastConstraint).setPhysicalProperties(props);
							} else {
								lastConstraint = new ForeignKeyConstraint(name, "", t); //$NON-NLS-1$
								IKeyConstraint refConstraint = uniqueKeys.get(remoteConstraint);
								// We have a reference to a non-imported constraint
								if (refConstraint == null) {
									try {
										refConstraint = (IKeyConstraint)CorePlugin.getService(
												IReferenceManager.class).findByTypeName(
												IElementType.getInstance("UNIQUE_KEY"), //$NON-NLS-1$
												remoteConstraint);
										LOGGER.warn("Database capture: A foreign key has been linked to a "
												+ "repository unique key because the unique key was not imported.");
									} catch (ReferenceNotFoundException e) {
										LOGGER.warn("Database capture: ignoring import of foreign key <"
												+ name
												+ "> because the dependent constraint not imported and not found in repository.");
										continue;
									}
								}
								ForeignKeyConstraint fk = (ForeignKeyConstraint)lastConstraint;
								fk.setRemoteConstraint(refConstraint);
								fk.setOnDeleteAction(CaptureHelper.getForeignKeyAction(onDeleteRule));
							}
							lastConstraint.setName(name);
							if ("P".equals(type) && (t instanceof IMaterializedView)) { //$NON-NLS-1$
								continue;
							}
							lastConstraint.setConstrainedTable(t);
							lastConstraint.setConstraintType("P".equals(type) ? ConstraintType.PRIMARY : "U" //$NON-NLS-1$ //$NON-NLS-2$
									.equals(type) ? ConstraintType.UNIQUE : ConstraintType.FOREIGN);

							t.addConstraint(lastConstraint);
						}
						if (c != null) {
							lastConstraint.addColumn(c);
						} else {
							LOGGER.warn("Constraint " + name + " references table column " + table + "." //$NON-NLS-3$
									+ col + " which cannot be found. Column has been ignored.");
						}
					}
				}
				if (LOGGER.isDebugEnabled()) LOGGER.debug("[Tables][Constraints] fetching time: " //$NON-NLS-1$
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving indexes...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT " //$NON-NLS-1$
						+ "    idx.index_name " //$NON-NLS-1$
						+ "  , idx.index_type " //$NON-NLS-1$
						+ "  , idx.uniqueness " //$NON-NLS-1$
						+ "  , idx.table_name " //$NON-NLS-1$
						+ "  , col.column_name " //$NON-NLS-1$
						+ "  , idx.tablespace_name " //$NON-NLS-1$
						+ "  , idx.pct_free " //$NON-NLS-1$
						+ "  , idx.pct_increase " //$NON-NLS-1$
						+ "  , idx.ini_trans " //$NON-NLS-1$
						+ "  , idx.max_trans " //$NON-NLS-1$
						+ "  , idx.partitioned " //$NON-NLS-1$
						+ "  , prt.locality " //$NON-NLS-1$
						+ "  , idx.logging " //$NON-NLS-1$
						+ "  , idx.compression " //$NON-NLS-1$
						+ "  , idx.funcidx_status " //$NON-NLS-1$
						+ "  , exp.column_expression " //$NON-NLS-1$
						+ "FROM user_indexes idx " //$NON-NLS-1$
						+ "  LEFT JOIN user_constraints con ON con.constraint_name = idx.index_name " //$NON-NLS-1$
						+ "    AND con.constraint_type IN ('P', 'U') " //$NON-NLS-1$
						+ "  INNER JOIN user_ind_columns col ON col.index_name = idx.index_name " //$NON-NLS-1$
						+ "  LEFT JOIN user_ind_expressions exp ON exp.index_name = col.index_name " //$NON-NLS-1$
						+ "    AND exp.column_position = col.column_position " //$NON-NLS-1$
						+ "  LEFT JOIN user_part_indexes prt ON prt.index_name = idx.index_name " //$NON-NLS-1$
						+ "WHERE idx.secondary = 'N' " //$NON-NLS-1$
						+ "  AND con.constraint_name IS NULL " //$NON-NLS-1$
						+ "ORDER BY idx.index_name, col.column_position"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Indexes] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				IIndex lastIndex = null;
				// String lastSkipped = null;
				while (rset.next()) {
					monitor.worked(1);
					final String name = rset.getString(1);
					final String type = rset.getString(2);
					final String uniq = rset.getString(3);
					final String table = rset.getString(4);
					final String col = rset.getString(5);
					final String ts = rset.getString(6);
					final int pctFree = rset.getInt(7);
					// final int pctInc = rset.getInt(8);
					final int initTrans = rset.getInt(9);
					final int maxTrans = rset.getInt(10);
					final boolean partIndex = "YES".equals(rset.getString(11)); //$NON-NLS-1$
					final boolean local = "LOCAL".equals(rset.getString(12)); //$NON-NLS-1$
					final String logging = rset.getString(13);
					final String compression = rset.getString(14);
					final String funcIdxStatus = rset.getString(15);
					final String colExpr = rset.getString(16);

					IVersionable<?> v = tablesMap.get(table);
					if (v == null) {
						LOGGER.warn("Skipping index <" + name + ">: related table '" + table
								+ "' was not in the capture set.");
						continue;
					}
					IBasicTable t = (IBasicTable)v.getVersionnedObject().getModel();
					IBasicColumn c = columns.get(table + "." + col); //$NON-NLS-1$
					// if(!"NORMAL".equals(type) && !name.equals(lastSkipped)) {
					// lastSkipped = name;
					// LOGGER.warn("Skipping index <" + name + ">: index type '" + type +
					// "' not supported." );
					// continue;
					// }
					if (lastIndex == null || !lastIndex.getIndexName().equals(name)) {
						// if(lastIndex!=null && lastIndex.getIndexedColumnsRef().isEmpty()) {
						// LOGGER.warn("Index '" + lastIndex.getName() +
						// "' has no valid columns, skipping index.");
						// indexes.remove(lastIndex);
						// }

						IVersionable<IIndex> index = VersionableFactory.createVersionable(IIndex.class);
						lastIndex = index.getVersionnedObject().getModel();
						lastIndex.setName(name);

						IndexType typeValue = IndexType.NON_UNIQUE;
						if ("UNIQUE".equals(uniq)) { //$NON-NLS-1$
							typeValue = IndexType.UNIQUE;
						} else if (type != null && type.startsWith("BITMAP")) { //$NON-NLS-1$
							typeValue = IndexType.BITMAP;
						}
						lastIndex.setIndexType(typeValue);

						if (t == null) {
							LOGGER.warn("Index <" + name + "> references an unknown table '"
									+ table + "', skipping index.");
						} else {
							lastIndex.setIndexedTableRef(t.getReference());
							t.addIndex(lastIndex);
							indexes.add(lastIndex);
							indexesMap.put(lastIndex.getIndexName(), (IOracleIndex)lastIndex);
						}

						// Initializing physical properties
						OracleIndexPhysicalProperties props = new OracleIndexPhysicalProperties();
						if (!partIndex) {
							props.setTablespaceName(ts);
							props.setAttribute(PhysicalAttribute.PCT_FREE, pctFree);
							// if(pctInc>0)
							// props.setAttribute(PhysicalAttribute.PCT_, pctUsed);
							props.setAttribute(PhysicalAttribute.INIT_TRANS, initTrans);
							props.setAttribute(PhysicalAttribute.MAX_TRANS, maxTrans);
							props.setLogging("YES".equals(logging)); //$NON-NLS-1$
							props.setCompressed("ENABLED".equals(compression)); //$NON-NLS-1$
							((IOracleIndex)lastIndex).setPhysicalProperties(props);
						} else {
							if (!local) {
								LOGGER.warn("Index '"
										+ name
										+ "' is partitioned globally. Global partitions not supported, ignoring physical properties.");
							} else {
								props.setTablespaceName(ts);
								props.setPartitioningMethod(PartitioningMethod.INDEX_LOCAL);
								List<IPartition> partitions = getIndexPartitions(
										name, conn, monitor);
								props.setPartitions(partitions);
								((IOracleIndex)lastIndex).setPhysicalProperties(props);
							}
						}

					}
					IReference indColRef = null;
					if (c == null) {
						/*
						 * If the current index is a function-based index we try to find a column
						 * name in the column expression.
						 */
						if (funcIdxStatus != null && colExpr != null) {
							indColRef = getColumnReference(t, colExpr);
							if (indColRef != null) {
								lastIndex.setFunction(indColRef, colExpr);

								/*
								 * Raise a warning if the column found in the expression is already
								 * referenced by the current index.
								 */
								if (lastIndex.getIndexedColumnsRef().contains(indColRef)) {
									LOGGER.warn("Column '" + col
											+ "' is already referenced by the index <"
											+ lastIndex.getName() + ">");
								}

							}
						}
					} else {
						indColRef = c.getReference();
					}
					if (indColRef != null) {
						lastIndex.addColumnRef(indColRef);
					} else {
						LOGGER.warn("Index <" + lastIndex.getName()
								+ "> references an unknown table column '" + col
								+ "', skipping column.");
					}
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Indexes] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving views...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT view_name, text " //$NON-NLS-1$
						+ "FROM user_views v " //$NON-NLS-1$
						+ "ORDER BY view_name"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Views] query time: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				IView currentView = null;
				while (rset.next()) {
					monitor.worked(1);
					final String viewName = rset.getString(1);
					final String sql = rset.getString(2);
					// final String colAlias = rset.getString(3);

					if (currentView == null || !viewName.equals(currentView.getName())) {
						IVersionable<IView> versionable = VersionableFactory.createVersionable(IView.class);
						currentView = versionable.getVersionnedObject().getModel();
						currentView.setName(viewName);
						currentView.setSQLDefinition(sql.trim().replaceAll("\n", "\r\n") + NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
						views.add(currentView);
						viewsMap.put(viewName, versionable);
					}
					// currentView.addColumnAlias(colAlias);
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Views] fetching time: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-2$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving triggers...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT trigger_name, table_name, s.text " //$NON-NLS-1$
						+ "FROM user_triggers t, user_source s " //$NON-NLS-1$
						+ "WHERE t.trigger_name NOT LIKE 'BIN$%' " //$NON-NLS-1$
						+ "  AND s.name = t.trigger_name " //$NON-NLS-1$
						+ "  AND s.type = 'TRIGGER' " //$NON-NLS-1$
						+ "ORDER BY table_name, trigger_name, line"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Triggers] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				ITrigger currentTrigger = null;
				while (rset.next()) {
					monitor.worked(1);
					final String name = rset.getString(1);
					final String tableName = rset.getString(2);
					String text = rset.getString(3);

					if (currentTrigger == null || !name.equals(currentTrigger.getName())) {
						// if(currentTrigger!=null) {
						// currentTrigger.setSourceCode(cleanSQLString(currentTrigger.getSourceCode(),
						// "TRIGGER") + NEWLINE);
						// }
						IVersionable<ITrigger> vt = VersionableFactory.createVersionable(ITrigger.class);
						currentTrigger = vt.getVersionnedObject().getModel();
						currentTrigger.setName(name);
						IVersionable<?> trigTable = tablesMap.get(tableName);
						if (trigTable != null) {
							currentTrigger.setTriggableRef(tablesMap.get(tableName).getReference());
						} else if (viewsMap.get(tableName) != null) {
							currentTrigger.setTriggableRef(viewsMap.get(tableName).getReference());
						} else {
							continue;
						}
						currentTrigger.setCustom(true);

						// Removing the '"USER".' trigger prefix
						// text.substring(beginIndex, endIndex)
						text = text.replace("\"" + context.getSchema() + "\".", "").trim() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						// text = removeChar(text, '\n');
						// text = removeChar(text, '\r');
						currentTrigger.setSourceCode(text);

						triggers.add(currentTrigger);
					} else {
						// text = removeChar(text, '\n');
						// text = removeChar(text, '\r');
						currentTrigger.setSourceCode(currentTrigger.getSourceCode() + text);
					}
					// currentTrigger.setSourceCode(
					// (currentTrigger.getSourceCode() == null ? "" :
					// currentTrigger.getSourceCode()) + text);
				}
				// if(currentTrigger!=null)
				// currentTrigger.setSourceCode(cleanSQLString(currentTrigger.getSourceCode(),
				// "TRIGGER") + NEWLINE);
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Triggers] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			monitor.worked(1);
			monitor.subTask("Retrieving checked constraints...");
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT table_name, constraint_name, search_condition " //$NON-NLS-1$
						+ "FROM user_constraints " //$NON-NLS-1$
						+ "WHERE constraint_type = 'C' " //$NON-NLS-1$ 
						+ "  AND generated LIKE 'USER%' " //$NON-NLS-1$
						+ "  AND table_name NOT LIKE 'BIN$%'"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Checked constraints] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				while (rset.next()) {
					monitor.worked(1);
					final String tabName = rset.getString(1);
					final String constraintName = rset.getString(2);
					final String conditionName = rset.getString(3);

					IVersionable<?> v = tablesMap.get(tabName);
					if (v == null) {
						LOGGER.warn("Skipping check constraint '" + constraintName + "' on table "
								+ tabName + ": table not in the imported set.");
						continue;
					}
					IOracleTable t = (IOracleTable)v.getVersionnedObject().getModel();
					ICheckConstraint c = CorePlugin.getTypedObjectFactory().create(
							ICheckConstraint.class);
					c.setConstrainedTable(t);
					c.setName(constraintName);
					c.setCondition(conditionName);
					t.addCheckConstraint(c);
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Tables][Checked constraints] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(null, stmt);
		}

		// Removing excluded tables
		for (String tabName : excludedTables) {
			tablesMap.remove(tabName);
		}

		return tables;
	}

	/**
	 * Retrieves the column reference used in the expression. When multiple column are referenced,
	 * the first one is returned.
	 * 
	 * @param t table scope
	 * @param expression expression referencing a column
	 * @return the table column reference
	 */
	private IReference getColumnReference(IBasicTable t, String expression) {
		for (IBasicColumn c : t.getColumns()) {
			if (expression.contains(c.getName())) {
				return c.getReference();
			}
		}
		return null;
	}

	/**
	 * This method retrieves partitions of a given table. It creates an independent statement to
	 * fetch partition information from database.
	 * 
	 * @param tableName name of the table to process (non-partitioned tables will return an empty
	 *        list)
	 * @param conn SQL connection
	 * @return a List of all defined table partitioned, ordered by the database partition order
	 * @throws SQLException whenever any SQL error occurs.
	 */
	private List<IPartition> getTablePartitions(OracleTablePhysicalProperties tabProps,
			String tableName, Connection conn, IProgressMonitor monitor) throws SQLException {
		final List<IPartition> tabParts = new ArrayList<IPartition>();
		ResultSet rset = null;
		long start = 0;

		monitor.subTask("Retrieving table partitions...");
		monitor.worked(1);
		try {
			tabPartStmt.setString(1, tableName);
			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			rset = tabPartStmt.executeQuery();
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Tables][Partitions] query time: "
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			while (rset.next()) {
				monitor.worked(1);
				final String partName = rset.getString(1);
				final String highValue = rset.getString(2);
				final String tablespace = rset.getString(3);
				final int pctFree = rset.getInt(4);
				final int pctUsed = rset.getInt(5);
				final int initTrans = rset.getInt(6);
				final int maxTrans = rset.getInt(7);
				final String logging = rset.getString(8);
				final String compression = rset.getString(9);
				final String partType = rset.getString(10);

				ITablePartition part = new TablePartition();
				part.setName(partName);
				part.setHighValue(highValue);

				tabProps.setPartitioningMethod(PartitioningMethod.valueOf(partType));
				PartitionPhysicalProperties props = new PartitionPhysicalProperties();
				props.setTablespaceName(tablespace);
				props.setAttribute(PhysicalAttribute.PCT_FREE, pctFree);
				if (pctUsed > 0) props.setAttribute(PhysicalAttribute.PCT_USED, pctUsed);
				props.setAttribute(PhysicalAttribute.INIT_TRANS, initTrans);
				props.setAttribute(PhysicalAttribute.MAX_TRANS, maxTrans);
				props.setLogging("YES".equals(logging)); //$NON-NLS-1$
				props.setCompressed("ENABLED".equals(compression)); //$NON-NLS-1$
				props.setPartitioningMethod(PartitioningMethod.NONE);
				part.setPhysicalProperties(props);
				tabParts.add(part);
			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Tables][Partitions] fetching time: "
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
		} finally {
			CaptureHelper.safeClose(rset, null);
		}

		return tabParts;
	}

	/**
	 * This method retrieves partitions of a given index. It creates an independent statement to
	 * fetch partition information from database.
	 * 
	 * @param indexName name of the index to process (non-partitioned indexes will return an empty
	 *        list)
	 * @param conn SQL connection
	 * @return a List of all defined index partitioned, ordered by the database partition order
	 * @throws SQLException whenever any SQL error occurs.
	 */
	private List<IPartition> getIndexPartitions(String indexName, Connection conn,
			IProgressMonitor monitor) throws SQLException {
		List<IPartition> indParts = new ArrayList<IPartition>();
		ResultSet rset = null;
		long start = 0;
		monitor.subTask("Retrieving index partitions...");
		monitor.worked(1);
		try {
			indPartStmt.setString(1, indexName);
			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			rset = indPartStmt.executeQuery();
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Indexes][Partitions] query time: "
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			while (rset.next()) {
				monitor.worked(1);
				final String partName = rset.getString(1);
				final String tablespace = rset.getString(3);
				final int pctFree = rset.getInt(4);
				// final int pctInc = rset.getInt(5);
				final int initTrans = rset.getInt(6);
				final int maxTrans = rset.getInt(7);
				final String logging = rset.getString(8);
				final String compression = rset.getString(9);

				IIndexPartition part = new IndexPartition();
				part.setName(partName);

				PartitionPhysicalProperties props = new PartitionPhysicalProperties();
				props.setTablespaceName(tablespace);
				props.setAttribute(PhysicalAttribute.PCT_FREE, pctFree);
				// if(pctInc>0)
				// props.setAttribute(PhysicalAttribute.PCT_USED, pctInc);
				props.setAttribute(PhysicalAttribute.INIT_TRANS, initTrans);
				props.setAttribute(PhysicalAttribute.MAX_TRANS, maxTrans);
				props.setLogging("YES".equals(logging)); //$NON-NLS-1$
				props.setCompressed("ENABLED".equals(compression)); //$NON-NLS-1$
				props.setPartitioningMethod(PartitioningMethod.NONE);
				part.setPhysicalProperties(props);
				indParts.add(part);
			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Indexes][Partitions] fetching time: "
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
		} finally {
			CaptureHelper.safeClose(rset, null);
		}

		return indParts;
	}

	@Override
	public Collection<ISynonym> getSynonyms(ICaptureContext context, IProgressMonitor m) {
		Collection<ISynonym> synonyms = new ArrayList<ISynonym>();
		final Connection conn = (Connection)context.getConnectionObject();
		final IProgressMonitor monitor = new CustomProgressMonitor(m, PROGRESS_RANGE);
		Statement stmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			stmt = conn.createStatement();

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			/*
			 * Public synonyms are not visible in the USER_SYNONYMS view because a public synonym is
			 * owned by the special user group PUBLIC. The ALL_SYNONYMS view describes the synonyms
			 * accessible to the current user, including public synonyms. Using this view, we can
			 * select all public synonyms that point to local objects, but those synonyms have not
			 * necessarily been created by the current user. The only way to retrieve the creator of
			 * a public synonym is to query the PUBLICSYN view which includes a CREATOR field. But
			 * this view is owned by the SYS user, and is not accessible without specific SELECT
			 * grant.
			 */
			rset = stmt.executeQuery("SELECT DECODE(owner, 'PUBLIC', 1, 0) is_public, synonym_name, " //$NON-NLS-1$
					+ "  DECODE(table_owner, USER, NULL, table_owner) schema_name, table_name, db_link " //$NON-NLS-1$
					+ "FROM all_synonyms WHERE owner = USER " //$NON-NLS-1$
					+ "  OR (owner = 'PUBLIC' AND table_owner = USER)"); //$NON-NLS-1$
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Synonyms] query time: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-2$

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			while (rset.next()) {
				monitor.worked(1);
				final boolean isPublic = rset.getBoolean(1);
				final String synName = rset.getString(2);
				final String refDbObjSchemaName = rset.getString(3);
				final String refDbObjName = rset.getString(4);
				final String refDbObjDbLinkName = rset.getString(5);

				IVersionable<ISynonym> vSynonym = VersionableFactory.createVersionable(ISynonym.class);

				/*
				 * Since we are in the OracleSchemaCapturer, the vendor context has been set to
				 * ORACLE and we can safely cast the previously created ISynonym instance to an
				 * IOracleSynonym.
				 */
				IOracleSynonym oraSynonym = (IOracleSynonym)vSynonym.getVersionnedObject().getModel();
				oraSynonym.setName(synName);
				oraSynonym.setRefDbObjSchemaName(refDbObjSchemaName);
				oraSynonym.setRefDbObjName(refDbObjName);
				oraSynonym.setRefDbObjDbLinkName(refDbObjDbLinkName);
				oraSynonym.setPublic(isPublic);

				synonyms.add(oraSynonym);
			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Synonyms] fetching time: " + (System.currentTimeMillis() - start)
						+ "ms"); //$NON-NLS-1$
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return synonyms;
	}

	@Override
	public Collection<ISequence> getSequences(ICaptureContext context, IProgressMonitor m) {
		Collection<ISequence> sequences = new ArrayList<ISequence>();
		final Connection conn = (Connection)context.getConnectionObject();
		final IProgressMonitor monitor = new CustomProgressMonitor(m, PROGRESS_RANGE);
		monitor.subTask("Retrieving sequences...");
		Statement stmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			stmt = conn.createStatement();

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			rset = stmt.executeQuery("SELECT sequence_name, min_value, max_value, increment_by, cycle_flag, order_flag, cache_size, last_number " //$NON-NLS-1$
					+ "FROM user_sequences"); //$NON-NLS-1$
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Sequences] query time: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-2$

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			while (rset.next()) {
				monitor.worked(1);
				final String name = rset.getString(1);
				final BigDecimal min = rset.getBigDecimal(2);
				final BigDecimal max = rset.getBigDecimal(3);
				final Long inc = rset.getLong(4);
				final String cycle = rset.getString(5);
				final String order = rset.getString(6);
				final int cacheSize = rset.getInt(7);
				final BigDecimal seqStart = rset.getBigDecimal(8);

				IVersionable<ISequence> v = VersionableFactory.createVersionable(ISequence.class);
				ISequence seq = v.getVersionnedObject().getModel();
				seq.setName(name);
				seq.setMinValue(min);
				seq.setStart(seqStart);
				seq.setMaxValue(max);
				seq.setIncrement(inc);
				seq.setCycle("Y".equals(cycle)); //$NON-NLS-1$
				seq.setCached(cacheSize > 0);
				seq.setCacheSize(cacheSize);
				seq.setOrdered("Y".equals(order)); //$NON-NLS-1$
				sequences.add(seq);
			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Sequences] fetching time: " + (System.currentTimeMillis() - start)
						+ "ms"); //$NON-NLS-1$
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return sequences;
	}

	/**
	 * Retrieves all user packages defined in the provided database connection. A collection of all
	 * versionable packages will be returned. An empty collection will be returned if no package
	 * exists in the provided database connection.
	 * 
	 * @param conn database connection from which packages should be retrieved
	 * @return a collection of all existing packages
	 * @throws SQLException if any error occurred while connecting with database
	 */
	private Collection<IVersionable<IPackage>> getPackages(Connection conn, IProgressMonitor monitor) {
		Collection<IVersionable<IPackage>> packages = new ArrayList<IVersionable<IPackage>>();
		monitor.subTask("Retrieving packages...");
		Statement stmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			stmt = conn.createStatement();

			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT name, type, text " //$NON-NLS-1$
						+ "FROM user_source " //$NON-NLS-1$
						+ "WHERE type IN ('PACKAGE', 'PACKAGE BODY') " //$NON-NLS-1$
						+ "ORDER BY name, type, line"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Packages][PLSQL] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				IPackage currentPackage = null;
				boolean isWrapped = false;
				boolean insideBody = false;
				// final String NEWLINE= System.getProperty("line.separator");
				while (rset.next()) {
					monitor.worked(1);
					final String name = rset.getString(1);
					final String type = rset.getString(2);
					final String text = rset.getString(3);

					if (currentPackage == null || !name.equals(currentPackage.getName())) {
						if (currentPackage != null) {
							if (currentPackage.getSpecSourceCode() == null) {
								LOGGER.warn("Skipping package '" + currentPackage.getName()
										+ "': Spec not found.");
								packages.remove(currentPackage);
							}
							if (currentPackage.getBodySourceCode() == null) {
								currentPackage.setBodySourceCode(""); //$NON-NLS-1$
							}
						}
						insideBody = false;
						if (!isWrapped) {
							// postPackageCleanUp(currentPackage);
						}
						currentPackage = VersionableFactory.createVersionable(IPackage.class).getVersionnedObject().getModel();
						currentPackage.setName(name);
						isWrapped = text.contains("wrapped"); //$NON-NLS-1$
						packages.add(VersionHelper.getVersionable(currentPackage));
					}
					if ("PACKAGE".equals(type)) { //$NON-NLS-1$
						// text = removeChar(text,'\n');
						// text = removeChar(text,'\r');
						currentPackage.setSpecSourceCode(currentPackage.getSpecSourceCode() == null ? text
								: (currentPackage.getSpecSourceCode() + text));
					} else if ("PACKAGE BODY".equals(type)) { //$NON-NLS-1$
						if (!insideBody) {
							isWrapped = text.contains("wrapped"); //$NON-NLS-1$
							insideBody = true;
						}
						if (!isWrapped) {
							// text = removeChar(text,'\n');
							// text = removeChar(text,'\r');
							currentPackage.setBodySourceCode(currentPackage.getBodySourceCode() == null ? text
									: (currentPackage.getBodySourceCode() + text));
						} else {
							currentPackage.setBodySourceCode(currentPackage.getBodySourceCode() == null ? text
									: (currentPackage.getBodySourceCode() + text));
						}
					}
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Packages][PLSQL] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (currentPackage != null) {
					if (currentPackage.getBodySourceCode() == null
							|| currentPackage.getSpecSourceCode() == null) {
						LOGGER.warn("Skipping package '" + currentPackage.getName()
								+ "': Spec or body not found.");
						packages.remove(currentPackage);
					}
				}
				if (currentPackage != null && !isWrapped) {
					// postPackageCleanUp(currentPackage);
				}
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			// FIXME [BGA] What is the purpose of this block of code since
			// retrieved Java procedure
			// are not put in the returned collection of this method?

			// Retrieving java source
			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT name, text " //$NON-NLS-1$
						+ "FROM user_source " //$NON-NLS-1$
						+ "WHERE type = 'JAVA SOURCE' " //$NON-NLS-1$
						+ "ORDER BY name, line"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled()) LOGGER.debug("[Packages][Java] query time: " //$NON-NLS-1$
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				IProcedure currentProc = null;
				while (rset.next()) {
					monitor.worked(1);
					final String name = rset.getString(1);
					final String line = rset.getString(2);

					if (currentProc == null || !name.equals(currentProc.getName())) {
						IVersionable<IProcedure> v = VersionableFactory.createVersionable(IProcedure.class);
						currentProc = v.getVersionnedObject().getModel();
						currentProc.setName(name);
						currentProc.setLanguageType(LanguageType.JAVA);
						currentProc.setSQLSource(line);
					} else {
						currentProc.setSQLSource(currentProc.getSQLSource() + line);
					}
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Packages][Java] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							DBVendor.ORACLE.toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(null, stmt);
		}

		return packages;
	}

	@Override
	public Collection<IProcedure> getProcedures(ICaptureContext context, IProgressMonitor m) {
		Collection<IProcedure> procedures = new ArrayList<IProcedure>();
		final Connection conn = (Connection)context.getConnectionObject();
		final IProgressMonitor monitor = new CustomProgressMonitor(m, PROGRESS_RANGE);
		monitor.subTask("Retrieving procedures...");
		Statement stmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			stmt = conn.createStatement();

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			/*
			 * FIXME [BGA] This query cannot retrieve the return type of functions with compilation
			 * errors. We may have to parse the source for these functions to retrieve the return
			 * type.
			 */
			rset = stmt.executeQuery("SELECT s.name, s.text, s.type, a.data_type " //$NON-NLS-1$
					+ "FROM user_source s " //$NON-NLS-1$
					+ "  LEFT JOIN user_arguments a " //$NON-NLS-1$
					+ "    ON a.object_name = s.name AND s.type = 'FUNCTION' AND a.in_out = 'OUT' AND a.position=0 " //$NON-NLS-1$
					+ "WHERE s.type IN ('JAVA SOURCE', 'PROCEDURE', 'FUNCTION') " //$NON-NLS-1$
					+ "ORDER BY s.name, s.line"); //$NON-NLS-1$
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Procedures] query time: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-2$

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			IProcedure currentProc = null;
			while (rset.next()) {
				monitor.worked(1);
				final String name = rset.getString(1);
				final String line = rset.getString(2);
				final String type = rset.getString(3);
				final String returnType = rset.getString(4);

				if (currentProc == null || !name.equals(currentProc.getName())) {
					IVersionable<IProcedure> v = VersionableFactory.createVersionable(IProcedure.class);
					currentProc = v.getVersionnedObject().getModel();
					currentProc.setName(name);
					if (type != null && type.startsWith("JAVA")) { //$NON-NLS-1$
						currentProc.setLanguageType(LanguageType.JAVA);
					} else {
						currentProc.setLanguageType(LanguageType.STANDARD);
					}
					currentProc.setSQLSource(line);
					// The return type of a function is null when the function
					// has compilation
					// errors.
					if (returnType != null) {
						currentProc.setReturnType(new Datatype(returnType));
					}
					procedures.add(currentProc);
				} else {
					currentProc.setSQLSource(currentProc.getSQLSource() + line);
				}
			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Procedures] fetching time: " + (System.currentTimeMillis() - start)
						+ "ms"); //$NON-NLS-1$
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return procedures;
	}

	@Override
	public Collection<IUserType> getUserTypes(ICaptureContext context, IProgressMonitor m) {
		Collection<IUserType> types = new ArrayList<IUserType>();
		final Connection conn = (Connection)context.getConnectionObject();
		final IProgressMonitor monitor = new CustomProgressMonitor(m, PROGRESS_RANGE);
		Map<String, IOracleUserType> typesMap = new HashMap<String, IOracleUserType>();
		monitor.subTask("Retrieving user types...");
		Statement stmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			stmt = conn.createStatement();
			monitor.subTask("Retrieving user-defined types...");

			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("select type_name, attr_name, attr_type_name, decode(attr_type_name,'NUMBER',precision,length), scale " //$NON-NLS-1$
						+ "from user_type_attrs " + "order by type_name,attr_no"); //$NON-NLS-1$ //$NON-NLS-2$
				if (LOGGER.isDebugEnabled()) LOGGER.debug("[User types][Attributes] query time: " //$NON-NLS-1$
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				IUserType currentType = null;
				while (rset.next()) {
					monitor.worked(1);
					final String name = rset.getString(1);
					final String colName = rset.getString(2);
					String colType = rset.getString(3);
					int colLength = rset.getInt(4);
					// int colPrecision = rset.getInt(5);
					int colScale = rset.getInt(5);

					// Altering datatype (bug for Timestamps containing
					// "(scale)")
					if (colType.startsWith("TIMESTAMP")) { //$NON-NLS-1$
						colType = "TIMESTAMP"; //$NON-NLS-1$
						colLength = 0;
						colScale = 0;
					}
					if (colType.startsWith("CLOB") || colType.startsWith("DATE")) { //$NON-NLS-1$ //$NON-NLS-2$
						colLength = 0;
					}
					if (currentType == null || !name.equals(currentType.getName())) {
						IVersionable<IUserType> versionable = VersionableFactory.createVersionable(IUserType.class);
						currentType = versionable.getVersionnedObject().getModel();
						currentType.setName(name);
						types.add(currentType);
						typesMap.put(name, (IOracleUserType)currentType);
					}
					ITypeColumn col = new TypeColumn();
					col.setName(colName);
					col.setDatatype(new Datatype(colType, colLength == 0 ? -1 : colLength,
							colScale == 0 ? -1 : colScale));
					currentType.addColumn(col);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[User types][Attributes] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
				}
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("SELECT name, text " //$NON-NLS-1$
						+ "FROM user_source " //$NON-NLS-1$
						+ "WHERE type = 'TYPE BODY' " //$NON-NLS-1$
						+ "ORDER BY name, line"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled()) LOGGER.debug("[User types][Body source] query time: " //$NON-NLS-1$
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				IOracleUserType current = null;
				String nameToSkip = null;
				while (rset.next()) {
					monitor.worked(1);
					final String name = rset.getString(1);
					final String line = rset.getString(2);

					// If we are skipping a type, we cycle.
					if (name.equals(nameToSkip)) {
						continue;
					}
					if (current == null || !current.getName().equals(name)) {
						current = typesMap.get(name);
						if (current == null) {
							LOGGER.warn(MessageFormat.format(
									OracleMessages.getString("cantLoadTypeBodyWithoutSpec"), name)); //$NON-NLS-1$
							nameToSkip = name;
						} else {
							current.setTypeBody(line);
						}
					} else {
						current.setTypeBody(current.getTypeBody() + line);
					}
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[User types][Body source] fetching time: " //$NON-NLS-1$
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return types;
	}

	/**
	 * Retrieves all user collections defined in the provided database connection. A collection of
	 * all versionable collection types will be returned. An empty collection will be returned if no
	 * collection type exists in the provided database connection.
	 * 
	 * @param conn database connection from which user collection types should be retrieved
	 * @return a collection of all defined user collection types
	 * @throws SQLException if any error occurred while connecting with database
	 */
	private Collection<IVersionable<IUserCollection>> getUserCollections(Connection conn,
			IProgressMonitor monitor) {
		Collection<IVersionable<IUserCollection>> collections = new ArrayList<IVersionable<IUserCollection>>();
		monitor.subTask("Retrieving collection types...");
		Statement stmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			stmt = conn.createStatement();
			monitor.subTask("Retrieving user-defined collections...");
			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			rset = stmt.executeQuery("SELECT type_name, coll_type, elem_type_name, " //$NON-NLS-1$
					+ "DECODE(elem_type_name, 'NUMBER', precision, length), scale, upper_bound " //$NON-NLS-1$
					+ "FROM user_coll_types " //$NON-NLS-1$
					+ "ORDER BY type_name"); //$NON-NLS-1$
			if (LOGGER.isDebugEnabled()) LOGGER.debug("[User collections] query time: " //$NON-NLS-1$
					+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

			if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
			IUserCollection currentCol = null;
			while (rset.next()) {
				monitor.worked(1);
				final String name = rset.getString(1);
				final String collectionTyp = rset.getString(2);
				String colType = rset.getString(3);
				int colLength = rset.getInt(4);
				int colScale = rset.getInt(5);
				final int upperBound = rset.getInt(6);

				// Altering datatype (bug for Timestamps containing "(scale)")
				if (colType.startsWith("TIMESTAMP")) { //$NON-NLS-1$
					colType = "TIMESTAMP"; //$NON-NLS-1$
					colLength = 0;
					colScale = 0;
				}
				if (colType.startsWith("CLOB") || colType.startsWith("DATE")) { //$NON-NLS-1$ //$NON-NLS-2$
					colLength = 0;
				}
				// Creating new user collection
				IVersionable<IUserCollection> versionable = VersionableFactory.createVersionable(IUserCollection.class);
				currentCol = versionable.getVersionnedObject().getModel();
				currentCol.setName(name);
				currentCol.setDatatype(new Datatype(colType, colLength == 0 ? -1 : colLength,
						colScale == 0 ? -1 : colScale));
				if ("TABLE".equals(collectionTyp)) { //$NON-NLS-1$
					currentCol.setCollectionType(CollectionType.NESTED_TABLE);
				} else {
					currentCol.setCollectionType(CollectionType.VARRAY);
					currentCol.setSize(upperBound);
				}
				collections.add(versionable);
			}
			if (LOGGER.isDebugEnabled()) LOGGER.debug("[User collections] fetching time: " //$NON-NLS-1$
					+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							DBVendor.ORACLE.toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return collections;
	}

	/**
	 * Retrieves the prefix of a string by extracting any prefixed string followed by "_"
	 * 
	 * @param name the prefix will be extracted from this string
	 * @return the string prefix or "" if none
	 */
	public String getPrefix(String name) {
		if (name.indexOf("_") > 0) { //$NON-NLS-1$
			String prefix = name.substring(0, name.indexOf("_")); //$NON-NLS-1$
			if (prefix != null && !prefix.equals(name) && prefix.length() > 0) {
				return prefix;
			}
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public Collection<IDatabaseObject<?>> getVendorSpecificDbObjects(ICaptureContext context,
			IProgressMonitor monitor) {
		Collection<IDatabaseObject<?>> specificObjects = new ArrayList<IDatabaseObject<?>>();
		Collection<IVersionable<IPackage>> packages = getPackages(
				(Connection)context.getConnectionObject(), monitor);
		Collection<IVersionable<IUserCollection>> userCollections = getUserCollections(
				(Connection)context.getConnectionObject(), monitor);

		specificObjects.addAll(clusters);
		specificObjects.addAll(mviewLogs.values());
		specificObjects.addAll((Collection<? extends IDatabaseObject<?>>)packages);
		specificObjects.addAll((Collection<? extends IDatabaseObject<?>>)userCollections);

		return specificObjects;
	}

	/**
	 * @see com.nextep.designer.sqlgen.model.base.AbstractDatabaseConnector#postCaptureObjects(java.util.Collection)
	 */
	protected void postCaptureObjects(Collection<IVersionable<?>> objects) {
		// Do nothing
	}

	private void initPreparedStatements(Connection conn) throws SQLException {
		tabPartStmt = conn.prepareStatement("select partition_name,high_value,tablespace_name,pct_free,pct_used,ini_trans,max_trans,logging,compression,pt.partitioning_type " //$NON-NLS-1$
				+ "from user_tab_partitions p, user_part_tables pt " //$NON-NLS-1$
				+ "where p.table_name=? and pt.table_name=p.table_name " //$NON-NLS-1$
				+ "order by partition_position"); //$NON-NLS-1$
		indPartStmt = conn.prepareStatement("select partition_name,null,tablespace_name,pct_free,pct_increase,ini_trans,max_trans,logging,compression " //$NON-NLS-1$
				+ "from user_ind_partitions " //$NON-NLS-1$
				+ "where index_name =? " //$NON-NLS-1$
				+ "order by partition_position"); //$NON-NLS-1$
	}

	private void closePreparedStatements() {
		CaptureHelper.safeClose(null, tabPartStmt);
		CaptureHelper.safeClose(null, indPartStmt);
	}

	@Override
	public Collection<ErrorInfo> getDatabaseErrors(ICaptureContext context) {
		final Collection<ErrorInfo> errors = new ArrayList<ErrorInfo>();
		final Connection conn = (Connection)context.getConnectionObject();
		Statement stmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			stmt = conn.createStatement();

			try {
				try {
					if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
					// This statement only works for Oracle 10g and above
					rset = stmt.executeQuery("select name, type, text, line, position, attribute " //$NON-NLS-1$
							+ "from user_errors"); //$NON-NLS-1$
				} catch (SQLException e) {
					if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
					// Trying a 9i compatible query
					rset = stmt.executeQuery("select name, type, text, line, position, 'ERROR' " //$NON-NLS-1$
							+ "from user_errors "); //$NON-NLS-1$
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Errors][Compiled objects] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				while (rset.next()) {
					final String name = rset.getString(1);
					final String type = rset.getString(2);
					final String text = rset.getString(3);
					final int line = rset.getInt(4);
					final int pos = rset.getInt(5);
					String attr = null;
					try {
						attr = rset.getString(6);
					} catch (SQLException e) {
						// OK : 9i fall here
					}
					errors.add(new ErrorInfo(name, type, text, line, pos, attr));
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Errors][Compiled objects] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}

			try {
				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				rset = stmt.executeQuery("select object_name, object_type, 'Object has non-valid status: '||status, 1, 1 " //$NON-NLS-1$
						+ "from user_objects " + "where status!='VALID'"); //$NON-NLS-1$ //$NON-NLS-2$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Errors][Objects status] query time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

				if (LOGGER.isDebugEnabled()) start = System.currentTimeMillis();
				while (rset.next()) {
					final String name = rset.getString(1);
					final String type = rset.getString(2);
					final String text = rset.getString(3);
					final int line = rset.getInt(4);
					final int pos = rset.getInt(5);
					errors.add(new ErrorInfo(name, type, text, line, pos, "ERROR")); //$NON-NLS-1$
				}
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("[Errors][Objects status] fetching time: "
							+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
			} finally {
				CaptureHelper.safeClose(rset, null);
			}
		} catch (SQLException e) {
			throw new ErrorException("Problems retrieving errors from database", e);
		} finally {
			CaptureHelper.safeClose(null, stmt);
		}
		return errors;
	}
}
