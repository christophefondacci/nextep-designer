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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.helpers.ExternalProgramHelper;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDeployHandler;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.services.ILoggingService;

/**
 * A handler for SQL-based artefacts. This handler executes SQL*Plus with the specified artefact
 * script file, displaying the SQL*Plus output to stdout.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class OracleDeployHandler implements IDeployHandler {

	public void deploy(IInstallConfiguration conf, IArtefact artefact) throws DeployException {
		final ILoggingService logger = getLoggingService();
		final String sqlPlusBinary = ExternalProgramHelper.getProgramLocation(conf, "sqlplus"); //$NON-NLS-1$
		final IDatabaseTarget target = conf.getTarget();

		// Building SQL plus binary location
		List<String> commandLine = new ArrayList<String>(3);
		commandLine.add(sqlPlusBinary);
		commandLine.add(getSQLPlusConnectString(target));
		commandLine.add("@" + artefact.getRelativePath() + File.separator + artefact.getFilename()); //$NON-NLS-1$
		ProcessBuilder pb = new ProcessBuilder(commandLine.toArray(new String[0]));
		pb.redirectErrorStream(true);

		try {
			/*
			 * TODO [BGA] We should set the NLS_LANG environment variable according to the encoding
			 * of the artefact script file.
			 */
			Process p = pb.start();
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				logger.log("\tOracle> " + line); //$NON-NLS-1$
			}
			input.close();
		} catch (IOException e) {
			throw new DeployException("I/O Exception occurred while executing SQL.", e); //$NON-NLS-1$
		}
	}

	private String getSQLPlusConnectString(IDatabaseTarget target) {
		StringBuilder sb = new StringBuilder(target.getUser());
		final String alias = target.getTnsAlias();

		sb.append("/") //$NON-NLS-1$
				.append("\"\\\"").append(target.getPassword()).append("\\\"\"") //$NON-NLS-1$ //$NON-NLS-2$
				.append("@") //$NON-NLS-1$
				.append("(DESCRIPTION=") //$NON-NLS-1$
				.append("(ADDRESS_LIST=") //$NON-NLS-1$
				.append("(ADDRESS=") //$NON-NLS-1$
				.append("(PROTOCOL=TCP)") //$NON-NLS-1$
				.append("(HOST=").append(target.getHost()).append(")") //$NON-NLS-1$ //$NON-NLS-2$
				.append("(PORT=").append(target.getPort()).append(")") //$NON-NLS-1$ //$NON-NLS-2$
				.append(")") //$NON-NLS-1$
				.append(")") //$NON-NLS-1$
				.append("(CONNECT_DATA="); //$NON-NLS-1$

		/*
		 * FIXME [BGA] DatabaseTarget#getTnsAlias() returns SID when no TNS alias has been set. It
		 * should return null instead, but for now we simply check that SID and TNS alias are not
		 * the same.
		 */
		if (alias != null && !"".equals(alias.trim()) && !alias.equals(target.getDatabase())) { //$NON-NLS-1$
			sb.append("(SERVICE_NAME=").append(alias).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			sb.append("(SID=").append(target.getDatabase()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		}

		sb.append(")").append(")"); //$NON-NLS-1$ //$NON-NLS-2$

		return sb.toString();
	}

	private ILoggingService getLoggingService() {
		return NextepInstaller.getService(ILoggingService.class);
	}

}
