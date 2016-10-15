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
package com.nextep.designer.sqlgen.helpers;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.dbgm.model.ForeignKeyAction;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.SQLGenMessages;

/**
 * @author Bruno Gautier
 */
public final class CaptureHelper {

	private static final Log LOGGER = LogFactory.getLog(CaptureHelper.class);

	/**
	 * Returns a unique name given a parent name and an object name.
	 * 
	 * @param parentName the name of the parent object
	 * @param objectName the name of the object
	 * @return a <code>String</code> representing the specified object's unique name
	 */
	public static String getUniqueObjectName(String parentName, String objectName) {
		return parentName + "." + objectName; //$NON-NLS-1$
	}

	/**
	 * Returns a unique name for the specified column. The parent table of the column must have been
	 * defined via the {@link IBasicColumn#setParent(IColumnable)}, because this method needs to
	 * call the {@link IBasicColumn#getParent()} method to uniquely identify this column.
	 * 
	 * @param column a {@link IBasicColumn}
	 * @return a <code>String</code> representing the specified column's unique name
	 * @throws NullPointerException if the specified column has no parent table
	 */
	public static String getUniqueColumnName(IBasicColumn column) {
		return getUniqueObjectName(column.getParent().getName(), column.getName());
	}

	/**
	 * Returns a unique name for the specified index. The indexed table must have been defined via
	 * the {@link IIndex#setIndexedTableRef(com.nextep.datadesigner.model.IReference)}, because this
	 * method needs to call the {@link IIndex#getIndexedTable()} method to uniquely identify this
	 * column.
	 * 
	 * @param index a {@link IIndex}
	 * @return a <code>String</code> representing the specified index's unique name
	 * @throws NullPointerException if the specified index has no indexed table
	 */
	public static String getUniqueIndexName(IIndex index) {
		return getUniqueObjectName(index.getIndexedTable().getName(), index.getIndexName());
	}

	/**
	 * Converts a JDBC foreign key action code into a neXtep {@link ForeignKeyAction} enumeration.
	 * 
	 * @param rule JDBC code of the FK action
	 * @return a {@link ForeignKeyAction} corresponding to the specified JDBC action code if it
	 *         match one of the {@link DatabaseMetaData} known values,
	 *         {@link ForeignKeyAction#NO_ACTION} otherwise
	 */
	public static ForeignKeyAction getForeignKeyAction(short rule) {
		switch (rule) {
		case DatabaseMetaData.importedKeyCascade:
			return ForeignKeyAction.CASCADE;
		case DatabaseMetaData.importedKeyRestrict:
			// FIXME [BGA] Very ugly fix to handle the jTDS JDBC driver bug that returns
			// importedKeyRestrict instead of importedKeyNoAction.
			DBVendor currVendor = DBGMHelper.getCurrentVendor();
			if (currVendor.equals(DBVendor.MSSQL)) {
				return ForeignKeyAction.NO_ACTION;
			} else {
				return ForeignKeyAction.RESTRICT;
			}
		case DatabaseMetaData.importedKeySetDefault:
			return ForeignKeyAction.SET_DEFAULT;
		case DatabaseMetaData.importedKeySetNull:
			return ForeignKeyAction.SET_NULL;
		case DatabaseMetaData.importedKeyNoAction:
		default:
			return ForeignKeyAction.NO_ACTION;
		}
	}

	/**
	 * Converts a ON UPDATE or ON DELETE foreign key SQL clause into a neXtep
	 * {@link ForeignKeyAction} enumeration.
	 * 
	 * @param sql a ON UPDATE or ON DELETE foreign key SQL clause ("CASCADE" for example)
	 * @return a {@link ForeignKeyAction} corresponding to the specified SQL clause if it match one
	 *         of the SQL clause of the <code>ForeignKeyAction</code> enum values,
	 *         {@link ForeignKeyAction#NO_ACTION} otherwise
	 */
	public static ForeignKeyAction getForeignKeyAction(String sql) {
		if (sql != null && !"".equals(sql.trim())) { //$NON-NLS-1$
			for (ForeignKeyAction action : ForeignKeyAction.values()) {
				if (action.getSql().equalsIgnoreCase(sql)) {
					return action;
				}
			}
		}
		return ForeignKeyAction.NO_ACTION;
	}

	/**
	 * Converts a JDBC index type code into a neXtep {@link IndexType} enumeration.
	 * 
	 * @param type JDBC code of the index type
	 * @return a corresponding {@link IndexType}
	 */
	public static IndexType getIndexType(short type) {
		switch (type) {
		case DatabaseMetaData.tableIndexHashed:
			return IndexType.HASH;
		case DatabaseMetaData.tableIndexStatistic:
		case DatabaseMetaData.tableIndexClustered:
		case DatabaseMetaData.tableIndexOther:
		default:
			return IndexType.NON_UNIQUE;
		}
	}

	/**
	 * Tries to extract and return the "SELECT..." query from a "CREATE...AS SELECT..." statement.
	 * 
	 * @param stmt a <code>String</code> representing a "CREATE...AS SELECT..." statement
	 * @return a <code>String</code> representing the "SELECT..." part of the specified statement,
	 *         <code>null</code> if the query couldn't be extracted
	 */
	public static String getQueryFromCreateAsStatement(String stmt) {
		return getBodyFromCreateAsStatement(stmt, "SELECT"); //$NON-NLS-1$
	}

