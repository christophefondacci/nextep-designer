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
package com.nextep.designer.sqlgen.db2.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.sqlgen.impl.AbstractExternalSQLSubmitter;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;

/**
 * @author Bruno Gautier
 */
public final class DB2SQLSubmitter extends AbstractExternalSQLSubmitter {

	private Process process = null;

	@Override
	protected boolean doSubmit(IProgressMonitor monitor, ISQLScript script, IConnection conn)
			throws IOException {
		/*
		 * The DB2 command line processor has to be run in batch mode to process a SQL script file.
		 * To achieve this, we need to run a list of successive commands, prefixed by the DB2
		 * command line processor binary name, in order to connect to the database, select the right
		 * schema, and execute the SQL script. The different front-end processes will be served by a
		 * single back-end process until a TERMINATE command is issued.
		 */

		// Connect to the database
		List<String> connectCommandLine = new ArrayList<String>(4);
		connectCommandLine.add(getBinaryProgram());
		connectCommandLine.add("CONNECT TO " + conn.getDatabase()); //$NON-NLS-1$
		connectCommandLine.add("USER " + conn.getLogin()); //$NON-NLS-1$
		connectCommandLine.add("USING " + conn.getPassword()); //$NON-NLS-1$
		ProcessBuilder pb = new ProcessBuilder(connectCommandLine.toArray(new String[0]));
		pb.redirectErrorStream(true);
		process = pb.start();
		logToConsole(process.getInputStream());

		String currentSchema = conn.getSchema();
		if (currentSchema != null && !"".equals(currentSchema.trim()) //$NON-NLS-1$
				&& !conn.getLogin().equals(currentSchema.trim())) {
			// Sets the current schema
			List<String> setSchemaCommandLine = new ArrayList<String>(2);
			setSchemaCommandLine.add(getBinaryProgram());
			setSchemaCommandLine.add("SET CURRENT SCHEMA " + currentSchema.trim()); //$NON-NLS-1$
			process = pb.command(setSchemaCommandLine).start();
			logToConsole(process.getInputStream());

			// Sets the current path used to resolve user-defined data types, procedures and
			// functions
			List<String> setPathCommandLine = new ArrayList<String>(2);
			setPathCommandLine.add(getBinaryProgram());
			setPathCommandLine.add("SET CURRENT PATH " + currentSchema.trim() + ", CURRENT PATH"); //$NON-NLS-1$ //$NON-NLS-2$
			process = pb.command(setPathCommandLine).start();
			logToConsole(process.getInputStream());
		}

		// Executes the SQL script
		List<String> scriptCommandLine = new ArrayList<String>(3);
		scriptCommandLine.add(getBinaryProgram());
		scriptCommandLine.add("-tmqvf"); //$NON-NLS-1$
		scriptCommandLine.add(script.getAbsolutePathname().replaceAll(
				Matcher.quoteReplacement("\\"), "/") //$NON-NLS-1$ //$NON-NLS-2$
				);
		process = pb.command(scriptCommandLine).start();
		boolean isOk = logToConsole(process.getInputStream());
		int exitValue = process.exitValue();

		// Executes the TERMINATE command
		List<String> terminateCommandLine = new ArrayList<String>(2);
		terminateCommandLine.add(getBinaryProgram());
		terminateCommandLine.add("TERMINATE"); //$NON-NLS-1$
		process = pb.command(terminateCommandLine).start();
		logToConsole(process.getInputStream());

		getConsole().log("Generator exit value was: " + exitValue); //$NON-NLS-1$

		return isOk;
	}

	private boolean logToConsole(InputStream stream) throws IOException {
		BufferedReader input = new BufferedReader(new InputStreamReader(stream,
				getEncodingPreference()));

		String line;
		boolean isOk = true;
		while ((line = input.readLine()) != null) {
			getConsole().log("DB2> " + line); //$NON-NLS-1$
			isOk = !(isOk && line.indexOf("SQLSTATE") > 0); //$NON-NLS-1$
		}
		input.close();

		return isOk;
	}

	@Override
	protected void abort() {
		if (process != null) {
			process.destroy();
		}
	}

	@Override
	public DBVendor getVendor() {
		return DBVendor.DB2;
	}

}
