/*******************************************************************************
 * Copyright (c) 2010 neXtep Software and contributors.
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
package com.nextep.designer.sqlgen.generic.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;

import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.helpers.ColumnsSorter;
import com.nextep.designer.sqlgen.model.ICaptureContext;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;
import com.nextep.designer.sqlgen.model.ISQLCommandWriter;
import com.nextep.designer.sqlgen.model.base.AbstractCapturer;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Bruno Gautier
 * @author Christophe Fondacci
 */
public final class JDBCCapturer extends AbstractCapturer {

	private static final Log LOGGER = LogFactory.getLog(JDBCCapturer.class);

	private static final String[] DB_TABLE_TYPE = { "TABLE" }; //$NON-NLS-1$
	private static final String[] DB_VIEW_TYPE = { "VIEW" }; //$NON-NLS-1$

	private static final String COLUMN_NAME_TABLE_CAT = "TABLE_CAT"; //$NON-NLS-1$
	private static final String COLUMN_NAME_TABLE_SCHEM = "TABLE_SCHEM"; //$NON-NLS-1$
	private static final String COLUMN_NAME_TABLE_NAME = "TABLE_NAME"; //$NON-NLS-1$
	private static final String COLUMN_NAME_COMMENT = "REMARKS"; //$NON-NLS-1$
	private static final String COLUMN_NAME_COLUMN_NAME = "COLUMN_NAME"; //$NON-NLS-1$
	private static final String COLUMN_NAME_ORDINAL_POSITION = "ORDINAL_POSITION"; //$NON-NLS-1$
	private static final String COLUMN_NAME_TYPE_NAME = "TYPE_NAME"; //$NON-NLS-1$
	private static final String COLUMN_NAME_COLUMN_SIZE = "COLUMN_SIZE"; //$NON-NLS-1$
	private static final String COLUMN_NAME_DECIMAL_DIGITS = "DECIMAL_DIGITS"; //$NON-NLS-1$
	private static final String COLUMN_NAME_NULLABLE = "NULLABLE"; //$NON-NLS-1$
	private static final String COLUMN_NAME_COLUMN_DEF = "COLUMN_DEF"; //$NON-NLS-1$
	private static final String COLUMN_NAME_KEY_SEQ = "KEY_SEQ"; //$NON-NLS-1$
	private static final String COLUMN_NAME_PK_NAME = "PK_NAME"; //$NON-NLS-1$
	private static final String COLUMN_NAME_FK_NAME = "FK_NAME"; //$NON-NLS-1$
	private static final String COLUMN_NAME_PKTABLE_NAME = "PKTABLE_NAME"; //$NON-NLS-1$
	private static final String COLUMN_NAME_FKCOLUMN_NAME = "FKCOLUMN_NAME"; //$NON-NLS-1$
	private static final String COLUMN_NAME_UPDATE_RULE = "UPDATE_RULE"; //$NON-NLS-1$
	private static final String COLUMN_NAME_DELETE_RULE = "DELETE_RULE"; //$NON-NLS-1$
	private static final String COLUMN_NAME_DEFERRABILITY = "DEFERRABILITY"; //$NON-NLS-1$
	private static final String COLUMN_NAME_INDEX_NAME = "INDEX_NAME"; //$NON-NLS-1$
	private static final String COLUMN_NAME_ASC_OR_DESC = "ASC_OR_DESC"; //$NON-NLS-1$
	private static final String COLUMN_NAME_NON_UNIQUE = "NON_UNIQUE"; //$NON-NLS-1$
	private static final String COLUMN_NAME_TYPE = "TYPE"; //$NON-NLS-1$

	private final ITypedObjectFactory typedObjFactory;
	private int counter = 0;

	public JDBCCapturer() {
		this.typedObjFactory = CorePlugin.getService(ITypedObjectFactory.class);
	}

	@Override
	public void initialize(IConnection conn, IMutableCaptureContext context) {
		super.initialize(conn, context);
		this.counter = 0;
	}

	private int getCounter() {
		return counter;
	}

