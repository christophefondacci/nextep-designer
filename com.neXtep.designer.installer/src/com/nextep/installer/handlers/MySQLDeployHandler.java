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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.helpers.ExternalProgramHelper;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDeployHandler;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Christophe Fondacci
 */
public class MySQLDeployHandler implements IDeployHandler {

	public void deploy(IInstallConfiguration conf, IArtefact artefact) throws DeployException {
		submit(conf, artefact.getFilename(), "source " + getFullArtefactPath(artefact) + "; "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void submit(IInstallConfiguration configuration, String filename, String sql)
			throws DeployException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final String mysqlBinary = ExternalProgramHelper.getProgramLocation(configuration, "mysql"); //$NON-NLS-1$
		final IDatabaseTarget target = configuration.getTarget();
		final String login = target.getUser();
		final String password = target.getPassword();
		final String sid = target.getDatabase();
		final String server = target.getHost();
		final String port = target.getPort();

		BufferedReader input = null;
		BufferedReader error = null;
		try {
			logger.log("Submitting artefact '" + filename + "', please wait...");

			ProcessBuilder pb = null;
			List<String> args = new ArrayList<String>();
			args.add(mysqlBinary);
			args.add("-u" + login); //$NON-NLS-1$

			if (password != null && !"".equals(password.trim())) { //$NON-NLS-1$
				args.add("-p" + password); //$NON-NLS-1$
			}

			if (server != null && !"".equals(server.trim())) { //$NON-NLS-1$
				args.add("-h" + server); //$NON-NLS-1$
			}

			args.add("-P" + port); //$NON-NLS-1$
			args.add("-vvv"); //$NON-NLS-1$
			args.add("-f"); //$NON-NLS-1$
			args.add("--unbuffered"); //$NON-NLS-1$
			args.add(sid);

			pb = new ProcessBuilder(args);
			pb.redirectErrorStream(true);
			Process p = pb.start();

			/*
			 * Keeping the input buffer association first because MySQL binary seems to have
			 * problems when writing output stream first on Linux (maybe Linux related?)
			 */
			input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			writer.write(sql);
			writer.newLine();
			writer.write("exit"); //$NON-NLS-1$
			writer.newLine();
			writer.flush();
			writer.close();
			String line;

			// Writing MySQL output
			while ((line = input.readLine()) != null) {
				logger.log("\tSQL> " + line); //$NON-NLS-1$
			}

		} catch (IOException e) {
			throw new DeployException("I/O Exception occurred while executing SQL.", e);
		} finally {
			try {
				if (input != null) {
					input.close();
				}
				if (error != null) {
					error.close();
				}
			} catch (IOException e) {
				logger.error("Problems while closing input streams.");
			}
		}
	}

	/**
	 * Builds a MySQL-compatible relative file path to the artefact file.
	 * 
	 * @param artefact artefact
	 * @return the MySQL-understandable local path
	 */
	protected String getFullArtefactPath(IArtefact artefact) {
		return artefact.getRelativePath().replace('\\', '/') + "/" //$NON-NLS-1$
				+ artefact.getFilename().replace('\\', '/');
	}

}
