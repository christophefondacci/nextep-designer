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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.helpers.PostgreSqlHelper;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.base.AbstractVendorRequirement;
import com.nextep.installer.model.impl.Status;

public class PostgreSqlRequirement extends AbstractVendorRequirement {

	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		boolean passed = true;
		try {

			final List<String> pbArgs = PostgreSqlHelper.getProcessBuilderArgs(configurator);
			pbArgs.add("-c");
			pbArgs.add("\\q");
			final ProcessBuilder pb = new ProcessBuilder(pbArgs);
			pb.redirectErrorStream(true);
			Process p = pb.start();

			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
			}
			input.close();
			// BufferedWriter writer = new BufferedWriter(new
			// OutputStreamWriter(p.getOutputStream()));
			// writer.write("\\q");
			// writer.newLine();
			// writer.flush();
			passed = isOK("PostgreSQL user login", p);
			if (!passed) {
				return new Status(false, "Failed to log in to PostgreSql."); // Tried to login with:
				// [" + cmd + "].");
			}
			// writer.close();

			input.close();

		} catch (IOException e) {
			throw new InstallerException(
					"PostgreSQL user login failed: "
							+ e.getMessage()
							+ ". You may need to explicitly define psql binary location through installer properties",
					e);
		}

		return new Status(true);
	}

	public String getName() {
		return "PostgreSql native login";
	}
}
