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
package com.nextep.designer.sqlgen.mssql.impl;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.ProcedureParameter;
import com.nextep.datadesigner.dbgm.impl.TypeColumn;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.dbgm.model.ParameterType;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.designer.core.helpers.CustomProgressMonitor;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.ICaptureContext;
import com.nextep.designer.sqlgen.model.ICapturer;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;
import com.nextep.designer.sqlgen.model.base.AbstractCapturer;
import com.nextep.designer.sqlgen.mssql.MSSQLMessages;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * Implementation of the <code>ICapturer</code> interface for the Microsoft SQL Server vendor.
 * 
 * @author Bruno Gautier
 * @author Darren Hartford
 */
public final class MSSQLCapturer extends AbstractCapturer {

	private static final Log LOGGER = LogFactory.getLog(MSSQLCapturer.class);

	private ICapturer jdbcCapturer = null;

	@Override
	public void initialize(IConnection conn, IMutableCaptureContext context) {
		super.initialize(conn, context);
		jdbcCapturer = SQLGenPlugin.getService(ICaptureService.class).getCapturer(DBVendor.JDBC);
		// context.setSchema("dbo"); //TESTING ONLY
	}

	@Override
	public Collection<IBasicTable> getTables(ICaptureContext context, IProgressMonitor monitor) {
		// DRH 2/17/2011 good enough
		Collection<IBasicTable> tables = jdbcCapturer.getTables(context, monitor);

		// We retrieve from the capture context the list of database objects captured with their
		// associated schema.
		Map<IDatabaseObject<?>, String> dbObjSchemas = context.getAttributeValues(ATTR_SCHEMA);

		// We create a map of the captured tables hashed by their name and schema so we can easily
		// retrieve them later to set their description.
		Map<MultiKey, IBasicTable> capturedTables = new HashMap<MultiKey, IBasicTable>();
		for (IBasicTable table : tables) {
			capturedTables.put(new MultiKey(dbObjSchemas.get(table), table.getName()), table);
		}

		// update table descriptions from MS_DESCRIPTION extended properties
		updateTablesDescriptions(context, monitor, capturedTables);

		// Update columns properties (IDENTITY, FILESTREAM, ROWGUIDCOL, etc.)
		updateColumnsProperties(context, monitor, capturedTables);

		return tables;
	}

	/**
	 * @param context a {@link ICaptureContext} representing the current capture context
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @param capturedTables a <code>Map</code> of all tables previously captured hashed by a
	 *        <code>MultiKey</code> containing their schema name and their object name
	 */
	private void updateTablesDescriptions(ICaptureContext context, IProgressMonitor monitor,
			Map<MultiKey, IBasicTable> capturedTables) {
		monitor.subTask("Retrieving tables and columns descriptions...");

		// We define the list of distinct schema names for all tables previously captured
		Set<String> schemaNames = new HashSet<String>();
		for (MultiKey key : capturedTables.keySet()) {
			schemaNames.add((String) key.getKey(0));
		}

		// For each unique schema, we fetch the extended properties of all tables
		for (String schemaName : schemaNames) {
			final Map<String, String> tablesDescriptions = getSchemaTablesDescriptions(context,
					monitor, schemaName);

			for (String tableName : tablesDescriptions.keySet()) {
				final IBasicTable table = capturedTables.get(new MultiKey(schemaName, tableName));
				if (table != null) {
					table.setDescription(tablesDescriptions.get(tableName));
				} else {
					LOGGER.warn("Table ["
							+ schemaName
							+ "."
							+ tableName
							+ "] description has been ignored during import because the referenced "
							+ "table could not be found in the current workspace");
				}
			}
		}

		// For each captured table, we fetch the extended properties of all columns
		for (MultiKey key : capturedTables.keySet()) {
			final String schemaName = (String) key.getKey(0);
			final String tableName = (String) key.getKey(1);
			final IBasicTable table = capturedTables.get(key);
			final Map<String, String> columnsDescriptions = getTableColumnsDescriptions(context,
					monitor, schemaName, tableName);

			if (!columnsDescriptions.isEmpty()) {
				// We create a map of the table's columns hashed by their name so we can easily
				// retrieve them to set their description.
				Map<String, IBasicColumn> tableColumns = new HashMap<String, IBasicColumn>();
				for (IBasicColumn column : table.getColumns()) {
					tableColumns.put(column.getName(), column);
				}

				// We browse the list of retrieved column descriptions to set each column
				// description.
				for (String columnName : columnsDescriptions.keySet()) {
					final IBasicColumn column = tableColumns.get(columnName);
					if (column != null) {
						column.setDescription(columnsDescriptions.get(columnName));
					} else {
						LOGGER.warn("Column [" + schemaName + "." + tableName + "." + columnName
								+ "] description has been ignored during import because the "
								+ "referenced column could not be found in the current workspace");
					}
				}
			}
		}
	}

