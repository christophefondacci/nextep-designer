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
package com.nextep.designer.sqlgen.oracle.parser;

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
import com.nextep.datadesigner.sqlgen.impl.Prototype;
import com.nextep.datadesigner.sqlgen.model.IPrototype;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.helpers.ConversionHelper;
import com.nextep.designer.sqlgen.model.ISQLParser;
import com.nextep.designer.sqlgen.oracle.strategies.SQLAutoIndentStrategy;

/**
 * Parser definition for Oracle SQL - PL/SQL.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleSQLParser implements ISQLParser, IDatatypeProvider {

	private final Collection<IAutoEditStrategy> editStrategies;
	private final static DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); //$NON-NLS-1$
	private final static String SQL_DATE_PATTERN = "YYYY/MM/DD HH24:MI:SS"; //$NON-NLS-1$

	// private static final Log log = LogFactory.getLog(OracleSQLParser.class);
	public OracleSQLParser() {
		editStrategies = new ArrayList<IAutoEditStrategy>();
		editStrategies.add(new SQLAutoIndentStrategy(this));
	}

	@Override
	public Map<String, List<String>> getTypedTokens() {
		Map<String, List<String>> tokens = new HashMap<String, List<String>>();
		tokens.put(DDL, Arrays.asList("CREATE", "ALTER", "TABLE", "INDEX", "VIEW", "MATERIALIZED",
				"CONSTRAINT", "PRIMARY", "DROP", "PACKAGE", "BODY", "SNAPSHOT", "USER", "SYNONYM",
				"GRANT", "ROLE", "TO", "WITH", "OPTION", "ADMIN", "CASCADE", "OR", "REPLACE",
				"DECLARE", "BEGIN", "END", "IDENTIFIED", "TEMPORARY", "TABLESPACE", "DATAFILE",
				"MAXEXTENTS", "MINEXTENTS", "AUTOEXTEND", "ON", "TRUE", "FALSE", "REVOKE", "IS",
				"ADD", "MODIFY", "FOREIGN", "UNIQUE", "PRIMARY", "KEY", "REFERENCES", "NULL",
				"NOT", "SEQUENCE", "INCREMENT", "START", "MINVALUE", "NOMINVALUE", "MAXVALUE",
				"NOMAXVALUE", "CYCLE", "NOCYCLE", "CACHE", "NOCACHE", "NOORDER", "PROCEDURE",
				"FUNCTION", "AS", "COLUMN", "PCTFREE", "PCTUSED", "INITRANS", "MAXTRANS", "RENAME",
				"TO", "TRIGGER", "AFTER", "BEFORE", "INSTEAD", "EACH", "ROW", "MOVE", "PARTITION",
				"LESS", "THAN", "LOCAL", "WRAPPED", "TYPE", "LOGGING", "NOLOGGING", "ORGANIZATION",
				"MAP", "MEMBER", "JAVA", "SOURCE", "NAMED", "DEFAULT", "CHECK", "USING", "COMMENT",
				"CLUSTER", "LANGUAGE", "PIPELINED", "BUILD", "REFRESH", "FAST", "COMPLETE",
				"DEMAND", "ENABLE", "DISABLE", "QUERY", "REWRITE", "NEXT", "COMMENT", "GLOBAL",
				"FETCH", "CLOSE", "OR", "GENERATED", "ALWAYS", "VIRTUAL"));

		tokens.put(DML, Arrays.asList("SELECT", "FROM", "WHERE", "HAVING", "GROUP", "BY",
				"CONNECT", "ORDER", "UPDATE", "SET", "DELETE", "INSERT", "NEXTVAL", "INTO", "BULK",
				"COLLECT", "VALUES", "BETWEEN", "IN", "OUT", "DISTINCT", "LIKE", "CAST", "AND",
				"OR", "ASC", "DESC", "DEFINE", "OFF", "EXIT", "SPOOL", "MERGE", "FULL", "OUTER",
				"JOIN", "MINUS", "UNION", "ALL", "EXISTS"));
		tokens.put(FUNC, Arrays.asList("TO_CHAR", "TO_NUMBER", "TO_DATE", "MIN", "MAX", "AVG",
				"LAG", "COUNT", "TRUNC", "ROUND", "SUM", "LENGTH", "SUBSTR", "INSTR", "GREATEST",
				"MOD", "DECODE", "NVL", "SYSDATE", "RAISE_APPLICATION_ERROR", "TRIM",
				"EXCEPTION_INIT", "TRANSLATE", "TO_NCHAR", "UPPER", "LOWER", "LTRIM", "RTRIM"));
		tokens.put(DATATYPE, listSupportedDatatypes());
		tokens.put(LANG, Arrays.asList("IF", "ELSE", "THEN", "ELSIF", "RETURN", "WHILE",
				"EXCEPTION", "WHEN", "OTHERS", "LOOP", "EXECUTE", "IMMEDIATE", "FOR", "RAISE",
				"MATCHED", "FORALL", "OPEN", "CASE", "CURSOR"));
		tokens.put(SPECIAL, Arrays.asList("COMMIT", "ROLLBACK", "PRAGMA", "AUTONOMOUS_TRANSACTION",
				"SAVEPOINT", "RESTRICT_REFERENCES", "SHOW", "ERRORS"));
		return tokens;
	}

	@Override
	public Collection<IAutoEditStrategy> getAutoEditStrategies() {
		return editStrategies;
	}

	@Override
	public String getVarSeparator() {
		return ":"; //$NON-NLS-1$
	}

	@Override
	public List<IPrototype> getPrototypes() {
		List<IPrototype> prototypes = new ArrayList<IPrototype>();

		prototypes.add(new Prototype("SELECT", "Generates a SQL template SELECT", //$NON-NLS-1$
				"SELECT  FROM  WHERE ;", 13)); //$NON-NLS-1$
		prototypes.add(new Prototype("SELECT", "Generates a PLSQL template SELECT INTO", //$NON-NLS-1$
				"SELECT  INTO  FROM  WHERE ;", 13)); //$NON-NLS-1$
		prototypes.add(new Prototype("INSERT", "Generates a SQL template INSERT", //$NON-NLS-1$
				"INSERT INTO  (  ) VALUES ( );", 12)); //$NON-NLS-1$
		prototypes.add(new Prototype("UPDATE", "Generates a SQL template UPDATE WHERE", //$NON-NLS-1$
				"UPDATE  SET  WHERE  ;", 7)); //$NON-NLS-1$
		prototypes.add(new Prototype("DELETE", "Generates a SQL template DELETE WHERE", //$NON-NLS-1$
				"DELETE FROM  WHERE  ;", 12)); //$NON-NLS-1$

		prototypes.add(new Prototype("PROCEDURE", "Creates an empty procedure body declaration", //$NON-NLS-1$
				"PROCEDURE  ( ) IS\nBEGIN\n\nEND;\n", 10)); //$NON-NLS-1$
		prototypes.add(new Prototype("FUNCTION", "Creates an empty function body declaration", //$NON-NLS-1$
				"FUNCTION  ( ) RETURN  IS\nBEGIN\n\nEND;\n", 9)); //$NON-NLS-1$

		return prototypes;
	}

	@Override
	public char getStringDelimiter() {
		return '\'';
	}

	@Override
	public String getStringConcatenator() {
		return "||"; //$NON-NLS-1$
	}

	@Override
	public List<String> listSupportedDatatypes() {
		return Arrays.asList("VARCHAR2", "NUMBER", "DATE", "TIMESTAMP", "CLOB", "BLOB", "ROWID",
				"NVARCHAR2", "CHAR", "VARCHAR", "NCHAR", "PLS_INTEGER", "BINARY_INTEGER", "LONG",
				"INTERVAL", "DAY", "MONTH", "RAW", "UROWID", "MLSLABEL", "NCLOB", "BFILE",
				"XMLTYPE", "CONSTANT", "INTEGER");
	}

	@Override
	public IDatatype getDefaultDatatype() {
		return new Datatype("NUMBER", 10, 0); //$NON-NLS-1$
	}

	@Override
	public List<String> listStringDatatypes() {
		return Arrays.asList("VARCHAR2", "DATE", "TIMESTAMP", "CLOB", "NVARCHAR2", "CHAR",
				"VARCHAR", "NCHAR", "NCLOB");
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, String> getEquivalentDatatypesMap() {
		return Collections.EMPTY_MAP;
	}

	@Override
	public String getPromptCommand() {
		return "PROMPT"; //$NON-NLS-1$
	}

	@Override
	public String getStatementDelimiter() {
		return "/"; //$NON-NLS-1$
	}

	@Override
	public String getColumnDefinitionEscaper() {
		return "\""; //$NON-NLS-1$
	}

	@Override
	public String getScriptCallerTag() {
		return "@@"; //$NON-NLS-1$
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
		return "SHOW ERRORS"; //$NON-NLS-1$
	}

	@Override
	public List<String> getUnsizableDatatypes() {
		return Arrays.asList("CLOB", "NCLOB", "LONG", "BLOB", "TIMESTAMP", "DATE");
	}

	@Override
	public BigDecimal getDatatypeMaxSize(String type) throws UnsupportedDatatypeException {
		// FIXME [BGA]: to implement for this vendor
		if (null == type)
			throw new UnsupportedDatatypeException("The specified data type must be not null");
		throw new UnsupportedDatatypeException("The maximum size of data type [" + type
				+ "] is unknown for database vendor " + DBVendor.ORACLE.toString());
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
				final String sqlDateStr = SQL_DATE_FORMAT.format(d);
				return "to_date('" + sqlDateStr + "','" + SQL_DATE_PATTERN + "')";
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
		return Arrays.asList("NUMBER", "PLS_INTEGER", "BINARY_INTEGER", "LONG");
	}

	@Override
	public boolean isDecimalDatatype(IDatatype type) {
		return (type != null && type.getPrecision() > 0);
	}

	@Override
	public List<String> getDateDatatypes() {
		return Arrays.asList("DATE", "TIMESTAMP");
	}

	@Override
	public boolean isTypedLengthSupportedFor(String datatype) {
		return true;
	}
}
