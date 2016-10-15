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
package com.nextep.designer.sqlgen.mysql.parser;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IAutoEditStrategy;

import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.exception.UnsupportedDatatypeException;
import com.nextep.datadesigner.sqlgen.model.IPrototype;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.helpers.ConversionHelper;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.mysql.strategies.SQLAutoIndentStrategy;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MySQLParser implements ISQLParser, IDatatypeProvider {

	private final static DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$
	private final Collection<IAutoEditStrategy> editStrategies;

	private final List<String> decimalDatatypes = Arrays.asList("FLOAT", "DOUBLE",
			"DOUBLE PRECISION", "NUMERIC", "DECIMAL");

	public MySQLParser() {
		editStrategies = new ArrayList<IAutoEditStrategy>();
		editStrategies.add(new SQLAutoIndentStrategy(this));
	}

	/**
	 * @see com.nextep.designer.sqlgen.model.ISQLParser#getAutoEditStrategies()
	 */
	@Override
	public Collection<IAutoEditStrategy> getAutoEditStrategies() {
		return editStrategies;
	}

	/**
	 * @see com.nextep.designer.sqlgen.model.ISQLParser#getPrototypes()
	 */
	@Override
	public List<IPrototype> getPrototypes() {
		return Collections.emptyList();
	}

	/**
	 * @see com.nextep.designer.sqlgen.model.ISQLParser#getStringConcatenator()
	 */
	@Override
	public String getStringConcatenator() {
		return "+"; //$NON-NLS-1$
	}

	/**
	 * @see com.nextep.designer.sqlgen.model.ISQLParser#getStringDelimiter()
	 */
	@Override
	public char getStringDelimiter() {
		return '\'';
	}

	/**
	 * @see com.nextep.designer.sqlgen.model.ISQLParser#getTypedTokens()
	 */
	@Override
	public Map<String, List<String>> getTypedTokens() {
		Map<String, List<String>> tokens = new HashMap<String, List<String>>();
		tokens.put(DDL, Arrays.asList("CREATE", "ALTER", "TABLE", "INDEX", "VIEW", "CONSTRAINT",
				"PRIMARY", "DROP", "DEFAULT", "CHANGE", "GRANT", "ROLE", "TO", "WITH", "CASCADE",
				"OR", "REPLACE", "IDENTIFIED", "TABLESPACE", "TRUE", "FALSE", "ADD", "MODIFY",
				"FOREIGN", "UNIQUE", "PRIMARY", "KEY", "REFERENCES", "NULL", "NOT",
				"AUTO_INCREMENT", "AS", "COLUMN", "ON", "PRECISION", "TRUNCATE", "IS", "CONVERT",
				"CHARACTER", "TRIGGER", "AFTER", "BEFORE", "FUNCTION", "PROCEDURE", "RETURNS",
				"EXISTS", "RETURN", "DETERMINISTIC", "CONDITION", "TEMPORARY", "ENGINE", "RENAME",
				"CHARSET", "DUPLICATE", "FULLTEXT", "HASH", "SPATIAL", "USING", "COLLATE",
				"COMMENT"));
		tokens.put(DML, Arrays.asList("SELECT", "FROM", "WHERE", "HAVING", "GROUP", "BY", "ORDER",
				"UPDATE", "SET", "DELETE", "INSERT", "INTO", "BULK", "COLLECT", "VALUES",
				"BETWEEN", "IN", "OUT", "DISTINCT", "LIKE", "AND", "OR", "ASC", "DESC", "LIMIT",
				"INTO", "OUTFILE", "UNION", "ALL", "AS", "LEFT", "RIGHT", "OUTER", "JOIN", "MATCH",
				"AGAINST", "BOOLEAN", "MODE", "CASE", "WHEN", "SEPARATOR"));
		tokens.put(FUNC, Arrays.asList("COUNT", "CONCAT", "IFNULL", "LOWER", "UPPER", "NVL", "COS",
				"SIN", "PI", "SQRT", "ATAN", "ATAN2", "TAN", "RAND", "GROUP_CONCAT", "CAST",
				"SUBSTR", "INSTR", "LOCATE", "NOW", "COALESCE"));
		tokens.put(DATATYPE, listSupportedDatatypes());
		tokens.put(LANG, Arrays.asList("FOR", "EACH", "ROW", "BEGIN", "DECLARE", "CONTINUE",
				"HANDLER", "FOUND", "WHILE", "DO", "REPEAT", "FETCH", "IF", "THEN", "ELSE",
				"ELSEIF", "END", "UNTIL", "CLOSE", "DELIMITER", "OPEN", "CURSOR", "EXPLAIN",
				"READS", "SQL", "DATA", "CALL", "START", "TRANSACTION", "COMMIT", "ANALYZE",
				"LOOP", "LEAVE", "LOCAL", "INFILE", "LOAD", "FIELDS", "TERMINATED", "OPTIONALLY",
				"ENCLOSED", "LINES"));
		return tokens;
	}

	/**
	 * @see com.nextep.designer.sqlgen.model.ISQLParser#getVarSeparator()
	 */
	@Override
	public String getVarSeparator() {
		return "@"; //$NON-NLS-1$
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IDatatypeProvider#listSupportedDatatypes()
	 */
	@Override
	public List<String> listSupportedDatatypes() {
		return Arrays.asList("VARCHAR", "DECIMAL", "DATE", "TIMESTAMP", "CLOB", "BLOB", "LONGBLOB",
				"CHAR", "INT", "BIGINT", "TEXT", "LONGTEXT", "UNSIGNED", "LONG", "INTEGER",
				"TINYINT", "DATETIME", "DOUBLE", "DOUBLE PRECISION", "FLOAT", "SMALLINT",
				"MEDIUMINT", "BIT", "MEDIUMTEXT", "MEDIUMBLOB");
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IDatatypeProvider#getDefaultDatatype()
	 */
	@Override
	public IDatatype getDefaultDatatype() {
		return new Datatype("INT"); //$NON-NLS-1$
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IDatatypeProvider#listStringDatatypes()
	 */
	@Override
	public List<String> listStringDatatypes() {
		return Arrays.asList("VARCHAR", "DATE", "TIMESTAMP", "CLOB", "CHAR", "TEXT", "LONGTEXT",
				"MEDIUMTEXT", "TINYTEXT", "NVARCHAR", "ENUM");
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IDatatypeProvider#getEquivalentDatatypesMap()
	 */
	@Override
	public Map<String, String> getEquivalentDatatypesMap() {
		Map<String, String> typesMap = new HashMap<String, String>();
		typesMap.put("INTEGER", "INT"); //$NON-NLS-1$ //$NON-NLS-2$

		return typesMap;
	}

	@Override
	public String getPromptCommand() {
		// No prompt for MySQL command line client, using comment
		return getCommentStartSequence();
	}

	@Override
	public String getStatementDelimiter() {
		return ";"; //$NON-NLS-1$
	}

	@Override
	public String getColumnDefinitionEscaper() {
		return "`"; //$NON-NLS-1$
	}

	@Override
	public String getScriptCallerTag() {
		return "source "; //$NON-NLS-1$
	}

	@Override
	public String getCommentStartSequence() {
		// TODO [BGA]: Check if we should follow MySQL recommendation for the
		// start-comment sequence
		//		return "# "; //$NON-NLS-1$
		return "--"; //$NON-NLS-1$
	}

	@Override
	public String getExitCommand() {
		return "EXIT"; //$NON-NLS-1$
	}

	@Override
	public String getShowErrorsCommand() {
		return "SHOW ERRORS"; //$NON-NLS-1$
	}

	@Override
	public List<String> getUnsizableDatatypes() {
		// FIXME [BGA]: to implement for this vendor
		return Collections.EMPTY_LIST;
	}

	@Override
	public BigDecimal getDatatypeMaxSize(String type) throws UnsupportedDatatypeException {
		// FIXME [BGA]: to implement for this vendor
		if (null == type)
			throw new UnsupportedDatatypeException("The specified data type must be not null");
		throw new UnsupportedDatatypeException("The maximum size of data type [" + type
				+ "] is unknown for database vendor " + DBVendor.MYSQL.toString());
	}

	@Override
	public String formatSqlScriptValue(IDatatype type, Object value) {
		final String typeName = type.getName().toUpperCase();
		if (value == null) {
			return "null";
		} else if ("DATE".equals(typeName) || "DATETIME".equals(typeName)
				|| "TIMESTAMP".equals(typeName)) {
			final Date d = ConversionHelper.getDate(value);
			if (d == null) {
				return "null";
			} else {
				final String sqlDateStr = SQL_DATE_FORMAT.format(d);
				return "'" + sqlDateStr + "'";
			}
		} else if (listStringDatatypes().contains(typeName) || typeName.startsWith("ENUM")) {
			String encapsulator = "'";
			String strVal = value.toString();
			return encapsulator + strVal.replace(encapsulator, encapsulator + encapsulator)
					+ encapsulator;
		} else {
			return value.toString();
		}
	}

	@Override
	public List<String> getNumericDatatypes() {
		return Arrays.asList("TINYINT", "SMALLINT", "MEDIUMINT", "INT", "INTEGER", "BIGINT",
				"FLOAT", "DOUBLE", "DECIMAL", "NUMERIC", "BIT");
	}

	@Override
	public boolean isDecimalDatatype(IDatatype type) {
		return type != null && decimalDatatypes.contains(type.getName());
	}

	@Override
	public List<String> getDateDatatypes() {
		return Arrays.asList("DATE", "DATETIME", "TIMESTAMP");
	}

	@Override
	public boolean isTypedLengthSupportedFor(String datatype) {
		return false;
	}
}
