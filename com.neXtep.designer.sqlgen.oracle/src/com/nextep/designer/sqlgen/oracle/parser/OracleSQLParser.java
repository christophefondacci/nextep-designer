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

		tokens.put(DDL, Arrays.asList("CREATE", "ALTER", "TABLE", "INDEX", "VIEW", "MATERIALIZED", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"CONSTRAINT", "PRIMARY", "DROP", "PACKAGE", "BODY", "SNAPSHOT", "USER", "SYNONYM", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"GRANT", "ROLE", "TO", "WITH", "OPTION", "ADMIN", "CASCADE", "OR", "REPLACE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"DECLARE", "BEGIN", "END", "IDENTIFIED", "TEMPORARY", "TABLESPACE", "DATAFILE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"MAXEXTENTS", "MINEXTENTS", "AUTOEXTEND", "ON", "TRUE", "FALSE", "REVOKE", "IS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"ADD", "MODIFY", "FOREIGN", "UNIQUE", "PRIMARY", "KEY", "REFERENCES", "NULL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"NOT", "SEQUENCE", "INCREMENT", "START", "MINVALUE", "NOMINVALUE", "MAXVALUE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"NOMAXVALUE", "CYCLE", "NOCYCLE", "CACHE", "NOCACHE", "NOORDER", "PROCEDURE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"FUNCTION", "AS", "COLUMN", "PCTFREE", "PCTUSED", "INITRANS", "MAXTRANS", "RENAME", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"TO", "TRIGGER", "AFTER", "BEFORE", "INSTEAD", "EACH", "ROW", "MOVE", "PARTITION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"LESS", "THAN", "LOCAL", "WRAPPED", "TYPE", "LOGGING", "NOLOGGING", "ORGANIZATION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"MAP", "MEMBER", "JAVA", "SOURCE", "NAMED", "DEFAULT", "CHECK", "USING", "COMMENT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"CLUSTER", "LANGUAGE", "PIPELINED", "BUILD", "REFRESH", "FAST", "COMPLETE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"DEMAND", "ENABLE", "DISABLE", "QUERY", "REWRITE", "NEXT", "COMMENT", "GLOBAL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"FETCH", "CLOSE", "OR", "COMPRESS", "GENERATED", "ALWAYS", "VIRTUAL")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		tokens.put(DML, Arrays.asList("SELECT", "FROM", "WHERE", "HAVING", "GROUP", "BY", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"CONNECT", "ORDER", "UPDATE", "SET", "DELETE", "INSERT", "NEXTVAL", "INTO", "BULK", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"COLLECT", "VALUES", "BETWEEN", "IN", "OUT", "DISTINCT", "LIKE", "CAST", "AND", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"OR", "ASC", "DESC", "DEFINE", "OFF", "EXIT", "SPOOL", "MERGE", "FULL", "OUTER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
				"JOIN", "MINUS", "UNION", "ALL", "EXISTS")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		tokens.put(FUNC, Arrays.asList("ABS", "ACOS", "ASIN", "ATAN", "ATAN2", "BITAND", "CEIL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"COS", "COSH", "EXP", "FLOOR", "LN", "LOG", "MOD", "NANVL", "POWER", "REMAINDER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
				"ROUND", "SIGN", "SIN", "SINH", "SQRT", "TAN", "TANH", "TRUNC", "WIDTH_BUCKET", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"CHR", "CONCAT", "INITCAP", "LOWER", "LPAD", "LTRIM", "NCHR", "NLS_INITCAP", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"NLS_LOWER", "NLS_UPPER", "NLSSORT", "REGEXP_REPLACE", "REGEXP_SUBSTR", "REPLACE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"RPAD", "RTRIM", "SOUNDEX", "SUBSTR", "TRANSLATE", "USING", "TRIM", "UPPER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"ASCII", "INSTR", "LENGTH", "REGEXP_COUNT", "REGEXP_INSTR", "NLS_CHARSET_DECL_LEN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"NLS_CHARSET_ID", "NLS_CHARSET_NAME", "ADD_MONTHS", "CURRENT_DATE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"CURRENT_TIMESTAMP", "DBTIMEZONE", "EXTRACT", "FROM_TZ", "LAST_DAY", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"LOCALTIMESTAMP", "MONTHS_BETWEEN", "NEW_TIME", "NEXT_DAY", "NUMTODSINTERVAL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"NUMTOYMINTERVAL", "ORA_DST_AFFECTED", "ORA_DST_CONVERT", "ORA_DST_ERROR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"SESSIONTIMEZONE", "SYS_EXTRACT_UTC", "SYSDATE", "SYSTIMESTAMP", "TO_CHAR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"TO_DSINTERVAL", "TO_TIMESTAMP", "TO_TIMESTAMP_TZ", "TO_YMINTERVAL", "TZ_OFFSET", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"GREATEST", "LEAST", "ASCIISTR", "BIN_TO_NUM", "CAST", "CHARTOROWID", "COMPOSE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"CONVERT", "DECOMPOSE", "HEXTORAW", "RAWTOHEX", "RAWTONHEX", "ROWIDTOCHAR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"ROWIDTONCHAR", "SCN_TO_TIMESTAMP", "TIMESTAMP_TO_SCN", "TO_BINARY_DOUBLE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"TO_BINARY_FLOAT", "TO_BLOB", "TO_CLOB", "TO_DATE", "TO_LOB", "TO_MULTI_BYTE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"TO_NCHAR", "TO_NCLOB", "TO_NUMBER", "TO_SINGLE_BYTE", "TREAT", "UNISTR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"BFILENAME", "EMPTY_BLOB", "EMPTY_CLOB", "CARDINALITY", "COLLECT", "POWERMULTISET", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"POWERMULTISET_BY_CARDINALITY", "SET", "SYS_CONNECT_BY_PATH", "CLUSTER_DETAILS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"CLUSTER_DISTANCE", "CLUSTER_ID", "CLUSTER_PROBABILITY", "CLUSTER_SET", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"FEATURE_DETAILS", "FEATURE_ID", "FEATURE_SET", "FEATURE_VALUE", "PREDICTION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"PREDICTION_BOUNDS", "PREDICTION_COST", "PREDICTION_DETAILS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"PREDICTION_PROBABILITY", "PREDICTION_SET", "APPENDCHILDXML", "DELETEXML", "DEPTH", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"EXISTSNODE", "EXTRACTVALUE", "INSERTCHILDXML", "INSERTCHILDXMLAFTER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"INSERTCHILDXMLBEFORE", "INSERTXMLAFTER", "INSERTXMLBEFORE", "PATH", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"SYS_DBURIGEN", "SYS_XMLAGG", "SYS_XMLGEN", "UPDATEXML", "XMLAGG", "XMLCAST", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"XMLCDATA", "XMLCOLATTVAL", "XMLCOMMENT", "XMLCONCAT", "XMLDIFF", "XMLELEMENT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"XMLEXISTS", "XMLFOREST", "XMLISVALID", "XMLPARSE", "XMLPATCH", "XMLPI", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"XMLQUERY", "XMLROOT", "XMLSEQUENCE", "XMLSERIALIZE", "XMLTABLE", "XMLTRANSFORM", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"JSON_QUERY", "JSON_TABLE", "JSON_VALUE", "DECODE", "DUMP", "ORA_HASH", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"STANDARD_HASH", "VSIZE", "COALESCE", "LNNVL", "NULLIF", "NVL", "NVL2", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"CON_DBID_TO_ID", "CON_GUID_TO_ID", "CON_NAME_TO_ID", "CON_UID_TO_ID", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"ORA_INVOKING_USER", "ORA_INVOKING_USERID", "SYS_CONTEXT", "SYS_GUID", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"SYS_TYPEID", "UID", "USER", "USERENV", "APPROX_COUNT_DISTINCT", "AVG", "CORR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"CORR_S", "CORR_K", "COUNT", "COVAR_POP", "COVAR_SAMP", "CUME_DIST", "DENSE_RANK", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"FIRST", "GROUP_ID", "GROUPING", "GROUPING_ID", "LAST", "LISTAGG", "MAX", "MEDIAN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"MIN", "PERCENT_RANK", "PERCENTILE_CONT", "PERCENTILE_DISC", "RANK", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"STATS_BINOMIAL_TEST", "STATS_CROSSTAB", "STATS_F_TEST", "STATS_KS_TEST", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"STATS_MODE", "STATS_MW_TEST", "STATS_ONE_WAY_ANOVA", "STATS_T_TEST", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"STATS_T_TEST_ONE", "STATS_T_TEST_PAIRED", "STATS_T_TEST_INDEP", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				"STATS_T_TEST_INDEPU", "STATS_WSR_TEST", "STDDEV", "STDDEV_POP", "STDDEV_SAMP", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"SUM", "SYS_OP_ZONE_ID", "VAR_POP", "VAR_SAMP", "VARIANCE", "FIRST_VALUE", "LAG", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"LAST_VALUE", "LEAD", "NTH_VALUE", "NTILE", "RATIO_TO_REPORT", "ROW_NUMBER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"DEREF", "MAKE_REF", "REF", "REFTOHEX", "VALUE", "CV", "ITERATION_NUMBER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"PRESENTNNV", "PRESENTV", "PREVIOUS", "CUBE_TABLE", "DATAOBJ_TO_MAT_PARTITION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"DATAOBJ_TO_PARTITION", "RAISE_APPLICATION_ERROR", "EXCEPTION_INIT")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		tokens.put(DATATYPE, listSupportedDatatypes());

		tokens.put(LANG, Arrays.asList("IF", "ELSE", "THEN", "ELSIF", "RETURN", "WHILE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"EXCEPTION", "WHEN", "OTHERS", "LOOP", "EXECUTE", "IMMEDIATE", "FOR", "RAISE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"MATCHED", "FORALL", "OPEN", "CASE", "CURSOR")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		tokens.put(SPECIAL, Arrays.asList("COMMIT", "ROLLBACK", "PRAGMA", "AUTONOMOUS_TRANSACTION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"SAVEPOINT", "RESTRICT_REFERENCES", "SHOW", "ERRORS")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

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
		return Arrays.asList("VARCHAR2", "NUMBER", "DATE", "TIMESTAMP", "CLOB", "BLOB", "ROWID", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"NVARCHAR2", "CHAR", "VARCHAR", "NCHAR", "PLS_INTEGER", "BINARY_INTEGER", "LONG", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"INTERVAL", "DAY", "MONTH", "RAW", "UROWID", "MLSLABEL", "NCLOB", "BFILE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"XMLTYPE", "CONSTANT", "INTEGER"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	@Override
	public IDatatype getDefaultDatatype() {
		return new Datatype("NUMBER", 10, 0); //$NON-NLS-1$
	}

	@Override
	public List<String> listStringDatatypes() {
		return Arrays.asList("VARCHAR2", "DATE", "TIMESTAMP", "CLOB", "NVARCHAR2", "CHAR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"VARCHAR", "NCHAR", "NCLOB"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		return Arrays.asList("CLOB", "NCLOB", "LONG", "BLOB", "TIMESTAMP", "DATE", "XMLTYPE"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
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
			return "NULL"; //$NON-NLS-1$
		} else if ("DATE".equals(typeName) || typeName.startsWith("TIMESTAMP")) { //$NON-NLS-1$ //$NON-NLS-2$
			final Date d = ConversionHelper.getDate(value);
			if (d == null) {
				return "NULL"; //$NON-NLS-1$
			} else {
				final String sqlDateStr = SQL_DATE_FORMAT.format(d);
				return "TO_DATE('" + sqlDateStr + "','" + SQL_DATE_PATTERN + "')"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		} else if (listStringDatatypes().contains(typeName)) {
			String encapsulator = "'"; //$NON-NLS-1$
			String strVal = value.toString();
			return encapsulator + strVal.replace(encapsulator, encapsulator + encapsulator)
					+ encapsulator;
		} else {
			return value.toString();
		}
	}

	@Override
	public List<String> getNumericDatatypes() {
		return Arrays.asList("NUMBER", "PLS_INTEGER", "BINARY_INTEGER", "LONG"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

	@Override
	public boolean isDecimalDatatype(IDatatype type) {
		return (type != null && type.getPrecision() > 0);
	}

	@Override
	public List<String> getDateDatatypes() {
		return Arrays.asList("DATE", "TIMESTAMP"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public boolean isTypedLengthSupportedFor(String datatype) {
		return true;
	}

}
