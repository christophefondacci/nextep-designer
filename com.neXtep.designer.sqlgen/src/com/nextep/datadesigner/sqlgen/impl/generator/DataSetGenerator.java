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
package com.nextep.datadesigner.sqlgen.impl.generator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.mergers.DataSetComparisonItem;
import com.nextep.designer.dbgm.model.DeltaType;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.dbgm.services.IStorageService;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * SQL generator for data sets.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public abstract class DataSetGenerator extends SQLGenerator {

	private final static Log LOGGER = LogFactory.getLog(DataSetGenerator.class);

	@Override
	public IGenerationResult generateDiff(IComparisonItem comparison) {
		final DataSetComparisonItem dsetComparison = (DataSetComparisonItem) comparison;
		final IDataDelta delta = dsetComparison.getDataDelta();
		final IGenerationResult result = GenerationFactory.createGenerationResult();
		final IDataSet set = (IDataSet) comparison.getSource();
		final ISQLScript insertScript = buildScript(set, delta.getAddedDataSet(), DeltaType.INSERT);
		if (insertScript != null) {
			result.addAdditionScript(
					new DatabaseReference(IElementType.getInstance(IDataSet.TYPE_ID), set.getName()),
					insertScript);
		}
		final ISQLScript updateScript = buildScript(set, delta.getUpdatedDataSet(),
				DeltaType.UPDATE);
		if (updateScript != null) {
			result.addUpdateScript(new DatabaseReference(
					IElementType.getInstance(IDataSet.TYPE_ID), set.getName()), updateScript);
		}
		final ISQLScript deletionScript = buildScript((IDataSet) comparison.getTarget(),
				delta.getDeletedDataSet(), DeltaType.DELETE);
		if (deletionScript != null) {
			result.addDropScript(new DatabaseReference(IElementType.getInstance(IDataSet.TYPE_ID),
					set.getName()), deletionScript);
		}
		// Adding preconditions if anything has been generated
		if (deletionScript != null || updateScript != null || insertScript != null) {
			addPreconditions(result, set);
		}
		return result;
	}

	private ISQLScript buildScript(IDataSet set, IDataSet valuesSet, DeltaType type) {
		final ISQLScript script = CorePlugin.getTypedObjectFactory().create(ISQLScript.class);
		script.setScriptType(ScriptType.DATA);
		script.setName(type.name().toLowerCase() + "." + set.getName()); //$NON-NLS-1$
		final StringBuilder buf = new StringBuilder(2000);
		final ISQLParser parser = SQLGenPlugin.getService(IGenerationService.class)
				.getCurrentSQLParser();
		// final IDataService dataService = DbgmPlugin.getService(IDataService.class);
		final IStorageService storageService = DbgmPlugin.getService(IStorageService.class);
		final IStorageHandle handle = valuesSet.getStorageHandle();
		if (handle != null) {
			Connection conn = null;
			Statement stmt = null;
			ResultSet rset = null;
			try {
				conn = storageService.getLocalConnection();
				stmt = conn.createStatement();
				stmt.execute(handle.getSelectStatement());
				rset = stmt.getResultSet();
				final ResultSetMetaData md = rset.getMetaData();
				while (rset.next()) {
					final List<Object> values = new LinkedList<Object>();
					for (int i = 1; i <= md.getColumnCount(); i++) {
						values.add(rset.getObject(i));
					}
					switch (type) {
					case INSERT:
						buf.append(buildInsert(parser, set, values));
						break;
					case UPDATE:
						buf.append(buildUpdate(parser, set, values));
						break;
					case DELETE:
						buf.append(buildDelete(parser, set, values));
						break;
					}
				}
			} catch (SQLException e) {
				throw new ErrorException("Data generation problem: " + e.getMessage(), e);
			} finally {
				safeClose(rset, stmt, conn);
			}
		}
		if (buf.length() == 0) {
			return null;
		} else {
			script.appendSQL(buf.toString());
			return script;
		}
	}

	/**
	 * Builds a single SQL insert statement for the specified data set and given values. The sql
	 * parser will be used to generate an appropriate SQL-value based on the data type.
	 * 
	 * @param parser the {@link ISQLParser} to use for generating SQL-script values
	 * @param set the data set for which this insert is generated
	 * @param values values to insert (must match the data set columns definition
	 * @return an ANSI SQL-insert statement
	 */
	private String buildInsert(ISQLParser parser, IDataSet set, List<?> values) {
		final StringBuilder buf = new StringBuilder(100);
		final IBasicTable table = set.getTable();
		buf.append("INSERT INTO ").append(escape(table.getName())).append("("); //$NON-NLS-1$ //$NON-NLS-2$
		String separator = ""; //$NON-NLS-1$
		List<IDatatype> columnTypes = new ArrayList<IDatatype>();
		for (IBasicColumn c : set.getColumns()) {
			buf.append(separator).append(escape(c.getName()));
			columnTypes.add(c.getDatatype());
			separator = ","; //$NON-NLS-1$
		}
		buf.append(") VALUES ("); //$NON-NLS-1$
		separator = ""; //$NON-NLS-1$
		int i = 0;
		for (Object o : values) {
			buf.append(separator);
			buf.append(parser.formatSqlScriptValue(columnTypes.get(i++), o));
			separator = ","; //$NON-NLS-1$
		}
		buf.append(");").append(NEWLINE); //$NON-NLS-1$
		return buf.toString();
	}

	/**
	 * Builds a single SQL update statement from specified data set and given values. Values list is
	 * expected to contain both new and old column values, new and old values set being
	 * concatenated.
	 * 
	 * @param parser the {@link ISQLParser} to use for generating a SQL expression from a given
	 *        value
	 * @param set the data set for which the update should be generated
	 * @param values the list of all column values, all new values first, then all old values.
	 * @return an ANSI SQL-update statement
	 */
	private String buildUpdate(ISQLParser parser, IDataSet set, List<?> values) {
		final StringBuilder buf = new StringBuilder(100);
		final StringBuilder whereBuf = new StringBuilder(100);
		final IBasicTable table = set.getTable();
		buf.append("UPDATE ").append(escape(table.getName())).append(" SET "); //$NON-NLS-1$ //$NON-NLS-2$
		final int columnCount = set.getColumnsRef().size();
		int i = 0;
		String separator = ""; //$NON-NLS-1$
		String whereSeparator = ""; //$NON-NLS-1$
		// Retrieving primary key for where clause
		final IKeyConstraint pk = DBGMHelper.getPrimaryKey(table);
		for (IBasicColumn c : set.getColumns()) {
			final Object newVal = values.get(i);
			final Object oldVal = values.get(i + columnCount);
			i++;
			if ((newVal == null && oldVal != null) || (newVal != null && !newVal.equals(oldVal))) {
				buf.append(separator).append(escape(c.getName())).append("="); //$NON-NLS-1$
				buf.append(parser.formatSqlScriptValue(c.getDatatype(), newVal));
				separator = ","; //$NON-NLS-1$
			}
			if (pk == null
					|| (pk != null && pk.getConstrainedColumnsRef().contains(c.getReference()))) {
				whereBuf.append(whereSeparator).append(escape(c.getName()));
				if (oldVal == null) {
					whereBuf.append(" IS NULL"); //$NON-NLS-1$
				} else {
					final String sqlVal = parser.formatSqlScriptValue(c.getDatatype(), oldVal);
					whereBuf.append("=").append(sqlVal); //$NON-NLS-1$
				}
				whereSeparator = " AND "; //$NON-NLS-1$
			}
		}
		buf.append(" WHERE "); //$NON-NLS-1$
		buf.append(whereBuf.toString());
		buf.append(";").append(NEWLINE); //$NON-NLS-1$
		return buf.toString();
	}

	private String buildDelete(ISQLParser parser, IDataSet set, List<?> deletedValues) {
		final StringBuilder buf = new StringBuilder(100);
		final IBasicTable table = set.getTable();
		buf.append("DELETE FROM ").append(escape(table.getName())).append(" WHERE "); //$NON-NLS-1$ //$NON-NLS-2$
		String separator = ""; //$NON-NLS-1$
		int i = 0;
		for (IBasicColumn c : set.getColumns()) {
			buf.append(separator).append(escape(c.getName()));
			final Object oldVal = deletedValues.get(i++);
			final String sqlVal = parser.formatSqlScriptValue(c.getDatatype(), oldVal);
			if (sqlVal == null) {
				buf.append(" IS NULL"); //$NON-NLS-1$
			} else {
				buf.append("=").append(sqlVal); //$NON-NLS-1$
			}
			separator = " AND "; //$NON-NLS-1$
		}
		buf.append(";").append(NEWLINE); //$NON-NLS-1$
		return buf.toString();
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		final IDataSet set = (IDataSet) model;
		// Ensuring our set is loaded
		final IDataService dataService = DbgmPlugin.getService(IDataService.class);
		dataService.loadDataLinesFromRepository(set, getMonitor());
		// Preparing result
		final IGenerationResult result = GenerationFactory.createGenerationResult();
		final ISQLScript insertions = buildScript(set, set, DeltaType.INSERT);
		if (insertions != null) {
			result.addAdditionScript(
					new DatabaseReference(IElementType.getInstance(IDataSet.TYPE_ID), set.getName()),
					insertions);
			addPreconditions(result, set);
		}
		return result;
	}

	/**
	 * Adds the table precondition to a dataset generation. This method browses all existing foreign
	 * keys of the table which this data set contributes to and defines precondition on any remote
	 * table. This will make the generator order SQL data set scripts according to their
	 * dependencies. A child table data will therefore be inserted after its parent table data.
	 * 
	 * @param r the {@link IGenerationResult} of the data set generation
	 * @param s the data set being generated
	 */
	private void addPreconditions(IGenerationResult r, IDataSet s) {
		// Getting table
		final IBasicTable t = s.getTable();
		// Getting foreign keys
		Collection<IKeyConstraint> keys = t.getConstraints();
		for (IKeyConstraint key : keys) {
			switch (key.getConstraintType()) {
			case FOREIGN:
				// Retrieving related foreign table
				final IBasicTable remoteTable = DBGMHelper
						.getRemoteTable((ForeignKeyConstraint) key);
				r.addPrecondition(new DatabaseReference(s.getType(), remoteTable.getName()));
			}

		}
	}

	private void safeClose(ResultSet rset, Statement stmt, Connection conn) {
		if (rset != null) {
			try {
				rset.close();
			} catch (SQLException e) {
				LOGGER.error("Unable to close resultset", e);
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error("Unable to close statement", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error("Unable to close connection", e);
			}
		}
	}

	/**
	 * Generates the SQL result for this file-based data files from incremental differences.
	 * 
	 * @param result comparison result
	 * @return the generation result
	 */
	protected abstract IGenerationResult generateDatafilesDiff(IComparisonItem result);

	/**
	 * Generates the datafile result for this data set
	 * 
	 * @param set set to generate (file-based)
	 * @return the generation result
	 */
	protected abstract IGenerationResult generateDatafilesFull(IDataSet set);

	@Override
	public IGenerationResult doDrop(Object model) {
		return null;
	}

}
