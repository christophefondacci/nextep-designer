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
package com.nextep.datadesigner.vcs.services;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import oracle.sql.BLOB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.vcs.impl.RepositoryFile;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.services.IConnectionService;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.IRepositoryFile;

/**
 * This service provides helper methods to manipulate repository files.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class VCSFiles {

	private static final Log LOGGER = LogFactory.getLog(VCSFiles.class);
	private static final IRepositoryService repoService = CorePlugin.getRepositoryService();
	private static final IConnectionService connService = CorePlugin.getConnectionService();
	private static VCSFiles instance;

	private VCSFiles() {
	}

	public static VCSFiles getInstance() {
		if (instance == null) {
			instance = new VCSFiles();
		}
		return instance;
	}

	/**
	 * Create a repository file from the given local file. The specified file must exist.
	 * 
	 * @param filePath path to the local file (must exist, else exception)
	 * @return the generated repository file
	 */
	public IRepositoryFile createFromLocalFile(String filePath) {
		final File file = new File(filePath);
		if (!file.exists()) {
			throw new ErrorException(VCSMessages.getString("fileMustExist")); //$NON-NLS-1$
		}
		IRepositoryFile repFile = new RepositoryFile();
		repFile.setName(file.getName());
		// Saving properties
		CorePlugin.getIdentifiableDao().save(repFile);

		final IConnection repoConn = repoService.getRepositoryConnection();
		// Generating BLOB
		Connection jdbcConn = null;
		try {
			jdbcConn = connService.connect(repoConn);
			switch (repoConn.getDBVendor()) {
			case ORACLE:
				writeOracleBlob(jdbcConn, repFile, file);
				break;
			case MYSQL:
				writeMySQLBlob(jdbcConn, repFile, file);
				break;
			}
			return repFile;
		} catch (SQLException sqle) {
			throw new ErrorException(sqle);
		} finally {
			try {
				if (jdbcConn != null) {
					jdbcConn.close();
				}
			} catch (SQLException sqle) {
				throw new ErrorException(VCSMessages.getString("files.closeProblem")); //$NON-NLS-1$
			}
		}
	}

	private void writeMySQLBlob(Connection conn, IRepositoryFile file, File localFile)
			throws SQLException {
		file.setFileSizeKB((localFile.length() / 1024) + 1);
		PreparedStatement stmt = null;
		InputStream is = null;
		try {
			/*
			 * Columns names in the SET clause cannot be qualified with an alias name because it
			 * would fail in Postgres.
			 */
			stmt = conn.prepareStatement("UPDATE rep_files rf " //$NON-NLS-1$
					+ "  SET file_content = ? " //$NON-NLS-1$
					+ "    , filesize = ? " //$NON-NLS-1$
					+ "WHERE rf.file_id = ? "); //$NON-NLS-1$
			is = new FileInputStream(localFile);
			stmt.setBinaryStream(1, is);
			stmt.setLong(2, file.getFileSizeKB());
			stmt.setLong(3, file.getUID().rawId());
			stmt.execute();
		} catch (IOException e) {
			throw new ErrorException(e);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			safeClose(is);
		}
	}

	/**
	 * Write the specified file to the given repository file with Oracle-specific Blob support.
	 * 
	 * @param conn Oracle connection
	 * @param file repository file which must have been created
	 * @param localFile local file to dump into the repository file
	 * @throws SQLException when any database connection problems occurs
	 */
	private void writeOracleBlob(Connection conn, IRepositoryFile file, File localFile)
			throws SQLException {
		PreparedStatement stmt = null;
		long size = 0;

		try {
			/*
			 * Columns names in the SET clause cannot be qualified with an alias name because it
			 * would fail in Postgres.
			 */
			stmt = conn.prepareStatement("UPDATE rep_files rf " //$NON-NLS-1$
					+ "  SET file_content = ? " //$NON-NLS-1$
					+ "    , filesize = ? " //$NON-NLS-1$
					+ "WHERE rf.file_id = ? "); //$NON-NLS-1$

			OutputStream os = null;
			FileInputStream is = null;
			BLOB tempBlob = null;

			try {
				// Get the oracle connection class for checking
				Class<?> oracleConnectionClass = Class.forName("oracle.jdbc.OracleConnection"); //$NON-NLS-1$

				// Make sure connection object is right type
				if (!oracleConnectionClass.isAssignableFrom(conn.getClass())) {
					throw new HibernateException(
							VCSMessages.getString("files.invalidOracleConnection") //$NON-NLS-1$
									+ VCSMessages.getString("files.invalidOracleConnection.2") //$NON-NLS-1$
									+ conn.getClass().getName());
				}

				// Create our temp BLOB
				tempBlob = BLOB.createTemporary(conn, true, BLOB.DURATION_SESSION);
				tempBlob.open(BLOB.MODE_READWRITE);
				os = tempBlob.getBinaryOutputStream();
				is = new FileInputStream(localFile);

				// Large 10K buffer for efficient read
				byte[] buffer = new byte[10240];
				int bytesRead = 0;

				while ((bytesRead = is.read(buffer)) >= 0) {
					os.write(buffer, 0, bytesRead);
					size += bytesRead;
				}
			} catch (ClassNotFoundException cnfe) {
				// could not find the class with reflection
				throw new ErrorException(VCSMessages.getString("files.classUnresolved") //$NON-NLS-1$
						+ cnfe.getMessage());
			} catch (FileNotFoundException fnfe) {
				throw new ErrorException(VCSMessages.getString("files.fileUnresolved")); //$NON-NLS-1$
			} catch (IOException ioe) {
				throw new ErrorException(VCSMessages.getString("files.readProblem"), ioe); //$NON-NLS-1$
			} finally {
				safeClose(os);
				safeClose(is);
				if (tempBlob != null) {
					tempBlob.close();
				}
			}
			stmt.setBlob(1, tempBlob);
			stmt.setLong(2, size);
			stmt.setLong(3, file.getUID().rawId());
			stmt.execute();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			file.setFileSizeKB(size / 1024);
		}
	}

	/**
	 * Generates the specified repository file back to the local file system path. The given path
	 * must point to the file to create (and not to its owning directory)
	 * 
	 * @param file repository file to generate
	 * @param path file path to the local file to generate
	 */
	public void generateFile(IRepositoryFile file, String path) {
		final IConnection repoConn = repoService.getRepositoryConnection();

		Connection jdbcConn = null;
		try {
			jdbcConn = connService.connect(repoConn);
			generateOracleFile(jdbcConn, file, path);
		} catch (SQLException sqle) {
			throw new ErrorException(sqle);
		} finally {
			try {
				if (jdbcConn != null) {
					jdbcConn.close();
				}
			} catch (SQLException sqle) {
				throw new ErrorException(VCSMessages.getString("files.closeProblem")); //$NON-NLS-1$
			}
		}
	}

	private void generateOracleFile(Connection conn, IRepositoryFile file, String path)
			throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		InputStream blobStream = null;
		OutputStream os = null;
		try {
			// Querying blob
			stmt = conn.prepareStatement("SELECT rf.file_content " //$NON-NLS-1$
					+ "FROM rep_files rf " //$NON-NLS-1$
					+ "WHERE rf.file_id = ? "); //$NON-NLS-1$
			stmt.setLong(1, file.getUID().rawId());
			rs = stmt.executeQuery();
			if (rs.next()) {
				// Retrieving blob input stream
				blobStream = rs.getBinaryStream(1);
				if (blobStream == null) {
					return;
				}

				// Opening output file
				File f = new File(path);
				os = new FileOutputStream(f);

				// Large 10K buffer for efficient read
				byte[] buffer = new byte[10240];
				int bytesRead = 0;

				while ((bytesRead = blobStream.read(buffer)) >= 0) {
					os.write(buffer, 0, bytesRead);
				}
			} else {
				throw new ErrorException(VCSMessages.getString("files.notFound")); //$NON-NLS-1$
			}
		} catch (IOException e) {
			throw new ErrorException(VCSMessages.getString("files.readRepositoryProblem"), //$NON-NLS-1$
					e);
		} finally {
			safeClose(os);
			safeClose(blobStream);
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	protected void safeClose(Closeable s) {
		if (s != null) {
			try {
				s.close();
			} catch (IOException e) {
				LOGGER.error(VCSMessages.getString("files.streamCloseProblem"), e); //$NON-NLS-1$
			}
		}
	}

	public String getResourceString(IRepositoryFile file) {
		final IConnection repoConn = repoService.getRepositoryConnection();

		Connection jdbcConn = null;
		try {
			jdbcConn = connService.connect(repoConn);
			return getFileAsString(jdbcConn, file);
		} catch (SQLException sqle) {
			throw new ErrorException(sqle);
		} finally {
			try {
				if (jdbcConn != null) {
					jdbcConn.close();
				}
			} catch (SQLException sqle) {
				throw new ErrorException(VCSMessages.getString("files.closeProblem")); //$NON-NLS-1$
			}
		}
	}

	private String getFileAsString(Connection conn, IRepositoryFile file) throws SQLException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		InputStream blobStream = null;
		InputStreamReader reader = null;
		StringWriter os = null;
		try {
			// Querying blob
			stmt = conn.prepareStatement("SELECT rf.file_content " //$NON-NLS-1$
					+ "FROM rep_files rf " //$NON-NLS-1$
					+ "WHERE rf.file_id = ? "); //$NON-NLS-1$
			stmt.setLong(1, file.getUID().rawId());
			rs = stmt.executeQuery();
			if (rs.next()) {
				// Retrieving blob input stream
				blobStream = rs.getBinaryStream(1);
				if (blobStream == null) {
					return ""; //$NON-NLS-1$
				}
				reader = new InputStreamReader(blobStream);

				// Opening output file
				os = new StringWriter(10240);

				// Large 10K buffer for efficient read
				char[] buffer = new char[10240];
				int bytesRead = 0;
				while ((bytesRead = reader.read(buffer)) >= 0) {
					os.write(buffer, 0, bytesRead);
				}
				return os.toString();
			} else {
				throw new ErrorException(VCSMessages.getString("files.notFound")); //$NON-NLS-1$
			}
		} catch (IOException e) {
			throw new ErrorException(VCSMessages.getString("files.readRepositoryProblem"), //$NON-NLS-1$
					e);
		} finally {
			safeClose(os);
			safeClose(blobStream);
			safeClose(reader);
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

}
