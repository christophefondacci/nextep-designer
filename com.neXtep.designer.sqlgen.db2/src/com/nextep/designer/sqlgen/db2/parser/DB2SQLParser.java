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
package com.nextep.designer.sqlgen.db2.parser;

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

/**
 * Parser definitions for DB2 SQL.
 * 
 * @author Bruno Gautier
 */
public class DB2SQLParser implements ISQLParser, IDatatypeProvider {

	private final static DateFormat SQL_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	public static final String DATATYPE_CHAR = "CHAR"; //$NON-NLS-1$
	public static final String DATATYPE_XML = "XML"; //$NON-NLS-1$
	public static final String DATATYPE_TIME = "TIME"; //$NON-NLS-1$
	public static final String DATATYPE_TIMESTAMP = "TIMESTAMP"; //$NON-NLS-1$
	public static final String DATATYPE_DATE = "DATE"; //$NON-NLS-1$
	public static final String DATATYPE_DECFLOAT = "DECFLOAT"; //$NON-NLS-1$
	public static final String DATATYPE_BLOB = "BLOB"; //$NON-NLS-1$
	public static final String DATATYPE_GRAPHIC = "GRAPHIC"; //$NON-NLS-1$
	public static final String DATATYPE_REAL = "REAL"; //$NON-NLS-1$
	public static final String DATATYPE_DOUBLE = "DOUBLE"; //$NON-NLS-1$
	public static final String DATATYPE_FLOAT = "FLOAT"; //$NON-NLS-1$
	public static final String DATATYPE_VARCHAR = "VARCHAR"; //$NON-NLS-1$
	public static final String DATATYPE_CLOB = "CLOB"; //$NON-NLS-1$
	public static final String DATATYPE_VARGRAPHIC = "VARGRAPHIC"; //$NON-NLS-1$
	public static final String DATATYPE_DBCLOB = "DBCLOB"; //$NON-NLS-1$
	public static final String DATATYPE_SMALLINT = "SMALLINT"; //$NON-NLS-1$
	public static final String DATATYPE_INTEGER = "INTEGER"; //$NON-NLS-1$
	public static final String DATATYPE_BIGINT = "BIGINT"; //$NON-NLS-1$
	public static final String DATATYPE_DECIMAL = "DECIMAL"; //$NON-NLS-1$
	public static final String DATATYPE_NUMERIC = "NUMERIC"; //$NON-NLS-1$

	private final static List<String> decimalDatatypes = Arrays.asList(DATATYPE_DECFLOAT,
			DATATYPE_DECIMAL, DATATYPE_DOUBLE, DATATYPE_FLOAT);
	private static final Map<String, BigDecimal> DATATYPE_MAXSIZE_MAP = initDatatypeMaxSizeMap();

	private static final Map<String, BigDecimal> initDatatypeMaxSizeMap() {
		Map<String, BigDecimal> m = new HashMap<String, BigDecimal>();

		// TODO [BGA]: Add the maximum sizes of the other supported data types
		m.put(DATATYPE_SMALLINT, new BigDecimal("32767")); //$NON-NLS-1$
		m.put(DATATYPE_INTEGER, new BigDecimal("2147483647")); //$NON-NLS-1$
		m.put(DATATYPE_BIGINT, new BigDecimal("9223372036854775807")); //$NON-NLS-1$

		return Collections.unmodifiableMap(m);
	}

