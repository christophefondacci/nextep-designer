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
package com.nextep.installer.test.requirements;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.impl.req.NextepAdminRequirement;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.impl.DefaultInstallerConfigurator;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.ILoggingService;
import com.nextep.installer.services.impl.AdminService;
import com.nextep.installer.services.impl.LoggingService;
import com.nextep.installer.test.configuration.TestConfiguration;

public class AdminRequirementTest {

	@Before
	public void setUp() throws Exception {
		NextepInstaller.registerService(ILoggingService.class, new LoggingService());
		NextepInstaller.registerService(IAdminService.class, new AdminService());
	}

	@Test
	public void testCheckRequirement() throws InstallerException {
		IInstallConfigurator configuration = new DefaultInstallerConfigurator();
		// Initializing test config
		IDatabaseTarget target = TestConfiguration.getTestTarget(DBVendor.MYSQL);
		configuration.setTarget(target);

		IRequirement req = new NextepAdminRequirement();
		// Testing only target with no admin installed => failure expected
		boolean fail = false;
		try {
			fail = req.checkRequirement(configuration).isSuccess();
		} catch (InstallerException e) {
			fail = true;
		}
		Assert.assertTrue(
				"Admin requirement should fail on non admin-target without admin.properties", fail);

		// Install mode
		configuration.defineOption(InstallerOption.INSTALL);
		IStatus status = req.checkRequirement(configuration);
		Assert.assertTrue(
				"Admin requirement should pass with INSTALL mode but was : " + status.getMessage(),
				status.isSuccess());
		Assert.assertNotNull("Admin connection should be defined after requirement passed",
				configuration.getAdminConnection());
		Assert.assertEquals("Admin connection should be the target", target.toString(),
				status.getMessage());
	}

}
