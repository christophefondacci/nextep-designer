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
package com.nextep.installer.model.base;

import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.services.ILoggingService;

/**
 * A vendor requirement is the requirement of a database vendor. Those requirements will be checked
 * immediately after the descriptor file has been parsed and validated.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractVendorRequirement implements IRequirement {

	private ILoggingService loggingService;

	public AbstractVendorRequirement() {
		loggingService = NextepInstaller.getService(ILoggingService.class);
	}

	/**
	 * Checks this vendor requirement.
	 * 
	 * @param login database login
	 * @param password database password
	 * @param sid database identifier
	 * @param host host name or ip of the server
	 * @param vendor vendor of the target database
	 * @return <code>true</code> if all requirements passed, else <code>false</code>
	 */
	// public abstract boolean checkRequirement(String login, String password, String sid,
	// String host, String port, DBVendor vendor);

	/**
	 * Displays the appropriate requirement log line depending on the specified process exit status
	 * 
	 * @param label label to display for this requirement
	 * @param p process to check
	 * @return <code>true</code> if process is ok, else <code>false</code>
	 */
	public boolean isOK(String label, Process p) throws InstallerException {
		// String lineSep = System.getProperty("line.separator");
		// loggingService.out(label, PADDING);
		Exception ex = null;
		int val = -1;
		do {
			try {
				val = p.exitValue();
				Thread.sleep(200);
				ex = null;
			} catch (IllegalThreadStateException e) {
				ex = e;
			} catch (InterruptedException e) {
				return false;
			}
		} while (ex != null);
		// loggingService.log(val == 0 ? "OK." : "ERROR! -> Exit value was : " + val);
		if (val != 0) {
			throw new InstallerException("External process exit value was: " + val);
		}
		return val == 0;
	}

	protected ILoggingService getLoggingService() {
		return loggingService;
	}
}
