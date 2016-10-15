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
package com.nextep.designer.sqlgen.postgre.parser;

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

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PostGreSQLParser implements ISQLParser, IDatatypeProvider {

	private final static String DATATYPE_BOOLEAN = "BOOLEAN"; //$NON-NLS-1$
	private final static String VALUE_TRUE = "true"; //$NON-NLS-1$
	private final static String VALUE_FALSE = "false"; //$NON-NLS-1$
	private final static DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private final static DateFormat SQL_TIMESTAMP_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	private final static List<String> decimalDatatypes = Arrays.asList("DECIMAL", "NUMERIC", //$NON-NLS-1$ //$NON-NLS-2$
			"REAL", "DOUBLE PRECISION"); //$NON-NLS-1$//$NON-NLS-2$

	@Override
	public List<IPrototype> getPrototypes() {
		return Collections.emptyList();
	}

	@Override
	public String getStringConcatenator() {
		return "||"; //$NON-NLS-1$
	}

	@Override
	public char getStringDelimiter() {
		return '\'';
	}

	@Override
	public Map<String, List<String>> getTypedTokens() {
		Map<String, List<String>> tokens = new HashMap<String, List<String>>();
		tokens.put(DDL, Arrays.asList("CREATE", "ALTER", "TABLE", "INDEX", "VIEW", "CONSTRAINT",
				"PRIMARY", "DROP", "DEFAULT", "CHANGE", "GRANT", "ROLE", "TO", "WITH", "CASCADE",
				"OR", "REPLACE", "IDENTIFIED", "TABLESPACE", "TRUE", "FALSE", "ADD", "MODIFY",
				"FOREIGN", "UNIQUE", "PRIMARY", "KEY", "REFERENCES", "NULL", "NOT",
				"AUTO_INCREMENT", "AS", "COLUMN", "ON", "TRUNCATE", "IS", "CONVERT", "CHARACTER",
				"TRIGGER", "AFTER", "BEFORE", "FUNCTION", "PROCEDURE", "RETURNS", "EXISTS",
				"RETURN", "DETERMINISTIC", "CONDITION", "SEQUENCE", "MAXVALUE", "MINVALUE", "NO",
				"CYCLE", "CACHE", "INCREMENT", "BY", "START", "LANGUAGE", "IMMUTABLE", "VOLATILE",
				"STABLE", "STRICT", "ALIAS", "TEMP", "COST", "USING", "CHECK", "INHERITS", "BTREE",
				"HASH", "GIN"));
		tokens.put(DML, Arrays.asList("SELECT", "FROM", "WHERE", "HAVING", "GROUP", "BY", "ORDER",
				"UPDATE", "SET", "DELETE", "INSERT", "INTO", "BULK", "COLLECT", "VALUES",
				"BETWEEN", "IN", "OUT", "DISTINCT", "LIKE", "AND", "OR", "ASC", "DESC", "LIMIT",
				"INTO", "OUTFILE", "UNION", "ALL", "AS", "OFFSET"));
		tokens.put(FUNC, Arrays.asList("COUNT", "CONCAT", "IFNULL", "LOWER", "UPPER", "NVL", "COS",
				"SIN", "PI", "SQRT", "ATAN", "ATAN2", "TAN", "STRPOS", "TRIM", "NEXTVAL", "NOW"));
		final List<String> datatypes = new ArrayList<String>(listSupportedDatatypes());
		datatypes.add("PRECISION");
		tokens.put(DATATYPE, datatypes);
		tokens.put(LANG, Arrays.asList("FOR", "EACH", "ROW", "BEGIN", "DECLARE", "CONTINUE",
				"HANDLER", "FOUND", "WHILE", "DO", "REPEAT", "FETCH", "IF", "THEN", "ELSE",
				"ELSIF", "END", "UNTIL", "CLOSE", "DELIMITER", "LOOP", "CURSOR", "EXIT", "WHEN",
				"OPEN", "CASE", "EXECUTE", "TIME", "ZONE", "WITH", "WITHOUT", "VARYING"));
		return tokens;
	}

	@Override
	public String getVarSeparator() {
		return "@"; //$NON-NLS-1$
	}

	@Override
	public IDatatype getDefaultDatatype() {
		return new Datatype("NUMERIC"); //$NON-NLS-1$
	}

	@Override
	public Map<String, String> getEquivalentDatatypesMap() {
		return Collections.emptyMap();
	}

	@Override
	public List<String> listStringDatatypes() {
		return Arrays.asList("TEXT", "CHAR", "VARCHAR", "REGCLASS", "CHARACTER VARYING",
				"CHARACTER");
	}

	@Override
	public List<String> listSupportedDatatypes() {
		return Arrays.asList("INTEGER", "BIGINT", "BYTEA", "TEXT", "CHAR", "VARCHAR", "BOOLEAN",
				"BIT", "REAL", "SMALLINT", "SERIAL", "DATE", "TIMESTAMP", "MONEY", "REGCLASS",
				"RECORD", "DOUBLE PRECISION", "DOUBLE", "NUMERIC");
	}

	@Override
	public Collection<IAutoEditStrategy> getAutoEditStrategies() {
		return Collections.emptyList();
	}

	@Override
	public String getPromptCommand() {
		return "\\echo"; //$NON-NLS-1$
	}

	@Override
	public String getStatementDelimiter() {
		return ";"; //$NON-NLS-1$
	}

	@Override
	public String getColumnDefinitionEscaper() {
		return "\""; //$NON-NLS-1$
	}

	@Override
	public String getScriptCallerTag() {
		return "\\i "; //$NON-NLS-1$
	}

	@Override
	public String getCommentStartSequence() {
		return "-- "; //$NON-NLS-1$
	}

	@Override
	public String getExitCommand() {
		return "\\q"; //$NON-NLS-1$
	}

	@Override
	public String getShowErrorsCommand() {
		// SHOW ERRORS command does not exist in PostgreSQL, errors are reported
		// automatically.
		return ""; //$NON-NLS-1$
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
				+ "] is unknown for database vendor " + DBVendor.POSTGRE.toString());
	}

	@Override
	public String formatSqlScriptValue(IDatatype type, Object value) {
		final String typeName = type.getName().toUpperCase();
		if (value == null) {
			return "null";
		} else if ("DATE".equals(typeName) || typeName.startsWith("TIMESTAMP")) {
			final Date d = ConversionHelper.getDate(value);
			if (d == null) {
				return "null";
			} else {
				String sqlDateStr = null;
				if ("DATE".equals(typeName)) {
					sqlDateStr = SQL_DATE_FORMAT.format(d);
				} else {
					sqlDateStr = SQL_TIMESTAMP_FORMAT.format(d);
				}
				return "'" + sqlDateStr + "'";
			}
		} else if (DATATYPE_BOOLEAN.equals(typeName)) {
			// Converting boolean numbers to true false
			if (value instanceof Number) {
				if (((Number) value).intValue() == 1) {
					return VALUE_TRUE;
				} else {
					return VALUE_FALSE;
				}
			} else if (value instanceof String) {
				try {
					int val = Integer.parseInt((String) value);
					if (val == 1) {
						return VALUE_TRUE;
					} else {
						return VALUE_FALSE;
					}
				} catch (NumberFormatException e) {
					if (Boolean.parseBoolean((String) value)) {
						return VALUE_TRUE;
					} else {
						return VALUE_FALSE;
					}
				}
			} else {
				return value == null ? null : value.toString();
			}
		} else if (listStringDatatypes().contains(typeName)) {
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
		return Arrays.asList("SMALLINT", "INTEGER", "BIGINT", "DECIMAL", "NUMERIC", "REAL",
				"DOUBLE PRECISION", "SERIAL", "BIGSERIAL", "INT");
	}

	@Override
	public boolean isDecimalDatatype(IDatatype type) {
		return type != null && decimalDatatypes.contains(type.getName());
	}

	@Override
	public List<String> getDateDatatypes() {
		return Arrays.asList("DATE", "TIMESTAMP");
	}

	@Override
	public boolean isTypedLengthSupportedFor(String datatype) {
		return false;
	}
}
