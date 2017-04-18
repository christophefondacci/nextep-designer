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
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Matcher;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.helpers.PostgreSqlHelper;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDeployHandler;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.services.ILoggingService;

/**
 * A handler for SQL-based artefacts. This handler executes psql with the specified artefact script
 * file, displaying the psql output to stdout.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PostgreSqlDeployHandler implements IDeployHandler {

	public void deploy(IInstallConfiguration conf, IArtefact artefact) throws DeployException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final IDatabaseTarget target = conf.getTarget();
		final String schema = target.getSchema();

		try {
			final List<String> pbArgs = PostgreSqlHelper.getProcessBuilderArgs(conf);
			final ProcessBuilder pb = new ProcessBuilder(pbArgs);
			pb.redirectErrorStream(true);
			Process p = pb.start();

			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			if (schema != null && !"".equals(schema.trim())) { //$NON-NLS-1$
				w.write("SET search_path TO " + schema + ",public;"); //$NON-NLS-1$ //$NON-NLS-2$
				w.newLine();
			}

			String scriptPath = artefact.getRelativePath() + File.separator
					+ artefact.getFilename();
			w.write("\\i '" //$NON-NLS-1$
					+ scriptPath.replaceAll(Matcher.quoteReplacement("\\"), "/") //$NON-NLS-1$ //$NON-NLS-2$
					+ "'"); //$NON-NLS-1$
			w.newLine();
			w.write("\\q"); //$NON-NLS-1$
			w.flush();
			w.close();

			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				logger.log("\tPostgres> " + line); //$NON-NLS-1$
			}
			input.close();
		} catch (IOException e) {
			throw new DeployException("I/O Exception occurred while executing SQL.", e);
		}

	}

}
