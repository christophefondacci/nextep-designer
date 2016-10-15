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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.sqlgen.parsers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jface.text.IAutoEditStrategy;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.model.IPrototype;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.ISQLParser;

/**
 * A specific parser for Derby, based on the JDBC parser, but removing any column escape character
 * so that derby SQL is never escaped.
 * 
 * @author Christophe Fondacci
 */
public class DerbySqlParser implements ISQLParser {

	private ISQLParser jdbcParser;

	public DerbySqlParser() {
		jdbcParser = GeneratorFactory.getSQLParser(DBVendor.JDBC);
	}

	public Map<String, List<String>> getTypedTokens() {
		final Map<String, List<String>> tokens = new HashMap<String, List<String>>();
		tokens.put(DDL, Arrays
				.asList("ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC",
						"ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT",
						"BIT", "BOOLEAN", "BOTH", "BY", "CALL", "CASCADE", "CASCADED", "CASE",
						"CAST", "CHAR", "CHARACTER", "CHECK", "CLOSE", "COALESCE", "COLLATE",
						"COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT",
						"CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", "CREATE", "CURRENT",
						"CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER",
						"CURSOR", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT",
						"DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DIAGNOSTICS",
						"DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "ELSE", "END", "END-EXEC",
						"ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXPLAIN",
						"EXTERNAL", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND",
						"FROM", "FULL", "FUNCTION", "GET", "GETCURRENTCONNECTION", "GLOBAL", "GO",
						"GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN",
						"INDICATOR", "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE",
						"INSERT", "INT", "INTEGER", "INTERSECT", "INTO", "IS", "ISOLATION", "JOIN",
						"KEY", "LAST", "LEFT", "LIKE", "LOWER", "LTRIM", "MATCH", "MAX", "MIN",
						"MINUTE", "NATIONAL", "NATURAL", "NCHAR", "NVARCHAR", "NEXT", "NO", "NOT",
						"NULL", "NULLIF", "NUMERIC", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR",
						"ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "PREPARE",
						"PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC",
						"READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT",
						"ROLLBACK", "ROWS", "RTRIM", "SCHEMA", "SCROLL", "SECOND", "SELECT",
						"SESSION_USER", "SET", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE",
						"SQLERROR", "SQLSTATE", "SUBSTR", "SUBSTRING", "SUM", "SYSTEM_USER",
						"TABLE", "TEMPORARY", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO",
						"TRANSACTION", "TRANSLATE", "TRANSLATION", "TRUE", "UNION", "UNIQUE",
						"UNKNOWN", "UPDATE", "UPPER", "USER", "USING", "VALUES", "VARCHAR",
						"VARYING", "VIEW", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE", "XML",
						"XMLEXISTS", "XMLPARSE", "XMLQUERY", "XMLSERIALIZE", "YEAR"));
		return tokens;
	}

	public String getVarSeparator() {
		return jdbcParser.getVarSeparator();
	}

	public char getStringDelimiter() {
		return jdbcParser.getStringDelimiter();
	}

	public String getStringConcatenator() {
		return jdbcParser.getStringConcatenator();
	}

	public Collection<IAutoEditStrategy> getAutoEditStrategies() {
		return jdbcParser.getAutoEditStrategies();
	}

	public List<IPrototype> getPrototypes() {
		return jdbcParser.getPrototypes();
	}

	public String getPromptCommand() {
		return jdbcParser.getPromptCommand();
	}

	public String getStatementDelimiter() {
		return jdbcParser.getStatementDelimiter();
	}

	public String getColumnDefinitionEscaper() {
		// Not escaping anything for derby
		return ""; //$NON-NLS-1$
	}

	public String getScriptCallerTag() {
		return jdbcParser.getScriptCallerTag();
	}

	public String getExitCommand() {
		return jdbcParser.getExitCommand();
	}

	public String getCommentStartSequence() {
		return jdbcParser.getCommentStartSequence();
	}

	public String getShowErrorsCommand() {
		return jdbcParser.getShowErrorsCommand();
	}

	public String formatSqlScriptValue(IDatatype type, Object value) {
		return jdbcParser.formatSqlScriptValue(type, value);
	}

}
