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
package com.nextep.designer.sqlgen.postgre.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.dbgm.model.TriggerEvent;
import com.nextep.datadesigner.dbgm.model.TriggerTime;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.CustomProgressMonitor;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.ICaptureContext;
import com.nextep.designer.sqlgen.model.ICapturer;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;
import com.nextep.designer.sqlgen.model.base.AbstractCapturer;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * PostgreSql database capturer.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PostgreSqlCapturer extends AbstractCapturer {

	private static final Log LOGGER = LogFactory.getLog(PostgreSqlDatabaseConnector.class);
	private static final int PROGRESS_RANGE = 100;

	private final Map<String, String> datatypeConversionMap = new HashMap<String, String>();
	private final List<String> parameteredTypes;
	private ICapturer jdbcCapturer;
	private final String NEWLINE;

	public PostgreSqlCapturer() {
		datatypeConversionMap.put("int8", "bigint"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("timestamptz", "timestamp with time zone"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("varchar", "character varying"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("_varchar", "character varying[]"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("_int8", "bigint[]"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("serial8", "bigserial"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("varbit", "bit varying"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("bool", "boolean"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("float8", "double precision"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("int4", "integer"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("int2", "smallint"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("int", "integer"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("serial4", "serial"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("bpchar", "character"); //$NON-NLS-1$ //$NON-NLS-2$
		datatypeConversionMap.put("_numeric", "numeric[]"); //$NON-NLS-1$ //$NON-NLS-2$
		parameteredTypes = Arrays.asList(
				"bit", "varbit", "varchar", "char", "character", "character varying", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"interval", "numeric", "decimal", "time", "timestamp"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
	}

	@Override
	public void initialize(IConnection conn, IMutableCaptureContext context) {
		jdbcCapturer = SQLGenPlugin.getService(ICaptureService.class).getCapturer(DBVendor.JDBC);
		super.initialize(conn, context);
	}

	boolean isInherited(IBasicColumn col, IBasicTable t, Map<IReference, IBasicTable> tablesRefMap) {
		IVersionable<?> v = (IVersionable<?>) t;
		if (v == null) {
			return false;
		}

		IPostgreSqlTable postgreTable = (IPostgreSqlTable) v.getVersionnedObject().getModel();
		for (IReference ref : postgreTable.getInheritances()) {
			final IReferenceable refTable = tablesRefMap.get(ref);
			if (refTable instanceof IColumnable) {
				final IColumnable table = (IColumnable) refTable;
				for (IBasicColumn c : table.getColumns()) {
					if (c.getName().equals(col.getName())) {
						return true;
					}
				}
			}

		}
		return false;
	}

	@Override
	public Collection<IBasicTable> getTables(ICaptureContext context, IProgressMonitor monitor) {
		final Collection<IBasicTable> tables = jdbcCapturer.getTables(context, monitor);
		Map<String, IBasicTable> tablesMap = new HashMap<String, IBasicTable>();
		Map<IReference, IBasicTable> tablesRefMap = new HashMap<IReference, IBasicTable>();
		Map<String, IKeyConstraint> ukMap = new HashMap<String, IKeyConstraint>();
		Map<String, IBasicColumn> columnsMap = new HashMap<String, IBasicColumn>();

		final Connection conn = (Connection) context.getConnectionObject();
		Statement stmt = null;
		ResultSet rset = null;

		for (IBasicTable table : tables) {
			tablesMap.put(table.getName(), table);
			tablesRefMap.put(table.getReference(), table);
		}

		// Fetching PostGreSql inheritance relations

		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT c.relname AS name, p.relname AS parent " //$NON-NLS-1$
					+ "FROM pg_inherits " //$NON-NLS-1$
					+ "  JOIN pg_class AS c ON (inhrelid=c.oid) " //$NON-NLS-1$
					+ "  JOIN pg_class as p ON (inhparent=p.oid) " //$NON-NLS-1$
					+ "ORDER BY 1"); //$NON-NLS-1$

			while (rset.next()) {
				monitor.worked(1);
				final String tabName = rset.getString(1);
				final String inheritsFrom = rset.getString(2);

				IVersionable<?> v = (IVersionable<?>) tablesMap.get(tabName);
				if (v == null) {
					LOGGER.warn("Skipping inherits constraint '" + inheritsFrom + "' on table "
							+ tabName + ": child table not in the imported set.");
					continue;
				}
				IVersionable<?> vi = (IVersionable<?>) tablesMap.get(inheritsFrom);
				if (vi == null) {
					LOGGER.warn("Skipping inherits constraint '" + inheritsFrom + "' on table "
							+ tabName + ": parent table not in the imported set.");
					continue;
				}
				IPostgreSqlTable t = (IPostgreSqlTable) v.getVersionnedObject().getModel();
				IPostgreSqlTable i = (IPostgreSqlTable) vi.getVersionnedObject().getModel();

				t.addInheritance(i);
			}

		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, null);
		}

		for (IBasicTable table : tables) {
			// tablesMap.put(table.getName(), table);
			List<IBasicColumn> ir = new ArrayList<IBasicColumn>();

			for (IBasicColumn c : table.getColumns()) {
				if (!isInherited(c, table, tablesRefMap)) {
					columnsMap.put(CaptureHelper.getUniqueColumnName(c), c);
					final IDatatype colType = c.getDatatype();
					final IDatatype d = convertDatatype(colType);
					final String dataDefault = c.getDefaultExpr();
					if ("SERIAL".equalsIgnoreCase(d.getName()) && dataDefault != null //$NON-NLS-1$
							&& !"".equals(dataDefault.trim())) { //$NON-NLS-1$
						d.setName("INTEGER"); //$NON-NLS-1$
					}
					if ("BIGSERIAL".equalsIgnoreCase(d.getName()) && dataDefault != null //$NON-NLS-1$
							&& !"".equals(dataDefault.trim())) { //$NON-NLS-1$
						d.setName("BIGINT"); //$NON-NLS-1$
					}
					if ("TIMESTAMP".equalsIgnoreCase(d.getName())) { //$NON-NLS-1$
						d.setLength(0);
						d.setPrecision(0);
					}
					// Fix for bug DES-933 regarding interval data types
					if ("INTERVAL".equalsIgnoreCase(d.getName())) { //$NON-NLS-1$
						d.setLength(d.getPrecision());
						d.setPrecision(0);
					}
					if (("VARCHAR".equalsIgnoreCase(d.getName())) //$NON-NLS-1$
							|| ("CHARACTER VARYING".equalsIgnoreCase(d.getName()))) { //$NON-NLS-1$
						if (d.getLength() == Integer.MAX_VALUE) {
							d.setLength(0);
						}
					}
					if ("NUMERIC".equalsIgnoreCase(d.getName())) { //$NON-NLS-1$
						if (d.getLength() == 131089) {
							d.setLength(0);
						}
					}
					c.setDatatype(d);
				} else {
					ir.add(c);
					LOGGER.warn("Not adding column '" + c.getName() + "' on table "
							+ table.getName() + ": inherited.");
				}
			}
			for (IBasicColumn c : ir) {
				table.removeColumn(c);
			}
		}

		// Fetching unique constraints
		try {
			stmt = conn.createStatement();
			stmt.execute("SELECT tc.constraint_name,tc.constraint_type, " //$NON-NLS-1$
					+ "          tc.table_name, kcu.column_name, tc.is_deferrable, " //$NON-NLS-1$
					+ "          tc.initially_deferred, " //$NON-NLS-1$
					+ "          ccu.table_name AS references_table, " //$NON-NLS-1$
					+ "          ccu.column_name AS references_field " //$NON-NLS-1$
					+ "     FROM information_schema.table_constraints tc " //$NON-NLS-1$
					+ "LEFT JOIN information_schema.key_column_usage kcu " //$NON-NLS-1$
					+ "       ON tc.constraint_catalog = kcu.constraint_catalog " //$NON-NLS-1$
					+ "      AND tc.constraint_schema = kcu.constraint_schema " //$NON-NLS-1$
					+ "      AND tc.constraint_name = kcu.constraint_name " //$NON-NLS-1$
					+ "LEFT JOIN information_schema.referential_constraints rc " //$NON-NLS-1$
					+ "       ON tc.constraint_catalog = rc.constraint_catalog " //$NON-NLS-1$
					+ "      AND tc.constraint_schema = rc.constraint_schema " //$NON-NLS-1$
					+ "      AND tc.constraint_name = rc.constraint_name " //$NON-NLS-1$
					+ "LEFT JOIN information_schema.constraint_column_usage ccu " //$NON-NLS-1$
					+ "       ON rc.unique_constraint_catalog = ccu.constraint_catalog" //$NON-NLS-1$
					+ "      AND rc.unique_constraint_schema = ccu.constraint_schema" //$NON-NLS-1$
					+ "      AND rc.unique_constraint_name = ccu.constraint_name" //$NON-NLS-1$
					+ "    WHERE tc.constraint_schema not in ( 'information_schema', 'pg_catalog') " //$NON-NLS-1$
					+ "		 and tc.constraint_type='UNIQUE'"); //$NON-NLS-1$
			rset = stmt.getResultSet();
			IKeyConstraint currentUk = null;

			while (rset.next()) {
				final String name = rset.getString(1);
				final String tableName = rset.getString(3);
				final String columnName = rset.getString(4);
				if (currentUk == null || !name.equals(currentUk.getName())) {
					currentUk = CorePlugin.getTypedObjectFactory()
							.create(UniqueKeyConstraint.class);
					currentUk.setName(name);
					final IBasicTable table = tablesMap.get(tableName);
					if (table != null) {
						currentUk.setConstrainedTable(table);
						table.addConstraint(currentUk);
						ukMap.put(name, currentUk);
					} else {
						continue;
					}
				}
				final IBasicColumn column = columnsMap.get(CaptureHelper.getUniqueObjectName(
						tableName, columnName));
				if (column != null) {
					currentUk.addColumn(column);
				} else {
					LOGGER.warn(MessageFormat.format(
							SQLGenMessages.getString("capturer.columnNotFound"), //$NON-NLS-1$
							CaptureHelper.getUniqueObjectName(currentUk.getName(), columnName),
							tableName));
				}
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

		// Fetching check constraints
		try {
			stmt = conn.createStatement();
			rset = stmt.executeQuery("SELECT " //$NON-NLS-1$
					+ "  CASE " //$NON-NLS-1$
					+ "    WHEN contypid = 0 THEN conrelid::regclass::name " //$NON-NLS-1$
					+ "    ELSE contypid::regtype::name " //$NON-NLS-1$
					+ "  END AS TABLE, " //$NON-NLS-1$
					+ "  conname AS name, " //$NON-NLS-1$ 
					+ "  consrc AS expression " //$NON-NLS-1$
					+ "FROM pg_constraint c " //$NON-NLS-1$
					+ "  JOIN pg_namespace ON (connamespace = pg_namespace.oid) " //$NON-NLS-1$
					+ "  LEFT JOIN pg_class i ON (conname = relname) " //$NON-NLS-1$
					+ "  LEFT JOIN pg_tablespace t ON (i.reltablespace = t.oid) " //$NON-NLS-1$
					+ "  LEFT JOIN pg_am ON (relam = pg_am.oid) " //$NON-NLS-1$
					+ "WHERE (nspname != 'pg_catalog' AND nspname != 'information_schema') " //$NON-NLS-1$
					+ "  AND conislocal " //$NON-NLS-1$
					+ "  AND contype = 'c' " //$NON-NLS-1$
					+ " ORDER BY 1, 2"); //$NON-NLS-1$

			while (rset.next()) {
				monitor.worked(1);
				final String tabName = rset.getString(1);
				final String constraintName = rset.getString(2);
				final String conditionName = rset.getString(3);

				IVersionable<?> v = (IVersionable<?>) tablesMap.get(tabName);
				if (v == null) {
					LOGGER.warn("Skipping check constraint '" + constraintName + "' on table "
							+ tabName + ": table not in the imported set.");
					continue;
				}
				IPostgreSqlTable t = (IPostgreSqlTable) v.getVersionnedObject().getModel();
				ICheckConstraint c = CorePlugin.getTypedObjectFactory().create(
						ICheckConstraint.class);
				c.setConstrainedTable(t);
				c.setName(constraintName);
				c.setCondition(conditionName);
				t.addCheckConstraint(c);
			}

		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, null);
		}

		// Fetching PostgreSql tablespaces
		fillTablespaces("r", context, tablesMap, ITablePhysicalProperties.class); //$NON-NLS-1$
		// Filling primary keys in the unique key map for proper management of tablespaces
		for (IBasicTable t : tables) {
			UniqueKeyConstraint pk = DBGMHelper.getPrimaryKey(t);
			if (pk != null) {
				ukMap.put(pk.getName(), pk);
			}
		}
		fillTablespaces("i", context, ukMap, IIndexPhysicalProperties.class); //$NON-NLS-1$

		return tables;
	}

	/**
	 * Fills the tablespace information for the given relation type. This generic method takes a
	 * relation type which is used while querying postgresql schema table. The objects map is the
	 * index of the objects that should be filled hashed by their name.
	 * 
	 * @param relType the postgresql relation type to query in the dictionary
	 * @param context the current {@link ICaptureContext}
	 * @param objectsMap map of objects to fill
	 * @param physicalClass class of the {@link IPhysicalProperties} of the implementation interface
	 *        to instantiate
	 */
	private void fillTablespaces(String relType, ICaptureContext context,
			Map<String, ?> objectsMap, Class<? extends IPhysicalProperties> physicalClass) {
		// Fetching PostgreSql tablespaces
		PreparedStatement prepStmt = null;
		ResultSet rset = null;
		Connection conn = (Connection) context.getConnectionObject();
		try {
			prepStmt = conn
					.prepareStatement("SELECT t.relname, tbs.spcname " //$NON-NLS-1$
							+ "FROM pg_catalog.pg_class t " //$NON-NLS-1$
							+ "  JOIN pg_catalog.pg_namespace s ON s.oid = t.relnamespace " //$NON-NLS-1$
							+ "  LEFT OUTER JOIN pg_catalog.pg_tablespace tbs ON tbs.oid = t.reltablespace " //$NON-NLS-1$
							+ "WHERE t.relkind = ? " //$NON-NLS-1$
							+ "  AND tbs.spcname IS NOT NULL " //$NON-NLS-1$
							+ "  AND s.nspname = ?"); //$NON-NLS-1$
			prepStmt.setString(1, relType);
			prepStmt.setString(2, context.getSchema());

			rset = prepStmt.executeQuery();

			while (rset.next()) {
				final String objectName = rset.getString(1);
				final String tablespaceName = rset.getString(2);
				final Object obj = objectsMap.get(objectName);
				if (obj != null && obj instanceof IPhysicalObject) {
					final IPhysicalObject physTable = (IPhysicalObject) obj;
					final IPhysicalProperties tablePhysicals = CorePlugin.getTypedObjectFactory()
							.create(physicalClass);
					tablePhysicals.setTablespaceName(tablespaceName);
					physTable.setPhysicalProperties(tablePhysicals);
				}
			}
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}
	}

	/**
	 * Retrieves the column reference used in the expression. When multiple column are referenced,
	 * the first one is returned.
	 * 
	 * @param t table scope
	 * @param expression expression referencing a column
	 * @return the table column reference
	 */
	private IBasicColumn getColumnReference(IBasicTable t, String expression) {
		for (IBasicColumn c : t.getColumns()) {
			if (c != null && expression.contains(c.getName())) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Retrieves PostgreSql indexes from custom SQL.
	 * 
	 * @param context
	 * @param monitor
	 * @return a collection of postgresql indexes
	 * @author madmattla
	 */
	private Collection<IIndex> getPostgresIndexes(ICaptureContext context, IProgressMonitor monitor) {
		final Map<String, IIndex> indexMap = new HashMap<String, IIndex>();
		final Collection<IIndex> indexes = new ArrayList<IIndex>();
		final Map<String, IIndex> indexesMap = new HashMap<String, IIndex>();
		final Connection conn = (Connection) context.getConnectionObject();
		ResultSet rset = null;
		ResultSet rsetInfo = null;
		PreparedStatement prepStmt = null;

		long start = 0;
		try {
			prepStmt = conn
					.prepareStatement(" SELECT c.relname AS Name, " //$NON-NLS-1$
							+ "  CASE c.relkind " //$NON-NLS-1$
							+ "    WHEN 'r' THEN 'table' " //$NON-NLS-1$
							+ "    WHEN 'v' THEN 'view' " //$NON-NLS-1$
							+ "    WHEN 'i' THEN 'index' " //$NON-NLS-1$
							+ "    WHEN 'S' THEN 'sequence' " //$NON-NLS-1$
							+ "    WHEN 's' THEN 'special' " //$NON-NLS-1$
							+ "    WHEN 'f' THEN 'foreign table' " //$NON-NLS-1$
							+ "  END AS Type, pg_catalog.pg_get_userbyid(c.relowner) AS Owner, c2.relname AS Table, " //$NON-NLS-1$
							+ "  c.oid AS OID, att.attname, pg_catalog.format_type(att.atttypid, att.atttypmod), " //$NON-NLS-1$
							+ "  att.attnotnull, att.attnum, " //$NON-NLS-1$
							+ "  pg_catalog.pg_get_indexdef(att.attrelid, att.attnum, TRUE) AS indexdef, " //$NON-NLS-1$
							+ "  am.amname, i.indisunique, i.indisprimary, i.indisclustered, i.indisvalid " //$NON-NLS-1$
							+ "FROM pg_catalog.pg_class c " //$NON-NLS-1$
							+ "  LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace " //$NON-NLS-1$
							+ "  LEFT JOIN pg_catalog.pg_index i ON i.indexrelid = c.oid " //$NON-NLS-1$
							+ "  LEFT JOIN pg_catalog.pg_attribute att ON att.attrelid = c.oid " //$NON-NLS-1$
							+ "  LEFT JOIN pg_catalog.pg_class c2 ON i.indrelid = c2.oid " //$NON-NLS-1$
							+ "  LEFT JOIN pg_catalog.pg_am am ON am.oid=c.relam " //$NON-NLS-1$
							+ "WHERE c.relkind IN ('i', '') " //$NON-NLS-1$
							+ "  AND n.nspname <> 'pg_catalog' " //$NON-NLS-1$
							+ "  AND n.nspname <> 'information_schema' " //$NON-NLS-1$
							+ "  AND n.nspname !~ '^pg_toast' " //$NON-NLS-1$
							+ "  AND pg_catalog.pg_table_is_visible(c.oid) " //$NON-NLS-1$
							+ "  AND i.indisprimary = 'f' " //$NON-NLS-1$
							+ "  AND i.indisvalid = 't' " //$NON-NLS-1$
							+ "ORDER BY 1, 2, 9"); //$NON-NLS-1$
			rset = prepStmt.executeQuery();
			IIndex lastIndex = null;
			while (rset.next()) {
				monitor.worked(1);
				final String name = rset.getString(1);
				final String type = rset.getString(7);
				final String uniq = rset.getString(12);
				final String tableName = rset.getString(4);
				final String col = rset.getString(6);
				final String indexdef = rset.getString(10);

				final String using = rset.getString(6);
				final int attnum = rset.getInt(9);
				final String accessMode = rset.getString(11);
				boolean isFunctionIndex = false;

				if (!col.equalsIgnoreCase(indexdef)) {
					isFunctionIndex = true;
				}

				IVersionable<?> v = VersionHelper.getVersionable(context.getTable(tableName));
				if (v == null) {
					LOGGER.warn("Skipping index <" + name + ">: related table '" + tableName
							+ "' was not in the capture set.");
					continue;
				}
				IPostgreSqlTable t = (IPostgreSqlTable) v.getVersionnedObject().getModel();
				IBasicColumn c = getColumnReference(t, col); //$NON-NLS-1$
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
					if ("t".equals(uniq)) { //$NON-NLS-1$
						typeValue = IndexType.UNIQUE;
					} else if (accessMode.startsWith("hash")) { //$NON-NLS-1$
						typeValue = IndexType.HASH;
					} else if (accessMode.startsWith("gin")) { //$NON-NLS-1$
						typeValue = IndexType.GIN;
					} else if (accessMode.startsWith("gist")) { //$NON-NLS-1$
						typeValue = IndexType.GIST;
					}
					lastIndex.setIndexType(typeValue);

					if (t == null) {
						LOGGER.warn("Index <" + name + "> references an unknown table '"
								+ tableName + "', skipping index.");
					} else {
						lastIndex.setIndexedTableRef(t.getReference());
						t.addIndex(lastIndex);
						indexes.add(lastIndex);
						indexesMap.put(lastIndex.getIndexName(), lastIndex);
					}
				}
				if (c == null) {
					// only warn if non function based
					if (!isFunctionIndex) { //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.warn("Index <" + lastIndex.getName()
								+ "> references an unknown table column '" + col
								+ "', skipping column.");
					} else {
						final IVersionable<?> table = (IVersionable<?>) context.getTable(tableName);
						final IBasicColumn ic = getColumnReference((IBasicTable) table
								.getVersionnedObject().getModel(), indexdef);
						if (ic != null) {
							final IReference colRef = ic.getReference();
							if (!lastIndex.getIndexedColumnsRef().contains(colRef)) {
								lastIndex.addColumnRef(colRef);
							}
							lastIndex.setFunction(colRef, indexdef);
						}
					}
				} else {
					lastIndex.addColumnRef(c.getReference());
				}

			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Tables][Indexes] fetching time: "
						+ (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$

		} catch (SQLException sqle) {
			LOGGER.info(SQLGenMessages.getString("capturer.costInfoNotAvailable")); //$NON-NLS-1$
			LOGGER.debug(sqle.getMessage(), sqle);
		} finally {
			CaptureHelper.safeClose(rset, null);
		}
		monitor.worked(1);
		return indexes;
	}

	@Override
	public Collection<IIndex> getIndexes(ICaptureContext context, IProgressMonitor monitor) {
		final Map<String, IIndex> indexMap = new HashMap<String, IIndex>();
		final Collection<IIndex> indexes = getPostgresIndexes(context, monitor);

		// Processing indexes to remove PK or UK indexes (DES-694)
		/*
		 * FIXME [BGA] Now that indexes are captured by a PostgreSQL specific implementation, PK or
		 * UK indexes can be filtered out upstream, so this block of code should not be necessary
		 * anymore.
		 */
		for (IIndex index : new ArrayList<IIndex>(indexes)) {
			final Object obj = context.getCapturedObject(
					IElementType.getInstance(UniqueKeyConstraint.TYPE_ID),
					CaptureHelper.getUniqueIndexName(index));
			if (obj != null) {
				indexes.remove(index);
			} else {
				indexMap.put(index.getIndexName(), index);
			}
		}
		// Fetching index tablespaces
		fillTablespaces("i", context, indexMap, IIndexPhysicalProperties.class); //$NON-NLS-1$
		return indexes;
	}

	@Override
	public Collection<ISequence> getSequences(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(SubMonitor.convert(m, 500),
				PROGRESS_RANGE);
		final Connection conn = (Connection) context.getConnectionObject();
		final Collection<ISequence> sequences = new ArrayList<ISequence>();
		final String seqSql = "SELECT min_value, max_value, increment_by, is_cycled, cache_value, last_value FROM "; //$NON-NLS-1$

		Statement stmt = null;
		ResultSet rset = null;
		ResultSet rsetInfo = null;
		long start = 0;
		try {
			stmt = conn.createStatement();

			if (LOGGER.isDebugEnabled())
				start = System.currentTimeMillis();

			DatabaseMetaData md = conn.getMetaData();
			rset = md.getTables(context.getCatalog(), context.getSchema(), "%", //$NON-NLS-1$
					new String[] { "SEQUENCE" }); //$NON-NLS-1$

			while (rset.next()) {
				final String name = rset.getString("TABLE_NAME"); //$NON-NLS-1$
				final String desc = rset.getString("REMARKS"); //$NON-NLS-1$
				final IVersionable<ISequence> seqV = VersionableFactory
						.createVersionable(ISequence.class);
				final ISequence seq = seqV.getVersionnedObject().getModel();

				seq.setName(name);
				seq.setDescription(desc);
				seq.setOrdered(false); // Ordered sequences are not supported by PostgreSQL

				try {
					rsetInfo = stmt.executeQuery(seqSql + name);

					if (rsetInfo.next()) {
						monitor.worked(1);

						final BigDecimal min = rsetInfo.getBigDecimal("min_value"); //$NON-NLS-1$
						final BigDecimal max = rsetInfo.getBigDecimal("max_value"); //$NON-NLS-1$
						final Long inc = rsetInfo.getLong("increment_by"); //$NON-NLS-1$
						final String cycle = rsetInfo.getString("is_cycled"); //$NON-NLS-1$
						final int cacheSize = rsetInfo.getInt("cache_value"); //$NON-NLS-1$
						final BigDecimal seqStart = rsetInfo.getBigDecimal("last_value"); //$NON-NLS-1$

						seq.setMinValue(min);
						seq.setMaxValue(max);
						seq.setIncrement(inc);
						seq.setCycle("Y".equals(cycle)); //$NON-NLS-1$
						seq.setCacheSize(cacheSize);
						seq.setCached(cacheSize > 0);
						seq.setStart(seqStart);

						sequences.add(seq);
					}
				} catch (SQLException e) {
					LOGGER.warn(
							MessageFormat.format(
									SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
									DBVendor.POSTGRE)
									+ e.getMessage(), e);
				} finally {
					CaptureHelper.safeClose(rsetInfo, null);
				}
			}
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("[Sequences] fetching time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ "ms"); //$NON-NLS-1$
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							DBVendor.POSTGRE)
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, stmt);
		}

		return sequences;
	}

	/**
	 * Ugly postgresql datatype conversion.<br>
	 * FIXME need a common clean way of converting datatypes for all vendors
	 * 
	 * @param datatype original vendor datatype
	 * @return a converted datatype
	 */
	private String convertType(String type) {
		final String t = datatypeConversionMap.get(type);
		if (t == null) {
			return type;
		} else {
			return t;
		}
	}

	private IDatatype convertDatatype(IDatatype datatype) {
		final String convertedType = convertType(datatype.getName().toLowerCase());
		datatype.setName(convertedType);
		if (!parameteredTypes.contains(convertedType)) {
			datatype.setLength(0);
			datatype.setPrecision(0);
		}
		return datatype;
	}

	@Override
	public Collection<IView> getViews(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(SubMonitor.convert(m, 500),
				PROGRESS_RANGE);
		final Connection conn = (Connection) context.getConnectionObject();
		final Collection<IView> views = new ArrayList<IView>();
		PreparedStatement prepStmt = null;
		ResultSet rset = null;
		long start = 0;

		try {
			prepStmt = conn.prepareStatement("SELECT viewname, definition " //$NON-NLS-1$
					+ "FROM pg_views " //$NON-NLS-1$
					+ "WHERE schemaname = ?"); //$NON-NLS-1$
			// prepStmt.setString(1, context.getConnection().getDatabase());
			prepStmt.setString(1, context.getSchema());

			if (LOGGER.isDebugEnabled())
				start = System.currentTimeMillis();
			rset = prepStmt.executeQuery();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[Views] query time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ "ms"); //$NON-NLS-1$
				start = System.currentTimeMillis();
			}

			while (rset.next()) {
				monitor.worked(1);
				final String name = rset.getString("viewname"); //$NON-NLS-1$
				final String sql = rset.getString("definition"); //$NON-NLS-1$

				IVersionable<IView> view = VersionableFactory.createVersionable(IView.class);
				IView v = view.getVersionnedObject().getModel();
				v.setName(name);
				v.setSQLDefinition(sql);
				views.add(v);
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[Views] fetch time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ "ms"); //$NON-NLS-1$
			}
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		return views;
	}

	@Override
	public Collection<ITrigger> getTriggers(ICaptureContext context, IProgressMonitor m) {
		final Collection<Object> tableObjects = context.getCapturedObjects(IElementType
				.getInstance(IBasicTable.TYPE_ID));
		final IProgressMonitor monitor = new CustomProgressMonitor(SubMonitor.convert(m,
				tableObjects.size()), PROGRESS_RANGE);
		final Connection conn = (Connection) context.getConnectionObject();
		final Collection<ITrigger> triggers = new ArrayList<ITrigger>();

		monitor.subTask(SQLGenMessages.getString("service.capture.retrievingTriggers")); //$NON-NLS-1$
		PreparedStatement prepStmt = null;
		ResultSet rset = null;
		long start = 0;
		try {
			prepStmt = conn.prepareStatement("SELECT trg.tgname AS trigger_name, " //$NON-NLS-1$
					+ "  CASE trg.tgtype & CAST(28 AS INT2) " //$NON-NLS-1$
					+ "    WHEN 16 THEN 'UPDATE' " //$NON-NLS-1$
					+ "    WHEN  8 THEN 'DELETE' " //$NON-NLS-1$
					+ "    WHEN  4 THEN 'INSERT' " //$NON-NLS-1$
					+ "    WHEN 20 THEN 'INSERT UPDATE' " //$NON-NLS-1$
					+ "    WHEN 28 THEN 'INSERT UPDATE DELETE' "//$NON-NLS-1$
					+ "    WHEN 24 THEN 'UPDATE DELETE' " //$NON-NLS-1$
					+ "    WHEN 12 THEN 'INSERT DELETE' " //$NON-NLS-1$
					+ "  END AS event_manipulation, " //$NON-NLS-1$
					+ "  tbl.relname AS event_object_table, " //$NON-NLS-1$
					+ "  prc.proname AS function_name, " //$NON-NLS-1$
					+ "  CASE trg.tgtype & CAST(2 AS INT2) " //$NON-NLS-1$
					+ "    WHEN 0 THEN 'AFTER' " //$NON-NLS-1$
					+ "    ELSE 'BEFORE' " //$NON-NLS-1$
					+ "  END AS trigger_time, " //$NON-NLS-1$
					+ "  CASE trg.tgtype & CAST(1 AS INT2) " //$NON-NLS-1$
					+ "    WHEN 0 THEN 'STATEMENT' " //$NON-NLS-1$
					+ "    ELSE 'ROW' " //$NON-NLS-1$
					+ "  END AS trigger_type " //$NON-NLS-1$
					+ "FROM pg_trigger trg " //$NON-NLS-1$
					+ "  JOIN pg_class tbl ON trg.tgrelid = tbl.oid " //$NON-NLS-1$
					+ "  JOIN pg_proc prc ON trg.tgfoid = prc.oid " //$NON-NLS-1$
					+ "  JOIN pg_namespace nsp ON nsp.oid = tbl.relnamespace " //$NON-NLS-1$
					+ "WHERE tbl.relname NOT LIKE 'pg_%' " //$NON-NLS-1$
					+ "  AND trg.tgisconstraint = FALSE " //$NON-NLS-1$
					+ "  AND nsp.nspname = ?"); //$NON-NLS-1$
			prepStmt.setString(1, context.getSchema());

			if (LOGGER.isDebugEnabled())
				start = System.currentTimeMillis();
			rset = prepStmt.executeQuery();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[Triggers] query time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ "ms"); //$NON-NLS-1$
				start = System.currentTimeMillis();
			}

			while (rset.next()) {
				monitor.worked(1);
				final String trigName = rset.getString("trigger_name"); //$NON-NLS-1$
				final String[] events = rset.getString("event_manipulation").split("(\\s)+"); //$NON-NLS-1$ //$NON-NLS-2$
				final String tabName = rset.getString("event_object_table"); //$NON-NLS-1$
				final String funcName = rset.getString("function_name"); //$NON-NLS-1$
				final String time = rset.getString("trigger_time"); //$NON-NLS-1$
				final String trigType = rset.getString("trigger_type"); //$NON-NLS-1$

				IVersionable<ITrigger> v = VersionableFactory.createVersionable(ITrigger.class);
				ITrigger trigger = v.getVersionnedObject().getModel();
				trigger.setName(trigName);
				IVersionable<?> refTable = VersionHelper.getVersionable(context.getTable(tabName));
				if (refTable != null) {
					trigger.setTriggableRef(refTable.getReference());
					trigger.setTime(TriggerTime.valueOf(time.toUpperCase()));
					trigger.setCustom(false);
					trigger.setSourceCode("EXECUTE PROCEDURE " + funcName + "()"); //$NON-NLS-1$ //$NON-NLS-2$
					for (String event : events) {
						trigger.addEvent(TriggerEvent.valueOf(event));
					}

					triggers.add(trigger);
				} else {
					LOGGER.warn("Skipped trigger [" + trigName + "]: referenced table not found"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[Sequences] fetch time: " + (System.currentTimeMillis() - start) //$NON-NLS-1$
						+ "ms"); //$NON-NLS-1$
			}
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		} finally {
			CaptureHelper.safeClose(rset, prepStmt);
		}

		return triggers;
	}

	@Override
	public Collection<IProcedure> getProcedures(ICaptureContext context, IProgressMonitor m) {
		final IProgressMonitor monitor = new CustomProgressMonitor(SubMonitor.convert(m, 100),
				PROGRESS_RANGE);
		Map<String, IProcedure> procedures = new HashMap<String, IProcedure>();
		Map<String, String> typeOidMap = new HashMap<String, String>();

		final Connection conn = (Connection) context.getConnectionObject();
		ResultSet rset = null;
		long start = 0;
		long queryTime = 0;
		long fetchTime = 0;
		try {
			Statement typStmt = null;
			try {
				typStmt = conn.createStatement();

				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				rset = typStmt.executeQuery("select oid, typname from pg_type"); //$NON-NLS-1$
				if (LOGGER.isDebugEnabled())
					queryTime += System.currentTimeMillis() - start;

				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				while (rset.next()) {
					final String oid = rset.getString(1);
					final String typeName = rset.getString(2);
					typeOidMap.put(oid, convertType(typeName));
				}
				if (LOGGER.isDebugEnabled())
					fetchTime += System.currentTimeMillis() - start;
			} finally {
				CaptureHelper.safeClose(rset, typStmt);
			}

			PreparedStatement prepStmt = null;
			try {
				// Querying attributes that are common to all database versions
				prepStmt = conn.prepareStatement("SELECT " //$NON-NLS-1$
						+ "    p.oid AS proc_id " //$NON-NLS-1$
						+ "  , p.proname AS proc_name " //$NON-NLS-1$
						+ "  , t.typname AS return_type " //$NON-NLS-1$
						+ "  , p.proretset AS returns_set " //$NON-NLS-1$
						+ "  , l.lanname AS language_type " //$NON-NLS-1$
						+ "  , p.proisstrict AS is_strict " //$NON-NLS-1$
						+ "  , p.proallargtypes AS all_arg_types_oids " //$NON-NLS-1$
						+ "  , p.proargtypes AS argument_types_oids " //$NON-NLS-1$
						+ "  , p.proargmodes AS arg_modes" //$NON-NLS-1$
						+ "  , p.proargnames AS arg_names" //$NON-NLS-1$
						+ "  , p.provolatile AS volatile" //$NON-NLS-1$
						+ "  , p.prosrc AS proc_body " //$NON-NLS-1$
						+ "  , p.probin AS bin_dir " //$NON-NLS-1$
						+ "FROM pg_proc p " //$NON-NLS-1$
						+ "  LEFT JOIN pg_type t ON p.prorettype = t.oid " //$NON-NLS-1$
						+ "  JOIN pg_language l ON p.prolang = l.oid " //$NON-NLS-1$
						+ "  JOIN pg_namespace n ON p.pronamespace = n.oid " //$NON-NLS-1$
						+ "WHERE n.nspname = ? " //$NON-NLS-1$
						+ "  AND l.lanname != 'internal'"); //$NON-NLS-1$
				prepStmt.setString(1, context.getSchema());

				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				rset = prepStmt.executeQuery();
				if (LOGGER.isDebugEnabled())
					queryTime += System.currentTimeMillis() - start;

				if (LOGGER.isDebugEnabled())
					start = System.currentTimeMillis();
				while (rset.next()) {
					monitor.worked(1);
					final String procOid = rset.getString("proc_id"); //$NON-NLS-1$
					final String name = rset.getString("proc_name"); //$NON-NLS-1$
					final String returnedType = rset.getString("return_type"); //$NON-NLS-1$
					final boolean returnsSet = rset.getBoolean("returns_set"); //$NON-NLS-1$
					final String languageType = rset.getString("language_type"); //$NON-NLS-1$
					final boolean isStrict = rset.getBoolean("is_strict"); //$NON-NLS-1$
					final String allArgTypesString = rset.getString("all_arg_types_oids"); //$NON-NLS-1$
					String[] allTypes = new String[0];
					if (allArgTypesString != null) {
						allTypes = allArgTypesString.substring(1, allArgTypesString.length() - 1)
								.split(","); //$NON-NLS-1$
					}
					final String argTypesString = rset.getString("argument_types_oids"); //$NON-NLS-1$
					String[] types = argTypesString.split("(\\s)+"); //$NON-NLS-1$
					final String argModesString = rset.getString("arg_modes"); //$NON-NLS-1$
					String[] argModes = new String[0];
					if (argModesString != null) {
						argModes = argModesString.substring(1, argModesString.length() - 1).split(
								","); //$NON-NLS-1$
					}
					final String argNamesString = rset.getString("arg_names"); //$NON-NLS-1$
					String[] argNames = new String[0];
					if (argNamesString != null) {
						argNames = argNamesString.substring(1, argNamesString.length() - 1).split(
								","); //$NON-NLS-1$
					}
					final String volatil = rset.getString("volatile"); //$NON-NLS-1$
					final String body = rset.getString("proc_body"); //$NON-NLS-1$
					final String binDir = rset.getString("bin_dir"); //$NON-NLS-1$

					if (LOGGER.isDebugEnabled()) {
						String logPrefix = "[" + name + "]"; //$NON-NLS-1$ //$NON-NLS-2$
						//						LOGGER.debug("== Retrieving procedure " + logPrefix + " =="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug("= " + logPrefix + " procedure Metadata ="); //$NON-NLS-1$ //$NON-NLS-2$
						LOGGER.debug(logPrefix + "[pg_proc.oid] " + procOid); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.prorettype] " + returnedType); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.proretset] " + returnsSet); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.prolang] " + languageType); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.proisstrict] " + isStrict); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.proallargtypes] " + allArgTypesString); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.proargtypes] " + argTypesString); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.proargmodes] " + argModesString); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.proargnames] " + argNamesString); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.provolatile] " + volatil); //$NON-NLS-1$
						LOGGER.debug(logPrefix + "[pg_proc.probin] " + binDir); //$NON-NLS-1$
					}

					IVersionable<IProcedure> v = VersionableFactory
							.createVersionable(IProcedure.class);
					IProcedure proc = v.getVersionnedObject().getModel();
					proc.setLanguageType(LanguageType.STANDARD); // TODO generic languages

					/*
					 * FIXME [BGA] This mechanism is currently not compatible with procedure name
					 * checking when editing procedure SQL definition.
					 */
					final StringBuffer args = (new StringBuffer(50)).append("("); //$NON-NLS-1$
					final StringBuffer returnedTable = new StringBuffer(50);

					/*
					 * The pg_proc.proallargtypes field contains data only if the mode of at least
					 * one function argument is different from IN, or if the return value of the
					 * function is a TABLE type. If pg_proc.proallargtypes is null, we fallback
					 * using pg_proc.proargtypes.
					 */
					if (allArgTypesString != null) {
						int typeIndex = 0;
						String argsSeparator = ""; //$NON-NLS-1$
						String colsSeparator = ""; //$NON-NLS-1$
						boolean isReturnValue = false;
						for (String type : allTypes) {
							/*
							 * Appends the mode of the function argument if it is not a return
							 * value, and sets the flag to differentiate arguments from return
							 * values.
							 */
							String argMode = argModes[typeIndex];
							if ("i".equals(argMode)) { //$NON-NLS-1$
								isReturnValue = false;
								args.append(argsSeparator).append("IN "); //$NON-NLS-1$
							} else if ("o".equals(argMode)) { //$NON-NLS-1$
								isReturnValue = false;
								args.append(argsSeparator).append("OUT "); //$NON-NLS-1$
							} else if ("b".equals(argMode)) { //$NON-NLS-1$
								isReturnValue = false;
								args.append(argsSeparator).append("INOUT "); //$NON-NLS-1$
							} else if ("v".equals(argMode)) { //$NON-NLS-1$
								isReturnValue = false;
								args.append(argsSeparator).append("VARIADIC "); //$NON-NLS-1$
							} else if ("t".equals(argMode)) { //$NON-NLS-1$
								isReturnValue = true;
								returnedTable.append(colsSeparator);
							}

							// Appends the name of the argument or return value
							if (argNames.length > typeIndex) {
								if (!isReturnValue) {
									args.append(argNames[typeIndex]).append(" "); //$NON-NLS-1$
								} else {
									returnedTable.append(argNames[typeIndex]).append(" "); //$NON-NLS-1$
								}
							}

							// Appends the type of the argument or return value
							if (typeOidMap.get(type) != null) {
								if (!isReturnValue) {
									args.append(convertType(typeOidMap.get(type)));
								} else {
									returnedTable.append(convertType(typeOidMap.get(type)));
								}
							}

							if (!isReturnValue) {
								argsSeparator = ", "; //$NON-NLS-1$
							} else {
								colsSeparator = ", "; //$NON-NLS-1$
								// colsCnt++;
							}
							typeIndex++;
						}

						if (!"".equals(returnedTable.toString())) { //$NON-NLS-1$
							returnedTable.insert(0, "TABLE(").append(")"); //$NON-NLS-1$ //$NON-NLS-2$
						}
					} else {
						String separator = ""; //$NON-NLS-1$
						int typeIndex = 0;
						for (String type : types) {
							args.append(separator);

							if (argNames.length > typeIndex) {
								args.append(argNames[typeIndex]);
								args.append(" "); //$NON-NLS-1$
							}

							if (typeOidMap.get(type) != null) {
								args.append(convertType(typeOidMap.get(type)));
							}

							separator = ", "; //$NON-NLS-1$
							typeIndex++;
						}
					}

					args.append(")"); //$NON-NLS-1$

					/*
					 * The procedure arguments types are appended to the procedure name in order to
					 * uniquely identify overloaded procedures.
					 */
					proc.setName(name + args);

					final StringBuffer sqlText = new StringBuffer(200);
					sqlText.append("CREATE OR REPLACE "); //$NON-NLS-1$
					sqlText.append(returnedType == null ? "PROCEDURE" : "FUNCTION"); //$NON-NLS-1$ //$NON-NLS-2$
					sqlText.append(" ").append(name); //$NON-NLS-1$
					sqlText.append(args.toString()).append(NEWLINE);

					if (returnedType != null) {
						sqlText.append("  RETURNS "); //$NON-NLS-1$

						if (!"".equals(returnedTable.toString())) { //$NON-NLS-1$
							sqlText.append(returnedTable);
						} else {
							if (returnsSet) {
								sqlText.append("SETOF "); //$NON-NLS-1$
							}
							final String convertedType = convertType(returnedType.toLowerCase());
							sqlText.append(convertedType);
						}
					}

					sqlText.append(" AS").append(NEWLINE); //$NON-NLS-1$
					if (languageType.equalsIgnoreCase("c")) { //$NON-NLS-1$
						sqlText.append("'").append(binDir).append("', '").append(body).append("'") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								.append(NEWLINE);
					} else {
						if (body != null) {
							sqlText.append("$BODY$").append(NEWLINE) //$NON-NLS-1$
									.append(body.trim()).append(NEWLINE)
									.append("$BODY$").append(NEWLINE); //$NON-NLS-1$
						}
					}
					sqlText.append("  LANGUAGE '").append(languageType).append("'"); //$NON-NLS-1$ //$NON-NLS-2$

					if ("i".equals(volatil)) { //$NON-NLS-1$
						sqlText.append(" IMMUTABLE"); //$NON-NLS-1$
					} else if ("s".equals(volatil)) { //$NON-NLS-1$
						sqlText.append(" STABLE"); //$NON-NLS-1$
					} else if ("v".equals(volatil)) { //$NON-NLS-1$
						sqlText.append(" VOLATILE"); //$NON-NLS-1$
					}

					if (isStrict) {
						sqlText.append(" STRICT"); //$NON-NLS-1$
					}

					proc.setSQLSource(sqlText.toString());
					procedures.put(procOid, proc);
				}
				if (LOGGER.isDebugEnabled())
					fetchTime += System.currentTimeMillis() - start;

				// Querying additional attributes for database versions equal or greater than 8.3
				try {
					prepStmt = conn.prepareStatement("SELECT " //$NON-NLS-1$
							+ "    p.oid AS proc_id " //$NON-NLS-1$
							+ "  , p.proname AS proc_name " //$NON-NLS-1$
							+ "  , p.procost AS cost " //$NON-NLS-1$
							+ "  , p.prorows AS result_rows " //$NON-NLS-1$
							+ "FROM pg_proc p " //$NON-NLS-1$
							+ "  LEFT JOIN pg_type t ON p.prorettype = t.oid " //$NON-NLS-1$
							+ "  JOIN pg_language l ON p.prolang = l.oid " //$NON-NLS-1$
							+ "  JOIN pg_namespace n ON p.pronamespace = n.oid " //$NON-NLS-1$
							+ "WHERE n.nspname = ? " //$NON-NLS-1$
							+ "  AND l.lanname != 'internal'"); //$NON-NLS-1$
					prepStmt.setString(1, context.getSchema());

					if (LOGGER.isDebugEnabled())
						start = System.currentTimeMillis();
					rset = prepStmt.executeQuery();
					if (LOGGER.isDebugEnabled())
						queryTime += System.currentTimeMillis() - start;

					if (LOGGER.isDebugEnabled())
						start = System.currentTimeMillis();
					while (rset.next()) {
						final String procOid = rset.getString("proc_id"); //$NON-NLS-1$
						final String name = rset.getString("proc_name"); //$NON-NLS-1$
						final String cost = rset.getString("cost"); //$NON-NLS-1$
						final String resultRows = rset.getString("result_rows"); //$NON-NLS-1$

						if (LOGGER.isDebugEnabled()) {
							String logPrefix = "[" + name + "]"; //$NON-NLS-1$ //$NON-NLS-2$
							LOGGER.debug("= " + logPrefix + " procedure Metadata (only for versions > 8.2) ="); //$NON-NLS-1$ //$NON-NLS-2$
							LOGGER.debug(logPrefix + "[pg_proc.procost] " + cost); //$NON-NLS-1$
							LOGGER.debug(logPrefix + "[pg_proc.prorows] " + resultRows); //$NON-NLS-1$
						}

						if (cost != null && !"".equals(cost.trim())) { //$NON-NLS-1$
							IProcedure proc = procedures.get(procOid);
							if (proc != null) {
								proc.setSQLSource(proc.getSQLSource() + NEWLINE + "  COST " + cost); //$NON-NLS-1$
							}
							if (resultRows != null && Float.valueOf(resultRows) > 0) {
								proc.setSQLSource(proc.getSQLSource() + NEWLINE + "  ROWS " //$NON-NLS-1$
										+ resultRows);
							}
						}
					}
					if (LOGGER.isDebugEnabled())
						fetchTime += System.currentTimeMillis() - start;
				} catch (SQLException sqle) {
					LOGGER.info(SQLGenMessages.getString("capturer.costInfoNotAvailable")); //$NON-NLS-1$
					LOGGER.debug(sqle.getMessage(), sqle);
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[Procedures] query time: " + queryTime + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
					LOGGER.debug("[Procedures] fetching time: " + fetchTime + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} finally {
				CaptureHelper.safeClose(rset, prepStmt);
			}
		} catch (SQLException e) {
			LOGGER.warn(
					MessageFormat.format(
							SQLGenMessages.getString("capturer.error.genericCapturerError"), //$NON-NLS-1$
							context.getConnection().getDBVendor().toString())
							+ e.getMessage(), e);
		}

		return procedures.values();
	}

}
