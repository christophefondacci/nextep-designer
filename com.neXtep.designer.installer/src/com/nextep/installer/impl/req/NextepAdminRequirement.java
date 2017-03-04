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
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.helpers.Assert;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.base.AbstractJDBCRequirement;
import com.nextep.installer.model.impl.DatabaseTarget;
import com.nextep.installer.model.impl.Delivery;
import com.nextep.installer.model.impl.Status;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.ILoggingService;

/**
 * This class implements requirements checks for the neXtep admin database connection.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class NextepAdminRequirement extends AbstractJDBCRequirement {

	public static final String PROP_ADMIN_USER = "admin.nextep.user"; //$NON-NLS-1$
	public static final String PROP_ADMIN_PASS = "admin.nextep.password"; //$NON-NLS-1$
	public static final String PROP_ADMIN_DBID = "admin.nextep.SID"; //$NON-NLS-1$
	public static final String PROP_ADMIN_VENDOR = "admin.nextep.vendor"; //$NON-NLS-1$
	public static final String PROP_ADMIN_BIN = "admin.nextep.bin.location"; //$NON-NLS-1$
	public static final String PROP_ADMIN_SERVER = "admin.nextep.server"; //$NON-NLS-1$
	public static final String PROP_ADMIN_PORT = "admin.nextep.port"; //$NON-NLS-1$
	public static final String PROP_ADMIN_TNS = "admin.nextep.tnsname"; //$NON-NLS-1$

	private static final String DEFAULT_SERVER = "127.0.0.1"; //$NON-NLS-1$

	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		final ILoggingService logger = NextepInstaller.getService(ILoggingService.class);
		try {
			// Initializing admin.properties files
			loadProperties(configurator);
			// Retrieving target database information
			final IDatabaseTarget target = configurator.getTarget();
			// If it is defined, we try to determine whether the admin component is present there
			if (target != null) {
				try {
					if (checkTargetIsAdmin(configurator, target)) {
						configurator.setAdminInTarget(true);
						return new Status(true, target.toString());
					}
				} catch (InstallerException e) {
					// Only displaying error in verbose mode
					if (configurator.isOptionDefined(InstallerOption.VERBOSE)) {
						logger.error("User target connection failed while checking for admin, "
								+ "properties-based admin will be checked: " + e.getMessage(), e);
					}
				}
			}
			// If ADMIN is not in the target database, we fallback on properties
			final IDatabaseTarget adminTarget = buildAdminTarget(configurator);
			boolean isAdmin = false;
			if (adminTarget != null) {
				/*
				 * Because we check admin release in the admin database we say that admin is in
				 * target to avoid considering target user / database while fetching release.
				 */
				configurator.setAdminInTarget(true);
				isAdmin = checkTargetIsAdmin(configurator, adminTarget);
			}
			// At this point, admin is not in the target DB
			configurator.setAdminInTarget(false);
			if (isAdmin) {
				/*
				 * If we are in install mode with an external admin database, we consider it as the
				 * target database of our deployment.
				 */
				if (configurator.isOptionDefined(InstallerOption.INSTALL)) {
					configurator.setTarget(adminTarget);
					configurator.setTargetConnection(configurator.getAdminConnection());
				}
				return new Status(true, adminTarget.toString());
			} else {
				return new Status(false, "No neXtep admin database found, try the -install option"); //$NON-NLS-1$
			}
		} catch (InstallerException e) {
			throw new InstallerException("Could not connect to admin database: " + e.getMessage(),
					e);
		}
	}

	private boolean checkTargetIsAdmin(IInstallConfigurator configurator, IDatabaseTarget target)
			throws InstallerException {
		// First, we try to connect
		Connection targetConnection = getConnectionFor(target);
		// If we are in admin INSTALL mode, connection is enough
		if (configurator.isOptionDefined(InstallerOption.INSTALL)
				|| configurator.isOptionDefined(InstallerOption.FULL_INSTALL)) {
			configurator.setAdminConnection(targetConnection);
		} else {
			// Otherwise we check whether the ADMIN database component is present there
			boolean isAdmin = checkAdmin(configurator, targetConnection);
			if (isAdmin) {
				configurator.setAdminConnection(targetConnection);
			}
		}
		// If admin connection is set we're done
		if (configurator.getAdminConnection() != null) {
			return true;
		} else {
			if (targetConnection != null) {
				try {
					targetConnection.close();
				} catch (SQLException e) {
					throw new InstallerException("Unable to close connection: " + e.getMessage(), e);
				}
			}
			return false;
		}
	}

	private boolean checkAdmin(IInstallConfiguration configuration, Connection connection) {
		IDelivery dlv = new Delivery(true);
		dlv.setRefUID(99999999999L);
		final IAdminService adminService = NextepInstaller.getService(IAdminService.class);
		try {
			IRelease repRelease = adminService.getRelease(configuration, connection,
					dlv.getRefUID(), true);
			return repRelease != null;
		} catch (InstallerException e) {
			return false;
		}
	}

	private void loadProperties(IInstallConfigurator configurator) throws InstallerException {
		final ILoggingService log = NextepInstaller.getService(ILoggingService.class);

		// Our property file
		File propFile = new File(configurator.getNextepHome() + File.separator + "properties" //$NON-NLS-1$
				+ File.separator + "neXtep.properties"); //$NON-NLS-1$
		// If it does not exist, we quit
		if (!propFile.exists()) {
			if (configurator.isOptionDefined(InstallerOption.VERBOSE)) {
				log.log("Warning: No admin.properties file found in: " + propFile.getAbsolutePath());
			}
			return;
		}
		// Reading properties
		Properties nextepProperties = new Properties();
		FileInputStream propIS = null;
		try {
			propIS = new FileInputStream(propFile);
			nextepProperties.load(propIS);
		} catch (IOException e) {
			throw new InstallerException("Error while reading neXtep properties file: "
					+ propFile.getAbsolutePath(), e);
		} finally {
			if (propIS != null) {
				try {
					propIS.close();
				} catch (IOException e) {
					throw new InstallerException("Problems while closing properties file stream: "
							+ e.getMessage(), e);
				}
			}
		}
		// Loading properties into the configurator
		for (Object key : nextepProperties.keySet()) {
			configurator.setProperty((String) key, nextepProperties.getProperty((String) key));
		}

		return;
	}

	private IDatabaseTarget buildAdminTarget(IInstallConfigurator nextepProperties)
			throws InstallerException {
		String adminLogin = nextepProperties.getProperty(PROP_ADMIN_USER);
		Assert.notNull(adminLogin, "Admin database login not defined in properties"); //$NON-NLS-1$

		String adminPassword = nextepProperties.getProperty(PROP_ADMIN_PASS);

		String adminSID = nextepProperties.getProperty(PROP_ADMIN_DBID);
		Assert.notNull(adminSID, "Admin database id not defined in properties"); //$NON-NLS-1$

		String serviceName = nextepProperties.getProperty(PROP_ADMIN_TNS);

		String adminVendor = nextepProperties.getProperty(PROP_ADMIN_VENDOR);
		Assert.notNull(adminVendor, "Admin database vendor not defined in properties"); //$NON-NLS-1$
		// Checking vendor
		DBVendor vendor = DBVendor.valueOf(adminVendor);

		String server = nextepProperties.getProperty(PROP_ADMIN_SERVER);
		if (server == null) {
			server = DEFAULT_SERVER;
		}

		String port = nextepProperties.getProperty(PROP_ADMIN_PORT);
		if (port == null) {
			port = String.valueOf(vendor.getDefaultPort());
		}

		IDatabaseTarget target = new DatabaseTarget(adminLogin, adminPassword, adminSID, server,
				port, vendor, serviceName);
		return target;
	}

	public String getName() {
		return "neXtep admin database"; //$NON-NLS-1$
	}

}
