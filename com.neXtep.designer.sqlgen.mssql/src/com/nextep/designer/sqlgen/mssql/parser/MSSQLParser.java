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
package com.nextep.designer.sqlgen.mssql.parser;

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
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.helpers.ConversionHelper;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * @author Darren Hartford
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MSSQLParser implements ISQLParser, IDatatypeProvider {

	private final static DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
	private final static DateFormat SQL_TIMESTAMP_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	public static final String DATATYPE_BIT = "BIT"; //$NON-NLS-1$
	public static final String DATATYPE_TIME = "TIME"; //$NON-NLS-1$
	public static final String DATATYPE_DATE = "DATE"; //$NON-NLS-1$
	public static final String DATATYPE_DATETIME = "DATETIME"; //$NON-NLS-1$
	public static final String DATATYPE_DATETIME2 = "DATETIME2"; //$NON-NLS-1$
	public static final String DATATYPE_DATETIMEOFFSET = "DATETIMEOFFSET"; //$NON-NLS-1$
	public static final String DATATYPE_SMALLDATETIME = "SMALLDATETIME"; //$NON-NLS-1$
	public static final String DATATYPE_CHAR = "CHAR"; //$NON-NLS-1$
	public static final String DATATYPE_VARCHAR = "VARCHAR"; //$NON-NLS-1$
	public static final String DATATYPE_NCHAR = "NCHAR"; //$NON-NLS-1$
	public static final String DATATYPE_NVARCHAR = "NVARCHAR"; //$NON-NLS-1$
	public static final String DATATYPE_VARBINARY = "VARBINARY"; //$NON-NLS-1$
	public static final String DATATYPE_BINARY = "BINARY"; //$NON-NLS-1$
	public static final String DATATYPE_TEXT = "TEXT"; //$NON-NLS-1$
	public static final String DATATYPE_NTEXT = "NTEXT"; //$NON-NLS-1$
	public static final String DATATYPE_XML = "XML"; //$NON-NLS-1$
	public static final String DATATYPE_TINYINT = "TINYINT"; //$NON-NLS-1$
	public static final String DATATYPE_SMALLINT = "SMALLINT"; //$NON-NLS-1$
	public static final String DATATYPE_INT = "INT"; //$NON-NLS-1$
	public static final String DATATYPE_BIGINT = "BIGINT"; //$NON-NLS-1$
	public static final String DATATYPE_DECIMAL = "DECIMAL"; //$NON-NLS-1$
	public static final String DATATYPE_REAL = "REAL"; //$NON-NLS-1$
	public static final String DATATYPE_FLOAT = "FLOAT"; //$NON-NLS-1$
	public static final String DATATYPE_NUMERIC = "NUMERIC"; //$NON-NLS-1$
	public static final String DATATYPE_MONEY = "MONEY"; //$NON-NLS-1$
	public static final String DATATYPE_SMALLMONEY = "SMALLMONEY"; //$NON-NLS-1$
	public static final String DATATYPE_GEOGRAPHY = "GEOGRAPHY"; //$NON-NLS-1$
	public static final String DATATYPE_TIMESTAMP = "TIMESTAMP"; //$NON-NLS-1$
	public static final String DATATYPE_UNIQUEIDENTIFIER = "UNIQUEIDENTIFIER"; //$NON-NLS-1$
	public static final String DATATYPE_HIERARCHYID = "HIERARCHYID"; //$NON-NLS-1$
	public static final String DATATYPE_SYSNAME = "SYSNAME"; //$NON-NLS-1$

	private static final Map<String, BigDecimal> DATATYPE_MAXSIZE_MAP = initDatatypeMaxSizeMap();

	private static final Map<String, BigDecimal> initDatatypeMaxSizeMap() {
		Map<String, BigDecimal> m = new HashMap<String, BigDecimal>();

		// TODO [BGA]: Add the maximum sizes of the other supported data types
		m.put(DATATYPE_NVARCHAR, new BigDecimal("1073741823")); //$NON-NLS-1$
		m.put(DATATYPE_VARBINARY, new BigDecimal("2147483647")); //$NON-NLS-1$

		return Collections.unmodifiableMap(m);
	}

	private String NEWLINE;

	public MSSQLParser() {
		NEWLINE = CorePlugin.getService(IGenerationService.class).getNewLine();
	}

	@Override
	public Map<String, List<String>> getTypedTokens() {
		Map<String, List<String>> tokens = new HashMap<String, List<String>>();

		tokens.put(DDL, Arrays.asList("ALTER", "CREATE", "TABLE", "INDEX", "VIEW", "CONSTRAINT", //$NON-NLS-1$
				"PRIMARY", "DROP", "DEFAULT", "CHANGE", "GRANT", "WITH", "CASCADE", "OR", //$NON-NLS-1$
				"REPLACE", "TRUE", "FALSE", "ADD", "MODIFY", "FOREIGN", "UNIQUE", "PRIMARY", "KEY", //$NON-NLS-1$
				"REFERENCES", "NULL", "NOT", "AUTO_INCREMENT", "AS", "COLUMN", "ON", "TRUNCATE", //$NON-NLS-1$
				"IS", "CONVERT", "CAST", "CHARACTER", "TRIGGER", "AFTER", "BEFORE", "FUNCTION", //$NON-NLS-1$
				"PROCEDURE", "RETURNS", "EXISTS", "RETURN", "DETERMINISTIC", "CONDITION", //$NON-NLS-1$
				"SEQUENCE", "MAXVALUE", "MINVALUE", "NO", "INCREMENT", "BY", "START", "STRICT", //$NON-NLS-1$
				"ALIAS", "TEMP", "BEGIN", "GO", "REPLICATION", "IDENTITY", "ROWGUIDCOL", //$NON-NLS-1$
				"CLUSTERED"));
		tokens.put(DML, Arrays.asList("SELECT", "FROM", "WHERE", "HAVING", "GROUP", "BY", "ORDER", //$NON-NLS-1$
				"UPDATE", "SET", "DELETE", "INSERT", "INTO", "BULK", "COLLECT", "VALUES", //$NON-NLS-1$
				"BETWEEN", "IN", "OUT", "DISTINCT", "LIKE", "AND", "OR", "ASC", "DESC", "LIMIT", //$NON-NLS-1$
				"TOP", "INTO", "OUTFILE", "UNION", "ALL", "AS", "OFFSET"));
		tokens.put(FUNC, Arrays.asList(// Date-based functions
				"SYSDATETIME", "SYSDATETIMEOFFSET", "CURRENT_TIMESTAMP", "GETDATE", "GETUTCDATE", //$NON-NLS-1$
				"DATENAME", "DATEPART", "DAY", "MONTH", "YEAR", "DATEDIFF", //$NON-NLS-1$
				"DATEADD", //$NON-NLS-1$
				// Math functions
				"ABS", "CEILING", "COS", "DEGREES", "EXP", "FLOOR", "LOG", "PI", "POWER", //$NON-NLS-1$
				"RADIANS", "RAND", "ROUND", "SQRT", "AVG", "SUM", //$NON-NLS-1$
				// String manip
				"ASCII", "CHAR", "CHARINDEX", "LEN", "LEFT", "LTRIM", "RIGHT", "RTRIM", "REPLACE", //$NON-NLS-1$
				"SOUNDEX", "SUBSTRING", "RAISEERROR", //$NON-NLS-1$
				// misc
				"OBECT_DEFINITION", "OBJECT_NAME", "OBJECT_ID", "DATALENGTH"));

		final List<String> datatypes = new ArrayList<String>(listSupportedDatatypes());
		tokens.put(DATATYPE, datatypes);
		tokens.put(LANG, Arrays.asList("FOR", "EACH", "ROW", "DECLARE", "CONTINUE", "HANDLER",
				"FOUND", "WHILE", "DO", "REPEAT", "FETCH", "IF", "THEN", "ELSE", "ELSIF", "END",
				"UNTIL", "CLOSE", "DELIMITER", "LOOP", "CURSOR", "EXIT", "WHEN", "OPEN", "CASE",
				"EXECUTE", "RAISERROR", "EXEC", "GO"));

		return tokens;
	}

	@Override
	public List<IPrototype> getPrototypes() {
		return Collections.emptyList();
	}

	@Override
	public String getStringConcatenator() {
		return "+"; //$NON-NLS-1$
	}

	@Override
	public char getStringDelimiter() {
		return '\'';
	}

	@Override
	public String getVarSeparator() {
		return "@"; //$NON-NLS-1$
	}

	@Override
	public IDatatype getDefaultDatatype() {
		return new Datatype("numeric"); //$NON-NLS-1$
	}

	@Override
	public Map<String, String> getEquivalentDatatypesMap() {
		return Collections.emptyMap();
	}

	@Override
	public Collection<IAutoEditStrategy> getAutoEditStrategies() {
		return Collections.emptyList();
	}

	@Override
	public String getStatementDelimiter() {
		//		return NEWLINE + "GO" + NEWLINE; //$NON-NLS-1$
		return "GO"; //$NON-NLS-1$
	}

	@Override
	public String getColumnDefinitionEscaper() {
		// RESEARCH is this intended for [column] brackets?
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getCommentStartSequence() {
		return "--"; //$NON-NLS-1$
	}

	@Override
	public String getScriptCallerTag() {
		// FIXME Check if this syntax is supported by sqlcmd utility
		return ""; //$NON-NLS-1$
	}

	@Override
	public String getPromptCommand() {
		return "PRINT";
	}

	@Override
	public String getExitCommand() {
		// TODO RESEARCH return "RETURN"; //$NON-NLS-1$
		return "";
	}

	@Override
	public String getShowErrorsCommand() {
		// TODO RESEARCH there is a 'RAISERROR'
		return ""; //$NON-NLS-1$
	}

	@Override
	public List<String> getUnsizableDatatypes() {
		return Arrays.asList(DATATYPE_BIT, DATATYPE_DATE, DATATYPE_DATETIME,
				DATATYPE_SMALLDATETIME, DATATYPE_TINYINT, DATATYPE_SMALLINT, DATATYPE_INT,
				DATATYPE_BIGINT, DATATYPE_XML, DATATYPE_MONEY, DATATYPE_SMALLMONEY,
				DATATYPE_UNIQUEIDENTIFIER, DATATYPE_SYSNAME, DATATYPE_GEOGRAPHY,
				DATATYPE_HIERARCHYID);
	}

	@Override
	public BigDecimal getDatatypeMaxSize(String type) throws UnsupportedDatatypeException {
		if (null == type)
			throw new UnsupportedDatatypeException("The specified data type must be not null");

		if (DATATYPE_MAXSIZE_MAP.containsKey(type)) {
			return DATATYPE_MAXSIZE_MAP.get(type);
		}

		throw new UnsupportedDatatypeException("The maximum size of data type [" + type
				+ "] is unknown for database vendor " + DBVendor.MSSQL.toString());
	}

	@Override
	public String formatSqlScriptValue(IDatatype type, Object value) {
		final String typeName = type.getName().toUpperCase();
		if (value == null) {
			return "null";
		} else if (isDateDatatype(type)) {
			final Date d = ConversionHelper.getDate(value);
			if (d == null) {
				return "null";
			} else {
				String sqlDateStr = null;
				if (DATATYPE_DATE.equals(typeName)) {
					sqlDateStr = SQL_DATE_FORMAT.format(d);
				} else {
					sqlDateStr = SQL_TIMESTAMP_FORMAT.format(d);
				}
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
	public List<String> listSupportedDatatypes() {
		return Arrays.asList(DATATYPE_BIT, DATATYPE_CHAR, DATATYPE_VARCHAR, DATATYPE_NCHAR,
				DATATYPE_NVARCHAR, DATATYPE_SMALLINT, DATATYPE_INT, DATATYPE_BIGINT,
				DATATYPE_TINYINT, DATATYPE_DECIMAL, DATATYPE_NUMERIC, DATATYPE_REAL,
				DATATYPE_FLOAT, DATATYPE_MONEY, DATATYPE_SMALLMONEY, DATATYPE_TIMESTAMP,
				DATATYPE_DATE, DATATYPE_DATETIME, DATATYPE_DATETIME2, DATATYPE_DATETIMEOFFSET,
				DATATYPE_SMALLDATETIME, DATATYPE_TIME, DATATYPE_VARBINARY, DATATYPE_XML,
				DATATYPE_UNIQUEIDENTIFIER, DATATYPE_SYSNAME, DATATYPE_GEOGRAPHY,
				DATATYPE_HIERARCHYID, DATATYPE_TEXT, DATATYPE_NTEXT, DATATYPE_BINARY);
	}

	@Override
	public List<String> listStringDatatypes() {
		return Arrays.asList(DATATYPE_CHAR, DATATYPE_NCHAR, DATATYPE_VARCHAR, DATATYPE_NVARCHAR,
				DATATYPE_TEXT, DATATYPE_NTEXT);
	}

	@Override
	public List<String> getNumericDatatypes() {
		return Arrays.asList(DATATYPE_BIT, DATATYPE_TINYINT, DATATYPE_SMALLINT, DATATYPE_INT,
				DATATYPE_BIGINT, DATATYPE_DECIMAL, DATATYPE_NUMERIC, DATATYPE_REAL, DATATYPE_FLOAT,
				DATATYPE_MONEY, DATATYPE_SMALLMONEY);
	}

	@Override
	public List<String> getDateDatatypes() {
		return Arrays.asList(DATATYPE_DATE, DATATYPE_TIMESTAMP, DATATYPE_DATETIME,
				DATATYPE_DATETIME2, DATATYPE_SMALLDATETIME);
	}

	@Override
	public boolean isDecimalDatatype(IDatatype type) {
		return type != null && decimalDatatypes.contains(type.getName());
	}

	public boolean isDateDatatype(IDatatype type) {
		return type != null && getDateDatatypes().contains(type.getName());
	}

	private final static List<String> decimalDatatypes = Arrays.asList(DATATYPE_DECIMAL,
			DATATYPE_NUMERIC, DATATYPE_REAL, DATATYPE_FLOAT, DATATYPE_MONEY, DATATYPE_SMALLMONEY);

	private final static List<String> otherDataTypes = Arrays.asList(DATATYPE_BIT, DATATYPE_TIME,
			DATATYPE_BINARY, DATATYPE_VARBINARY, DATATYPE_XML);

	@Override
	public boolean isTypedLengthSupportedFor(String datatype) {
		return false;
	}

}
