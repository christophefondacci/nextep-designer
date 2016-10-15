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
package com.nextep.installer.impl.req;

import java.io.File;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.impl.Status;

/**
 * This requirements ensures that the NXTP_HOME system variable is defined and points to an existing
 * location.
 * 
 * @author Christophe Fondacci
 */
public class NextepHomeRequirement implements IRequirement {

	public IStatus checkRequirement(IInstallConfigurator configurator) {
		// Retrieving home directory from system properties
		String home = System.getProperty(NextepInstaller.NXTP_HOME);
		if (home != null && !"".equals(home)) {
			File f = new File(home);
			if (!f.exists()) {
				// If the directory does not exist, it is not valid
				return new Status(false, "The home directory defined in the {"
						+ NextepInstaller.NXTP_HOME
						+ "} environment variable points to a non existing directory.");
			}
		} else {
			// No home defined, we quit
			return new Status(false, "Environment variable {" + NextepInstaller.NXTP_HOME
					+ "} not set.");
		}
		configurator.setNextepHome(home);
		return new Status(true, home);
	}

	public String getName() {
		return "neXtep Home NXTP_HOME";
	}
}
