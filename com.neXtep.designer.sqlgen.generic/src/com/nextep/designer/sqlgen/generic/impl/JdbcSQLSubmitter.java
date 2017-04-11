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
package com.nextep.designer.sqlgen.generic.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.eclipse.core.runtime.IProgressMonitor;
import com.neXtep.shared.parser.JdbcScriptParser;
import com.neXtep.shared.parser.ParsedItem;
import com.nextep.datadesigner.sqlgen.impl.AbstractSQLSubmitter;
import com.nextep.datadesigner.sqlgen.impl.GeneratorFactory;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.impl.SubmitErrorsMarkerProvider;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.sqlgen.model.ISQLParser;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class JdbcSQLSubmitter extends AbstractSQLSubmitter {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("EEE d MMM yyyy"); //$NON-NLS-1$
	private static final String QUERY_SEPARATOR = "--------------"; //$NON-NLS-1$
	private static final String JDBC_PROMPT = "JDBC> "; //$NON-NLS-1$

	@Override
	protected void abort() {
	}

	@Override
	protected boolean doSubmit(IProgressMonitor monitor, ISQLScript script, IConnection conn)
			throws SQLException, IOException {
		getConsole().log("neXtep JDBC database client 1.0.0.0 - Started on " //$NON-NLS-1$
				+ DATE_FORMATTER.format(new Date()));
		getConsole().log("Copyright (c) 2007-2011 neXtep Softwares."); //$NON-NLS-1$
		getConsole().log(""); //$NON-NLS-1$

		Connection jdbcConn = null;
		try {
			jdbcConn = CorePlugin.getConnectionService().connect(conn);
			process(conn.getDBVendor(), script, jdbcConn);
		} finally {
			if (jdbcConn != null) {
				jdbcConn.close();
			}
		}

		getConsole().log(""); //$NON-NLS-1$
		getConsole().log("JDBC client terminated."); //$NON-NLS-1$

		return true;
	}

	private void process(DBVendor vendor, ISQLScript script, Connection c) throws SQLException,
			IOException {
		final ISQLParser sqlParser = GeneratorFactory.getSQLParser(vendor);
		String stmtDelimiter = sqlParser.getStatementDelimiter();

		if (vendor == DBVendor.ORACLE) {
			stmtDelimiter = "\r\n" + stmtDelimiter + "\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		JdbcScriptParser parser = new JdbcScriptParser(script.getSql(),
				sqlParser.getPromptCommand(), stmtDelimiter, SQLGenUtil.getScriptCallerTag(vendor));
		ParsedItem item = null;

		Statement stmt = null;
		try {
			stmt = c.createStatement();
			while ((item = parser.getNextItem()) != null) {
				switch (item.getItemType()) {
				case PROMPT:
					log(item.getContent());
					break;
				case SCRIPT_CALL:
					if (script instanceof SQLWrapperScript) {
						SQLWrapperScript w = (SQLWrapperScript) script;
						for (ISQLScript child : w.getChildren()) {
							if (item.getContent().equals(child.getFilename())) {
								process(vendor, child, c);
							}
						}
					}
					break;
				case STATEMENT:
					String query = item.getContent();
					log(QUERY_SEPARATOR);
					log(query);
					log(QUERY_SEPARATOR);
					try {
						String msg = "Query OK, "; //$NON-NLS-1$
						long start = System.currentTimeMillis();
						boolean isRset = stmt.execute(query);
						if (!isRset) {
							msg += stmt.getUpdateCount() + " row(s) updated "; //$NON-NLS-1$
						} else {
							msg += "skipped results "; //$NON-NLS-1$
						}
						msg += "(elapsed "; //$NON-NLS-1$
						long millis = System.currentTimeMillis() - start;
						long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
						long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
						msg += String.format("%d min, %d sec, %d ms", //$NON-NLS-1$
								minutes, seconds - TimeUnit.MINUTES.toSeconds(minutes), millis
										- TimeUnit.SECONDS.toMillis(seconds));
						msg += ")"; //$NON-NLS-1$
						log(msg);
					} catch (SQLException e) {
						log("Query failed [" + e.getErrorCode() + "]: " + e.getMessage()); //$NON-NLS-1$//$NON-NLS-2$
						if (SubmitErrorsMarkerProvider.getInstance() != null) {
							SubmitErrorsMarkerProvider.getInstance().addErrorMarker(script,
									e.getMessage(), parser.getCurrentLine());
						}
					}
					break;
				}
				log(""); //$NON-NLS-1$
			}
		} finally {
			CaptureHelper.safeClose(null, stmt);
		}
	}

	private void log(String text) throws IOException {
		if (text.indexOf('\n') >= 0) {
			BufferedReader r = new BufferedReader(new StringReader(text));
			String line;
			while ((line = r.readLine()) != null) {
				log(line);
			}
			r.close();
		} else {
			getConsole().log(JDBC_PROMPT + text);
		}
	}

	@Override
	public DBVendor getVendor() {
		return DBVendor.JDBC;
	}

}
