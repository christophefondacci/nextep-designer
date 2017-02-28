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
package com.nextep.installer.test.services;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.factories.InstallerFactory;
import com.nextep.installer.impl.req.NextepAdminRequirement;
import com.nextep.installer.impl.req.TargetUserRequirement;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.ICheck;
import com.nextep.installer.model.IDBObject;
import com.nextep.installer.model.IDatabaseObjectCheck;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.impl.DBObject;
import com.nextep.installer.model.impl.DatabaseTarget;
import com.nextep.installer.model.impl.DefaultInstallerConfigurator;
import com.nextep.installer.model.impl.Delivery;
import com.nextep.installer.model.impl.Release;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.IInstallerService;
import com.nextep.installer.services.ILoggingService;
import com.nextep.installer.services.impl.AdminService;
import com.nextep.installer.services.impl.InstallerService;
import com.nextep.installer.services.impl.LoggingService;
import com.nextep.installer.test.configuration.TestConfiguration;

public class AdminServiceTest {

	static IInstallConfigurator configurator;
	private long releaseId1, releaseId2;

	@BeforeClass
	public static void setUp() throws InstallerException {
		// Registering services
		NextepInstaller.registerService(ILoggingService.class, new LoggingService());
		NextepInstaller.registerService(IAdminService.class, new AdminService());
		NextepInstaller.registerService(IInstallerService.class, new InstallerService());
		// Configuring installer to deploy the admin
		IInstallConfigurator installConf = new DefaultInstallerConfigurator();
		installConf.setNextepHome(TestConfiguration.getProperty(TestConfiguration.PROP_NXTP_HOME));
		installConf.defineOption(InstallerOption.INSTALL);
		// Admin needs to be deployed on our test target
		IDatabaseTarget target = TestConfiguration.getTestTarget(DBVendor.MYSQL);
		installConf.setOption(InstallerOption.USER, target.getUser());
		installConf.setOption(InstallerOption.PASSWORD, target.getPassword());
		installConf.setOption(InstallerOption.DATABASE, target.getDatabase());
		installConf.setOption(InstallerOption.HOST, target.getHost());
		installConf.setOption(InstallerOption.PORT, target.getPort());
		installConf.setOption(InstallerOption.VENDOR, target.getVendor().getName());
		// Deploying
		NextepInstaller.getService(IInstallerService.class).install(installConf, ".");

		// Preparing configurator
		configurator = new DefaultInstallerConfigurator(installConf);
		List<IRequirement> reqs = NextepInstaller.buildStandaloneRequirements();
		for (IRequirement r : reqs) {
			r.checkRequirement(installConf);
		}
	}

	@Test
	public void testStandaloneAdmin() throws InstallerException {
		IDatabaseTarget target = TestConfiguration.getTestTarget(DBVendor.MYSQL);

		IInstallConfigurator configurator = new DefaultInstallerConfigurator();
		configurator.setProperty(NextepAdminRequirement.PROP_ADMIN_USER, target.getUser());
		configurator.setProperty(NextepAdminRequirement.PROP_ADMIN_PASS, target.getPassword());
		configurator.setProperty(NextepAdminRequirement.PROP_ADMIN_DBID, target.getDatabase());
		configurator.setProperty(NextepAdminRequirement.PROP_ADMIN_SERVER, target.getHost());
		configurator.setProperty(NextepAdminRequirement.PROP_ADMIN_PORT, target.getPort());
		configurator.setProperty(NextepAdminRequirement.PROP_ADMIN_VENDOR, target.getVendor()
				.getName());

		// Building fake target
		IDatabaseTarget fakeTarget = new DatabaseTarget("invalidTarget", "password", "db",
				"127.0.0.1", "3306", DBVendor.MYSQL, null);
		configurator.setTarget(fakeTarget);
		List<IRequirement> reqs = new ArrayList<IRequirement>();
		reqs.add(new NextepAdminRequirement());
		NextepInstaller.getService(IInstallerService.class).checkRequirements(configurator, reqs);
	}

