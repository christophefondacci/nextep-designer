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
package com.nextep.designer.sqlgen.postgre.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

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
public class PostGreSQLSubmitter extends AbstractExternalSQLSubmitter {

	private static final String PASSWORD_ENV_VAR_NAME = "PGPASSWORD"; //$NON-NLS-1$

	private Process process = null;

	@Override
	protected void abort() {
		if (process != null) {
			process.destroy();
		}
	}

	@Override
	protected boolean doSubmit(IProgressMonitor monitor, ISQLScript script, IConnection conn)
			throws IOException {
		List<String> commandLine = new ArrayList<String>(5);
		commandLine.add(getBinaryProgram());
		commandLine.add("-h" + conn.getServerIP()); //$NON-NLS-1$
		commandLine.add("-p" + conn.getServerPort()); //$NON-NLS-1$
		commandLine.add("-U" + conn.getLogin()); //$NON-NLS-1$
		commandLine.add("-d" + conn.getDatabase()); //$NON-NLS-1$

		ProcessBuilder pb = new ProcessBuilder(commandLine.toArray(new String[0]));
		pb.redirectErrorStream(true);

		String dbPassword = conn.getPassword();
		if (dbPassword != null && !"".equals(dbPassword.trim())) { //$NON-NLS-1$
			Map<String, String> env = pb.environment();
			env.put(PASSWORD_ENV_VAR_NAME, dbPassword);
		}

		process = pb.start();

		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

		if (conn.getSchema() != null && !"".equals(conn.getSchema().trim())) { //$NON-NLS-1$
			w.write("SET search_path TO " + conn.getSchema() + ",public;"); //$NON-NLS-1$ //$NON-NLS-2$
			w.newLine();
		}
		w.write("\\i '" //$NON-NLS-1$
				+ script.getAbsolutePathname().replaceAll(Matcher.quoteReplacement("\\"), "/") //$NON-NLS-1$ //$NON-NLS-2$
				+ "'"); //$NON-NLS-1$
		w.newLine();
		w.write("\\q"); //$NON-NLS-1$
		w.flush();
		w.close();
		boolean isOk = true;
		String line;
		int lineCount = 0;
		int errorCount = 0;
		while ((line = input.readLine()) != null) {
			lineCount++;
			getConsole().log("Postgres> " + line); //$NON-NLS-1$
			if (line.indexOf(": ERR") > 0) { //$NON-NLS-1$
				isOk = false;
				handleError(script, line, lineCount);
				errorCount++;
			}
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
		return DBVendor.POSTGRE;
	}

	private void handleError(ISQLScript script, String errorLine, int lineCount) {
		if (SubmitErrorsMarkerProvider.getInstance() != null) {

			final String cleanLine = errorLine.replace("psql:", "");
			final int index = cleanLine.indexOf(":");
			int line = 0;
			if (index > -1) {
				final int end = cleanLine.indexOf(':', index + 1);
				if (end > 0) {
					final String lineNbStr = cleanLine.substring(index + 1, end);
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
