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
package com.nextep.designer.sqlgen.mysql.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.MergeStrategy;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.CustomProgressMonitor;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.dbgm.mysql.impl.MySQLColumn;
import com.nextep.designer.dbgm.mysql.model.IMySQLColumn;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;
import com.nextep.designer.dbgm.mysql.services.IMySqlModelService;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.ICaptureContext;
import com.nextep.designer.sqlgen.model.ICapturer;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;
import com.nextep.designer.sqlgen.model.base.AbstractCapturer;
import com.nextep.designer.sqlgen.mysql.MySQLMessages;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MySqlCapturer extends AbstractCapturer {

	private static final Log LOGGER = LogFactory.getLog(MySqlCapturer.class);

	private ICapturer jdbcCapturer;

	@Override
	public void initialize(IConnection conn, IMutableCaptureContext context) {
		super.initialize(conn, context);
		jdbcCapturer = SQLGenPlugin.getService(ICaptureService.class).getCapturer(DBVendor.JDBC);
	}

	@Override
	public Collection<IBasicTable> getTables(ICaptureContext context, IProgressMonitor m) {
		final Map<String, IBasicColumn> columnsMap = new HashMap<String, IBasicColumn>();
		final Map<String, IKeyConstraint> keysMap = new HashMap<String, IKeyConstraint>();
		Connection conn = (Connection) context.getConnectionObject();
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100, true);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("flush tables"); //$NON-NLS-1$
		} catch (SQLException e) {
			LOGGER.error("Unable to flush tables : " + e.getMessage(), e); //$NON-NLS-1$
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					LOGGER.error("Unable to close statement : " + e.getMessage(), e); //$NON-NLS-1$
				}
			}
		}
		try {
			// Creating statement to retrieve tables
			final DatabaseMetaData md = conn.getMetaData();
			final Map<String, IBasicTable> tablesMap = buildTablesMap(conn, md, context, monitor);
			monitor.worked(1);

			// Building columns
			monitor.subTask(MySQLMessages.getString("capturer.mysql.retrievingColumns")); //$NON-NLS-1$
			Map<String, IBasicColumn> localColumnsMap = buildColumnsMap(conn, md, context, monitor,
					tablesMap);
			columnsMap.putAll(localColumnsMap);
			monitor.worked(1);
			// Temporary storing columns into a map, hashed by
			// table_name.column_name
			for (String tabName : tablesMap.keySet()) {
				IBasicTable t = tablesMap.get(tabName);

				// Building unique keys
				monitor.subTask(MessageFormat.format(
						MySQLMessages.getString("capturer.mysql.retrievingPKs"), //$NON-NLS-1$
						tabName));
				Map<String, IKeyConstraint> localKeysMap = buildUniqueKeyMap(md, monitor, t,
						columnsMap);
				keysMap.putAll(localKeysMap);
			}

			// We iterate foreign keys in a specific loop to make sure all
			// unique keys are here
			monitor.worked(1);
			monitor.subTask(MySQLMessages.getString("capturer.mysql.retrievingFKs")); //$NON-NLS-1$
			for (String tabName : tablesMap.keySet()) {
				monitor.worked(1);
				IBasicTable t = tablesMap.get(tabName);
				fillForeignKeys(md, monitor, t, keysMap, columnsMap);
			}
			monitor.worked(1);
			return tablesMap.values();
		} catch (SQLException e) {
			LOGGER.warn(MessageFormat.format(
					MySQLMessages.getString("capturer.mysql.fetchTablesError"), //$NON-NLS-1$
					e.getMessage()), e);
		}
		return Collections.emptyList();
	}

	@Override
	public Collection<IIndex> getIndexes(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100, true);
		monitor.subTask(MySQLMessages.getString("capturer.mysql.retrievingIndexes")); //$NON-NLS-1$
		final Connection conn = (Connection) context.getConnectionObject();
		Collection<IIndex> indexes = Collections.emptyList();

		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			indexes = jdbcCapturer.getIndexes(context, monitor);
			final Collection<IBasicTable> indexedTables = new HashSet<IBasicTable>();
			final Map<String, IIndex> indexMap = new HashMap<String, IIndex>();

			for (IIndex index : new ArrayList<IIndex>(indexes)) {
				final IBasicTable t = index.getIndexedTable();
				// Eliminating PRIMARY named index (name based, it seems that is
				// is the way MySql
				// makes the difference, a bit crappy)
				if ("PRIMARY".equals(index.getIndexName())) { //$NON-NLS-1$
					indexes.remove(index);
				} else {
					indexedTables.add(t);
					final String indexName = CaptureHelper.getUniqueIndexName(index);
					indexMap.put(indexName, index);
				}
			}

			for (IBasicTable table : indexedTables) {
				final String tabName = table.getName();
				// Getting Mysql specific information that we could not get
				// elsewhere
				try {
					rset = stmt.executeQuery("show index from `" + tabName + "`"); //$NON-NLS-1$ //$NON-NLS-2$
					while (rset.next()) {
						final String prefixLength = rset.getString("Sub_part"); //$NON-NLS-1$
						final String indexType = rset.getString("Index_type"); //$NON-NLS-1$
						final String columnName = rset.getString("Column_name"); //$NON-NLS-1$
						final String indexName = rset.getString("Key_name"); //$NON-NLS-1$
						final String indexUniqueName = CaptureHelper.getUniqueObjectName(tabName,
								indexName);
						final IIndex i = indexMap.get(indexUniqueName);
						if (i instanceof IMySQLIndex) {
							final IMySQLIndex index = (IMySQLIndex) i;
							if (prefixLength != null) {
								final String indexedColName = CaptureHelper.getUniqueObjectName(
										tabName, columnName);
								final IBasicColumn c = (IBasicColumn) context.getCapturedObject(
										IElementType.getInstance(IBasicColumn.TYPE_ID),
										indexedColName);
								if (c != null && index != null) {
									index.setColumnPrefixLength(c.getReference(),
											Integer.valueOf(prefixLength));
								}
							}
							if (!"BTREE".equals(indexType)) { //$NON-NLS-1$
								if (index != null) {
									index.setIndexType(IndexType.valueOf(indexType));
								}
							}
						}
					}
				} finally {
					CaptureHelper.safeClose(rset, null);
				}
			}

		} catch (SQLException e) {
			LOGGER.warn(MessageFormat.format(
					MySQLMessages.getString("capturer.mysql.fetchIndexesError"), e.getMessage()), //$NON-NLS-1$
					e);
		} finally {
			CaptureHelper.safeClose(null, stmt);
		}
		monitor.worked(1);

		return indexes;
	}

	@Override
	public Collection<IView> getViews(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100);
		final Collection<IView> views = new ArrayList<IView>();
		monitor.subTask(MySQLMessages.getString("capturer.mysql.retrievingViews")); //$NON-NLS-1$
		final Connection conn = (Connection) context.getConnectionObject();

		ResultSet rset = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rset = stmt
					.executeQuery("select table_name,view_definition from information_schema.views where table_schema='" //$NON-NLS-1$
							+ context.getSchema() + "'"); //$NON-NLS-1$
			while (rset.next()) {
				monitor.worked(1);
				final String name = rset.getString("table_name"); //$NON-NLS-1$
				final String sql = rset.getString("view_definition"); //$NON-NLS-1$

				IVersionable<IView> view = VersionableFactory.createVersionable(IView.class,
						DBVendor.MYSQL);
				IView v = view.getVersionnedObject().getModel();
				v.setName(name);
				v.setSQLDefinition(cleanViewSQL(context, sql));
				views.add(v);
			}
		} catch (SQLException e) {
			LOGGER.warn(MessageFormat.format(
					MySQLMessages.getString("capturer.mysql.fetchViewsError"), e.getMessage()), e); //$NON-NLS-1$
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return views;
	}

	/**
	 * Cleans the original MySQL view source code.
	 * 
	 * @param originalSource
	 *            SQL code as read from DB
	 * @return the cleans SQL code which may be imported
	 */
	private String cleanViewSQL(ICaptureContext context, String originalSource) {
		// Removing the schema prefix
		String s = originalSource.replace("`" + context.getSchema().toLowerCase() + "`.", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		s = s.replace("`" + context.getSchema() + "`.", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// Removing all name encapsulation
		s = s.replace("`", ""); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("_(latin1|latin2|ascii|cp125.|utf8|macce)'", "'"); //$NON-NLS-1$ //$NON-NLS-2$
		// Removing multiline comments (regexp generate stack overflow
		int index = s.indexOf("/*"); //$NON-NLS-1$
		while (index != -1) {
			int end = s.indexOf("*/", index + 2); //$NON-NLS-1$
			s = s.substring(0, index) + ((end == -1) ? "" : s.substring(end + 2)); //$NON-NLS-1$

			index = s.indexOf("/*"); //$NON-NLS-1$
		}
		return s.trim();
	}

	@Override
	public Collection<ITrigger> getTriggers(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100);
		final Collection<ITrigger> triggers = new ArrayList<ITrigger>();
		monitor.subTask(MySQLMessages.getString("capturer.mysql.retrievingTriggers")); //$NON-NLS-1$
		final Connection conn = (Connection) context.getConnectionObject();

		ResultSet rset = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SHOW TRIGGERS"); //$NON-NLS-1$
			while (rset.next()) {
				monitor.worked(1);
				String name = rset.getString("Trigger"); //$NON-NLS-1$
				String event = rset.getString("Event"); //$NON-NLS-1$
				String triggerTabName = rset.getString("Table"); //$NON-NLS-1$
				String sql = rset.getString("Statement"); //$NON-NLS-1$
				String timing = rset.getString("Timing"); //$NON-NLS-1$

				IVersionable<ITrigger> trigger = VersionableFactory.createVersionable(
						ITrigger.class, DBVendor.MYSQL);
				ITrigger trig = trigger.getVersionnedObject().getModel();
				trig.setName(name);
				try {
					trig.setTime(TriggerTime.valueOf(timing));
					trig.addEvent(TriggerEvent.valueOf(event));
				} catch (RuntimeException e) {
					LOGGER.warn(MessageFormat.format(
							MySQLMessages.getString("capturer.mysql.unsupportedTriggerType"), name)); //$NON-NLS-1$
					continue;
				}
				trig.setTriggableRef(context.getTable(triggerTabName).getReference());
				trig.setCustom(false);
				trig.setSourceCode(DBGMHelper.trimEmptyLines(sql));
				triggers.add(trig);
			}
		} catch (SQLException e) {
			LOGGER.warn(MessageFormat.format(
					MySQLMessages.getString("capturer.mysql.FetchTriggersError"), e.getMessage()), //$NON-NLS-1$
					e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return triggers;
	}

	@Override
	public Collection<IProcedure> getProcedures(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100);
		Collection<IProcedure> procedures = new ArrayList<IProcedure>();
		monitor.subTask(MySQLMessages.getString("capturer.mysql.retrievingProcs")); //$NON-NLS-1$
		final Connection conn = (Connection) context.getConnectionObject();
		ResultSet rset = null;
		Statement stmt = null;

		try {
			// Creating statement to retrieve tables
			stmt = conn.createStatement();
			rset = stmt
					.executeQuery("select name,body,param_list,returns,is_deterministic,sql_data_access " //$NON-NLS-1$
							+ "from mysql.proc " //$NON-NLS-1$
							+ "where upper(db)='" //$NON-NLS-1$
							+ context.getSchema().toUpperCase() + "'"); //$NON-NLS-1$
			IProcedure currentProc = null;
			while (rset.next()) {
				monitor.worked(1);
				final String name = rset.getString(1);
				final String body = rset.getString(2);
				final String params = rset.getString(3);
				final String retCode = rset.getString(4);
				final boolean deterministic = "YES".equals(rset.getString(5)); //$NON-NLS-1$
				final String dataAccess = rset.getString(6);
				if (currentProc == null || !name.equals(currentProc.getName())) {
					IVersionable<IProcedure> v = VersionableFactory.createVersionable(
							IProcedure.class, DBVendor.MYSQL);
					currentProc = v.getVersionnedObject().getModel();
					currentProc.setName(name);
					currentProc.setLanguageType(LanguageType.STANDARD);
					final StringBuffer buf = new StringBuffer(300);
					buf.append("create " //$NON-NLS-1$
							+ (retCode == null || retCode.trim().isEmpty() ? "procedure " //$NON-NLS-1$
									: "function ") + name); //$NON-NLS-1$
					buf.append(params == null || params.trim().isEmpty() ? "()" : "(" + params //$NON-NLS-1$ //$NON-NLS-2$
							+ ")"); //$NON-NLS-1$
					buf.append((retCode == null || retCode.trim().isEmpty() ? "" : " returns " //$NON-NLS-1$ //$NON-NLS-2$
							+ retCode) + "\n"); //$NON-NLS-1$
					if (dataAccess != null && !dataAccess.startsWith("CONTAINS")) { //$NON-NLS-1$
						buf.append("READS SQL DATA\n"); //$NON-NLS-1$
					}
					if (deterministic) {
						buf.append("DETERMINISTIC" + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					buf.append(body + ";"); //$NON-NLS-1$
					currentProc.setSQLSource(buf.toString());
					currentProc.setReturnType(new Datatype(retCode));
					procedures.add(currentProc);
				}
			}
		} catch (SQLException e) {
			LOGGER.warn(MySQLMessages.getString("capturer.mysql.mysqlGrantNeeded")); //$NON-NLS-1$
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return procedures;
	}

	/**
	 * Builds the table map of new {@link IBasicTable} instances fetched from
	 * the database and hashed by names
	 * 
	 * @param conn
	 *            {@link Connection} to the database
	 * @param context
	 *            current {@link ICaptureContext}
	 * @param monitor
	 *            current {@link IProgressMonitor} to report progress
	 * @return a map of {@link IBasicTable} hashed by name
	 * @throws SQLException
	 */
	private Map<String, IBasicTable> buildTablesMap(Connection conn, DatabaseMetaData md,
			ICaptureContext context, IProgressMonitor monitor) throws SQLException {
		final Map<String, IBasicTable> tablesMap = new HashMap<String, IBasicTable>();
		ResultSet rset = null;
		Statement stmt = null;
		try {
			// TODO change location of caseSensitive... put quickly in
			// MergeStrategy
			MergeStrategy.setCaseSensitive(!md.storesLowerCaseIdentifiers()
					&& !md.storesUpperCaseIdentifiers());

			monitor.subTask(MySQLMessages.getString("capturer.mysql.capturingTables")); //$NON-NLS-1$
			stmt = conn.createStatement();
			rset = stmt
					.executeQuery("select t.table_name, t.table_comment, t.engine, c.character_set_name, c.collation_name from " //$NON-NLS-1$
							+ "information_schema.tables t, information_schema.collations c where t.table_schema='" //$NON-NLS-1$
							+ context.getSchema() + "' and t.table_type='BASE TABLE' and " //$NON-NLS-1$
							+ "c.collation_name=t.table_collation order by t.table_name"); //$NON-NLS-1$

			monitor.worked(1);
			// Fetching results and temporarily store them in the map
			while (rset.next()) {
				monitor.worked(10);

				final String tableName = rset.getString(1);
				final String tableComments = rset.getString(2);
				final String engine = rset.getString(3);
				final String charset = rset.getString(4);
				final String collation = rset.getString(5);

				IVersionable<?> v = VersionableFactory.createVersionable(IBasicTable.class,
						DBVendor.MYSQL);
				IMySQLTable t = (IMySQLTable) v.getVersionnedObject().getModel();
				t.setName(tableName);
				t.setDescription(tableComments);
				t.setEngine(engine);
				t.setCharacterSet(charset);
				t.setCollation(collation);
				// Registering table
				tablesMap.put(tableName, t);
			}
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return tablesMap;
	}

	/**
	 * Builds the columns map of the given table. This table will be filled with
	 * the new fetched columns.
	 * 
	 * @param conn
	 *            Connection to fetch columns
	 * @param md
	 *            pre-computed {@link DatabaseMetaData}
	 * @param context
	 *            the current {@link ICaptureContext}
	 * @param monitor
	 *            the {@link IProgressMonitor} to report progress to
	 * @param table
	 *            the table to fetch columns for
	 * @return a map of {@link IBasicColumn} hashed by their qualified column
	 *         name
	 * @throws SQLException
	 */
	private Map<String, IBasicColumn> buildColumnsMap(Connection conn, DatabaseMetaData md,
			ICaptureContext context, IProgressMonitor monitor, Map<String, IBasicTable> tablesMap)
			throws SQLException {
		final IMySqlModelService mysqlModelService = CorePlugin
				.getService(IMySqlModelService.class);
		final IDatatypeProvider datatypeProvider = DBGMHelper.getDatatypeProvider(DBVendor.MYSQL);
		final Map<String, IBasicColumn> columnsMap = new HashMap<String, IBasicColumn>();
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn
					.prepareStatement("select TABLE_NAME, COLUMN_NAME, ORDINAL_POSITION, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, IS_NULLABLE, EXTRA, COLUMN_DEFAULT, CHARACTER_SET_NAME, COLLATION_NAME, COLUMN_TYPE, COLUMN_COMMENT "
							+ " from information_schema.columns where table_schema=?");
			stmt.setString(1, context.getSchema());

			rset = stmt.executeQuery();
			while (rset.next()) {
				monitor.worked(1);
				final String tableName = rset.getString("TABLE_NAME"); //$NON-NLS-1$
				final IMySQLTable table = (IMySQLTable) tablesMap.get(tableName);
				if (table == null) {
					continue;
				}
				final String columnName = rset.getString("COLUMN_NAME"); //$NON-NLS-1$
				final int rank = rset.getInt("ORDINAL_POSITION"); //$NON-NLS-1$
				String datatype = rset.getString("DATA_TYPE").toUpperCase(); //$NON-NLS-1$
				int numericLength = rset.getInt("NUMERIC_PRECISION"); //$NON-NLS-1$
				int dataPrecision = rset.getInt("NUMERIC_SCALE"); //$NON-NLS-1$
				BigDecimal dataCharLength = rset.getBigDecimal("CHARACTER_MAXIMUM_LENGTH"); //$NON-NLS-1$
				final boolean nullable = "YES".equals(rset.getString("IS_NULLABLE")); //$NON-NLS-1$ //$NON-NLS-2$
				final boolean autoInc = "auto_increment".equals(rset.getString("EXTRA")); //$NON-NLS-1$ //$NON-NLS-2$
				String dataDefault = rset.getString("COLUMN_DEFAULT"); //$NON-NLS-1$

				final String charset = rset.getString("CHARACTER_SET_NAME"); //$NON-NLS-1$
				final String collation = rset.getString("COLLATION_NAME"); //$NON-NLS-1$
				final String columnType = rset.getString("COLUMN_TYPE"); //$NON-NLS-1$
				final String colComments = rset.getString("COLUMN_COMMENT"); //$NON-NLS-1$
				boolean unsigned = columnType.toLowerCase().indexOf("unsigned") >= 0; //$NON-NLS-1$

				int dataLength = (dataCharLength != null && dataCharLength.intValue() > 0) ? dataCharLength
						.intValue() : numericLength > 0 ? numericLength : 0;
				dataPrecision = Math.max(dataPrecision, 0);
				// Workaround the bloody management of MySQL default values !!
				// TODO: Check for JDBC updates of the mysql driver...
				if ("CURRENT_TIMESTAMP".equalsIgnoreCase(dataDefault)) { //$NON-NLS-1$
					dataDefault = null;
				}
				if (dataDefault != null && !"".equals(dataDefault)) { //$NON-NLS-1$
					// Adding quotes and escaping quotes in default values for
					// string datatypes
					if (datatypeProvider.listStringDatatypes().contains(datatype)) {
						dataDefault = "'" + dataDefault.replace("'", "''") + "'"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
				}

				/*
				 * Since 1.0.6, for every type, we base our capture on the
				 * COLUMN_TYPE information which provides much more accurate
				 * information (mostly on length/precision) and is the field
				 * used by the MySQL "show create table" command.
				 */
				if (datatype.startsWith("ENUM") || datatype.startsWith("SET")) { //$NON-NLS-1$ //$NON-NLS-2$
					// Taking the whole column type
					dataLength = 0;
					dataPrecision = 0;
					datatype = columnType;
				} else {
					if (columnType != null) {
						final String mysqlType = columnType.toUpperCase();
						// Parsing the type as "datatype(length,precision)"
						int ind = mysqlType.indexOf('(');
						// Have we got a size ?
						if (ind > 0) {
							try {
								// Data type is the text before bracket
								datatype = mysqlType.substring(0, ind).toUpperCase();
								int comma = mysqlType.indexOf(',', ind);
								int rightpar = mysqlType.indexOf(')', ind);
								// Have we got a comma ?
								if (comma > 0) {
									// The comma separates length and precision
									dataLength = Integer.parseInt(mysqlType.substring(ind + 1,
											comma));
									dataPrecision = Integer.parseInt(mysqlType.substring(comma + 1,
											rightpar));
								} else {
									// Otherwise we got a 0 precision
									dataLength = Integer.parseInt(mysqlType.substring(ind + 1,
											rightpar));
									dataPrecision = 0;
								}
							} catch (NumberFormatException nfe) {
								// Unknown type definition, resetting everything
								// to 0
								LOGGER.warn("Could not parse the data type [" + columnType
										+ "] of column [" + tableName + "." + columnName + "]: " //$NON-NLS-2$ //$NON-NLS-3$
										+ "data type could be incorrect");
								datatype = columnType;
								dataLength = 0;
								dataPrecision = 0;
							}
						} else {
							// No size definition, resetting everything to 0
							datatype = mysqlType;
							dataLength = 0;
							dataPrecision = 0;
						}
					}
				}

				// Specific behavior when data type name contains the "UNSIGNED"
				// keyword
				if (datatype.indexOf("UNSIGNED") > -1) { //$NON-NLS-1$
					unsigned = true;
					datatype = datatype.replaceAll("UNSIGNED", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				}

				IDatatype d = new Datatype(datatype, dataLength, dataPrecision);
				IMySQLColumn c = new MySQLColumn(columnName, colComments, d, rank - 1);
				// (IBasicColumn)ControllerFactory.getController(IElementType.COLUMN).emptyInstance(t);
				c.setName(columnName);
				c.setDescription(colComments);
				d.setUnsigned(unsigned);
				c.setDatatype(d);
				c.setRank(rank - 1);
				c.setAutoIncremented(autoInc);
				c.setNotNull(!nullable);
				c.setDefaultExpr(dataDefault == null ? "" : dataDefault.trim()); //$NON-NLS-1$

				// Character set management
				final String tabCharset = table.getCharacterSet();
				final String tabCollation = table.getCollation();

				if (charset != null && !charset.equals(tabCharset)) {
					c.setCharacterSet(charset);
				}
				if (collation != null && !collation.equals(tabCollation)
						&& !collation.equals(mysqlModelService.getDefaultCollation(charset))) {
					c.setCollation(collation);
				}
				// TODO Warning: might cause save problems / column list
				// duplicates because
				// adding unsaved columns
				c.setParent(table);
				table.addColumn(c);
				// Storing columns for later use
				final String colName = CaptureHelper.getUniqueColumnName(c);
				columnsMap.put(colName, c);

			}
		} finally {
			CaptureHelper.safeClose(rset, null);
		}

		return columnsMap;
	}

	/**
	 * Builds the map of unique keys for the specified table. Note that the
	 * fetched unique keys will be added to the table
	 * 
	 * @param md
	 *            {@link DatabaseMetaData} of the underlying database connection
	 * @param monitor
	 *            a {@link IProgressMonitor} to report progress to
	 * @param table
	 *            the {@link IBasicTable} to fetch unique keys for
	 * @param columnsMap
	 *            the map of {@link IBasicColumn} hashed by their unique name
	 * @return a map of {@link IKeyConstraint} hashed by their unique name
	 * @throws SQLException
	 */
	private Map<String, IKeyConstraint> buildUniqueKeyMap(DatabaseMetaData md,
			IProgressMonitor monitor, IBasicTable table, Map<String, IBasicColumn> columnsMap)
			throws SQLException {
		final Map<String, IKeyConstraint> keysMap = new HashMap<String, IKeyConstraint>();
		final String tabName = table.getName();
		ResultSet rset = null;
		try {
			// Creating primary keys for this table
			rset = md.getPrimaryKeys(null, null, tabName);
			IKeyConstraint uk = null;
			List<MultiKey> pkCols = new ArrayList<MultiKey>();
			// Because JDBC may not give us a sorted list, we first fill
			// a list with all pk columns, we sort it by KEY_SEQ, and we
			// fill our neXtep PK.
			while (rset.next()) {
				monitor.worked(1);
				final String pkName = rset.getString("PK_NAME"); //$NON-NLS-1$
				final String colName = rset.getString("COLUMN_NAME"); //$NON-NLS-1$
				final int colIndex = rset.getInt("KEY_SEQ") - 1; //$NON-NLS-1$
				pkCols.add(new MultiKey(pkName, colIndex, colName));
			}
			Collections.sort(pkCols, new Comparator<MultiKey>() {

				@Override
				public int compare(MultiKey o1, MultiKey o2) {
					if ((Integer) o1.getKeys()[1] > (Integer) o2.getKeys()[1]) {
						return 1;
					}
					return -1;
				}
			});
			for (MultiKey pkCol : pkCols) {
				final String pkName = (String) pkCol.getKey(0);
				final String colName = (String) pkCol.getKey(2);

				monitor.worked(1);
				if (uk == null) {
					uk = new UniqueKeyConstraint(pkName, "", table); //$NON-NLS-1$
					uk.setConstraintType(ConstraintType.PRIMARY);
					table.addConstraint(uk);
					keysMap.put(tabName.toUpperCase(), uk);
				}
				// Retrieving UK column and adding it to UK
				final String columnKey = CaptureHelper.getUniqueObjectName(tabName, colName);
				final IBasicColumn ukColumn = columnsMap.get(columnKey);
				if (ukColumn != null) {
					uk.addColumn(ukColumn);
				} else {
					LOGGER.warn(MessageFormat.format(
							MySQLMessages.getString("capturer.mysql.uniqueKeyNotFound"), columnKey)); //$NON-NLS-1$
				}

			}
		} finally {
			CaptureHelper.safeClose(rset, null);
		}
		return keysMap;
	}

	private void fillForeignKeys(DatabaseMetaData md, IProgressMonitor monitor, IBasicTable table,
			Map<String, IKeyConstraint> keysMap, Map<String, IBasicColumn> columnsMap)
			throws SQLException {
		final String tabName = table.getName();
		ResultSet rset = null;
		// Creating foreign keys for this table
		try {
			rset = md.getImportedKeys(null, null, tabName);
			ForeignKeyConstraint fk = null;
			while (rset.next()) {
				monitor.worked(1);
				String fkName = rset.getString("FK_NAME"); //$NON-NLS-1$
				String colName = rset.getString("FKCOLUMN_NAME"); //$NON-NLS-1$
				String remoteTableName = rset.getString("PKTABLE_NAME"); //$NON-NLS-1$
				final short onUpdateRule = rset.getShort("UPDATE_RULE"); //$NON-NLS-1$
				final short onDeleteRule = rset.getShort("DELETE_RULE"); //$NON-NLS-1$

				if (fk == null || (fk != null && !fkName.equals(fk.getName()))) {
					fk = new ForeignKeyConstraint(fkName, "", table); //$NON-NLS-1$
					// Retrieving primary key from loaded keys
					IKeyConstraint refConstraint = keysMap.get(remoteTableName.toUpperCase());
					// We have a reference to a non-imported constraint
					if (refConstraint == null) {
						try {
							IBasicTable remoteTable = (IBasicTable) CorePlugin.getService(
									IReferenceManager.class).findByTypeName(
									IElementType.getInstance(IBasicTable.TYPE_ID),
									DBVendor.MYSQL.getNameFormatter().format(remoteTableName));
							refConstraint = DBGMHelper.getPrimaryKey(remoteTable);
							if (refConstraint == null) {
								LOGGER.warn(MessageFormat.format(
										MySQLMessages.getString("capturer.mysql.fkIgnored"), //$NON-NLS-1$
										fkName));
								continue;
							}
							LOGGER.warn(MessageFormat.format(
									MySQLMessages.getString("capturer.mysql.fkRelinked"), //$NON-NLS-1$
									fkName, refConstraint.getName()));
						} catch (ReferenceNotFoundException e) {
							LOGGER.warn(MessageFormat.format(
									MySQLMessages.getString("capturer.mysql.fkIgnored"), //$NON-NLS-1$
									fkName));
							continue;
						}
					}
					fk.setRemoteConstraint(refConstraint);
					fk.setOnUpdateAction(CaptureHelper.getForeignKeyAction(onUpdateRule));
					fk.setOnDeleteAction(CaptureHelper.getForeignKeyAction(onDeleteRule));
					table.addConstraint(fk);
				}
				final String columnKey = CaptureHelper.getUniqueObjectName(tabName, colName);
				final IBasicColumn fkColumn = columnsMap.get(columnKey);
				if (fkColumn != null) {
					fk.addColumn(fkColumn);
				} else {
					LOGGER.warn(MessageFormat.format(
							MySQLMessages.getString("capturer.mysql.foreignKeyNotFound"), columnKey)); //$NON-NLS-1$
				}

			}
		} finally {
			CaptureHelper.safeClose(rset, null);
		}
	}

}
