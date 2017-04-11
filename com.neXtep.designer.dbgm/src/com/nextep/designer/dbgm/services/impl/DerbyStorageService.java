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
package com.nextep.designer.dbgm.services.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.dbgm.DBGMMessages;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.services.IConnectionService;
import com.nextep.designer.dbgm.helpers.ConversionHelper;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.model.impl.StorageHandle;
import com.nextep.designer.dbgm.services.IStorageService;

/**
 * @author Christophe Fondacci
 */
public class DerbyStorageService implements IStorageService {

	private static final Log LOGGER = LogFactory.getLog(DerbyStorageService.class);

	private static final String MIRROR_COLUMN_PREFIX = "meta_"; //$NON-NLS-1$
	private static final int MAX_DERBY_VARCHAR_LENGTH = 32672;

	private IConnectionService connectionService;
	private IConnection localConnection;
	private List<String> reservedWords;

	public DerbyStorageService() {
		// Reserved derby words used for escaping
		reservedWords = Arrays.asList(
				"ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"BIT", "BOOLEAN", "BOTH", "BY", "CALL", "CASCADE", "CASCADED", "CASE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"CAST", "CHAR", "CHARACTER", "CHECK", "CLOSE", "COALESCE", "COLLATE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", "CREATE", "CURRENT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				"CURSOR", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DIAGNOSTICS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "ELSE", "END", "END-EXEC", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXPLAIN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"EXTERNAL", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"FROM", "FULL", "FUNCTION", "GET", "GETCURRENTCONNECTION", "GLOBAL", "GO", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"INDICATOR", "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"INSERT", "INT", "INTEGER", "INTERSECT", "INTO", "IS", "ISOLATION", "JOIN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"KEY", "LAST", "LEFT", "LIKE", "LOWER", "LTRIM", "MATCH", "MAX", "MIN", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"MINUTE", "NATIONAL", "NATURAL", "NCHAR", "NVARCHAR", "NEXT", "NO", "NOT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"NULL", "NULLIF", "NUMERIC", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
				"ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "PREPARE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"ROLLBACK", "ROWS", "RTRIM", "SCHEMA", "SCROLL", "SECOND", "SELECT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"SESSION_USER", "SET", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"SQLERROR", "SQLSTATE", "SUBSTR", "SUBSTRING", "SUM", "SYSTEM_USER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"TABLE", "TEMPORARY", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				"TRANSACTION", "TRANSLATE", "TRANSLATION", "TRUE", "UNION", "UNIQUE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				"UNKNOWN", "UPDATE", "UPPER", "USER", "USING", "VALUES", "VARCHAR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
				"VARYING", "VIEW", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE", "XML", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
				"XMLEXISTS", "XMLPARSE", "XMLQUERY", "XMLSERIALIZE", "YEAR"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	public void startup() {
		final String location = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		final String derbyLocation = location + File.separator + "neXtepLocalDB"; //$NON-NLS-1$

		// First cleaning up temp directory
		final File f = new File(derbyLocation);
		deleteDirectory(f);

		// Now starting derby
		System.setProperty("derby.system.home", location); //$NON-NLS-1$
		System.setProperty("derby.database.sqlAuthorization", "true"); //$NON-NLS-1$ //$NON-NLS-2$

		localConnection = CorePlugin.getTypedObjectFactory().create(IConnection.class);
		localConnection.setDatabase(derbyLocation);
		localConnection.setDBVendor(DBVendor.DERBY);
	}

	@Override
	public Connection getLocalConnection() throws SQLException {
		return connectionService.connect(localConnection);
	}

	@Override
	public IStorageHandle createDataSetStorage(final IDataSet set, final boolean mirrored) {
		String name = set.getTable().getName();
		if (set.getReference() == null || set.getReference().isVolatile()) {
			// Building storage name
			final long time = System.currentTimeMillis();
			final int seed = (int) (Math.random() * 10);
			name = "NEXTEP_" + time + "_" + seed; //$NON-NLS-1$ //$NON-NLS-2$
		}

		// Creating table
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getLocalConnection();
			stmt = conn.createStatement();
			// First dropping any previous table storage
			final String dropTableStmt = "DROP TABLE " + escape(name); //$NON-NLS-1$
			try {
				stmt.execute(dropTableStmt);
			} catch (SQLException e) {
				// Ignoring, it will happen most of the times
			}
			// Creating local derby table
			final String createTableStmt = buildCreateDatasetTableStatement(name, set, mirrored);
			stmt.execute(createTableStmt);
			// Indexing local table
			final String indexTableStmt = buildCreateIndexStatement(name, set);
			stmt.execute(indexTableStmt);
			// Initializing storage handle
			final StorageHandle handle = new StorageHandle(name, set);
			final String insertStmt = buildInsertDatalineStatement(set, handle, mirrored);
			final String selectStmt = buildSelectDatalineStatement(set, handle, mirrored);
			// Configuring insert and select statements
			handle.setInsertStatement(insertStmt);
			handle.setSelectStatement(selectStmt);
			handle.setDisplayedColumnsCount(set.getColumnsRef().size());
			set.setStorageHandle(handle);
			return handle;
		} catch (SQLException e) {
			throw new ErrorException(
					DBGMMessages.getString("service.derbyStorage.createLocalDbFailed") + e.getMessage(), e); //$NON-NLS-1$
		} finally {
			safeClose(null, stmt, conn);
		}
	}

	private String buildCreateIndexStatement(String name, IDataSet set) {
		final IBasicTable table = set.getTable();
		StringBuilder buf = new StringBuilder(100);
		final String indexName = getIndexName(name);
		buf.append("CREATE INDEX ").append(escape(indexName)).append(" ON ").append(escape(name)) //$NON-NLS-1$ //$NON-NLS-2$
				.append(" ("); //$NON-NLS-1$

		List<IBasicColumn> dsetCols = set.getColumns();
		List<IBasicColumn> indexedCols = table.getColumns();
		IKeyConstraint pk = DBGMHelper.getPrimaryKey(table);
		if (pk != null) {
			indexedCols = pk.getColumns();
		}

		/*
		 * TODO [BGA] The maximum number of columns for an index key in Derby cannot exceed 16, so
		 * we must ensure that we remain within this limit. Maybe we should raise a warning to the
		 * user when this limit is reached.
		 */
		int indexColCount = 0;
		String separator = ""; //$NON-NLS-1$
		for (IBasicColumn c : indexedCols) {
			// We check that the column is part of the data set columns
			if (indexColCount < 16 && dsetCols.contains(c)) {
				buf.append(separator).append(escape(c.getName()));
				separator = ","; //$NON-NLS-1$
				indexColCount++;
			}
		}
		buf.append(")"); //$NON-NLS-1$
		return buf.toString();
	}

	private String getIndexName(String storageName) {
		return storageName + System.currentTimeMillis() + "_idx"; //$NON-NLS-1$
	}

	private String buildInsertDatalineStatement(IDataSet set, IStorageHandle handle,
			boolean isMirrored) {
		StringBuilder buf = new StringBuilder(1000);
		StringBuilder mirrorBuf = new StringBuilder(500);
		StringBuilder valuesBuf = new StringBuilder(100);
		StringBuilder mirrorValuesBuf = new StringBuilder(100);
		buf.append("insert into " + escape(handle.getStorageUnitName()) + " ("); // values ("); //$NON-NLS-1$ //$NON-NLS-2$
		String separator = ""; //$NON-NLS-1$
		// Repository handles store the rowid
		// if (handle.isRepositoryHandle()) {
		buf.append(ROWID_COLUMN_NAME);
		valuesBuf.append("?"); //$NON-NLS-1$
		if (isMirrored) {
			mirrorBuf.append("," + MIRROR_COLUMN_PREFIX + ROWID_COLUMN_NAME); //$NON-NLS-1$
			mirrorValuesBuf.append(",?"); //$NON-NLS-1$
		}
		separator = ","; //$NON-NLS-1$
		// }
		for (IBasicColumn c : set.getColumns()) {
			buf.append(separator + escape(c.getName())); //$NON-NLS-1$ //$NON-NLS-2$
			valuesBuf.append(separator + "?"); //$NON-NLS-1$
			// Duplicate definition for mirrored tables
			if (isMirrored) {
				mirrorBuf.append("," + escape(MIRROR_COLUMN_PREFIX + c.getName())); //$NON-NLS-1$ //$NON-NLS-2$
				mirrorValuesBuf.append(",?"); //$NON-NLS-1$
			}
			separator = ","; //$NON-NLS-1$
		}
		// Appending mirrored column definition in INSERT statement
		if (isMirrored) {
			buf.append(mirrorBuf);
		}
		buf.append(") values ("); //$NON-NLS-1$
		buf.append(valuesBuf);
		// Appending mirrored values section in INSERT statement
		if (isMirrored) {
			buf.append(mirrorValuesBuf);
		}
		buf.append(')');
		return buf.toString();
	}

	private String buildSelectDatalineStatement(IDataSet set, IStorageHandle handle,
			boolean isMirrored) {
		StringBuilder buf = new StringBuilder(1000);
		StringBuilder mirrorBuf = new StringBuilder(100);
		buf.append("SELECT "); // values ("); //$NON-NLS-1$
		String separator = ""; //$NON-NLS-1$
		for (IBasicColumn c : set.getColumns()) {
			buf.append(separator + escape(c.getName()));
			if (isMirrored) {
				mirrorBuf.append("," + escape(MIRROR_COLUMN_PREFIX + c.getName())); //$NON-NLS-1$ //$NON-NLS-2$
			}
			separator = ","; //$NON-NLS-1$
		}
		// Appending mirror selection
		if (isMirrored) {
			buf.append(mirrorBuf.toString());
		}
		buf.append(" FROM " + escape(handle.getStorageUnitName())); //$NON-NLS-1$
		return buf.toString();
	}

	@Override
	public String getSelectStatement(IDataSet set) {
		final IBasicTable table = set.getTable();
		if (table != null) {
			// Instantiating a fake handle so that we could use internal methods
			final IStorageHandle fakeHandle = new StorageHandle(table.getName(), set);
			return buildSelectDatalineStatement(set, fakeHandle, false);
		}
		return ""; //$NON-NLS-1$
	}

	private String buildCreateDatasetTableStatement(String name, IDataSet set,
			boolean createMirrorTable) {
		StringBuilder buf = new StringBuilder(100);
		StringBuilder mirrorBuf = new StringBuilder(100);

		buf.append("CREATE TABLE " + escape(name) + " "); //$NON-NLS-1$ //$NON-NLS-2$
		String separator = "("; //$NON-NLS-1$
		// Creating the rowid column for repository handles
		// if (set.getUID() != null) {
		buf.append("(" + ROWID_COLUMN_NAME + " bigint"); //$NON-NLS-1$ //$NON-NLS-2$
		mirrorBuf.append(", " + MIRROR_COLUMN_PREFIX + ROWID_COLUMN_NAME + " bigint"); //$NON-NLS-1$ //$NON-NLS-2$
		separator = ","; //$NON-NLS-1$
		// }
		for (IBasicColumn c : set.getColumns()) {
			IDatatype columnType = getColumnDatatype(set, c);
			buf.append(separator + escape(c.getName()) + " "); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append(columnType.toString());
			mirrorBuf.append("," + escape(MIRROR_COLUMN_PREFIX + c.getName()) + " "); //$NON-NLS-1$ //$NON-NLS-2$
			mirrorBuf.append(columnType.toString());
			separator = ", "; //$NON-NLS-1$
		}

		// If a mirror table is requested, we create our meta columns
		if (createMirrorTable) {
			buf.append(mirrorBuf);
		}
		buf.append(')');
		return buf.toString();
	}

	@Override
	public String escape(String name) {
		if (reservedWords.contains(name.toUpperCase())) {
			return "\"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return name;
		}
	}

	public IDatatype getColumnDatatype(IDataSet set, IBasicColumn c) {
		final int jdbcType = getColumnSqlType(set, c);
		final IDatatype type = c.getDatatype();
		IDatatype derbyType = null;
		switch (jdbcType) {
		case Types.DOUBLE:
			derbyType = new Datatype("DOUBLE"); //$NON-NLS-1$
			break;
		case Types.BIGINT:
			derbyType = new Datatype("BIGINT"); //$NON-NLS-1$
			break;
		case Types.TIMESTAMP:
			derbyType = new Datatype("TIMESTAMP"); //$NON-NLS-1$
			break;
		default:
			derbyType = new Datatype("VARCHAR"); //$NON-NLS-1$
			if (type.getLength() > 0) {
				if (type.getLength() > MAX_DERBY_VARCHAR_LENGTH) {
					LOGGER.warn("Dataset datatype for VARCHAR column " + c.getName()
							+ " has been truncated to " + MAX_DERBY_VARCHAR_LENGTH);
				}
				derbyType.setLength(Math.min(type.getLength(), MAX_DERBY_VARCHAR_LENGTH));
			} else {
				derbyType.setLength(4000);
			}
			break;
		}

		return derbyType;
	}

	@Override
	public int getColumnSqlType(IDataSet set, IBasicColumn c) {
		final DBVendor vendor = DBGMHelper.getVendorFor(set);
		final IDatatypeProvider datatypeProvider = DBGMHelper.getDatatypeProvider(vendor);
		final IDatatype type = c.getDatatype();
		int jdbcType;
		// Analyzing type
		final String typeName = type.getName().toUpperCase();
		if (datatypeProvider.getNumericDatatypes().contains(typeName)) {
			if (datatypeProvider.isDecimalDatatype(type)) {
				jdbcType = Types.DOUBLE;
			} else {
				jdbcType = Types.BIGINT;
			}
		} else if (datatypeProvider.getDateDatatypes().contains(typeName)) {
			jdbcType = Types.TIMESTAMP;
		} else {
			jdbcType = Types.VARCHAR;
		}
		return jdbcType;
	}

	@Override
	public Object decodeValue(IReference colRef, String strValue) {
		final DBVendor vendor = DBGMHelper.getCurrentVendor();
		final IDatatypeProvider datatypeProvider = DBGMHelper.getDatatypeProvider(vendor);
		final IBasicColumn column = (IBasicColumn) VersionHelper.getReferencedItem(colRef);
		final IDatatype type = column.getDatatype();
		final String typeName = type.getName();
		Object decodedValue;
		if (datatypeProvider.getNumericDatatypes().contains(typeName)) {
			decodedValue = ConversionHelper.getNumber(strValue);
		} else if (datatypeProvider.getDateDatatypes().contains(typeName)) {
			final Date date = ConversionHelper.getDate(strValue);
			if (date != null) {
				final Timestamp sqlTimestamp = new Timestamp(date.getTime());
				decodedValue = sqlTimestamp;
			} else {
				decodedValue = null;
			}
		} else {
			decodedValue = strValue;
		}
		return decodedValue;
	}

	private void safeClose(ResultSet rset, Statement stmt, Connection conn) {
		if (rset != null) {
			try {
				rset.close();
			} catch (SQLException e) {
				LOGGER.error(DBGMMessages.getString("service.derbyStorage.closeResultSetFailed"), e); //$NON-NLS-1$
			}
		}
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				LOGGER.error(DBGMMessages.getString("service.derbyStorage.closeStatementFailed"), e); //$NON-NLS-1$
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				LOGGER.error(
						DBGMMessages.getString("service.derbyStorage.closeConnectionFailed"), e); //$NON-NLS-1$
			}
		}
	}

	public void shutdown() {
		try {
			LOGGER.info(DBGMMessages.getString("service.derbyStorage.derbyShutdown")); //$NON-NLS-1$
			DriverManager.getConnection("jdbc:derby:;shutdown=true"); //$NON-NLS-1$
		} catch (SQLException e) {
			// Weird behaviour of Derby which throws an exception to say it is properly shut down
			LOGGER.info(e.getMessage());
		}
		// Now deleting our Derby directory
		final File f = new File(localConnection.getDatabase());
		deleteDirectory(f);
	}

	private boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	@Override
	public void renameStorageHandle(IDataSet set, String newName) {
		IStorageHandle handle = set.getStorageHandle();
		if (handle == null) {
			handle = createDataSetStorage(set);
		}
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getLocalConnection();
			stmt = conn.createStatement();
			try {
				stmt.execute("DROP TABLE " + newName); //$NON-NLS-1$
			} catch (SQLException e) {
				// May generally fail when no previously existing table, it's normal behaviour
			}
			stmt.execute("RENAME TABLE " + handle.getStorageUnitName() + " TO " + newName); //$NON-NLS-1$ //$NON-NLS-2$
			final StorageHandle newHandle = new StorageHandle(newName, set);
			newHandle.setInsertStatement(buildInsertDatalineStatement(set, newHandle, false));
			newHandle.setSelectStatement(buildSelectDatalineStatement(set, newHandle, false));
			set.setStorageHandle(newHandle);
		} catch (SQLException e) {
			throw new ErrorException(
					DBGMMessages.getString("service.derbyStorage.renameDatasetFailed") + e.getMessage(), e); //$NON-NLS-1$
		}
	}

	@Override
	public IStorageHandle createDataSetStorage(IDataSet set) {
		return createDataSetStorage(set, false);
	}

	public void setConnectionService(IConnectionService connectionService) {
		this.connectionService = connectionService;
	}

}