	@Override
	public Map<String, List<String>> getTypedTokens() {
		Map<String, List<String>> tokens = new HashMap<String, List<String>>();

		tokens.put(DDL, Arrays.asList("ACTION", "ALIAS", "ALTER", "AUDIT", "BUFFERPOOL", "CLASS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"COMMENT", "COMPONENT", "CONTEXT", "CREATE", "DATABASE", "DROP", "EXTENSION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"FUNCTION", "GRANT", "HISTOGRAM", "INDEX", "INTEGRITY", "LABEL", "MAPPING", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"METHOD", "NICKNAME", "OWNERSHIP", "PARTITION", "POLICY", "PROCEDURE", "REFRESH", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"RENAME", "REVOKE", "ROLE", "SCHEMA", "SECURITY", "SEQUENCE", "SERVER", "SERVICE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"TABLE", "TABLESPACE", "TEMPLATE", "THRESHOLD", "TRANSFER", "TRANSFORM", "TRIGGER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"TRUSTED", "TYPE", "USER", "VARIABLE", "VIEW", "WORK", "WORKLOAD", "WRAPPER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"XSROBJECT", "DEFAULT", "CATEGORIES", "STATUS", "BOTH", "FAILURE", "NONE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"SUCCESS", "CHECKING", "WITHOUT", "OBJMAINT", "SECMAINT", "SYSADMIN", "VALIDATE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"ERROR", "NORMAL", "DEFERRED", "AUTOMATIC", "NUMBLOCKPAGES", "BLOCKSIZE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"DBPARTITIONNUMS", "TABLESPACES", "STORAGE", "NAME", "EXTERNAL", "FENCED", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"THREADSAFE", "HIGH", "BIN", "OPTIONS", "ADD", "COLUMN", "COLUMNS", "LOCAL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"FOREIGN", "KEY", "PRIMARY", "UNIQUE", "CONSTRAINT", "CHECK", "REFERENCES", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"ALLOW", "DISALLOW", "CACHING", "DETERMINED", "ENABLE", "DISABLE", "QUERY", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"OPTIMIZATION", "RESTART", "INCREMENT", "MINVALUE", "MAXVALUE", "NO", "CYCLE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"CACHE", "RESTRICT", "MATERIALIZED", "CASCADE", "DISTRIBUTION", "CAPTURE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"CHANGES", "INCLUDE", "LONGVAR", "ACTIVATE", "DEACTIVATE", "INITIALLY", "LOGGED", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"EMPTY", "PCTFREE", "LOCKSIZE", "BLOCKINSERT", "APPEND", "OFF", "VOLATILE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"COMPRESS", "COMPRESSION", "LOG", "BUILD", "ATTACH", "DETACH", "STARTING", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"INCLUSIVE", "EXCLUSIVE", "AT", "SYSTEM", "ENDING", "SECURED", "HIDDEN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"IMPLICITLY", "COMPACT", "GENERATED", "ALWAYS", "DISTRIBUTE", "MAINTAINED",
				"FEDERATED_TOOL", "EXPRESSION", "INLINE", "IDENTITY", "ATTRIBUTE", "SPECIFIC",
				"DETERMINISTIC", "UNICODE", "READS", "CONTAINS", "MODIFIES", "SQL", "LANGUAGE",
				"STATIC", "DISPATCH", "CALLED", "INPUT", "INHERIT", "SPECIAL", "REGISTERS",
				"PREDICATES", "ISOLATION", "LEVEL", "REQUEST"));

		tokens.put(DML, Arrays.asList("DELETE", "INSERT", "INTO", "MERGE", "SELECT", "SET", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"VALUES", "DESCRIBE", "CASE", "FROM", "WHERE", "HAVING", "GROUP", "BY", "ORDER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"UPDATE", "BETWEEN", "DISTINCT", "LIKE", "AND", "OR", "ASC", "DESC", "UNION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"ALL", "AS", "FULL", "CROSS", "OUTER", "INNER", "JOIN", "ON", "WHEN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"EXISTS", "IN", "DYNAMIC", "VALIDATED", "NOT", "SELECTIVITY", "SOME", "ANY", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"ESCAPE", "IS", "OF", "ONLY", "TO", "ACCORDING", "XMLSCHEMA", "ID", "URI", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"NAMESPACE", "XMLEXISTS", "REF", "PASSING", "YEARS", "MONTHS", "HOURS", "MINUTES", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"SECONDS", "MICROSECONDS", "CAST", "SCOPE", "PRECISION", "VARYING", "BIT", "SBCS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"MIXED", "DATA", "1M", "K", "M", "G", "LARGE", "OBJECT", "BINARY", "SYSPROC", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
				"DB2SECURITYLABEL", "XMLCAST", "OVER", "RANK", "DENSE_RANK", "LAG", "LEAD", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"RESPECT", "IGNORE", "NULLS", "LAST", "FIRST", "ROW_NUMBER", "RANGE", "UNBOUNDED", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"PRECEDING", "FOLLOWING", "ROW", "ROWS", "FIRST_VALUE", "LAST_VALUE", "TREAT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"NEXT", "PREVIOUS", "CHANGE", "TOKEN", "WITH", "TABLESAMPLE", "BERNOULLI", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"REPEATABLE", "LATERAL", "WITHIN", "FINAL", "UNNEST", "ORDINALITY", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"SETS", "ROLLUP", "CUBE", "EXCEPT", "INTERSECT", "READ", "OPTIMIZE", "RR", "RS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"CS", "UR", "USE", "KEEP", "SHARE", "LOCKS")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$

		tokens.put(FUNC, Arrays.asList("CODEUNITS16", "CODEUNITS32", "OCTETS", "ABS", "ABSVAL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"ACOS", "ARRAY_AGG", "ASCII", "ASIN", "ATAN", "ATAN2", "ATANH", "AVG", "BITAND", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"BITANDNOT", "BITNOT", "BITOR", "BITXOR", "CARDINALITY", "CEILING", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"CHARACTER_LENGTH", "CHR", "COALESCE", "COLLATION_KEY_BIT", "COMPARE_DECFLOAT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"CONCAT", "CORRELATION", "COS", "COSH", "COT", "COUNT", "COUNT_BIG", "COVARIANCE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"DATAPARTITIONNUM", "DAY", "DAYNAME", "DAYOFWEEK", "DAYOFWEEK_ISO", "DAYOFYEAR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"DAYS", "DBPARTITIONNUM", "DECODE", "DECRYPT_BIN", "DECRYPT_CHAR", "DEGREES", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"DEREF", "DIFFERENCE", "DIGITS", "EMPTY_BLOB", "EMPTY_CLOB", "EMPTY_DBCLOB", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"ENCRYPT", "EVENT_MON_STATE", "EXP", "FLOOR", "GENERATE_UNIQUE", "GETHINT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"GREATEST", "GROUPING", "HASHEDVALUE", "HEX", "HOUR", "IDENTITY_VAL_LOCAL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"JULIAN_DAY", "LCASE", "LEAST", "LEFT", "LENGTH", "LN", "LOCATE", "LOG10", "LOWER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"LTRIM", "MAX", "MAX_CARDINALITY", "MICROSECOND", "MIDNIGHT_SECONDS", "MIN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"MINUTE", "MOD", "MONTH", "MONTHNAME", "MULTIPLY_ALT", "NORMALIZE_DECFLOAT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"NULLIF", "NVL", "OCTET_LENGTH", "OVERLAY", "PARAMETER", "POSITION", "POSSTR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"POWER", "QUANTIZE", "QUARTER", "RADIANS", "RAISE_ERROR", "RAND", "REAL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"REC2XML", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_ICPT", "REGR_INTERCEPT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "REPEAT", "REPLACE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"RID", "RID_BIT", "RIGHT", "ROUND", "RTRIM", "SECLABEL", "SECLABEL_BY_NAME", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"SECLABEL_TO_CHAR", "SECOND", "SIGN", "SIN", "SINH", "SOUNDEX", "SPACE", "SQRT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"STDDEV", "STRIP", "SUBSTR", "SUBSTRING", "SUM", "TABLE_NAME", "TABLE_SCHEMA", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"TAN", "TANH", "TIMESTAMP_FORMAT", "TIMESTAMP_ISO", "TIMESTAMPDIFF", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"TO_CHAR", "TO_DATE", "TOTALORDER", "TRANSLATE", "TRIM", "TRUNCATE", "TYPE_ID", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"TYPE_NAME", "TYPE_SCHEMA", "UCASE", "UPPER", "VALUE", "VARCHAR_FORMAT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"VARIANCE", "WEEK", "WEEK_ISO", "XMLAGG", "XMLATTRIBUTES", "XMLCOMMENT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"XMLCONCAT", "XMLDOCUMENT", "XMLELEMENT", "XMLFOREST", "XMLGROUP", "XMLNAMESPACES", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"XMLPARSE", "XMLPI", "XMLQUERY", "XMLROW", "XMLSERIALIZE", "XMLTABLE", "XMLTEXT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"XMLVALIDATE", "XMLXSROBJECTID", "XSLTRANSFORM", "YEAR")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

		final List<String> datatypes = new ArrayList<String>(listSupportedDatatypes());
		datatypes.add("LONG"); //$NON-NLS-1$
		tokens.put(DATATYPE, datatypes);

		tokens.put(LANG, Arrays.asList("FOR", "ALLOCATE", "ASSOCIATE", "CLOSE", "CURSOR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"DECLARE", "FETCH", "FREE", "LOCATOR", "LOCATORS", "LOCK", "OPEN", "EXECUTE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"IMMEDIATE", "PREPARE", "BEGIN", "END", "GET", "RESIGNAL", "SECTION", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"DIAGNOSTICS", "SIGNAL", "WHENEVER", "CALL", "GOTO", "IF", "ITERATE", "LEAVE",
				"LOOP", "REPEAT", "RETURN", "WHILE", "THEN", "ELSE", "UNTIL", "RESULT", "EACH"));

		tokens.put(SPECIAL, Arrays.asList("PACKAGE", "EVENT", "FLUSH", "MONITOR", "COMMIT",
				"ROLLBACK", "SAVEPOINT", "CONNECT", "DISCONNECT", "CONNECTION", "RELEASE",
				"OUTPUT", "AGE", "ASYNCHRONY", "AUTHORIZATION", "COMPILATION", "CURRENT", "DEGREE",
				"ENCRYPTION", "ENVIRONMENT", "EXPLAIN", "FEDERATED", "GLOBAL", "IMPLICIT", "MDC",
				"MODE", "OPTION", "PACKAGESET", "PASSTHRU", "PASSWORD", "PATH", "PROFILE",
				"ROLLOUT", "ROUNDING", "SESSION", "SNAPSHOT", "STATE", "TEMPORARY", "TIMEOUT",
				"TYPES", "XMLPARSE", "NULL"));

		return tokens;
	}

	@Override
	public String getVarSeparator() {
		return ":"; //$NON-NLS-1$
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

		// TODO [BGA]: Check for prototypes to declare
		// prototypes.add(new
		// Prototype("procedure","Creates an empty procedure body declaration",
		// "procedure  ( ) is\nbegin\n\nend;\n",10));
		// prototypes.add(new
		// Prototype("function","Creates an empty function body declaration",
		// "function  ( ) return  is\nbegin\n\nend;\n",9));

		return prototypes;
	}

	@Override
	public String getPromptCommand() {
		return "ECHO"; //$NON-NLS-1$
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
		// FIXME [BGA]: This is not possible in DB2 to call an external script
		// interactively, should
		// this method return null or an empty String?
		return null;
	}

	/*
	 * LONG VARCHAR and LONG VARGRAPHIC data types are still supported by DB2
	 * but are deprecated (they have been respectively replaced by CLOB and
	 * DBCLOB). These types have not been listed here since the parser does not
	 * support spaces in keywords.
	 */
	@Override
	public List<String> listSupportedDatatypes() {
		return Arrays.asList(DATATYPE_XML, DATATYPE_TIME, DATATYPE_TIMESTAMP, DATATYPE_DATE,
				DATATYPE_DECFLOAT, DATATYPE_BLOB, DATATYPE_CHAR, DATATYPE_GRAPHIC, DATATYPE_REAL,
				DATATYPE_DOUBLE, DATATYPE_FLOAT, DATATYPE_VARCHAR, DATATYPE_CLOB,
				DATATYPE_VARGRAPHIC, DATATYPE_DBCLOB, DATATYPE_SMALLINT, DATATYPE_INTEGER,
				DATATYPE_BIGINT, DATATYPE_DECIMAL, DATATYPE_NUMERIC);
	}

	@Override
	public IDatatype getDefaultDatatype() {
		return new Datatype(DATATYPE_NUMERIC);
	}

	@Override
	public List<String> listStringDatatypes() {
		return Arrays.asList(DATATYPE_CHAR, DATATYPE_GRAPHIC, DATATYPE_VARCHAR, DATATYPE_CLOB,
				DATATYPE_VARGRAPHIC, DATATYPE_DBCLOB);
	}

	@Override
	public Map<String, String> getEquivalentDatatypesMap() {
		// Map<String, String> typesMap = new HashMap<String, String>();
		//
		// typesMap.put("INTEGER", "INT");
		// typesMap.put("DECIMAL", "DEC");
		// typesMap.put("NUMERIC", "NUM");
		//
		// return typesMap;
		return Collections.EMPTY_MAP;
	}

	@Override
	public Collection<IAutoEditStrategy> getAutoEditStrategies() {
		// TODO [BGA]: Check which strategy is needed
		return Collections.emptyList();
	}

	@Override
	public String getExitCommand() {
		return "QUIT"; //$NON-NLS-1$
	}

	@Override
	public String getCommentStartSequence() {
		return "--"; //$NON-NLS-1$
	}

	@Override
	public String getShowErrorsCommand() {
		// TODO [BGA]: Check show errors command
		return "";
	}

	@Override
	public List<String> getUnsizableDatatypes() {
		return Arrays.asList(DATATYPE_SMALLINT, DATATYPE_INTEGER, DATATYPE_BIGINT, DATATYPE_REAL,
				DATATYPE_DOUBLE, DATATYPE_DATE, DATATYPE_TIME, DATATYPE_TIMESTAMP, DATATYPE_XML);
	}

	@Override
	public BigDecimal getDatatypeMaxSize(String type) throws UnsupportedDatatypeException {
		if (null == type)
			throw new UnsupportedDatatypeException("The specified data type must be not null");

		if (DATATYPE_MAXSIZE_MAP.containsKey(type)) {
			return DATATYPE_MAXSIZE_MAP.get(type);
		}

		throw new UnsupportedDatatypeException("The maximum size of data type [" + type
				+ "] is unknown for database vendor " + DBVendor.DB2.toString());
	}

	@Override
	public String formatSqlScriptValue(IDatatype type, Object value) {
		if (value == null) {
			return "null";
		}

		final String typeName = type.getName().toUpperCase();
		if (DATATYPE_DATE.equals(typeName) || DATATYPE_TIMESTAMP.equals(typeName)) {
			final Date d = ConversionHelper.getDate(value);
			if (d == null) {
				return formatSqlScriptValue(type, d);
			} else {
				return enclose(SQL_DATE_FORMAT.format(d));
			}
		} else if (listStringDatatypes().contains(typeName) || DATATYPE_TIME.equals(typeName)) {
			return enclose(value.toString());
		} else {
			return value.toString();
		}
	}

	private String enclose(String s) {
		String delimiter = String.valueOf(getStringDelimiter());
		return delimiter + s.replace(delimiter, delimiter + delimiter) + delimiter;
	}

	@Override
	public List<String> getNumericDatatypes() {
		return Arrays.asList(DATATYPE_BIGINT, DATATYPE_DECFLOAT, DATATYPE_DECIMAL, DATATYPE_DOUBLE,
				DATATYPE_FLOAT, DATATYPE_INTEGER, DATATYPE_NUMERIC, DATATYPE_REAL,
				DATATYPE_SMALLINT);
	}

	@Override
	public boolean isDecimalDatatype(IDatatype type) {
		return type != null && decimalDatatypes.contains(type);
	}

	@Override
	public List<String> getDateDatatypes() {
		return Arrays.asList(DATATYPE_DATE, DATATYPE_TIMESTAMP);
	}

	@Override
	public boolean isTypedLengthSupportedFor(String datatype) {
		return false;
	}
}