	@Test
	public void testInstallRelease() throws InstallerException {
		IAdminService service = NextepInstaller.getService(IAdminService.class);
		IDelivery dlv = new Delivery(false);
		dlv.setRefUID(1234);
		dlv.setName("Test");
		dlv.setFromRelease(null);
		IRelease rel1 = new Release(1, 0, 0, 1, 1);
		dlv.setRelease(rel1);
		dlv.setDBVendor(DBVendor.JDBC);
		configurator.setDelivery(dlv);
		service.installRelease(configurator, dlv, true);
		releaseId1 = rel1.getId();
		IRelease rel2 = new Release(1, 0, 0, 2, 2);
		dlv.setRelease(rel2);
		IDatabaseObjectCheck check = InstallerFactory.buildDatabaseObjectCheckerFor(DBVendor.MYSQL);
		check.addObject(new DBObject("TABLE", "NADM_MODULES"));
		dlv.addCheck(check);
		service.installRelease(configurator, dlv, true);
		releaseId2 = rel2.getId();
	}

	@Test
	public void testCheck() {
		IAdminService service = NextepInstaller.getService(IAdminService.class);
		IDelivery dlv = new Delivery(false);
		dlv.setRefUID(1234);
		IDatabaseObjectCheck check = InstallerFactory.buildDatabaseObjectCheckerFor(DBVendor.MYSQL);
		// This check should work as the table is already in the admin schema
		check.addObject(new DBObject("TABLE", "NADM_INSTALLED_RELEASES"));
		dlv.addCheck(check);
		boolean result = service.check(configurator, dlv);
		Assert.assertTrue("Check of db table failed", result);
		// Now adding a check which should fail
		check.addObject(new DBObject("TABLE", "NON_EXISTING_TABLE"));
		result = service.check(configurator, dlv);
		Assert.assertFalse("Check of db table succeed with non existing table", result);
	}

	@Test
	public void testCheckAll() throws InstallerException {
		IAdminService service = NextepInstaller.getService(IAdminService.class);
		service.checkAll(configurator, false);
	}

	@Test
	public void testGetRelease() throws InstallerException {
		IAdminService service = NextepInstaller.getService(IAdminService.class);
		// Checking explicit connection
		IRelease rel1 = service.getRelease(configurator, configurator.getAdminConnection(), 1234,
				true);
		// Checking implicit connection
		IRelease rel2 = service.getRelease(configurator, 1234, true);
		// Checking explicit delivery
		IDelivery dlv = new Delivery(false);
		dlv.setRefUID(1234);
		IRelease rel3 = service.getRelease(configurator, dlv);
		// Admin = target so release should be retrieved whatever the database & user is
		IRelease rel4, rel5;
		IInstallConfigurator newConfig = new DefaultInstallerConfigurator(configurator);
		newConfig.setOption(InstallerOption.USER, "root");
		newConfig.setOption(InstallerOption.PASSWORD,
				TestConfiguration.getProperty(TestConfiguration.PROP_ROOT_PASSWORD));
		// Installing new user configuration
		new TargetUserRequirement().checkRequirement(newConfig);
		rel4 = service.getRelease(newConfig, dlv);

		// Now simulating standalon admin
		newConfig.setAdminInTarget(false);
		rel5 = service.getRelease(newConfig, dlv);
		Assert.assertEquals("getRelease differs when calling overriden method", rel1, rel2);
		Assert.assertEquals("getRelease differs when calling overriden method", rel2, rel3);
		Assert.assertEquals("getRelease differs when called with different user", rel3, rel4);
		Assert.assertNull(
				"getRelease returned a release for a non installed user (standalone emulation)",
				rel5);
	}

	@Test
	public void testShowInstalledReleases() throws InstallerException {
		IAdminService service = NextepInstaller.getService(IAdminService.class);
		service.showInstalledReleases(configurator);
	}

	@Test
	public void testGetReleaseCheck() throws InstallerException {
		IAdminService service = NextepInstaller.getService(IAdminService.class);
		IDelivery dlv = new Delivery(false);
		dlv.setRefUID(1234);
		configurator.setDelivery(dlv);
		IRelease r = service.getRelease(configurator, dlv);
		ICheck check = service.getReleaseCheck(configurator, r);
		if (!(check instanceof IDatabaseObjectCheck)) {
			Assert.fail("Delivery check needs to be a IDatabaseObjectCheck");
		}
		final IDatabaseObjectCheck dbCheck = (IDatabaseObjectCheck) check;
		// Checking that we have only one check as TABLE / NADM_MODULES
		boolean passed = true;
		for (IDBObject o : dbCheck.getObjectsToCheck()) {
			if ("TABLE".equals(o.getType()) && "NADM_MODULES".equals(o.getName()) && passed) {
				passed = true;
			} else {
				passed = false;
			}
		}
		Assert.assertTrue("Installed release expected to have one check for TABLE / NADM_MODULES",
				passed);
	}

}