	/**
	 * Tries to extract and return the "BEGIN...END" query from a "CREATE...AS BEGIN...END"
	 * statement.
	 * 
	 * @param stmt a <code>String</code> representing a "CREATE...AS BEGIN...END" statement
	 * @return a <code>String</code> representing the "BEGIN...END" part of the specified statement,
	 *         <code>null</code> if the query couldn't be extracted
	 */
	public static String getBodyFromCreateAsStatement(String stmt) {
		return getBodyFromCreateAsStatement(stmt, "BEGIN"); //$NON-NLS-1$
	}

	private static String getBodyFromCreateAsStatement(String stmt, String bodyStartKeyword) {
		String body = null;
		if (stmt != null) {
			/*
			 * FIXME [BGA] This expression will not work for bodies that start with a DECLARE
			 * keyword. The expression should not rely on the first keyword of the body section, as
			 * this first keyword can vary, but should instead rely on the previous AS keyword which
			 * is always the same.
			 */
			Pattern p = Pattern.compile("^.+?\\s+AS\\s+(" + bodyStartKeyword + "\\s+.+)", //$NON-NLS-1$ //$NON-NLS-2$
					Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m = p.matcher(stmt);
			if (m.find()) {
				try {
					body = m.group(1);
					if (body != null && !"".equals(body.trim())) { //$NON-NLS-1$
						return body;
					}
				} catch (IllegalStateException ise) {
					LOGGER.warn(bodyStartKeyword
							+ " keyword could not be found in the specified CREATE AS statement ["
							+ stmt + "]", ise); //$NON-NLS-1$
				}
			}
		}
		if (body == null || "".equals(body.trim())) { //$NON-NLS-1$
			// If we failed extracting the BODY clause, we simply try to return the passed statement
			// as this is better than nothing. (Attempt to fix DES-927)
			return stmt;
		} else {
			return body;
		}
	}

	/**
	 * Checks if the specified "CREATE...(columns_list) AS SELECT..." statement contains a columns
	 * list clause.
	 * 
	 * @param stmt a <code>String</code> representing a "CREATE AS SELECT" statement
	 * @return <code>true</code> if the specified statement contains a columns list clause,
	 *         <code>false</code> otherwise
	 */
	public static boolean containsColumnsListClause(String stmt) {
		if (stmt != null) {
			Pattern p = Pattern.compile(".+?\\s*\\([^,]+(?:,[^,]+)*\\)\\s+AS\\s+SELECT\\s+.+", //$NON-NLS-1$
					Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(stmt);
			return m.find();
		}
		return false;
	}

	/**
	 * Returns the specified list of values as a <code>String</code> representing a comma-separated
	 * list of values.
	 * 
	 * @param values a {@link List} of <code>String</code> values
	 * @return a <code>String</code> representing a comma-separated list of values, an empty
	 *         <code>String</code> if the specified list is <code>null</code> or empty
	 */
	public static String getCommaSeparatedValues(List<String> values) {
		if (null == values || values.size() == 0)
			return ""; //$NON-NLS-1$

		StringBuilder b = new StringBuilder("'").append(values.get(0)).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 1, len = values.size(); i < len; i++) {
			b.append(",'").append(values.get(i)).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		return b.toString();
	}

	/**
	 * Close the specified <code>ResultSet</code> and <code>Statement</code> objects if not
	 * <code>null</code> while catching for potential {@link SQLException} that could be thrown by
	 * the close operation if a database access error occurs.
	 * 
	 * @param rset a {@link ResultSet} object to be closed; may be <code>null</code>
	 * @param stmt a {@link Statement} object to be closed; may be <code>null</code>
	 */
	public static void safeClose(ResultSet rset, Statement stmt) {
		try {
			if (rset != null)
				rset.close();
		} catch (SQLException sqle) {
			LOGGER.warn(SQLGenMessages.getString("captureHelper.closeResultSetFailed"), sqle); //$NON-NLS-1$
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException sqle) {
			LOGGER.warn(SQLGenMessages.getString("captureHelper.closeStatementFailed"), sqle); //$NON-NLS-1$
		}
	}

	/**
	 * Increment the specified monitor counter by one and notify the specified monitor that an
	 * amount of work corresponding to the specified <code>range</code> value multiplied by the
	 * specified <code>work</code> parameter has been done if the monitor counter was incremented by
	 * a value of {@link #PROGRESS_RANGE}.
	 * 
	 * @param weight the multiplier of the {@link #PROGRESS_RANGE} value to compute the amount of
	 *        work that will be notified to the progress monitor.
	 */
	public static void updateMonitor(IProgressMonitor monitor, int counter, int range, int weight) {
		if (monitor.isCanceled()) {
			throw new CancelException("Capture has been cancelled by user.");
		}

		if (isWorkRangeDone(counter, range)) {
			monitor.worked(weight * range);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Progress monitor has been notified for an amount of work of [" //$NON-NLS-1$
						+ (weight * range) + "]"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Increment the specified monitor counter by one and check if it was incremented by a value of
	 * specified range.
	 * 
	 * @param counter the monitor counter that is incremented each time a piece of work is done.
	 * @param range the amount of work that needs to be done before the monitor should be notified.
	 * @return <code>true</code> if the monitor counter was incremented by a value of
	 *         <code>range</code>, <code>false</code> otherwise.
	 */
	private static boolean isWorkRangeDone(int counter, int range) {
		return (counter++ % range == 0);
	}

}