	private Map<String, String> getSchemaTablesDescriptions(ICaptureContext context,
			IProgressMonitor monitor, String schemaName) {
		final Map<String, String> descriptions = new HashMap<String, String>();

		Connection conn = (Connection) context.getConnectionObject();
		PreparedStatement prepStmt = null;
		ResultSet rset = null;
		try {
			prepStmt = conn
					.prepareStatement("SELECT objname as table_name, value as table_description " //$NON-NLS-1$
							+ "FROM FN_LISTEXTENDEDPROPERTY ( " //$NON-NLS-1$
							+ "  'MS_Description', " //$NON-NLS-1$
							+ "  'SCHEMA', ?, " //$NON-NLS-1$
							+ "  'TABLE', DEFAULT, " //$NON-NLS-1$
							+ "  NULL, NULL)"); //$NON-NLS-1$
			prepStmt.setString(1, schemaName);

			rset = prepStmt.executeQuery();
			while (rset.next()) {
				monitor.worked(1);
				final String tableName = rset.getString("table_name"); //$NON-NLS-1$
				final String tableDesc = rset.getString("table_description"); //$NON-NLS-1$

				if (tableName != null && !"".equals(tableName.trim())) { //$NON-NLS-1$
					if (LOGGER.isDebugEnabled()) {
						String logPrefix = "[" + schemaName + "." + tableName + "] "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						LOGGER.debug("= " + logPrefix + "Table Description Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + tableDesc);
					}

					descriptions.put(tableName, tableDesc);
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch tables descriptions for schema [" + schemaName
					+ "] from SQL Server server: " + sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		return descriptions;
	}

	private Map<String, String> getTableColumnsDescriptions(ICaptureContext context,
			IProgressMonitor monitor, String schemaName, String tableName) {
		final Map<String, String> descriptions = new HashMap<String, String>();

		Connection conn = (Connection) context.getConnectionObject();
		PreparedStatement prepStmt = null;
		ResultSet rset = null;
		try {
			prepStmt = conn
					.prepareStatement("SELECT objname as column_name, value as column_description " //$NON-NLS-1$
							+ "FROM FN_LISTEXTENDEDPROPERTY ( " //$NON-NLS-1$
							+ "  'MS_Description', " //$NON-NLS-1$
							+ "  'SCHEMA', ?, " //$NON-NLS-1$
							+ "  'TABLE', ?, " //$NON-NLS-1$
							+ "  'COLUMN', DEFAULT)"); //$NON-NLS-1$
			prepStmt.setString(1, schemaName);
			prepStmt.setString(2, tableName);

			rset = prepStmt.executeQuery();
			while (rset.next()) {
				monitor.worked(1);
				final String columnName = rset.getString("column_name"); //$NON-NLS-1$
				final String columnDesc = rset.getString("column_description"); //$NON-NLS-1$

				if (columnName != null && !"".equals(columnName.trim())) { //$NON-NLS-1$
					if (LOGGER.isDebugEnabled()) {
						String logPrefix = "[" + schemaName + "." + tableName + "." + columnName + "] "; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						LOGGER.debug("= " + logPrefix + "Column Description Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + columnDesc);
					}

					descriptions.put(columnName, columnDesc);
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch columns descriptions for table [" + schemaName + "."
					+ tableName + "] from SQL Server server: " + sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		return descriptions;
	}

	/**
	 * @param context a {@link ICaptureContext} representing the current capture context
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @param capturedTables a <code>Map</code> of all tables previously captured hashed by a
	 *        <code>MultiKey</code> containing their schema name and their object name
	 */
	private void updateColumnsProperties(ICaptureContext context, IProgressMonitor monitor,
			Map<MultiKey, IBasicTable> capturedTables) {
		/*
		 * Retrieving identity columns attributes (seed and increment) to set accordingly the length
		 * and precision attributes of the corresponding columns. This is a temporary workaround to
		 * handle the columns IDENTITY property until we implement a MSSQL specific model to store
		 * it properly in the repository.
		 */
		final Map<MultiKey, MultiKey> identityColumnsAttributes = getIdentityColumnsAttributes(
				context, monitor);

		identityColsLoop:
		for (MultiKey tableKey : identityColumnsAttributes.keySet()) {
			final IBasicTable table = capturedTables.get(tableKey);

			if (table != null) {
				MultiKey identityColKey = identityColumnsAttributes.get(tableKey);
				String identityColName = (String) identityColKey.getKey(0);

				// TODO [BGA] Add a getColumn(String columnName) method to the IBasicTable interface
				for (IBasicColumn column : table.getColumns()) {
					final String columnName = column.getName();

					if (columnName.equals(identityColName)) {
						final IDatatype colDatatype = column.getDatatype();

						if (colDatatype.getName().contains("IDENTITY")) { //$NON-NLS-1$
							final int seed = (Integer) identityColKey.getKey(1);
							final int increment = (Integer) identityColKey.getKey(2);
							colDatatype.setLength(seed);
							colDatatype.setPrecision(increment);
						} else {
							LOGGER.warn("Identity column [" + tableKey.getKey(0) + "."
									+ tableKey.getKey(1) + "." + identityColName
									+ "] attributes have been discarded during import because "
									+ "the IDENTITY property of the referenced column "
									+ "could not be fetched from SQL Server server");
						}

						// Once we have found the corresponding IDENTITY column, we resume the
						// identity columns loop.
						continue identityColsLoop;
					}
				}

				// No corresponding column has been found in the captured columns of the referenced
				// table, we raise a warning.
				LOGGER.warn("Identity column [" + identityColName
						+ "] attributes have been discarded during import because "
						+ "the corresponding column could not be found in the current workspace "
						+ "for the referenced table [" + tableKey.getKey(0) + "."
						+ tableKey.getKey(1) + "]");
			} else {
				LOGGER.warn("Identity column [" + identityColumnsAttributes.get(tableKey).getKey(0)
						+ "] attributes have been discarded during import because "
						+ "the referenced table [" + tableKey.getKey(0) + "." + tableKey.getKey(1)
						+ "] could not be found in the current workspace");
			}
		}

		// TODO [BGA] Retrieve other columns properties (FILESTREAM,ROWGUIDCOL,etc.)
	}

	/**
	 * @param context a {@link ICaptureContext} representing the current capture context
	 * @param monitor the {@link IProgressMonitor} to notify while capturing objects
	 * @return a <code>Map</code> of all identity columns in the current database hashed by a
	 *         <code>MultiKey</code> containing the schema and table names of the parent tables.
	 *         Each identity column is represented by a <code>MultiKey</code> containing the name of
	 *         the column, followed by the seed and increment attributes
	 */
	private Map<MultiKey, MultiKey> getIdentityColumnsAttributes(ICaptureContext context,
			IProgressMonitor monitor) {
		monitor.subTask("Retrieving identity columns attributes...");
		final Map<MultiKey, MultiKey> attributes = new HashMap<MultiKey, MultiKey>();
		final String schema = context.getSchema();

		Connection conn = (Connection) context.getConnectionObject();
		PreparedStatement prepStmt = null;
		ResultSet rset = null;
		try {
			String query = "SELECT s.name AS schema_name, t.name AS table_name, c.name AS column_name, " //$NON-NLS-1$
					+ "  c.seed_value, c.increment_value, IDENT_CURRENT(s.name + '.' + t.name) AS last_value " //$NON-NLS-1$
					+ "FROM sys.schemas AS s " //$NON-NLS-1$
					+ "  JOIN sys.tables AS t ON t.schema_id = s.schema_id " //$NON-NLS-1$
					+ "  JOIN sys.identity_columns AS c ON c.object_id = t.object_id"; //$NON-NLS-1$
			if (schema != null) {
				query += " WHERE s.name = ?"; //$NON-NLS-1$
			}

			prepStmt = conn.prepareStatement(query);
			if (schema != null) {
				prepStmt.setString(1, schema);
			}

			rset = prepStmt.executeQuery();
			while (rset.next()) {
				monitor.worked(1);
				final String schemaName = rset.getString("schema_name"); //$NON-NLS-1$
				final String tableName = rset.getString("table_name"); //$NON-NLS-1$
				final String columnName = rset.getString("column_name"); //$NON-NLS-1$
				final int seed = rset.getInt("seed_value"); //$NON-NLS-1$
				final int increment = rset.getInt("increment_value"); //$NON-NLS-1$
				final BigDecimal lastValue = rset.getBigDecimal("last_value"); //$NON-NLS-1$

				// We check that the schema name is not null, and that the table and column names
				// are not null and not empty.
				if (schemaName != null && tableName != null && !"".equals(tableName.trim()) //$NON-NLS-1$
						&& columnName != null && !"".equals(columnName.trim())) { //$NON-NLS-1$
					if (LOGGER.isDebugEnabled()) {
						String logPrefix = "[" + schemaName + "." + tableName + "." + columnName //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								+ "] "; //$NON-NLS-1$
						LOGGER.debug("= " + logPrefix + "Identity Property Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + "[SEED] " + seed); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[INCREMENT] " + increment); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[LAST_VALUE] " + lastValue); //$NON-NLS-1$
					}

					attributes.put(new MultiKey(schemaName, tableName), new MultiKey(columnName,
							seed, increment));
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch identity columns attributes from SQL Server server: "
					+ sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		return attributes;
	}

	/*
	 * confirmed SQL Server 2005, 2008 DRH 3/2/2011 (non-Javadoc)
	 * @see com.nextep.designer.sqlgen.model.ICapturer#getIndexes(com.nextep.designer.sqlgen.model.
	 * ICaptureContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Collection<IIndex> getIndexes(ICaptureContext context, IProgressMonitor m) {
		m.subTask(MSSQLMessages.getString("capturer.mssql.retrievingIndexes")); //$NON-NLS-1$

		// DRH 2/17/2011 jdbcCapturer worked good enough, but improving for index type.
		Collection<IIndex> indexes2 = jdbcCapturer.getIndexes(context, m);

		final Connection conn = (Connection) context.getConnectionObject();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = conn.createStatement();
			rset = stmt
					.executeQuery("SELECT sysobjects.name as TABLE_NAME, (sysobjects.name + '.' + sysindexes.name) as TABLE_INDEX_NAME, " //$NON-NLS-1$
							+ " CASE  " //$NON-NLS-1$
							+ " WHEN INDEXPROPERTY(OBJECT_ID(sysobjects.name),sysindexes.name,'IsClustered') = 1 THEN 'CLUSTER' " //$NON-NLS-1$
							+ " WHEN INDEXPROPERTY(OBJECT_ID(sysobjects.name),sysindexes.name,'IsFulltextKey') = 1 THEN 'FULLTEXT' " //$NON-NLS-1$
							+ " WHEN INDEXPROPERTY(OBJECT_ID(sysobjects.name),sysindexes.name,'IsUnique') = 1 THEN 'UNIQUE' " //$NON-NLS-1$
							+ " ELSE 'NON_UNIQUE' " //$NON-NLS-1$
							+ " END INDEX_TYPE " //$NON-NLS-1$
							+ " FROM   sysobjects INNER JOIN  " //$NON-NLS-1$
							+ " sysindexes ON sysobjects.id = sysindexes.id " //$NON-NLS-1$
							+ " INNER JOIN sysusers ON sysobjects.uid = sysusers.uid " //$NON-NLS-1$
							+ " WHERE  type = 'U' and sysindexes.name is not null " //$NON-NLS-1$
							+ " and sysindexes.root is not null " //$NON-NLS-1$
							+ " and sysusers.name = '" + notNull(context.getSchema()).toUpperCase() + "';" //$NON-NLS-1$ //$NON-NLS-2$
					);

			// make only one sql call, collect results, then process
			final Map<String, String> tableIndexType = new HashMap<String, String>();
			final Set<String> tableList = new HashSet<String>();
			while (rset.next()) {
				m.worked(1);
				final String tableIndexName = rset.getString("TABLE_INDEX_NAME"); //$NON-NLS-1$
				final String indexType = rset.getString("INDEX_TYPE"); //$NON-NLS-1$   
				final String tableName = rset.getString("TABLE_NAME"); //$NON-NLS-1$                  
				tableIndexType.put(tableIndexName, indexType);
				tableList.add(tableName);
			}
			Map<String, String> retrieveIndexesDescriptions = retrieveIndexesDescriptions(conn,
					context, tableList);
			for (Iterator iterator = indexes2.iterator(); iterator.hasNext();) {
				IIndex iIndex = (IIndex) iterator.next();
				String newIndexType = tableIndexType.get(iIndex.getIndexedTable().getName() + "." //$NON-NLS-1$
						+ iIndex.getIndexName());
				if (newIndexType != null) {
					iIndex.setIndexType(IndexType.valueOf(newIndexType));
				}
				iIndex.setDescription(retrieveIndexesDescriptions.get(iIndex.getIndexName()));
			}

		} catch (SQLException e) {
			LOGGER.warn(MessageFormat.format(
					MSSQLMessages.getString("capturer.mssql.fetchIndexesError"), e.getMessage()), e); //$NON-NLS-1$
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return indexes2;
	}

	// TODO [BGA] Add support for alias columns
	@Override
	public Collection<IView> getViews(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100);
		monitor.subTask(MSSQLMessages.getString("capturer.mssql.retrievingViews")); //$NON-NLS-1$

		final Map<MultiKey, IView> views = new HashMap<MultiKey, IView>();
		final IFormatter formatter = context.getConnection().getDBVendor().getNameFormatter();
		final Connection conn = (Connection) context.getConnectionObject();
		final String schema = context.getSchema();

		ResultSet rset = null;
		PreparedStatement prepStmt = null;
		try {
			/*
			 * [BGA] Migrating to the Catalog Views approach, since information_schema.views will
			 * show only the first 4000 characters of the SQL definition. This query works with SQL
			 * Server 2005/2008 but not with SQL Server 2000. Compatibility views might be used as a
			 * fallback strategy if we plan to support SQL Server 2000.
			 */
			String query = "SELECT s.name AS schema_name, v.name AS view_name, m.definition AS view_definition " //$NON-NLS-1$
					+ "FROM sys.schemas AS s " //$NON-NLS-1$
					+ "  JOIN sys.views AS v ON v.schema_id = s.schema_id " //$NON-NLS-1$
					+ "  JOIN sys.sql_modules AS m ON m.object_id = v.object_id"; //$NON-NLS-1$
			if (schema != null) {
				query += " WHERE s.name = ?"; //$NON-NLS-1$
			}

			// alternative approach for older versions
			// [BGA] Warning with this query, views with a SQL definition longer than 4000
			// characters might be returned with multiple records
			// SELECT u.name AS schema_name, o.name AS view_name, c.text AS view_definition
			// FROM sysobjects AS o
			// JOIN syscomments AS c ON o.id = c.id
			// JOIN sysusers AS u ON o.uid = u.uid
			// WHERE o.xtype = 'V';

			prepStmt = conn.prepareStatement(query);
			if (schema != null) {
				prepStmt.setString(1, schema);
			}

			rset = prepStmt.executeQuery();
			while (rset.next()) {
				monitor.worked(1);
				final String schemaName = rset.getString("schema_name"); //$NON-NLS-1$
				final String viewName = rset.getString("view_name"); //$NON-NLS-1$
				final String sql = rset.getString("view_definition"); //$NON-NLS-1$

				String viewQuery = CaptureHelper.getQueryFromCreateAsStatement(sql);

				if (viewQuery != null && !"".equals(viewQuery.trim())) { //$NON-NLS-1$
					IVersionable<IView> v = VersionableFactory.createVersionable(IView.class);
					IView view = v.getVersionnedObject().getModel();
					view.setName(formatter.format(viewName));
					view.setSQLDefinition(viewQuery);

					views.put(new MultiKey(schemaName, viewName), view);
				} else {
					LOGGER.warn("View [" + viewName
							+ "] has been ignored during import because the SQL definition "
							+ "could not be extracted from dictionary tables. "
							+ "View definition [" + sql + "]"); //$NON-NLS-2$
				}
			}
		} catch (SQLException e) {
			LOGGER.warn(MessageFormat.format(
					MSSQLMessages.getString("capturer.mssql.fetchViewsError"), e.getMessage()), e); //$NON-NLS-1$
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		// TODO [BGA] Implement the views descriptions retrieval as what has been done for the
		// tables descriptions.
		// Update views descriptions from MS_Description extended properties
		// updateViewsDescriptions(context, monitor, views);

		return views.values();
	}

	@Override
	public Collection<IProcedure> getProcedures(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100);
		monitor.subTask(MSSQLMessages.getString("capturer.mssql.retrievingProcs")); //$NON-NLS-1$

		final Collection<IProcedure> procedures = new ArrayList<IProcedure>();
		final Connection conn = (Connection) context.getConnectionObject();
		final String schema = context.getSchema();

		// DRH 2/17/2011 initial attempt, however may have permission issues to INFORMATION_SCHEMA.
		// otherwise, may need to use exec sp_stored_procedures and exec sp_sproc_columns

		ResultSet rset = null;
		PreparedStatement prepStmt = null;
		try {
			String query = "SELECT routine_name " // -- or specific_name //$NON-NLS-1$
					+ "  ,routine_definition, routine_schema, routine_catalog, is_deterministic " //$NON-NLS-1$
					+ "FROM information_schema.routines " //$NON-NLS-1$
					+ "WHERE routine_type = 'PROCEDURE'"; //$NON-NLS-1$
			if (schema != null) {
				query += "  AND routine_schema = ?"; //$NON-NLS-1$
			}

			// alternative approach
			// SELECT * FROM sysobjects WHERE type IN ('P', 'RF', 'X', 'PC');

			prepStmt = conn.prepareStatement(query);
			if (schema != null) {
				prepStmt.setString(1, schema);
			}

			rset = prepStmt.executeQuery();
			IProcedure currentProc = null;
			Map<String, String> retrieveSprocDescriptions = retrieveSprocDescriptions(conn, context);
			while (rset.next()) {
				monitor.worked(1);
				final String name = rset.getString(1);
				final String body = rset.getString(2);
				final String procSchema = rset.getString("routine_schema"); //$NON-NLS-1$
				// final String params = rset.getString(3);
				// final String retCode = rset.getString(4);
				final boolean deterministic = "YES".equals(rset.getString(5)); //$NON-NLS-1$
				// final String dataAccess = rset.getString(6);

				if (currentProc == null || !name.equals(currentProc.getName())) {
					IVersionable<IProcedure> v = VersionableFactory
							.createVersionable(IProcedure.class);
					currentProc = v.getVersionnedObject().getModel();
					currentProc.setName(name);
					currentProc.setLanguageType(LanguageType.STANDARD);
					currentProc.setDescription(retrieveSprocDescriptions.get(name));
					currentProc.setSQLSource(body);

					// Setting schema name as attribute in the context so it will be available for
					// other methods.
					context.setDbObjectAttribute((IDatabaseObject<?>) currentProc, ATTR_SCHEMA,
							procSchema);
					updateProcedureWithParameters(conn, context, currentProc); // update params
					// currentProc.setReturnType(new Datatype(retCode));

					procedures.add(currentProc);
				}
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		return procedures;
	}

	private void updateProcedureWithParameters(Connection conn, ICaptureContext context,
			IProcedure proc) {
		ResultSet rset = null;
		PreparedStatement prepStmt = null;
		try {
			prepStmt = conn
					.prepareStatement("select PARAMETER_MODE, IS_RESULT, PARAMETER_NAME, DATA_TYPE,  " //$NON-NLS-1$
							+ " CASE " //$NON-NLS-1$
							+ "   WHEN CHARACTER_MAXIMUM_LENGTH is not null then CHARACTER_MAXIMUM_LENGTH " //$NON-NLS-1$
							+ "   WHEN NUMERIC_PRECISION is not null then NUMERIC_PRECISION  " //$NON-NLS-1$
							+ "   WHEN DATETIME_PRECISION is not null then 0 " //--need to evaluate value DATETIME_PRECISION representation //$NON-NLS-1$
							+ "   WHEN INTERVAL_TYPE is not null then INTERVAL_TYPE   " //--need to evaluate value INTERVAL_* representation //$NON-NLS-1$
							+ " END DATA_LENGTH, " //$NON-NLS-1$
							+ " CASE " //$NON-NLS-1$
							+ "   WHEN CHARACTER_MAXIMUM_LENGTH is not null then 0 " //$NON-NLS-1$
							+ "   WHEN NUMERIC_PRECISION is not null then NUMERIC_SCALE   " //$NON-NLS-1$
							+ "   WHEN DATETIME_PRECISION is not null then DATETIME_PRECISION   " //--need to evaluate value DATETIME_PRECISION representation  //$NON-NLS-1$
							+ "   WHEN INTERVAL_TYPE is not null then  INTERVAL_PRECISION   " //--need to evaluate value INTERVAL_* representation  //$NON-NLS-1$
							+ " END DATA_PRECISION, " //$NON-NLS-1$
							+ " ORDINAL_POSITION " //$NON-NLS-1$
							+ " FROM information_schema.parameters " //$NON-NLS-1$
							+ " WHERE specific_name = ? " //$NON-NLS-1$
							+ "   AND specific_schema = ? " //$NON-NLS-1$
							+ " ORDER BY ordinal_position" //$NON-NLS-1$
					);
			prepStmt.setString(1, proc.getName());
			prepStmt.setString(2, context.getDbObjectAttributeValue(proc, ATTR_SCHEMA));

			rset = prepStmt.executeQuery();
			while (rset.next()) {
				final String name = rset.getString("PARAMETER_NAME"); //$NON-NLS-1$
				final String paramMode = rset.getString("PARAMETER_MODE"); //$NON-NLS-1$
				final String paramDataType = rset.getString("DATA_TYPE"); //$NON-NLS-1$
				final int dataLength = rset.getInt("DATA_LENGTH"); //$NON-NLS-1$
				final int dataPrecision = rset.getInt("DATA_PRECISION"); //$NON-NLS-1$

				IDatatype d = new Datatype(paramDataType, dataLength, dataPrecision);
				IProcedureParameter param = new ProcedureParameter(name,
						ParameterType.valueOf(paramMode), d);
				proc.addParameter(param);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

	}

	@Override
	public Collection<ITrigger> getTriggers(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100);
		monitor.subTask(MSSQLMessages.getString("capturer.mssql.retrievingTriggers")); //$NON-NLS-1$

		final Collection<ITrigger> triggers = new ArrayList<ITrigger>();
		final Connection conn = (Connection) context.getConnectionObject();
		final String schema = context.getSchema();

		ResultSet rset = null;
		PreparedStatement prepStmt = null;
		try {
			String query = "SELECT sys1.name TRIGGER_NAME," //$NON-NLS-1$
					+ " sys2.name TABLE_NAME, " //$NON-NLS-1$
					+ " c.text TRIGGER_CONTENT, " //$NON-NLS-1$
					+ " CASE " //$NON-NLS-1$
					+ "   WHEN OBJECTPROPERTY(sys1.id, 'ExecIsTriggerDisabled') = 1 " //$NON-NLS-1$
					+ "   THEN 0 ELSE 1 " //$NON-NLS-1$
					+ " END TRIGGER_ENABLED, " //$NON-NLS-1$
					+ " CASE " //$NON-NLS-1$
					+ "   WHEN OBJECTPROPERTY(sys1.id, 'ExecIsInsertTrigger') = 1 THEN 'INSERT' " //$NON-NLS-1$
					+ "   WHEN OBJECTPROPERTY(sys1.id, 'ExecIsUpdateTrigger') = 1 THEN 'UPDATE' " //$NON-NLS-1$
					+ "   WHEN OBJECTPROPERTY(sys1.id, 'ExecIsDeleteTrigger') = 1 THEN 'DELETE' " //$NON-NLS-1$
					+ " END TRIGGER_EVENT, " //$NON-NLS-1$
					+ " CASE " //$NON-NLS-1$
					+ "   WHEN OBJECTPROPERTY(sys1.id, 'ExecIsInsteadOfTrigger') = 1 THEN 'INSTEAD' " //$NON-NLS-1$
					+ "   WHEN OBJECTPROPERTY(sys1.id, 'ExecIsAfterTrigger') = 1 THEN 'AFTER' " //$NON-NLS-1$
					//					+ "   WHEN OBJECTPROPERTY(sys1.id, '') = 1 THEN 'BEFORE' " //BEFORE is undefined  //$NON-NLS-1$
					+ " END TRIGGER_TIMING " //$NON-NLS-1$
					+ " FROM sysobjects sys1 " //$NON-NLS-1$
					+ " JOIN sysobjects sys2 ON sys1.parent_obj = sys2.id " //$NON-NLS-1$
					+ " JOIN syscomments c ON sys1.id = c.id " //$NON-NLS-1$
					+ " INNER JOIN sysusers ON sys1.uid = sysusers.uid " //$NON-NLS-1$
					+ " WHERE sys1.type = 'TR'"; //$NON-NLS-1$
			if (schema != null) {
				query += "  AND sysusers.name = ?"; //$NON-NLS-1$
			}

			prepStmt = conn.prepareStatement(query);
			if (schema != null) {
				prepStmt.setString(1, schema);
			}

			rset = prepStmt.executeQuery();
			while (rset.next()) {
				monitor.worked(1);
				String triggerName = rset.getString("TRIGGER_NAME"); //$NON-NLS-1$
				String event = rset.getString("TRIGGER_EVENT"); //$NON-NLS-1$
				String triggerTabName = rset.getString("TABLE_NAME"); //$NON-NLS-1$
				String sql = rset.getString("TRIGGER_CONTENT"); //$NON-NLS-1$
				String timing = rset.getString("TRIGGER_TIMING"); //$NON-NLS-1$

				/*
				 * [BGA] We remove the CREATE part of the statement supplied by the database server
				 * to get rid of the schema names.
				 */
				String triggerBody = CaptureHelper.getBodyFromCreateAsStatement(sql);

				if (triggerBody != null && !"".equals(triggerBody.trim())) { //$NON-NLS-1$
					IVersionable<ITrigger> trigger = VersionableFactory
							.createVersionable(ITrigger.class);
					ITrigger trig = trigger.getVersionnedObject().getModel();
					trig.setName(triggerName);
					trig.addEvent(TriggerEvent.valueOf(event)); // INSERT, UPDATE, DELETE
					trig.setTime(TriggerTime.valueOf(timing)); // BEFORE, AFTER, INSTEAD;
					trig.setTriggableRef(context.getTable(triggerTabName).getReference());
					trig.setCustom(false);
					trig.setSourceCode(DBGMHelper.trimEmptyLines(triggerBody));
					triggers.add(trig);
				} else {
					LOGGER.warn("Trigger [" + triggerName
							+ "] has been ignored during import because the SQL body "
							+ "could not be extracted from dictionary tables. "
							+ "Trigger definition [" + sql + "]"); //$NON-NLS-2$
				}

			}
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							MSSQLMessages.getString("capturer.mssql.FetchTriggersError"), //$NON-NLS-1$
							e.getMessage()), e);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		return triggers;
	}

	@Override
	public Collection<IUserType> getUserTypes(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(m, 100);
		monitor.subTask("Retrieving user-defined types..."); //$NON-NLS-1$

		final Collection<IUserType> types = new ArrayList<IUserType>();
		final Connection conn = (Connection) context.getConnectionObject();
		final String schema = context.getSchema();

		ResultSet rset = null;
		PreparedStatement prepStmt = null;
		try {
			String query = "SELECT s.name AS schema_name, t.name AS user_type_name, st.name AS sys_type_name, " //$NON-NLS-1$
					+ "  TYPEPROPERTY(s.name + '.' + t.name, 'Precision') AS user_type_precision, " //$NON-NLS-1$
					+ "  TYPEPROPERTY(s.name + '.' + t.name, 'Scale') AS user_type_scale, " //$NON-NLS-1$
					+ "  t.is_nullable " //$NON-NLS-1$
					+ "FROM sys.schemas AS s" //$NON-NLS-1$
					+ "   JOIN sys.types AS t ON t.schema_id = s.schema_id " //$NON-NLS-1$
					+ "   JOIN sys.types AS st ON st.user_type_id = t.system_type_id " //$NON-NLS-1$
					+ "WHERE t.is_user_defined = 1 " //$NON-NLS-1$
					+ "  AND t.is_table_type = 0"; //$NON-NLS-1$
			if (schema != null) {
				query += "  AND s.name = ?"; //$NON-NLS-1$
			}

			prepStmt = conn.prepareStatement(query);
			if (schema != null) {
				prepStmt.setString(1, schema);
			}

			rset = prepStmt.executeQuery();
			while (rset.next()) {
				monitor.worked(1);
				final String schemaName = rset.getString("schema_name"); //$NON-NLS-1$
				final String userTypeName = rset.getString("user_type_name"); //$NON-NLS-1$
				final String colType = rset.getString("sys_type_name"); //$NON-NLS-1$
				final int colPrecision = rset.getInt("user_type_precision"); //$NON-NLS-1$
				final int colScale = rset.getInt("user_type_scale"); //$NON-NLS-1$

				// [BGA] Might be used when advanced MSSQL support will be provided with a specific
				// dbgm model
				final boolean isColNullable = (rset.getInt("is_nullable") == 1); //$NON-NLS-1$

				IVersionable<IUserType> versionable = VersionableFactory
						.createVersionable(IUserType.class);
				IUserType userType = versionable.getVersionnedObject().getModel();
				userType.setName(userTypeName);

				ITypeColumn column = new TypeColumn();
				column.setName(""); //$NON-NLS-1$

				final IDatatype datatype = new Datatype(colType);
				if (!DBGMHelper.getDatatypeProvider(DBVendor.MSSQL).getUnsizableDatatypes()
						.contains(datatype.getName())) {
					datatype.setLength(colPrecision == 0 ? -1 : colPrecision);
					datatype.setPrecision(colScale == 0 ? -1 : colScale);
				}
				column.setDatatype(datatype);

				userType.addColumn(column);
				types.add(userType);
			}
		} catch (SQLException sqle) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							DBVendor.MSSQL.toString())
							+ sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		return types;
	}

	// FIXME [BGA] This method does not work when no schema name has been specified in the
	// connection settings.
	private Map<String, String> retrieveSprocDescriptions(Connection conn, ICaptureContext context) {
		final Map<String, String> results = new HashMap<String, String>();

		ResultSet rset = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rset = stmt
					.executeQuery("SELECT objname as SPROC_NAME, value as SPROC_DESCRIPTION  " //$NON-NLS-1$
							+ " FROM fn_listextendedproperty ('MS_DESCRIPTION', 'schema','" + notNull(context.getSchema()).toUpperCase() + "', 'procedure',default , null, null); " //$NON-NLS-1$
					);
			while (rset.next()) {
				final String oname = rset.getString("SPROC_NAME"); //$NON-NLS-1$
				final String odescription = rset.getString("SPROC_DESCRIPTION"); //$NON-NLS-1$
				results.put(oname, odescription);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return results;
	}

	// FIXME [BGA] This method does not work when no schema name has been specified in the
	// connection settings.
	private Map<String, String> retrieveViewDescriptions(Connection conn, ICaptureContext context) {
		final Map<String, String> results = new HashMap<String, String>();

		ResultSet rset = null;
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			rset = stmt
					.executeQuery("SELECT objname as VIEW_NAME, value as VIEW_DESCRIPTION  " //$NON-NLS-1$
							+ " FROM fn_listextendedproperty ('MS_DESCRIPTION', 'schema','" + notNull(context.getSchema()).toUpperCase() + "', 'view',default , null, null); " //$NON-NLS-1$
					);
			while (rset.next()) {
				final String oname = rset.getString("VIEW_NAME");
				final String odescription = rset.getString("VIEW_DESCRIPTION");
				results.put(oname, odescription);
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return results;
	}

	// FIXME [BGA] This method does not work when no schema name has been specified in the
	// connection settings.
	// index name is unique, so can do a hashmap/batch call
	private Map<String, String> retrieveIndexesDescriptions(Connection conn,
			ICaptureContext context, Set<String> tablelist) {
		final Map<String, String> results = new HashMap<String, String>();
		ResultSet rset = null;
		Statement stmt = null;

		StringBuilder sb = new StringBuilder();

		for (Iterator<String> iterator = tablelist.iterator(); iterator.hasNext();) {
			String tableName = iterator.next();
			sb.append("SELECT objname as INDEX_NAME, value as INDEX_DESCRIPTION " //$NON-NLS-1$
					+ "FROM fn_listextendedproperty ('MS_DESCRIPTION', 'schema','" //$NON-NLS-1$
					+ notNull(context.getSchema()).toUpperCase() + "', 'table','" + tableName //$NON-NLS-1$
					+ "' , 'index', null) "); //$NON-NLS-1$
			if (iterator.hasNext()) {
				sb.append(" UNION ");//$NON-NLS-1$
			}
		}

		// [BGA] We check if a query has been generated in the event of an empty tables list
		if (sb.length() > 0) {
			try {
				stmt = conn.createStatement();
				rset = stmt.executeQuery(sb.toString());
				while (rset.next()) {
					final String oname = rset.getString("INDEX_NAME"); //$NON-NLS-1$
					final String odescription = rset.getString("INDEX_DESCRIPTION"); //$NON-NLS-1$
					results.put(oname, odescription);
				}
			} catch (SQLException e) {
				LOGGER.error(e);
			} finally {
				CaptureHelper.safeClose(rset, stmt);
			}
		}

		return results;
	}

	// DRH 2/24/2011 for future use
	private String getDatabaseVersion() {
		String databaseVersion = null;
		// SELECT SERVERPROPERTY('productversion')
		// only works on 2000 up.
		// MS SQL Server 2008 R2 example product version = 10.50.1600.1
		// MS SQL Server 2005 example product version = 9.00.1399.06
		// MS SQL Server 2000 SP2 example product version = 8.00.534

		// SELECT @@VERSION
		// will work for all versions of MS SQL
		// MS SQL Server 7 SP4 example product version (parsed from text) = 7.00.1063
		// MS SQL Server 6.5 example product version (parsed from text) = 6.50.479
		/*
		 * example text Microsoft SQL Server 7.00 - 7.00.623 (Intel X86) Nov 27 1998 22:20:07
		 * Copyright (c) 1988-1998 Microsoft Corporation Desktop Edition on Windows NT 5.1 (Build
		 * 2600: )
		 */

		return databaseVersion;
	}

	private String notNull(String s) {
		return (s == null ? "" : s); //$NON-NLS-1$
	}

}