	@Override
	public Collection<IBasicTable> getTables(ICaptureContext context, IProgressMonitor monitor) {
		Map<String, IBasicTable> tables = new HashMap<String, IBasicTable>();
		Map<String, IBasicColumn> allTablesColumns = new HashMap<String, IBasicColumn>();

		try {
			final DatabaseMetaData md = ((Connection) context.getConnectionObject()).getMetaData();
			ResultSet rset = null;
			if (md != null) {
				rset = md.getTables(context.getCatalog(), context.getSchema(), null, DB_TABLE_TYPE);
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);
			}

			if (rset != null) {
				try {
					while (rset.next()) {
						final IBasicTable table = getColumnableFromResultSet(context,
								IBasicTable.class, rset);
						tables.put(table.getName(), table);
						CaptureHelper.updateMonitor(monitor, getCounter(), 2, 1);
					}
				} finally {
					CaptureHelper.safeClose(rset, null);
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch tables from " + getConnectionVendorName(context)
					+ " server: " + sqle.getMessage(), sqle);
		}

		monitor.subTask("Retrieving tables columns and primary keys...");
		List<String> invalidTableNames = new ArrayList<String>();
		for (IBasicTable table : tables.values()) {
			final String tableName = table.getName();

			// monitor.subTask("Retrieving columns for table " + tableName + "..."); //$NON-NLS-2$
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("== Retrieving columns for table [" + tableName + "] =="); //$NON-NLS-1$ //$NON-NLS-2$

			final IBasicColumn[] columns = getColumns(context, monitor, table);
			if (columns.length > 0) {
				for (IBasicColumn column : columns) {
					column.setParent(table);
					table.addColumn(column);
					allTablesColumns.put(CaptureHelper.getUniqueColumnName(column), column);
				}

				// monitor.subTask("Retrieving primary key for table " + tableName + "..."); //$NON-NLS-2$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("== Retrieving primary key for table [" + tableName + "] =="); //$NON-NLS-1$ //$NON-NLS-2$

				final UniqueKeyConstraint pk = getTablePrimaryKey(context, monitor,
						allTablesColumns, table);
				if (pk != null) {
					pk.setConstrainedTable(table);
					table.addConstraint(pk);
					CaptureHelper.updateMonitor(monitor, getCounter(), 2, 1);
				}
			} else {
				LOGGER.warn("Table [" + tableName
						+ "] has been ignored during import because the table's columns "
						+ "could not be retrieved");
				/*
				 * Since the table's columns could not be retrieved, we mark the
				 * table as invalid so can remove it from the captured list.
				 */
				invalidTableNames.add(tableName);
			}
		}

		/*
		 * Removes from the list the tables marked as invalid before retrieving
		 * foreign keys information.
		 */
		for (String tableName : invalidTableNames) {
			tables.remove(tableName);
		}

		/*
		 * We need to initialize all tables with their columns and primary key
		 * before retrieving foreign keys because each foreign key might
		 * reference the primary key of any other table.
		 */
		monitor.subTask("Retrieving tables foreign keys...");
		for (IBasicTable table : tables.values()) {
			final String tableName = table.getName();

			// monitor.subTask("Retrieving foreign keys for table " + tableName + "..."); //$NON-NLS-2$
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("== Retrieving foreign keys for table [" + tableName + "] =="); //$NON-NLS-1$ //$NON-NLS-2$

			final Collection<ForeignKeyConstraint> keys = getTableForeignKeys(context, monitor,
					tables, allTablesColumns, table);

			for (IKeyConstraint fk : keys) {
				table.addConstraint(fk);
				CaptureHelper.updateMonitor(monitor, getCounter(), 2, 1);
			}
		}

		return tables.values();
	}

	/**
	 * Returns an array of the columns of the specified database object found in
	 * the data source pointed to by the connection object provided by the
	 * specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.
	 * 
	 * @param context
	 *            a {@link ICaptureContext} to get the connection object to the
	 *            data source and the list of previously captured objects
	 * @param monitor
	 *            the {@link IProgressMonitor} to notify while capturing objects
	 * @param dbObj
	 *            a {@link IDatabaseObject} representing the database object for
	 *            which columns must be captured
	 * @return an array of {@link IBasicColumn} objects, an empty array if no
	 *         columns could be found
	 */
	private IBasicColumn[] getColumns(ICaptureContext context, IProgressMonitor monitor,
			IDatabaseObject<?> dbObj) {
		ColumnsSorter colSorter = new ColumnsSorter();

		final String dbObjName = dbObj.getName();
		try {
			final DatabaseMetaData md = ((Connection) context.getConnectionObject()).getMetaData();
			ResultSet rset = null;
			if (md != null) {
				rset = md.getColumns(getObjectOrContextCatalog(context, dbObj),
						getObjectOrContextSchema(context, dbObj), dbObjName, null);
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);
			}

			if (rset != null) {
				try {
					int pos = 0;
					while (rset.next()) {
						final IBasicColumn column = getColumnFromResultSet(context, rset, dbObjName);
						colSorter.addColumn(column, pos++);
						CaptureHelper.updateMonitor(monitor, getCounter(), 5, 1);
					}
				} finally {
					CaptureHelper.safeClose(rset, null);
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch columns for table or view [" + dbObjName + "] from "
					+ getConnectionVendorName(context) + " server: " + sqle.getMessage(), sqle);
		}

		return colSorter.getColumnsSortedArray();
	}

