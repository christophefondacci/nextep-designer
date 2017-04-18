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
package com.nextep.installer.handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import com.neXtep.shared.parser.JdbcScriptParser;
import com.neXtep.shared.parser.ParsedItem;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDeployHandler;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.impl.Artefact;
import com.nextep.installer.services.ILoggingService;
import com.nextep.installer.util.JdbcUtils;

/**
 * This handler is able to deploy any script through the JDBC.
 * 
 * @author Christophe Fondacci
 */
public class JdbcDeployHandler implements IDeployHandler {

	public void deploy(IInstallConfiguration conf, IArtefact artefact) throws DeployException {
		final Connection conn = conf.getTargetConnection();
		final DBVendor vendor = conf.getTarget().getVendor();
		try {
			process(artefact, conn, vendor);
		} catch (SQLException e) {
			throw new DeployException("Problems while submitting SQL script through JDBC client", e);
		}
	}

	private void process(IArtefact artefact, Connection conn, DBVendor vendor)
			throws DeployException, SQLException {
		Statement stmt = null;
		try {
			// We load everything in memory which may not be the best...
			final String sql = loadSql(artefact);
			JdbcScriptParser parser = new JdbcScriptParser(sql, JdbcUtils.getPromptTag(vendor),
					JdbcUtils.getStatementEndTag(vendor), JdbcUtils.getScriptCallerTag(vendor));
			ParsedItem item = null;
			stmt = conn.createStatement();
			while ((item = parser.getNextItem()) != null) {
				switch (item.getItemType()) {
				case PROMPT:
					log(item.getContent());
					break;
				case SCRIPT_CALL:
					IArtefact innerArtefact = new Artefact();
					innerArtefact.setRelativePath(artefact.getRelativePath());
					innerArtefact.setFilename(item.getContent());
					process(innerArtefact, conn, vendor);
					break;
				case STATEMENT:
					String query = item.getContent();
					log("--------------"); //$NON-NLS-1$
					log(query);
					log("--------------"); //$NON-NLS-1$
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
						int seconds = (int) ((millis / 1000) % 60);
						long minutes = (int) ((millis / 1000) / 60);
						msg += String.format("%d min, %d sec, %d ms", //$NON-NLS-1$
								minutes, seconds - minutes * 60, millis - seconds * 1000);
						msg += ")"; //$NON-NLS-1$
						log(msg);
					} catch (SQLException e) {
						log("Query failed [" + e.getErrorCode() + "]: " + e.getMessage()); //$NON-NLS-1$//$NON-NLS-2$
					}
					break;
				}
				log(""); //$NON-NLS-1$
			}
		} catch (IOException e) {
			throw new DeployException("Problems while parsing SQL script", e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException sqle) {
				try {
					log("Unable to close statement: " + sqle.getMessage());
				} catch (IOException ioe) {
					// Do nothing
				}
			}
		}
	}

	private String loadSql(IArtefact a) throws DeployException {
		final String fullFilePath = a.getRelativePath() + File.separator + a.getFilename();
		File f = new File(fullFilePath);
		if (!f.exists()) {
			throw new DeployException(MessageFormat.format("Unable to load artefact: {0}",
					fullFilePath));
		}
		FileReader fr = null;
		char[] buff = new char[10240];
		StringBuffer str = new StringBuffer(1024);
		try {
			fr = new FileReader(f);
			int bytesRead = -1;
			while ((bytesRead = fr.read(buff)) > 0) {
				str.append(buff, 0, bytesRead);
			}
		} catch (IOException e) {
			throw new DeployException("I/O problems while reading file [" + fullFilePath + "]: " //$NON-NLS-2$
					+ e.getMessage(), e);
		} finally {
			if (fr != null) {
				try {
					fr.close();
				} catch (IOException e) {
					throw new DeployException("I/O problems while closing file [" + fullFilePath
							+ "]: " + e.getMessage(), e); //$NON-NLS-1$
				}
			}
		}
		return str.toString();
	}

	private void log(String text) throws IOException {
		final ILoggingService logger = ServicesHelper.getLoggingService();

		if (text.indexOf('\n') >= 0) {
			BufferedReader r = new BufferedReader(new StringReader(text));
			String line;
			while ((line = r.readLine()) != null) {
				log(line);
			}
			r.close();
		} else {
			logger.log("JDBC> " + text); //$NON-NLS-1$
		}
	}

}
