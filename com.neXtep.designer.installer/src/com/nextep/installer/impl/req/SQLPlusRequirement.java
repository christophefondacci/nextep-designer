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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
public class SQLPlusRequirement extends AbstractVendorRequirement {

	/**
	 * @see com.nextep.installer.model.base.AbstractVendorRequirement.model.IVendorRequirement#checkRequirement(String,
	 *      String, String)
	 */
	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		IDatabaseTarget target = configurator.getTarget();
		// Locating SQL*Plus binary
		final String sqlplusBinary = ExternalProgramHelper.getProgramLocation(configurator,
				"sqlplus");
		// Locating TNS*Ping
		final String tnspingBinary = ExternalProgramHelper.getProgramLocation(configurator,
				"tnsping");

		boolean passed = true;
		try {
			Process p = Runtime.getRuntime().exec(tnspingBinary + " " + target.getTnsAlias());
			passed = isOK("TNS ping", p);
			p = Runtime.getRuntime().exec(sqlplusBinary + " /nolog");
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			writer.write("exit");
			writer.newLine();
			writer.flush();
			passed = passed && isOK("SQL*Plus", p);
			writer.close();

			// if(!NextepInstaller.isOptionEnabled(NextepInstaller.OPTION_REMOTE)) {
			// p = Runtime.getRuntime().exec
			// ("sqlplus /nolog");
			// writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			// writer.write("whenever sqlerror exit failure");
			// writer.newLine();
			// writer.write("connect / as sysdba");
			// writer.newLine();
			// writer.write("exit");
			// writer.newLine();
			// writer.flush();
			// final boolean dbaOk = isOK("SQL*Plus sysdba login",p);
			// passed = passed && dbaOk;
			// if(!dbaOk) {
			// NextepInstaller.out("No internal sysdba login, try with the option -remote if your are executing non locally");
			// }
			// writer.close();
			// }

			p = Runtime.getRuntime().exec(sqlplusBinary + " /nolog");
			writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			writer.write("whenever sqlerror exit failure");
			writer.newLine();
			writer.write("connect " + target.getUser() + "/" + target.getPassword() + "@"
					+ target.getTnsAlias());
			writer.newLine();
			writer.write("exit");
			writer.newLine();
			writer.flush();
			boolean userLogin = isOK("SQL*Plus user login", p);
			// // Creating user if non existing TODO Improve user creation(should be allowed in
			// descriptor.xml)
			// if(!userLogin && !NextepInstaller.isOptionEnabled(NextepInstaller.OPTION_REMOTE)) {
			// NextepInstaller.out("User does not exist, create it ? [yes/no] ");
			// String confirm = NextepInstaller.getUserInput("no");
			// if("YES".equals(confirm.toUpperCase())) {
			// // Creating user
			// p = Runtime.getRuntime().exec
			// ("sqlplus /nolog");
			// writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			// writer.write("whenever sqlerror exit failure");
			// writer.newLine();
			// writer.write("connect / as sysdba");
			// writer.newLine();
			// writer.write("create user " + login + " identified by " + password + ";");
			// writer.newLine();
			// writer.write("grant connect,resource to " + login + ";");
			// writer.newLine();
			// writer.write("exit");
			// writer.newLine();
			// writer.flush();
			// userLogin = isOK("User creation",p);
			// writer.close();
			// }
			// }
			passed = passed && userLogin;
			writer.close();

		} catch (IOException e) {
			throw new InstallerException("I/O Exception occurred.", e);
		}
		// Returning our passed flag
		return new Status(passed);
	}

	public String getName() {
		return "SQL*Plus native login";
	}
}