	/**
	 * Returns a <code>IBasicColumn</code> object representing the column
	 * described in the currently selected row of the specified
	 * <code>ResultSet</code>.
	 * 
	 * @param rset
	 *            a {@link ResultSet} where each line is a description of a
	 *            column; must not be <code>null</code>
	 * @param dbObjName
	 *            the name of the database object for which the
	 *            {@link IBasicColumn} must be created
	 * @return a {@link IBasicColumn} object if the column name in the currently
	 *         selected row of the specified <code>ResultSet</code> is not
	 *         <code>null</code> or empty, <code>null</code> otherwise
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	private IBasicColumn getColumnFromResultSet(ICaptureContext context, ResultSet rset,
			String dbObjName) throws SQLException {
		IBasicColumn column = null;

		final String columnName = rset.getString(COLUMN_NAME_COLUMN_NAME);
		final int position = rset.getInt(COLUMN_NAME_ORDINAL_POSITION);
		final String dsTypeName = rset.getString(COLUMN_NAME_TYPE_NAME);
		// final String sqlTypeName = rset.getString(COLUMN_NAME_DATA_TYPE);
		final int length = rset.getInt(COLUMN_NAME_COLUMN_SIZE);
		final int precision = rset.getInt(COLUMN_NAME_DECIMAL_DIGITS);
		final boolean nullable = (rset.getInt(COLUMN_NAME_NULLABLE) == DatabaseMetaData.columnNullable);

		String desc = ""; //$NON-NLS-1$
		try {
			desc = rset.getString(COLUMN_NAME_COMMENT);
		} catch (SQLException sqle) {
			LOGGER.warn("Table column [" + dbObjName + "][" //$NON-NLS-2$
					+ (null == columnName ? "" : columnName) + "]" //$NON-NLS-1$ //$NON-NLS-2$
					+ " comment could not be fetched from database", sqle);
		}

		String defaultValue = ""; //$NON-NLS-1$
		try {
			defaultValue = rset.getString(COLUMN_NAME_COLUMN_DEF);
		} catch (SQLException sqle) {
			LOGGER.warn("Table column [" + dbObjName + "][" //$NON-NLS-2$
					+ (null == columnName ? "" : columnName) + "]" //$NON-NLS-1$ //$NON-NLS-2$
					+ " default value could not be fetched from database", sqle);
		}

		if (columnName != null && !"".equals(columnName.trim())) { //$NON-NLS-1$
			if (LOGGER.isDebugEnabled()) {
				String logPrefix = "[" + dbObjName + "][" + columnName + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				LOGGER.debug("= " + logPrefix + " Column Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_ORDINAL_POSITION + "] " + position); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_TYPE_NAME + "] " + dsTypeName); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_COLUMN_SIZE + "] " + length); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_DECIMAL_DIGITS + "] " + precision); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_NULLABLE + "] " + nullable); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_COMMENT + "] " + desc); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_COLUMN_DEF + "] " + defaultValue); //$NON-NLS-1$ //$NON-NLS-2$
			}

			column = typedObjFactory.create(IBasicColumn.class);
			column.setName(getConnectionVendor(context).getNameFormatter().format(columnName));
			column.setDescription(desc);
			column.setRank(position - 1);
			column.setNotNull(!nullable);
			column.setDefaultExpr(defaultValue != null ? defaultValue.trim() : ""); //$NON-NLS-1$

			final IDatatype datatype = new Datatype(dsTypeName);
			if (!DBGMHelper.getDatatypeProvider(getConnectionVendor(context))
					.getUnsizableDatatypes().contains(datatype.getName())) {
				datatype.setLength(length);
				datatype.setPrecision(precision);
			}
			column.setDatatype(datatype);
		}

		return column;
	}

	/**
	 * Returns a <code>UniqueKeyConstraint</code> object representing the
	 * primary key of the specified table present in the data source pointed to
	 * by the connection object provided by the specified <code>context</code>
	 * and notifies the specified <code>monitor</code> while capturing.
	 * 
	 * @param context
	 *            a {@link ICaptureContext} to store the captured objects
	 * @param monitor
	 *            the {@link IProgressMonitor} to notify while capturing objects
	 * @param allTablesColumns
	 *            a <code>Map</code> of all columns previously captured
	 * @param table
	 *            the {@link IBasicTable} for which the primary key must be
	 *            captured
	 * @return a {@link UniqueKeyConstraint} object if the specified table has a
	 *         primary key, <code>null</code> otherwise
	 */
	private UniqueKeyConstraint getTablePrimaryKey(ICaptureContext context,
			IProgressMonitor monitor, Map<String, IBasicColumn> allTablesColumns, IBasicTable table) {
		UniqueKeyConstraint pk = null;

		final String tableName = table.getName();
		try {
			final DatabaseMetaData md = ((Connection) context.getConnectionObject()).getMetaData();

			ResultSet rset = null;
			if (md != null) {
				rset = md.getPrimaryKeys(getObjectOrContextCatalog(context, table),
						getObjectOrContextSchema(context, table), tableName);
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);
			}

			if (rset != null) {
				ColumnsSorter pkColumnsSorter = null;

				try {
					while (rset.next()) {
						final String pkName = rset.getString(COLUMN_NAME_PK_NAME);
						final String pkColumnName = rset.getString(COLUMN_NAME_COLUMN_NAME);

						/*
						 * We need to fetch the column's index in the primary
						 * key because columns are ordered by COLUMN_NAME in the
						 * ResultSet.
						 */
						final short position = rset.getShort(COLUMN_NAME_KEY_SEQ);

						if (pkName != null && !"".equals(pkName.trim())) { //$NON-NLS-1$
							if (LOGGER.isDebugEnabled()) {
								String logPrefix = "[" + tableName + "][" + pkName + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								LOGGER.debug("= " + logPrefix + " Primary Key Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_COLUMN_NAME + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ pkColumnName);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_KEY_SEQ + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ position);
							}

							if (null == pk) {
								pk = typedObjFactory.create(UniqueKeyConstraint.class);
								pk.setName(getConnectionVendor(context).getNameFormatter().format(
										pkName));
								pk.setConstraintType(ConstraintType.PRIMARY);
								pkColumnsSorter = new ColumnsSorter();
							}

							if (pkColumnsSorter != null && pkColumnName != null
									&& !"".equals(pkColumnName.trim())) { //$NON-NLS-1$
								final IBasicColumn pkColumn = allTablesColumns.get(CaptureHelper
										.getUniqueObjectName(tableName, pkColumnName));
								if (pkColumn != null) {
									pkColumnsSorter.addColumn(pkColumn, position);
								} else {
									// TODO [BGA]: Raise a warning and manage
									// the PK creation
									// cancellation
								}
							}
						}
					}
				} finally {
					CaptureHelper.safeClose(rset, null);
				}

				if (pk != null && pkColumnsSorter != null) {
					int index = 0;
					for (IBasicColumn column : pkColumnsSorter.getColumnsSortedArray()) {
						pk.addConstrainedColumn(index++, column);
					}
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch primary key for table [" + tableName + "] from "
					+ getConnectionVendorName(context) + " server: " + sqle.getMessage(), sqle);
		}

		return pk;
	}

	/**
	 * Returns a <code>Collection</code> of the foreign keys of the specified
	 * table present in the data source pointed to by the connection object
	 * provided by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.
	 * 
	 * @param context
	 *            a {@link ICaptureContext} to store the captured objects
	 * @param monitor
	 *            the {@link IProgressMonitor} to notify while capturing objects
	 * @param allTables
	 *            a <code>Map</code> of all tables previously captured
	 * @param allTablesColumns
	 *            a <code>Map</code> of all columns previously captured
	 * @param table
	 *            the {@link IBasicTable} for which foreign keys must be
	 *            captured
	 * @return a {@link Collection} of {@link ForeignKeyConstraint} objects if
	 *         the specified table has foreign keys, an empty
	 *         <code>Collection</code> otherwise
	 */
	private Collection<ForeignKeyConstraint> getTableForeignKeys(ICaptureContext context,
			IProgressMonitor monitor, Map<String, IBasicTable> allTables,
			Map<String, IBasicColumn> allTablesColumns, IBasicTable table) {
		Collection<ForeignKeyConstraint> foreignKeys = new ArrayList<ForeignKeyConstraint>();
		IFormatter formatter = getConnectionVendor(context).getNameFormatter();

		final String tableName = table.getName();
		try {
			final DatabaseMetaData md = ((Connection) context.getConnectionObject()).getMetaData();

			ResultSet rset = null;
			if (md != null) {
				rset = md.getImportedKeys(getObjectOrContextCatalog(context, table),
						getObjectOrContextSchema(context, table), tableName);
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);
			}

			if (rset != null) {
				ForeignKeyConstraint currFk = null;
				String currFkName = null;
				boolean keyIsValid = false;

				try {
					while (rset.next()) {
						final String fkName = rset.getString(COLUMN_NAME_FK_NAME);
						final String fkColumnName = rset.getString(COLUMN_NAME_FKCOLUMN_NAME);
						final String pkTableName = rset.getString(COLUMN_NAME_PKTABLE_NAME);
						final String pkName = rset.getString(COLUMN_NAME_PK_NAME);
						final short onUpdateRule = rset.getShort(COLUMN_NAME_UPDATE_RULE);
						final short onDeleteRule = rset.getShort(COLUMN_NAME_DELETE_RULE);
						final short deferrability = rset.getShort(COLUMN_NAME_DEFERRABILITY);

						if (fkName != null && !"".equals(fkName.trim())) { //$NON-NLS-1$
							if (LOGGER.isDebugEnabled()) {
								String logPrefix = "[" + tableName + "][" + fkName + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								LOGGER.debug("= " + logPrefix + " Foreign Key Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_FKCOLUMN_NAME + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ fkColumnName);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_PKTABLE_NAME + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ pkTableName);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_PK_NAME + "] " + pkName); //$NON-NLS-1$ //$NON-NLS-2$
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_UPDATE_RULE + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ onUpdateRule);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_DELETE_RULE + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ onDeleteRule);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_DEFERRABILITY + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ deferrability);
							}

							if (null == currFkName || !currFkName.equals(fkName) || keyIsValid) {
								currFkName = fkName;
								final String formatFkName = formatter.format(fkName);
								final String formatFkColumnName = formatter.format(fkColumnName);

								/*
								 * We need to check for each foreign key's
								 * column that the referenced table exists in
								 * the current context because some columns
								 * might be pointing to a synonym.
								 */
								final String formatPkTableName = formatter.format(pkTableName);
								IBasicTable pkTable = allTables.get(formatPkTableName);

								if (pkTable != null) {

									if (null == currFk || !formatFkName.equals(currFk.getName())) {
										final IKeyConstraint refPk = DBGMHelper
												.getPrimaryKey(pkTable);

										if (refPk != null) {
											/*
											 * FIXME [BGA]: The
											 * TypedObjectFactory does not work
											 * as UniqueKeyConstraint and
											 * ForeignKeyConstraint classes have
											 * the same super interface
											 * IKeyConstraint. We use an
											 * explicit constructor instead.
											 */
											// currFk = typedObjFactory
											// .create(ForeignKeyConstraint.class);
											// currFk.setName(formatFkName);
											// currFk.setConstrainedTable(pkTable);
											currFk = new ForeignKeyConstraint(formatFkName, "", //$NON-NLS-1$
													pkTable);

											currFk.setRemoteConstraint(refPk);
											currFk.setOnUpdateAction(CaptureHelper
													.getForeignKeyAction(onUpdateRule));
											currFk.setOnDeleteAction(CaptureHelper
													.getForeignKeyAction(onDeleteRule));
											foreignKeys.add(currFk);
											keyIsValid = true;
										} else {
											LOGGER.warn("Foreign key ["
													+ formatFkName
													+ "] has been ignored during import because the referenced primary key ["
													+ formatPkTableName
													+ "[" //$NON-NLS-1$
													+ formatter.format(pkName)
													+ "]] could not be found in the current workspace");
											keyIsValid = false;
											continue;
										}
									}

									final IBasicColumn column = allTablesColumns.get(CaptureHelper
											.getUniqueObjectName(tableName, formatFkColumnName));
									if (column != null) {
										/*
										 * Columns are ordered by PKTABLE_NAME,
										 * KEY_SEQ in the returned ResultSet, so
										 * we don't have to specify the position
										 * of the constrained column when adding
										 * it to the foreign key constraint.
										 */
										currFk.addColumn(column);
									} else {
										LOGGER.warn("Foreign key ["
												+ formatFkName
												+ "] has been ignored during import because the referencing column ["
												+ tableName + "[" + formatFkColumnName //$NON-NLS-1$
												+ "]] could not be found in the current workspace");
										keyIsValid = false;

										/*
										 * Now the foreign key is invalid, we
										 * remove it from the foreign keys list
										 * that will be returned to the caller
										 * of this method.
										 */
										foreignKeys.remove(currFk);
									}
								} else {
									if (LOGGER.isDebugEnabled()) {
										LOGGER.debug("Foreign key column ["
												+ formatFkName
												+ "[" //$NON-NLS-1$
												+ formatFkColumnName
												+ "]] has been ignored during import because the referenced table ["
												+ formatPkTableName
												+ "] could not be found in the current workspace");
									}
								}
							}
						}
					}
				} finally {
					CaptureHelper.safeClose(rset, null);
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch foreign keys for table [" + tableName + "] from "
					+ getConnectionVendorName(context) + " server: " + sqle.getMessage(), sqle);
		}

		return foreignKeys;
	}

	@Override
	public Collection<IIndex> getIndexes(ICaptureContext context, IProgressMonitor monitor) {
		Collection<IIndex> indexes = new ArrayList<IIndex>();
		final Collection<Object> objects = context.getCapturedObjects(IElementType
				.getInstance(IBasicTable.TYPE_ID));

		if (objects != null) {
			for (Object obj : objects) {
				final IBasicTable table = (IBasicTable) obj;
				final String tableName = table.getName();

				// monitor.subTask("Retrieving indexes for table " + tableName + "..."); //$NON-NLS-2$
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("== Retrieving indexes for table [" + tableName + "] =="); //$NON-NLS-1$ //$NON-NLS-2$

				final Collection<IIndex> tableIndexes = getTableIndexes(context, monitor, table);

				for (IIndex index : tableIndexes) {
					index.setIndexedTableRef(table.getReference());
					table.addIndex(index);
					indexes.add(index);
					CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);
				}
			}
		}
		return indexes;
	}

	/**
	 * Returns a <code>Collection</code> of the indexes for the specified table
	 * present in the data source pointed to by the connection object provided
	 * by the specified <code>context</code> and notifies the specified
	 * <code>monitor</code> while capturing.
	 * 
	 * @param context
	 *            a {@link ICaptureContext} to store the captured objects
	 * @param monitor
	 *            the {@link IProgressMonitor} to notify while capturing objects
	 * @param table
	 *            the {@link IBasicTable} for which foreign keys must be
	 *            captured
	 * @return a {@link Collection} of {@link IIndex} objects if the specified
	 *         table has indexes, an empty <code>Collection</code> otherwise
	 */
	private Collection<IIndex> getTableIndexes(ICaptureContext context, IProgressMonitor monitor,
			IBasicTable table) {
		Collection<IIndex> indexes = new ArrayList<IIndex>();
		IFormatter formatter = getConnectionVendor(context).getNameFormatter();

		final String tableName = table.getName();
		try {
			final DatabaseMetaData md = ((Connection) context.getConnectionObject()).getMetaData();

			ResultSet rset = null;
			if (md != null) {
				rset = md.getIndexInfo(getObjectOrContextCatalog(context, table),
						getObjectOrContextSchema(context, table), tableName, false, false);
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);
			}

			if (rset != null) {
				IIndex currIndex = null;
				String currIndexName = null;
				boolean indexIsValid = false;

				try {
					while (rset.next()) {
						final String indexName = rset.getString(COLUMN_NAME_INDEX_NAME);
						final boolean nonUnique = rset.getBoolean(COLUMN_NAME_NON_UNIQUE);
						final String indexColumnName = rset.getString(COLUMN_NAME_COLUMN_NAME);
						final String ascOrDesc = rset.getString(COLUMN_NAME_ASC_OR_DESC);
						final short indexType = rset.getShort(COLUMN_NAME_TYPE);

						if (indexName != null && !"".equals(indexName.trim())) { //$NON-NLS-1$
							if (LOGGER.isDebugEnabled()) {
								String logPrefix = "[" + indexName + "]"; //$NON-NLS-1$ //$NON-NLS-2$
								LOGGER.debug("= " + logPrefix + " Index Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_INDEX_NAME + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ indexName);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_NON_UNIQUE + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ nonUnique);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_COLUMN_NAME + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ indexColumnName);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_ASC_OR_DESC + "] " //$NON-NLS-1$ //$NON-NLS-2$
										+ ascOrDesc);
								LOGGER.debug(logPrefix + "[" + COLUMN_NAME_TYPE + "] " + indexType); //$NON-NLS-1$ //$NON-NLS-2$
							}

							if (null == currIndexName || !currIndexName.equals(indexName)
									|| indexIsValid) {
								currIndexName = indexName;
								final String formatIndexName = formatter.format(indexName);
								final String formatIndexColumnName = formatter
										.format(indexColumnName);

								if (null == currIndex
										|| !formatIndexName.equals(currIndex.getIndexName())) {
									IVersionable<IIndex> v = VersionableFactory.createVersionable(
											IIndex.class, context.getConnection().getDBVendor());
									currIndex = v.getVersionnedObject().getModel();
									currIndex.setName(formatIndexName);
									currIndex.setIndexType(nonUnique ? CaptureHelper
											.getIndexType(indexType) : IndexType.UNIQUE);
									indexes.add(currIndex);
									indexIsValid = true;
								}

								final IBasicColumn column = (IBasicColumn) context
										.getCapturedObject(IElementType
												.getInstance(IBasicColumn.TYPE_ID), CaptureHelper
												.getUniqueObjectName(tableName,
														formatIndexColumnName));
								if (column != null) {
									/*
									 * Columns are ordered by INDEX_NAME,
									 * ORDINAL_POSITION in the returned
									 * ResultSet, so we don't have to specify
									 * the position of the index column when
									 * adding it to the index.
									 */
									currIndex.addColumnRef(column.getReference());
								} else {
									LOGGER.warn("Index ["
											+ formatIndexName
											+ "] has been partially captured during import because the referencing column ["
											+ tableName + "[" + formatIndexColumnName //$NON-NLS-1$
											+ "]] could not be found in the current workspace");
									indexIsValid = false;

									/*
									 * Now the index is invalid, we remove it
									 * from the indexes list that will be
									 * returned to the caller of this method.
									 */
									indexes.remove(currIndex);
								}
							}
						}
					}
				} finally {
					CaptureHelper.safeClose(rset, null);
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch indexes for table [" + tableName + "] from "
					+ getConnectionVendorName(context) + " server: " + sqle.getMessage(), sqle);
		}

		return indexes;
	}

	/**
	 * This implementation is very basic since views SQL definition cannot be
	 * retrieved with {@link DatabaseMetaData} methods. Returned views will only
	 * have their name and columns defined.
	 */
	@Override
	public Collection<IView> getViews(ICaptureContext context, IProgressMonitor monitor) {
		Map<String, IView> views = new HashMap<String, IView>();
		ISQLCommandWriter commandWriter = SQLGenPlugin.getService(IGenerationService.class)
				.getSQLCommandWriter(getConnectionVendor(context));

		try {
			final DatabaseMetaData md = ((Connection) context.getConnectionObject()).getMetaData();

			ResultSet rset = null;
			if (md != null) {
				rset = md.getTables(context.getCatalog(), context.getSchema(), null, DB_VIEW_TYPE);
				CaptureHelper.updateMonitor(monitor, getCounter(), 1, 1);
			}

			if (rset != null) {
				try {
					while (rset.next()) {
						final IView view = getColumnableFromResultSet(context, IView.class, rset);
						view.setSql(commandWriter
								.comment("View SQL definition not available with the default JDBC capturer"));
						views.put(view.getName(), view);
						CaptureHelper.updateMonitor(monitor, getCounter(), 2, 1);
					}
				} finally {
					CaptureHelper.safeClose(rset, null);
				}
			}
		} catch (SQLException sqle) {
			LOGGER.error("Unable to fetch views from " + getConnectionVendorName(context)
					+ " server: " + sqle.getMessage(), sqle);
		}

		List<String> invalidViewNames = new ArrayList<String>();
		for (IView view : views.values()) {
			final String viewName = view.getName();

			// monitor.subTask("Retrieving column aliases for view " + viewName + "..."); //$NON-NLS-2$
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("== Retrieving column aliases for view [" + viewName + "] =="); //$NON-NLS-1$ //$NON-NLS-2$

			final IBasicColumn[] columns = getColumns(context, monitor, view);
			if (columns.length > 0) {
				for (IBasicColumn column : columns) {
					view.addColumnAlias(column.getName());
				}
			} else {
				LOGGER.warn("View [" + viewName
						+ "] has been ignored during import because the view's columns "
						+ "could not be retrieved");
				/*
				 * Since the view's columns could not be retrieved, we mark the
				 * view as invalid so can remove it from the captured list.
				 */
				invalidViewNames.add(viewName);
			}
		}

		// Removes from the list the views marked as invalid before returning it
		for (String viewName : invalidViewNames) {
			views.remove(viewName);
		}

		return views.values();
	}

	/**
	 * Returns a <code>IColumnable</code> object representing the table or view
	 * described in the currently selected row of the specified
	 * <code>ResultSet</code>.
	 * 
	 * @param clazz
	 *            class for which we want to create a database object
	 * @param rset
	 *            a {@link ResultSet} where each line is a description of a
	 *            table; must not be <code>null</code>
	 * @return a {@link IColumnable} object if the object name in the currently
	 *         selected row of the specified <code>ResultSet</code> is not
	 *         <code>null</code> or empty, <code>null</code> otherwise
	 * @throws SQLException
	 *             if a database access error occurs
	 */
	private <T extends IColumnable> T getColumnableFromResultSet(ICaptureContext context,
			Class<T> clazz, ResultSet rset) throws SQLException {
		T columnable = null;

		final String catalog = rset.getString(COLUMN_NAME_TABLE_CAT);
		final String schema = rset.getString(COLUMN_NAME_TABLE_SCHEM);
		final String name = rset.getString(COLUMN_NAME_TABLE_NAME);
		String desc = ""; //$NON-NLS-1$
		try {
			desc = rset.getString(COLUMN_NAME_COMMENT);
		} catch (SQLException sqle) {
			LOGGER.warn("Table or view [" + (null == name ? "" : name) + "]" //$NON-NLS-2$ //$NON-NLS-3$
					+ " comment could not be fetched from database", sqle);
		}

		if (name != null && !"".equals(name.trim())) { //$NON-NLS-1$
			if (LOGGER.isDebugEnabled()) {
				String logPrefix = "[" + name + "]"; //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug("= " + logPrefix + " Table or view Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_TABLE_CAT + "] " + catalog); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_TABLE_SCHEM + "] " + schema); //$NON-NLS-1$ //$NON-NLS-2$
				LOGGER.debug(logPrefix + "[" + COLUMN_NAME_COMMENT + "] " + desc); //$NON-NLS-1$ //$NON-NLS-2$
			}

			IVersionable<T> v = VersionableFactory.createVersionable(clazz, context.getConnection()
					.getDBVendor());
			columnable = v.getVersionnedObject().getModel();
			columnable.setName(getConnectionVendor(context).getNameFormatter().format(name));
			if (desc != null && !"".equals(desc.trim())) { //$NON-NLS-1$
				columnable.setDescription(desc);
			}

			// Setting catalog and schema names as attributes in the context
			context.setDbObjectAttribute((IDatabaseObject<?>) columnable, ATTR_CATALOG, catalog);
			context.setDbObjectAttribute((IDatabaseObject<?>) columnable, ATTR_SCHEMA, schema);
		}

		return columnable;
	}

	private DBVendor getConnectionVendor(ICaptureContext context) {
		return context.getConnection().getDBVendor();
	}

	private String getConnectionVendorName(ICaptureContext context) {
		return getConnectionVendor(context).name();
	}

	/**
	 * Convenience method to return the specified object catalog name if a
	 * specific catalog name has been saved in the capture context for this
	 * object, or the default catalog name that was set in the capture context
	 * before starting the capture process.
	 * 
	 * @param context
	 *            the current capture context
	 * @param dbObj
	 *            the database object for which we must return a catalog name
	 * @return the catalog name associated with the specified database object if
	 *         it exists, the default catalog name set in the specified capture
	 *         context otherwise
	 */
	private String getObjectOrContextCatalog(ICaptureContext context, IDatabaseObject<?> dbObj) {
		String objCatalog = context.getDbObjectAttributeValue(dbObj, ATTR_CATALOG);
		if (objCatalog == null) {
			return context.getCatalog();
		}
		return objCatalog;
	}

	/**
	 * Convenience method to return the specified object schema name if a
	 * specific schema name has been saved in the capture context for this
	 * object, or the default schema name that was set in the capture context
	 * before starting the capture process.
	 * 
	 * @param context
	 *            the current capture context
	 * @param dbObj
	 *            the database object for which we must return a catalog name
	 * @return the schema name associated with the specified database object if
	 *         it exists, the default schema name set in the specified capture
	 *         context otherwise
	 */
	private String getObjectOrContextSchema(ICaptureContext context, IDatabaseObject<?> dbObj) {
		String objSchema = context.getDbObjectAttributeValue(dbObj, ATTR_SCHEMA);
		if (objSchema == null) {
			return context.getSchema();
		}
		return objSchema;
	}

}
