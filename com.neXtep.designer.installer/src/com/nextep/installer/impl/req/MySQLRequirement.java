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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.helpers.ExternalProgramHelper;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.base.AbstractVendorRequirement;
import com.nextep.installer.model.impl.Status;

/**
 * @author Christophe Fondacci
 */
public class MySQLRequirement extends AbstractVendorRequirement {

	/**
	 * @see com.nextep.installer.model.base.AbstractVendorRequirement.model.IVendorRequirement#checkRequirement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		IDatabaseTarget target = configurator.getTarget();
		boolean passed = true;
		try {
			final String bin = ExternalProgramHelper.getProgramLocation(configurator, "mysql");
			final String login = target.getUser();
			final String password = target.getPassword();
			final String sid = target.getDatabase();
			final String host = target.getHost();
			final String port = target.getPort();

			ProcessBuilder pb = null;
			List<String> args = new ArrayList<String>();
			args.add(bin);
			args.add("-u" + login);
			if (password != null && !"".equals(password.trim())) {
				args.add("-p" + password);
			}
			if (host != null && !"".equals(host.trim())) {
				args.add("-h" + host);
			}
			if (port != null && !"".equals(port.trim())) {
				args.add("-P" + String.valueOf(port));
			}
			args.add("-vvv");
			args.add("-f");
			args.add("--unbuffered");
			args.add(sid);
			pb = new ProcessBuilder(args);
			pb.redirectErrorStream(true);

			Process p = pb.start(); // Runtime.getRuntime().exec(cmd);

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			writer.write("exit");
			writer.newLine();
			writer.flush();
			passed = isOK("MySQL user login", p);
			if (!passed) {
				return new Status(false, "Failed to log in to mysql."); // Tried
																		// to
																		// login
																		// with:
				// [" + cmd + "].");
			}
			writer.close();

			input.close();
			// String line;
			// while ((line = input.readLine()) != null) {
			// NextepInstaller.log(vendor.toString() + "> " + line);
			// if(line.indexOf("ORA-")>0 || line.indexOf("PLS-")>0 ||
			// line.indexOf("ERROR")>0) {
			// result.setStatus(Status.KO);
			// }
			// }
		} catch (IOException e) {
			throw new InstallerException("I/O Exception occurred: " + e.getMessage(), e);
		}

		return new Status(true);
	}

	public String getName() {
		return "MySql native login";
	}
}
