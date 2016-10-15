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
import java.util.List;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.helpers.PostgreSqlHelper;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDeployHandler;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.services.ILoggingService;

/**
 * A handler for SQL-based artefacts. This handler executes SQL*Plus with the specified artefact
 * script file, displaying the SQL*Plus output to stdout.
 * 
 * @author Christophe Fondacci
 */
public class PostgreSqlDeployHandler implements IDeployHandler {

	/**
	 * @see com.neXtep.installer.model.IDeployHandler#deploy(com.neXtep.installer.model.IArtefact,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	public void deploy(IInstallConfiguration conf, IArtefact artefact) throws DeployException {
		final ILoggingService logger = getLoggingService();

		try {
			final List<String> pbArgs = PostgreSqlHelper.getProcessBuilderArgs(conf);
			pbArgs.add("-f");
			pbArgs.add(artefact.getRelativePath() + File.separator + artefact.getFilename());
			final ProcessBuilder pb = new ProcessBuilder(pbArgs);
			pb.redirectErrorStream(true);
			Process p = pb.start();
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				logger.log("\tPostgres> " + line);
			}
			input.close();
		} catch (IOException e) {
			throw new DeployException("I/O Exception occurred while executing SQL.", e);
		}

	}

	private ILoggingService getLoggingService() {
		return NextepInstaller.getService(ILoggingService.class);
	}
}
