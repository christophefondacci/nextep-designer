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

import java.sql.Connection;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.helpers.Assert;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.base.AbstractJDBCRequirement;
import com.nextep.installer.model.impl.DatabaseTarget;
import com.nextep.installer.model.impl.Status;

public class TargetUserRequirement extends AbstractJDBCRequirement {

	private static final String DEFAULT_SERVER = "127.0.0.1"; //$NON-NLS-1$

	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		try {
			final String user = configurator.getOption(InstallerOption.USER);
			final String password = configurator.getOption(InstallerOption.PASSWORD);
			final String database = configurator.getOption(InstallerOption.DATABASE);
			String host = configurator.getOption(InstallerOption.HOST);
			String port = configurator.getOption(InstallerOption.PORT);
			String vendor = configurator.getOption(InstallerOption.VENDOR);
			String tns = configurator.getOption(InstallerOption.TNS);

			Assert.notNull(user, "No username defined");
			Assert.notNull(database, "No database identifier defined");
			if (host == null) {
				host = DEFAULT_SERVER;
			}
			DBVendor dbVendor;
			if (vendor == null) {
				Assert.notNull(configurator.getDelivery(),
						"Cannot determine database vendor, try to specify it explicitly with -vendor option");
				dbVendor = configurator.getDelivery().getDBVendor();
				if (dbVendor == DBVendor.JDBC) {
					return new Status(false,
							"No database vendor specified: JDBC deliveries need an explicit vendor");
				}
			} else {
				dbVendor = DBVendor.valueOf(vendor);
			}
			Assert.notNull(dbVendor,
					"Unable to retrieve database vendor, try specifying it explicitly");
			if (port == null) {
				port = String.valueOf(dbVendor.getDefaultPort());
			}

			IDatabaseTarget target = new DatabaseTarget(user, password, database, host, port,
					dbVendor, tns);
			configurator.setTarget(target);

			// Establishing connection
			Connection targetConnection = getConnectionFor(target);
			configurator.setTargetConnection(targetConnection);
			return new Status(true, target.toString());
		} catch (InstallerException e) {
			if (configurator.isAnyOptionDefined(InstallerOption.INSTALL)) {
				// Outputting exception in verbose mode
				if (configurator.isOptionDefined(InstallerOption.VERBOSE)) {
					e.printStackTrace();
				}
				return new Status(true, "Skipped [Failed with: " + e.getMessage() + "]"); //$NON-NLS-2$
			} else {
				throw new InstallerException(
						"Cannot connect to target database: " + e.getMessage(), e);
			}
		}
	}

	public String getName() {
		return "Target JDBC connection";
	}

}
