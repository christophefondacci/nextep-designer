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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.impl.req.TargetUserRequirement;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.impl.DefaultInstallerConfigurator;
import com.nextep.installer.model.impl.Delivery;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.ILoggingService;
import com.nextep.installer.services.impl.AdminService;
import com.nextep.installer.services.impl.LoggingService;
import com.nextep.installer.test.configuration.TestConfiguration;

public class TargetUserRequirementTest {

	@Before
	public void setUp() throws Exception {
		NextepInstaller.registerService(ILoggingService.class, new LoggingService());
		NextepInstaller.registerService(IAdminService.class, new AdminService());
	}

	@Test
	public void testCheckRequirement() throws InstallerException {
		IRequirement req = new TargetUserRequirement();
		IInstallConfigurator configurator = new DefaultInstallerConfigurator();
		// Initializing test config
		IDatabaseTarget target = TestConfiguration.getTestTarget(DBVendor.MYSQL);

		// Checking failure for no configuration
		boolean fail = false;
		try {
			fail = !req.checkRequirement(configurator).isSuccess();
		} catch (InstallerException e) {
			fail = true;
		}
		Assert.assertTrue("Requirement passed with no configuration", fail);
		configurator.setOption(InstallerOption.USER, target.getUser());
		configurator.setOption(InstallerOption.PASSWORD, target.getPassword());
		fail = false;
		try {
			fail = !req.checkRequirement(configurator).isSuccess();
		} catch (InstallerException e) {
			fail = true;
		}
		Assert.assertTrue("Requirement passed with user only", fail);
		configurator.setOption(InstallerOption.DATABASE, target.getDatabase());
		fail = false;
		try {
			fail = !req.checkRequirement(configurator).isSuccess();
		} catch (InstallerException e) {
			fail = true;
		}
		Assert.assertTrue("Requirement passed with user & database", fail);
		configurator.setOption(InstallerOption.VENDOR, target.getVendor().getName());
		fail = false;
		fail = !req.checkRequirement(configurator).isSuccess();
		Assert
				.assertFalse("Requirement with user, password, database and vendor should work",
						fail);
		Assert.assertNotNull("Target database should be filled", configurator.getTarget());
		Assert.assertNotNull("Target connection should be set", configurator.getTargetConnection());
		IDatabaseTarget reqTarget = configurator.getTarget();
		Assert.assertEquals("Database target user should match input", target.getUser(), reqTarget
				.getUser());
		Assert.assertEquals("Database target password should match input", target.getPassword(),
				reqTarget.getPassword());
		Assert.assertEquals("Database target database should match input", target.getDatabase(),
				reqTarget.getDatabase());
		Assert.assertEquals("Database target host should match 127.0.0.1", "127.0.0.1", reqTarget
				.getHost());
		Assert.assertEquals("Database target port should match 3306", "3306", reqTarget.getPort());
		// Testing JDBC delivery
		IDelivery dlv = new Delivery(false);
		dlv.setDBVendor(DBVendor.JDBC);
		configurator.setDelivery(dlv);
		fail = !req.checkRequirement(configurator).isSuccess();
		Assert.assertFalse("JDBC delivery with explicit vendor should be OK", fail);
		// Now checking failure
		configurator.setOption(InstallerOption.VENDOR, null);
		// Checking failure (no explicit vendor)
		fail = false;
		try {
			fail = !req.checkRequirement(configurator).isSuccess();
		} catch (InstallerException e) {
			fail = true;
		}
		Assert.assertTrue("JDBC delivery with no explicit vendor should fail", fail);

		// Checking delivery vendor
		dlv.setDBVendor(DBVendor.MYSQL);
		IStatus status = req.checkRequirement(configurator);
		Assert.assertTrue("MYSQL delivery with no explicit vendor should be OK, was: "
				+ status.getMessage(), status.isSuccess());
	}
}
