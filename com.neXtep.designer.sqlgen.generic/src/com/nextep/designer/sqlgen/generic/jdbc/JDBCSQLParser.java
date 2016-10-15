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
package com.nextep.designer.sqlgen.generic.jdbc;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import com.nextep.designer.dbgm.helpers.ConversionHelper;
import com.nextep.designer.sqlgen.model.ISQLParser;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class JDBCSQLParser implements ISQLParser, IDatatypeProvider {

	private final static DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	private final static List<String> decimalDatatypes = Arrays.asList("NUMERIC", "DECIMAL",
			"FLOAT", "REAL", "DOUBLE PRECISION");

	@Override
	public String getPromptCommand() {
		/*
		 * FIXME [BGA] PROMPT does not seem to be a ANSI-92 SQL command and
		 * should be replaced by a comment start sequence.
		 */
		return "PROMPT"; //$NON-NLS-1$
	}

	@Override
	public List<IPrototype> getPrototypes() {
		return Collections.emptyList();
	}

	@Override
	public String getStatementDelimiter() {
		return ";"; //$NON-NLS-1$
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
				"AUTO_INCREMENT", "AS", "COLUMN", "ON", "PRECISION", "TRUNCATE", "IS", "CONVERT",
				"CHARACTER", "TRIGGER", "AFTER", "BEFORE", "FUNCTION", "PROCEDURE", "RETURNS",
				"EXISTS", "RETURN", "DETERMINISTIC", "CONDITION"));
		tokens.put(DML, Arrays.asList("SELECT", "FROM", "WHERE", "HAVING", "GROUP", "BY", "ORDER",
				"UPDATE", "SET", "DELETE", "INSERT", "INTO", "BULK", "COLLECT", "VALUES",
				"BETWEEN", "IN", "OUT", "DISTINCT", "LIKE", "AND", "OR", "ASC", "DESC", "LIMIT",
				"INTO", "OUTFILE", "UNION", "ALL", "AS"));
		tokens.put(FUNC, Arrays.asList("COUNT", "CONCAT", "IFNULL", "LOWER", "UPPER", "NVL", "COS",
				"SIN", "PI", "SQRT", "ATAN", "ATAN2", "TAN"));
		tokens.put(DATATYPE, listSupportedDatatypes());
		tokens.put(LANG, Arrays.asList("FOR", "EACH", "ROW", "BEGIN", "DECLARE", "CONTINUE",
				"HANDLER", "FOUND", "WHILE", "DO", "REPEAT", "FETCH", "IF", "THEN", "ELSE", "END",
				"UNTIL", "CLOSE", "DELIMITER"));
		return tokens;
	}

	@Override
	public String getVarSeparator() {
		return ":"; //$NON-NLS-1$
	}

	@Override
	public Collection<IAutoEditStrategy> getAutoEditStrategies() {
		return Collections.emptyList();
	}

	@Override
	public IDatatype getDefaultDatatype() {
		return new Datatype("INTEGER"); //$NON-NLS-1$
	}

	@Override
	public Map<String, String> getEquivalentDatatypesMap() {
		return Collections.emptyMap();
	}

	@Override
	public List<String> listStringDatatypes() {
		return Arrays.asList("CHAR", "CLOB", "LONGNVARCHAR", "LONGVARCHAR", "NCHAR", "NCLOB",
				"NVARCHAR", "SQLXML", "VARCHAR");
	}

	@Override
	public List<String> listSupportedDatatypes() {
		return Arrays.asList("BIGINT", "BINARY", "BIT", "BLOB", "BOOLEAN", "CHAR", "CLOB", "DATE",
				"DECIMAL", "DOUBLE", "FLOAT", "INTEGER", "LONGNVARCHAR", "LONGVARBINARY",
				"LONGVARCHAR", "NCHAR", "NCLOB", "NUMERIC", "NVARCHAR", "REAL", "ROWID",
				"SMALLINT", "SQLXML", "TIME", "TIMESTAMP", "TINYINT", "VARBINARY", "VARCHAR");
	}

	@Override
	public String getColumnDefinitionEscaper() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getScriptCallerTag() {
		return "@"; //$NON-NLS-1$
	}

	@Override
	public String getCommentStartSequence() {
		return "--"; //$NON-NLS-1$
	}

	@Override
	public String getExitCommand() {
		return "EXIT"; //$NON-NLS-1$
	}

	@Override
	public String getShowErrorsCommand() {
		// SHOW ERRORS command is not supported by all vendors, returns nothing
		// by default.
		return ""; //$NON-NLS-1$
	}

	@Override
	public List<String> getUnsizableDatatypes() {
		// FIXME [BGA]: to implement for this vendor
		return Collections.EMPTY_LIST;
	}

	@Override
	public BigDecimal getDatatypeMaxSize(String type) throws UnsupportedDatatypeException {
		throw new UnsupportedDatatypeException(
				"The maximum size of a data type is unknown in a vendor-neutral context");
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
		return Arrays.asList("NUMERIC", "DECIMAL", "INTEGER", "SMALLINT", "FLOAT", "REAL",
				"DOUBLE PRECISION", "BIT", "BIGINT");
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
