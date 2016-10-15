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

import java.text.MessageFormat;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.sqlgen.impl.SQLGenerator;
import com.nextep.datadesigner.sqlgen.impl.SQLScript;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.mergers.DataLineMerger;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.IDataLine;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.factories.GenerationFactory;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class DataLineGenerator extends SQLGenerator {

	private static final Log LOGGER = LogFactory.getLog(DataLineGenerator.class);

	/** The last computed column declaration to avoid recomputation */
	private static StringBuffer lastColumnDecl;
	/** The last processed data set context */
	private static IDataSet lastSet;

	@Override
	public IGenerationResult generateDiff(IComparisonItem result) {
		if (isOnlyRepositioned(result)) {
			return GenerationFactory.createGenerationResult();
		}

		StringBuffer sql = new StringBuffer(200);
		IDatatypeProvider datatypeProvider = DBGMHelper.getDatatypeProvider(VersionHelper
				.getCurrentView().getDBVendor());
		IDataLine src = (IDataLine) result.getSource();
		IDataLine tgt = (IDataLine) result.getTarget();
		IDataSet set = src.getDataSet();

		// First checking if we have primary key columns in the set
		List<IBasicColumn> pkColumns = getPKColumnsInSet(set);
		if (pkColumns == null) {
			LOGGER.warn(MessageFormat.format(SQLGenMessages.getString("UnsafeDatalineUpdate"), src //$NON-NLS-1$
					.getRowId(), set.getName(), set.getTable().getName()));
			// We make all columns of the set as the unique identifier of the line
			pkColumns = set.getColumns();
		}
		// Building the update
		sql.append("UPDATE ").append(set.getTable().getName()).append(" SET ").append(NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
		// Appending column updates string
		boolean first = true;
		for (IBasicColumn c : set.getColumns()) {
			if (first) {
				first = false;
			} else {
				sql.append(","); //$NON-NLS-1$
			}
			// Updates string
			sql.append(c.getName()).append("="); //$NON-NLS-1$
			// Retrieving column value
			IColumnValue v = src.getColumnValue(c.getReference());

			// Appending value to our value buffer
			appendColumnValue(datatypeProvider, sql, v, "", "NULL"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Building the WHERE clause
		sql.append(NEWLINE);
		sql.append(buildWhereClause(tgt, pkColumns, datatypeProvider));
		sql.append(";").append(NEWLINE); //$NON-NLS-1$

		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addUpdateScript(
				new DatabaseReference(src.getType(), String.valueOf(src.getRowId()), set.getName()),
				new SQLScript("", "", sql.toString(), //$NON-NLS-1$ //$NON-NLS-2$
						ScriptType.DATA));

		return r;
	}

	/**
	 * Builds a where clause from the given dataline column values. Columns of the where clause are
	 * specified in a separate list.
	 * 
	 * @param line line to retrieve values from
	 * @param whereColumns columns to add in the where clause
	 * @param datatypeProvider datatype provider
	 * @return the where clause string
	 */
	private String buildWhereClause(IDataLine line, List<IBasicColumn> whereColumns,
			IDatatypeProvider datatypeProvider) {
		StringBuffer sql = new StringBuffer(200);

		// Building the WHERE clause
		sql.append("WHERE "); //$NON-NLS-1$
		boolean first = true;
		for (IBasicColumn c : whereColumns) {
			if (c != null) {
				if (first) {
					first = false;
				} else {
					sql.append(" AND "); //$NON-NLS-1$
				}
				// Column locator
				sql.append(c.getName());
				IColumnValue v = line.getColumnValue(c.getReference());

				// Appending value to our value buffer
				appendColumnValue(datatypeProvider, sql, v, "=", " IS NULL"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				LOGGER.warn(MessageFormat.format(
						SQLGenMessages.getString("dataLineGenerator.columnNotFoundInWhereClause"), //$NON-NLS-1$
						line.getRowId(), line.getDataSet()));
			}
		}
		return sql.toString();
	}

	/**
	 * This method checks that primary key columns are all included in this set definition and
	 * returns the list of those columns. If one of the PK columns is not defined in this set,
	 * returns <code>null</code>.
	 * 
	 * @param set set to process
	 * @return columns of this set which compose the PK or <code>null</code> if this set does not
	 *         contain all the PK columns
	 */
	private List<IBasicColumn> getPKColumnsInSet(IDataSet set) {
		// Processing primary key
		IKeyConstraint pk = DBGMHelper.getPrimaryKey(set.getTable());
		if (pk != null) {
			for (IReference r : pk.getConstrainedColumnsRef()) {
				if (!set.getColumnsRef().contains(r)) {
					return null;
				}
			}
			return pk.getColumns();
		} else {
			return null;
		}
	}

	@Override
	public IGenerationResult doDrop(Object model) {
		StringBuffer sql = new StringBuffer(200);
		IDatatypeProvider datatypeProvider = DBGMHelper.getDatatypeProvider(VersionHelper
				.getCurrentView().getDBVendor());
		IDataLine line = (IDataLine) model;
		IDataSet set = line.getDataSet();

		// Building delete string
		sql.append("DELETE FROM ").append(set.getTable().getName()).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
		sql.append(buildWhereClause(line, set.getColumns(), datatypeProvider));
		sql.append(";").append(NEWLINE); //$NON-NLS-1$

		// Building generation result
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addDropScript(
				new DatabaseReference(line.getType(), String.valueOf(line.getRowId()), set
						.getName()), new SQLScript("", "", sql.toString(), //$NON-NLS-1$ //$NON-NLS-2$
						ScriptType.DATA));

		// Returning drop script
		return r;
	}

	@Override
	public IGenerationResult generateFullSQL(Object model) {
		// Using string buffers since there could be a very large amount of string concatenation
		StringBuffer sql = new StringBuffer(10000);
		IDatatypeProvider datatypeProvider = DBGMHelper.getDatatypeProvider(VersionHelper
				.getCurrentView().getDBVendor());
		IDataLine l = (IDataLine) model;
		IDataSet set = l.getDataSet();

		sql.append("INSERT INTO ").append(set.getTable().getName()).append(" (").append(NEWLINE); //$NON-NLS-1$ //$NON-NLS-2$
		sql.append(getColumnDeclaration(set));
		sql.append(NEWLINE).append(") values (").append(NEWLINE); //$NON-NLS-1$
		// Appending column names and building data string
		boolean first = true;
		for (IReference r : set.getColumnsRef()) {
			if (first) {
				first = false;
			} else {
				sql.append(","); //$NON-NLS-1$
			}
			// Retrieving column value
			IColumnValue v = l.getColumnValue(r);

			// Appending value to our value buffer
			appendColumnValue(datatypeProvider, sql, v, "", "NULL"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Building sql data section
		sql.append(NEWLINE).append(");").append(NEWLINE); //$NON-NLS-1$
		IGenerationResult r = GenerationFactory.createGenerationResult();
		r.addAdditionScript(new DatabaseReference(l.getType(), String.valueOf(l.getRowId()), l
				.getDataSet().getName()), new SQLScript("", "", sql //$NON-NLS-1$ //$NON-NLS-2$
				.toString(), ScriptType.DATA));

		// Returning full SQL script
		return r;
	}

	/**
	 * Convenience method to append the specified column value to the <code>StringBuffer</code>
	 * representing a SQL script.
	 * 
	 * @param datatypeProvider the datatype provider to determine the list of string types supported
	 *        by the corresponding database vendor.
	 * @param sql a <code>StringBuffer</code> representing a SQL script to which the column value
	 *        should be appended.
	 * @param v the column value to append to the SQL script.
	 * @param valuePrefix an optional <code>String</code> to be appended before the column value.
	 *        Should be equal to "" if no prefix is needed.
	 * @param nullValue a <code>String</code> representing the value to be appended to the script
	 *        when the specified column value is <code>null</code>.
	 */
	private void appendColumnValue(IDatatypeProvider datatypeProvider, StringBuffer sql,
			IColumnValue v, String valuePrefix, String nullValue) {
		/*
		 * [BGA]: If new columns are added to the Data set after some lines have been entered in the
		 * Data set, there could be no value for the current column in the repository. So we first
		 * check if the specified column value is not null.
		 */
		String colStringValue = (null == v ? "" : (null == v.getStringValue() ? "" : v //$NON-NLS-1$ //$NON-NLS-2$
				.getStringValue()));

		if (!colStringValue.equals("")) { //$NON-NLS-1$
			sql.append(valuePrefix);

			final String colType = v.getColumn().getDatatype().getName().toUpperCase();
			if (datatypeProvider.listStringDatatypes().contains(colType)
					|| colType.startsWith("ENUM")) { //$NON-NLS-1$
				sql.append("'").append( //$NON-NLS-1$
						colStringValue.replace("\\", "\\\\").replace("'", "\\'")).append( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
						"'"); //$NON-NLS-1$
			} else {
				sql.append(colStringValue);
			}
		} else {
			sql.append(nullValue);
		}
	}

	private static synchronized String getColumnDeclaration(IDataSet set) {
		if (set == lastSet) {
			return lastColumnDecl.toString();
		}
		lastSet = set;
		// Generating our buffer containing column names declaration
		lastColumnDecl = new StringBuffer(200);
		boolean first = true;
		for (IBasicColumn c : set.getColumns()) {
			if (first) {
				first = false;
			} else {
				lastColumnDecl.append(","); //$NON-NLS-1$
			}
			lastColumnDecl.append(c.getName());
		}
		return lastColumnDecl.toString();
	}

	protected boolean isOnlyRepositioned(IComparisonItem item) {
		for (IComparisonItem i : item.getSubItems(DataLineMerger.ATTR_CONTENTS)) {
			if (i.getDifferenceType() != DifferenceType.EQUALS) {
				return false;
			}
		}
		return true;
	}
}
