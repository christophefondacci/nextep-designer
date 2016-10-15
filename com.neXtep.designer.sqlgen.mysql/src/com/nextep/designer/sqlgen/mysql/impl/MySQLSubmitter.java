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
package com.nextep.designer.sqlgen.mysql.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nextep.datadesigner.sqlgen.impl.AbstractExternalSQLSubmitter;
import com.nextep.datadesigner.sqlgen.impl.SubmitErrorsMarkerProvider;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MySQLSubmitter extends AbstractExternalSQLSubmitter {

	private static final Map<String, String> MYSQL_CHARSET_MAP = initMysqlCharsetMap();

	private Process process = null;

	private static final Map<String, String> initMysqlCharsetMap() {
		Map<String, String> m = new HashMap<String, String>();

		m.put("CP1252", "latin1"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("ISO-8859-1", "latin1"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("ISO-8859-15", "latin1"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("US-ASCII", "ascii"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("UTF-16", "utf8"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("UTF-16BE", "utf8"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("UTF-16LE", "utf8"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("UTF-8", "utf8"); //$NON-NLS-1$ //$NON-NLS-2$

		return Collections.unmodifiableMap(m);
	}

	@Override
	protected void abort() {
		if (process != null) {
			process.destroy();
		}
	}

	@Override
	protected boolean doSubmit(IProgressMonitor monitor, ISQLScript script, IConnection conn)
			throws IOException {
		List<String> commandLine = new ArrayList<String>(9);
		commandLine.add(getBinaryProgram());
		commandLine.add("-u" + conn.getLogin()); //$NON-NLS-1$
		if (conn.getPassword() != null && !conn.getPassword().equals("")) { //$NON-NLS-1$
			commandLine.add("-p" + conn.getPassword()); //$NON-NLS-1$
		}
		commandLine.add("-h" + conn.getServerIP()); //$NON-NLS-1$
		commandLine.add("-P" + conn.getServerPort()); //$NON-NLS-1$

		/*
		 * Retrieves the encoding specified in the neXtep preferences and tries
		 * to find a corresponding MySQL encoding. If no match has been found,
		 * let the MySQL client using its default character set.
		 */
		String encoding = getEncodingPreference();
		if (MYSQL_CHARSET_MAP.containsKey(encoding)) {
			commandLine.add("--default-character-set=" + MYSQL_CHARSET_MAP.get(encoding)); //$NON-NLS-1$
		}

		commandLine.add("-vvv"); //$NON-NLS-1$
		commandLine.add("-f"); //$NON-NLS-1$
		commandLine.add("--unbuffered"); //$NON-NLS-1$

		ProcessBuilder pb = new ProcessBuilder(commandLine.toArray(new String[0]));
		pb.redirectErrorStream(true);

		process = pb.start();
		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(process.getOutputStream()));
		writer.write("use " + conn.getDatabase()); //$NON-NLS-1$
		writer.newLine();
		writer.write("source " + script.getAbsolutePathname()); //$NON-NLS-1$
		writer.newLine();
		writer.write("exit"); //$NON-NLS-1$
		writer.newLine();
		writer.flush();
		writer.close();
		String line;
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(),
				encoding));
		boolean isOk = true;
		int lineCount = 1;
		int errorCount = 0;
		while ((line = input.readLine()) != null) {
			getConsole().log("MySQL> " + line); //$NON-NLS-1$
			if (line.indexOf("ERROR") >= 0) { //$NON-NLS-1$
				isOk = false;
				handleError(script, line, lineCount);
				errorCount++;
			}
			lineCount++;
		}
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			getConsole().log("Interrupted process: " + e.getMessage()); //$NON-NLS-1$
		}
		logErrors(errorCount);
		getConsole().log("Generator exit value was: " + process.exitValue()); //$NON-NLS-1$
		input.close();
		return isOk;
	}

	@Override
	public DBVendor getVendor() {
		return DBVendor.MYSQL;
	}

	private void handleError(ISQLScript script, String errorLine, int lineCount) {
		if (SubmitErrorsMarkerProvider.getInstance() != null) {
			final int index = errorLine.indexOf("line ");
			int line = 0;
			if (index > -1) {
				final int end = errorLine.indexOf(' ', index + 5);
				if (end > 0) {
					final String lineNbStr = errorLine.substring(index + 5, end);
					try {
						line = Integer.valueOf(lineNbStr);
					} catch (NumberFormatException e) {
						// log.error("Line error", e);
					}
				}
			}

			SubmitErrorsMarkerProvider.getInstance().addErrorMarker(script, errorLine, line);
		}
	}
}
