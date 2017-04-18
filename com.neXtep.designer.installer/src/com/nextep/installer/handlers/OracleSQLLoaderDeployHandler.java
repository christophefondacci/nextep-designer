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
import com.neXtep.shared.model.SharedDeliveryServices;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDeployHandler;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.services.ILoggingService;

public class OracleSQLLoaderDeployHandler implements IDeployHandler {

	public void deploy(IInstallConfiguration conf, IArtefact artefact) throws DeployException {
		final ILoggingService logger = ServicesHelper.getLoggingService();

		final IDatabaseTarget target = conf.getTarget();
		final String login = target.getUser();
		final String password = target.getPassword();
		final String sid = target.getTnsAlias();

		String datafileName = artefact.getFilename();
		String controlFile = SharedDeliveryServices.getControlFileName(datafileName);

		// Checking if control file exists
		final String ctrlLoc = artefact.getRelativePath() + File.separator + controlFile;

		// Checking if datafile needs a control file to be loaded through SQL*Loader
		final boolean needControlFile = needsControlFile(artefact);

		File ctrl = new File(ctrlLoc);
		ProcessBuilder pb = null;
		if (ctrl.exists() && needControlFile) {
			pb = new ProcessBuilder(new String[] { "sqlldr", //$NON-NLS-1$
					"userid=" + login + "/" + password + "@" + sid, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"control=" + ctrlLoc, //$NON-NLS-1$
					"data=" + artefact.getRelativePath() + File.separator //$NON-NLS-1$
							+ artefact.getFilename() });
		} else {
			pb = new ProcessBuilder(new String[] { "sqlldr", //$NON-NLS-1$
					"userid=" + login + "/" + password + "@" + sid, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					"data=" + artefact.getRelativePath() + File.separator //$NON-NLS-1$
							+ artefact.getFilename() });
		}
		pb.redirectErrorStream(true);
		try {
			Process p = pb.start();
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				logger.log("\tSQL*Loader> " + line); //$NON-NLS-1$
			}
			input.close();
		} catch (IOException e) {
			throw new DeployException("I/O Exception occurred while executing SQL.", e);
		}
	}

	/**
	 * Indicates whether this datafile artefact contains control file <b>and</b> the data or the
	 * data only.
	 * 
	 * @param a an Oracle SQL*Loader artefact
	 * @return <code>true</code> if the datafile needs a control file or <code>false</code> if the
	 *         data file can be loaded alone.
	 * @throws DeployException when the datafile could not be found
	 */
	private boolean needsControlFile(IArtefact a) throws DeployException {
		final String path = a.getRelativePath() + File.separator + a.getFilename();
		final File dataFile = new File(path);

		if (!dataFile.exists()) {
			throw new DeployException("Unable to find datafile: " + path);
		}

		return false;
	}

}
