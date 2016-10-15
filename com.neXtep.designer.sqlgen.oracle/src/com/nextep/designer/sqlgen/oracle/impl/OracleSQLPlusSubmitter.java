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
package com.nextep.designer.sqlgen.oracle.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
public class OracleSQLPlusSubmitter extends AbstractExternalSQLSubmitter {

	private static final String NLS_LANG_ENV_VAR_NAME = "NLS_LANG"; //$NON-NLS-1$

	private static final Map<String, String> ORACLE_CHARSET_MAP = initOracleCharsetMap();

	private Process process = null;

	private static final Map<String, String> initOracleCharsetMap() {
		Map<String, String> m = new HashMap<String, String>();

		m.put("CP1252", "WE8MSWIN1252"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("ISO-8859-1", "WE8ISO8859P1"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("ISO-8859-15", "WE8ISO8859P15"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("US-ASCII", "US7ASCII"); //$NON-NLS-1$ //$NON-NLS-2$ 
		m.put("UTF-16", "AL16UTF16"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("UTF-16BE", "AL16UTF16"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("UTF-16LE", "AL16UTF16"); //$NON-NLS-1$ //$NON-NLS-2$
		m.put("UTF-8", "AL32UTF8"); //$NON-NLS-1$ //$NON-NLS-2$

		return Collections.unmodifiableMap(m);
	}

	@Override
	protected boolean doSubmit(IProgressMonitor monitor, ISQLScript script, IConnection conn)
			throws IOException {
		List<String> commandLine = new ArrayList<String>(3);
		commandLine.add(getBinaryProgram());
		String alias = conn.getTnsAlias();
		commandLine.add(conn.getLogin() + "/" + conn.getPassword() + "@" //$NON-NLS-1$ //$NON-NLS-2$
				+ (alias != null && !"".equals(alias.trim()) ? alias : conn.getDatabase())); //$NON-NLS-1$
		commandLine.add("@" //$NON-NLS-1$
				+ script.getAbsolutePathname().replaceAll(Matcher.quoteReplacement("\\"), "/")); //$NON-NLS-1$ //$NON-NLS-2$
		ProcessBuilder pb = new ProcessBuilder(commandLine.toArray(new String[0]));
		pb.redirectErrorStream(true);

		/*
		 * Retrieves the encoding specified in the neXtep preferences and tries
		 * to find a corresponding Oracle charset. If no match has been found,
		 * let the Oracle client use its default character set.
		 */
		String encoding = getEncodingPreference();
		if (ORACLE_CHARSET_MAP.containsKey(encoding)) {
			String nlsLangEnvVar = "." + ORACLE_CHARSET_MAP.get(encoding); //$NON-NLS-1$

			/*
			 * Sets the NLS_LANG environment variable to specify the encoding
			 * used to encode the SQL script to submit. As NLS_LANG is specified
			 * without <Language>_<Territory> part, the <Language>_<Territory>
			 * part will default to AMERICAN_AMERICA. This is safer since
			 * console messages are not encoded with the same encoding as the
			 * file, but with the system default's encoding (Cp850 for Window's
			 * DOS environment for example).
			 */
			Map<String, String> env = pb.environment();
			env.put(NLS_LANG_ENV_VAR_NAME, nlsLangEnvVar);
		}

		process = pb.start();
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(),
				encoding));
		// Logging from output loop
		String line;
		boolean isOk = true;
		int lineCount = 0;
		int errorCount = 0;
		while ((line = input.readLine()) != null) {
			lineCount++;
			getConsole().log("Oracle> " + line); //$NON-NLS-1$
			if (isOk && (line.indexOf("ORA-") > 0 || line.indexOf("PLS-") > 0 || line //$NON-NLS-1$ //$NON-NLS-2$
					.indexOf("ERROR") > 0)) { //$NON-NLS-1$
				handleError(script, line, lineCount);
				isOk = false;
				errorCount++;
			}
		}
		getConsole().log("Generator exit value was: " + process.exitValue()); //$NON-NLS-1$
		input.close();
		return isOk;
	}

	private void handleError(ISQLScript script, String errorLine, int lineCount) {
		if (SubmitErrorsMarkerProvider.getInstance() != null) {
			SubmitErrorsMarkerProvider.getInstance().addErrorMarker(script, errorLine, lineCount);
		}
	}

	@Override
	protected void abort() {
		if (process != null) {
			process.destroy();
		}
	}

	@Override
	public DBVendor getVendor() {
		return DBVendor.ORACLE;
	}
}
