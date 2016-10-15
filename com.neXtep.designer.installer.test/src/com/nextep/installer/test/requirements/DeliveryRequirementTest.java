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
import com.nextep.installer.impl.req.DeliveryRequirement;
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

public class DeliveryRequirementTest {

	@Before
	public void setUp() throws Exception {
		NextepInstaller.registerService(ILoggingService.class, new LoggingService());
		NextepInstaller.registerService(IAdminService.class, new AdminService());
	}

	@Test
	public void testCheckRequirement() throws InstallerException {
		IInstallConfigurator configurator = new DefaultInstallerConfigurator();
		final String dlvPath = TestConfiguration.getProperty(TestConfiguration.PROP_DELIVERY_PATH);
		configurator.setDeliveryPath(dlvPath);
		IRequirement req = new DeliveryRequirement();
		IStatus status = req.checkRequirement(configurator);
		Assert.assertTrue("Delivery should be OK but was: " + status.getMessage(), status
				.isSuccess());
		Assert.assertEquals("Delivery should be the one specifed", configurator.getDeliveryPath(),
				dlvPath);
		Assert.assertNotNull("Delivery should be set in configuration", configurator.getDelivery());

		configurator = new DefaultInstallerConfigurator();
		configurator.setDeliveryPath(dlvPath);
		String home = TestConfiguration.getProperty(TestConfiguration.PROP_NXTP_HOME);
		configurator.setNextepHome(home);
		configurator.defineOption(InstallerOption.INSTALL);
		status = req.checkRequirement(configurator);
		Assert.assertTrue("Delivery should be OK but was: " + status.getMessage(), status
				.isSuccess());
		Assert.assertNotSame("Delivery should be the ADMIN", configurator.getDeliveryPath(),
				dlvPath);
		Assert.assertNotNull("Delivery should be set in configuration", configurator.getDelivery());

	}
}
