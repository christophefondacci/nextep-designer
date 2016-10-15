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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.neXtep.shared.model.SharedDeliveryServices;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDeployHandler;
import com.nextep.installer.model.IInstallConfiguration;

public class MySQLLoadDeployHandler extends MySQLDeployHandler implements IDeployHandler {

	public void deploy(IInstallConfiguration configuration, IArtefact artefact)
			throws DeployException {
		// Loading control file
		String sql = getControlFile(artefact);

		submit(configuration, artefact.getFilename(), sql.replaceAll("\\$file",
				getFullArtefactPath(artefact)));

	}

	private String getControlFile(IArtefact artefact) throws DeployException {
		final String ctrlFilepath = artefact.getRelativePath() + File.separator
				+ SharedDeliveryServices.getControlFileName(artefact.getFilename());
		File ctrlFile = new File(ctrlFilepath);
		if (!ctrlFile.exists()) {
			throw new DeployException("No control file found while trying to load MySQL datafile");
		}
		FileReader is = null;
		StringBuffer buf = new StringBuffer(200);
		try {
			is = new FileReader(ctrlFile);
			char[] buffer = new char[10240];
			int bytesRead = 0;

			while ((bytesRead = is.read(buffer)) > 0) {
				buf.append(buffer, 0, bytesRead);
			}
		} catch (IOException e) {
			throw new DeployException("Problems while reading control file " + ctrlFilepath, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new DeployException("Unable to close stream while reading control file "
							+ ctrlFilepath);
				}
			}
		}
		return buf.toString();
	}
}
