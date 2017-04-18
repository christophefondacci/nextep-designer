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
package com.nextep.installer.services.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.nextep.installer.InstallerMessages;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.handlers.DeployHandlerManager;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.impl.req.DeliveryRequirement;
import com.nextep.installer.impl.req.NextepAdminRequirement;
import com.nextep.installer.impl.req.TargetUserRequirement;
import com.nextep.installer.impl.req.TargetVendorRequirement;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IArtefact;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IDeployHandler;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.IRequiredDelivery;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.impl.DefaultInstallerConfigurator;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.IConnectionService;
import com.nextep.installer.services.IInstallerService;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Christophe Fondacci
 */
public class InstallerService implements IInstallerService {

	public boolean install(IInstallConfigurator configurator, String deliveryLocation,
			List<IRequirement> requirements) throws InstallerException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final IAdminService adminService = ServicesHelper.getAdminService();

		configurator.setDeliveryPath(deliveryLocation);
		// Checking requirements
		checkRequirements(configurator, requirements);

		// Prompt delivery info
		final IDelivery delivery = configurator.getDelivery();
		final IDatabaseTarget target = configurator.getTarget();
		final DBVendor vendor = target.getVendor();

		logger.log(""); //$NON-NLS-1$
		logger.log(MessageFormat.format(
				InstallerMessages.getString("service.installer.deployModuleRelease"), //$NON-NLS-1$
				delivery.getName(), delivery.getRelease(), vendor.getLabel()));
		// Do we need to do anything ?
		if (!needInstall(configurator, delivery)) {
			boolean failed = false;
			if (!configurator.isOptionDefined(InstallerOption.FULL_INSTALL)) {
				failed = !adminService.checkAll(configurator, false);
			}
			logger.log(""); //$NON-NLS-1$
			if (!failed) {
				logger.log(InstallerMessages.getString("service.installer.releaseUpToDate")); //$NON-NLS-1$
			} else {
				logger.log(InstallerMessages
						.getString("service.installer.releaseUpToDateWithErrors")); //$NON-NLS-1$
			}
			return !failed;
		}
		// Displaying currently installed modules if not in INSTALL mode
		// (because that would not
		// be available yet)
		if (!isInstallMode(configurator)) {
			logger.log(""); //$NON-NLS-1$
			adminService.showInstalledReleases(configurator);
		}

		// Deploying
		logger.log(""); //$NON-NLS-1$
		logger.getMonitor().start(
				MessageFormat.format(InstallerMessages.getString("service.installer.installing"), //$NON-NLS-1$
						delivery.getName()), 5);

		// Checking required deliveries
		final List<IRequiredDelivery> requiredDeliveries = configurator.getDelivery()
				.getRequiredDeliveries();
		for (IRequiredDelivery dlv : requiredDeliveries) {
			try {
				logger.pad();
				// We copy the configurator so we make sure required deliveries
				// will not interfere
				// with us
				IInstallConfigurator subConfigurator = copyConfigurator(configurator);
				// We only need to check the delivery of this "required" package
				// as all other
				// checks have been done (we deploy to the same target, with
				// same admin, etc.)
				List<IRequirement> subRequirements = new ArrayList<IRequirement>();
				subRequirements.add(new DeliveryRequirement());
				// Recursively fires installation of the sub module
				install(subConfigurator, (deliveryLocation == null ? "" : deliveryLocation //$NON-NLS-1$
						+ File.separator) + "requirements" + File.separator + dlv.getName(), //$NON-NLS-1$
						subRequirements);
			} finally {
				logger.unpad();
			}
		}
		// Deploying current install configuration
		return deploy(configurator);
	}

	public boolean install(IInstallConfigurator configurator, String deliveryLocation)
			throws InstallerException {
		List<IRequirement> requirements = new ArrayList<IRequirement>();
		requirements.add(new DeliveryRequirement());
		requirements.add(new TargetUserRequirement());
		requirements.add(new NextepAdminRequirement());
		requirements.add(new TargetVendorRequirement());
		return install(configurator, deliveryLocation, requirements);
	}

	public IInstallConfigurator copyConfigurator(IInstallConfiguration configurator) {
		return new DefaultInstallerConfigurator(configurator);
	}

	/**
	 * Checks all specified requirement
	 * 
	 * @param configurator the installer configurator
	 * @param requirements requirements to check
	 * @throws InstallerException when requirements fail
	 */
	public void checkRequirements(IInstallConfigurator configurator, List<IRequirement> requirements)
			throws InstallerException {
		final ILoggingService logger = ServicesHelper.getLoggingService();

		logger.log(""); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("service.installer.configCheck")); //$NON-NLS-1$
		logger.pad();
		try {
			for (IRequirement requirement : requirements) {
				try {
					logger.out(requirement.getName(), IRequirement.PADDING);
					IStatus status = requirement.checkRequirement(configurator);
					if (!status.isSuccess()) {
						if (status.getMessage() != null) {
							logger.log(MessageFormat.format(InstallerMessages
									.getString("service.installer.failureWithMessage"), status //$NON-NLS-1$
									.getMessage()));
						} else {
							logger.log(InstallerMessages.getString("service.installer.checkFail")); //$NON-NLS-1$
						}
						logger.log(""); //$NON-NLS-1$
						throw new InstallerException(
								InstallerMessages
										.getString("service.installer.missingRequirements") //$NON-NLS-1$
										+ (status.getMessage() == null ? "." : ": " //$NON-NLS-1$ //$NON-NLS-2$
												+ status.getMessage()));
					} else {
						if (status.getMessage() != null) {
							logger.log(MessageFormat.format(InstallerMessages
									.getString("service.installer.successWithMessage"), status //$NON-NLS-1$
									.getMessage()));
						} else {
							logger.log(InstallerMessages.getString("service.installer.success")); //$NON-NLS-1$
						}
					}
				} catch (InstallerException e) {
					logger.log(InstallerMessages.getString("service.installer.failure")); //$NON-NLS-1$
					throw e;
				}
			}
		} finally {
			logger.unpad();
		}
	}

	/**
	 * Deploys the specified delivery to the specified database server.
	 * 
	 * @param delivery delivery to deploy
	 * @param user database username
	 * @param password database password
	 * @param SID database sid
	 * @param server database server (host name or IP)
	 * @param vendor used for generic-jdbc deployments
	 * @throws DeployException whenever the deployment failed for any reason.
	 */
	private boolean deploy(IInstallConfiguration configuration) throws InstallerException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final IAdminService adminService = ServicesHelper.getAdminService();

		// Getting information from configuration
		final IDelivery delivery = configuration.getDelivery();
		final IDatabaseTarget target = configuration.getTarget();
		final DBVendor vendor = target.getVendor();

		try {
			// Retrieving delivery sequence
			logger.out(InstallerMessages.getString("service.installer.dependencyCheck"), 24); //$NON-NLS-1$
			List<IDelivery> deliveries = getInstallDeliveriesSequence(configuration, delivery);
			// Checking we have something to do
			if (deliveries == null || deliveries.size() == 0) {
				logger.log(InstallerMessages.getString("service.installer.nothingToDo")); //$NON-NLS-1$
				return true;
			}
			logger.log(InstallerMessages.getString("service.installer.OK")); //$NON-NLS-1$
			// Checking structure
			// checkStructure(user,password);
			// User should confirm the deploy set
			// confirmDeploySequence(deliveries, user, SID);

			// Deploying...
			for (IDelivery d : deliveries) {

				logger.log(""); //$NON-NLS-1$
				logger.log(MessageFormat.format(
						InstallerMessages.getString("service.installer.upgrading"), //$NON-NLS-1$
						d.getName(),
						(d.getFromRelease() == null ? InstallerMessages
								.getString("service.installer.voidRelease") : "[" //$NON-NLS-1$ //$NON-NLS-2$
								+ d.getFromRelease().toString() + "]"), d.getRelease().toString())); //$NON-NLS-1$
				logger.getMonitor().work(
						MessageFormat.format(
								InstallerMessages.getString("service.installer.verifyingModule"), //$NON-NLS-1$
								d.getName()));
				if (!d.isAdmin()) {
					checkStructure(configuration);
				}
				logger.pad();
				try {
					logger.getMonitor().work(
							MessageFormat.format(InstallerMessages
									.getString("service.installer.upgradingModule"), d.getName(), d //$NON-NLS-1$
									.getRelease().toString()));
					boolean isDeployed = deployArtefacts(configuration, d, vendor);
					// If nothing deployed, we fall back on JDBC deployment
					if (!isDeployed) {
						isDeployed = deployArtefacts(configuration, d, DBVendor.JDBC);
						// Backward compatibility for deliveries without any
						// explicit vendor specified
						if (!isDeployed) {
							deployArtefacts(configuration, d, null);
						}
					}
				} finally {
					logger.unpad();
				}
			}
			// Checking release
			logger.log(""); //$NON-NLS-1$
			logger.out(InstallerMessages.getString("service.installer.structureCheck"), 21); //$NON-NLS-1$
			logger.getMonitor().work(
					MessageFormat.format(
							InstallerMessages.getString("service.installer.structureCheckRelease"), //$NON-NLS-1$
							delivery.getName(), delivery.getRelease().toString()));
			boolean ok = adminService.check(configuration, delivery);
			if (ok) {
				logger.log(InstallerMessages.getString("service.installer.OK")); //$NON-NLS-1$
			} else {
				logger.log(InstallerMessages.getString("service.installer.failure")); //$NON-NLS-1$
				// logger.out(MessageFormat.format(
				//						InstallerMessages.getString("service.installer.installFailForceQuestion"), delivery //$NON-NLS-1$
				// .getName(), delivery.getRelease().toString()));
				//				String userInput = NextepInstaller.getUserInput("force"); //$NON-NLS-1$
				//				if (!"FORCE".equalsIgnoreCase(userInput)) { //$NON-NLS-1$
				// // Registering the "error" release
				// adminService.installRelease(configuration, delivery, false);
				// adminService.checkAll(configuration, false);
				// throw new DeployException(
				//							InstallerMessages.getString("service.installer.failStructureCheck")); //$NON-NLS-1$
				// }
			}

			logger.out(MessageFormat.format(
					InstallerMessages.getString("service.installer.registeringRelease"), //$NON-NLS-1$
					delivery.getName(), delivery.getRelease()));
			logger.getMonitor()
					.work(MessageFormat.format(
							InstallerMessages.getString("service.installer.registeringReleaseTask"), //$NON-NLS-1$
							delivery.getName(), delivery.getRelease().toString()));
			adminService.installRelease(configuration, delivery, true);
			logger.log(InstallerMessages.getString("service.installer.OK")); //$NON-NLS-1$
			logger.log(""); //$NON-NLS-1$

			return adminService.checkAll(configuration, false);

		} catch (SQLException e) {
			logger.error("SQL Exception:"); //$NON-NLS-1$
			e.printStackTrace();
			return false;
		}
	}

	private boolean deployArtefacts(IInstallConfiguration configuration, IDelivery d,
			DBVendor vendor) throws DeployException {
		boolean isDeployed = false;
		for (IArtefact a : d.getArtefacts()) {
			// We process artefacts :
			// - defined for the specified vendor
			// - with no explicit vendor ONLY when another vendor-specific
			// artefact has already been
			// processed
			// This is required for the JDBC fallback to work
			if (a.getDBVendor() == vendor || (a.getDBVendor() == null && isDeployed)) {
				IDeployHandler handler = DeployHandlerManager.getDeployHandler(d.getDBVendor(), d,
						a.getType());
				handler.deploy(configuration, a);
				isDeployed = true;
			}
		}
		return isDeployed;
	}

	/**
	 * Builds the sequence of deliveries to apply from dependencies
	 * 
	 * @param delivery delivery to process
	 * @param user owner of the target database schema
	 * @return a list of IDelivery to deploy
	 * @throws DeployException
	 */
	private List<IDelivery> getInstallDeliveriesSequence(IInstallConfiguration configuration,
			IDelivery delivery) throws InstallerException {
		final IAdminService adminService = ServicesHelper.getAdminService();

		// List of deliveries to install
		List<IDelivery> deliveries = new ArrayList<IDelivery>();
		// Retrieving current release
		IRelease currentRelease = null;
		try {
			currentRelease = adminService.getRelease(configuration, delivery); // (IRelease)
			// executeDatabase(new GetReleaseCommand(delivery, user,database));
		} catch (InstallerException e) {
			// We might have this exception when admin schema has not been
			// initialized
			if (isInstallMode(configuration) && delivery.getFromRelease() == null) {
				deliveries.add(delivery);
				return deliveries;
			} else {
				throw new DeployException(
						InstallerMessages.getString("service.installer.getReleaseFail"), e); //$NON-NLS-1$
			}
		}
		if (currentRelease == null && delivery.getFromRelease() != null) {
			// Checking if first deployment
			if (delivery.isFirstRelease()) {
				// NextepInstaller.out("First deployment, initializing first release...");
				// new InstallInitialReleaseCommand(delivery,user,database,true,false);
				currentRelease = delivery.getFromRelease();
				// NextepInstaller.log("OK.");
			} else {
				// Looking for a dependency which could start from scratch
				boolean dependenciesCanUpgrade = false;
				for (IDelivery dependentDlv : delivery.getDependencies()) {
					if (dependentDlv.getRefUID() == delivery.getRefUID()
							&& dependentDlv.getFromRelease() == null) {
						dependenciesCanUpgrade = true;
					}
				}
				if (!dependenciesCanUpgrade) {
					throw new DeployException(
							InstallerMessages.getString("service.installer.needFullDelivery")); //$NON-NLS-1$
				}
			}
		}
		if (currentRelease != null) {
			if (currentRelease.compareTo(delivery.getFromRelease()) < 0) {
				throw new DeployException(MessageFormat.format(
						InstallerMessages.getString("service.installer.minimumReleaseFail"), //$NON-NLS-1$
						currentRelease.toString(), delivery.getFromRelease().toString()));
			} else if (currentRelease.compareTo(delivery.getRelease()) >= 0) {
				return Collections.emptyList();
			}
			// If we have a strict release policy, we must have an exact
			// release match of the fromRelease
			if (delivery.checkRange()) {
				if (currentRelease.compareTo(delivery.getFromRelease()) != 0) {
					throw new DeployException(MessageFormat.format(
							InstallerMessages.getString("service.installer.cannotUpgrade"), //$NON-NLS-1$
							currentRelease.toString()));
				}
			}
		}

		try {
			for (IDelivery depDelivery : delivery.getDependencies()) {
				IRelease delvRelease = adminService.getRelease(configuration, depDelivery); // (IRelease)
				// executeDatabase(new GetReleaseCommand(depDelivery, user, database));
				if (depDelivery.getRelease().compareTo(delvRelease) > 0) {
					if (depDelivery.getArtefacts().size() > 0) {
						deliveries.add(depDelivery);
					} else {
						throw new DeployException(MessageFormat.format(
								InstallerMessages.getString("service.installer.missingDependency"), //$NON-NLS-1$
								depDelivery.getName(), depDelivery.getRelease().toString()));
					}
				}
			}
		} catch (InstallerException e) {
			e.printStackTrace();
			throw new DeployException(
					InstallerMessages.getString("service.installer.getReleaseFail"), e); //$NON-NLS-1$
		}
		deliveries.add(delivery);
		return deliveries;
	}

	/**
	 * Checks and validates the structure of all installed modules. If the validation fails, a
	 * DeployException will be raised unless the [-nocheck] command line argument has been
	 * specified.
	 * 
	 * @param user user of the target database to check
	 * @param password password of the target database to check
	 * @throws SQLException on connection problems with target db
	 * @throws DeployException if the check is unsuccessful
	 */
	private void checkStructure(IInstallConfiguration configuration) throws SQLException,
			InstallerException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final IAdminService adminService = ServicesHelper.getAdminService();

		boolean noCheck = configuration.isOptionDefined(InstallerOption.NOCHECK);
		if (configuration.isOptionDefined(InstallerOption.INSTALL)
				|| configuration.isOptionDefined(InstallerOption.FULL_INSTALL)) {
			return;
		}
		// Checking all installed releases prior to installation (silent)
		logger.out(InstallerMessages.getString("service.installer.structureCheck"), 21); //$NON-NLS-1$
		Boolean b = adminService.checkAll(configuration, true);
		if (!b.booleanValue()) {
			logger.log(noCheck ? InstallerMessages.getString("service.installer.skippedFail") : InstallerMessages.getString("service.installer.failure")); //$NON-NLS-1$ //$NON-NLS-2$
			if (!noCheck) {
				logger.log(InstallerMessages.getString("service.installer.repairMsg")); //$NON-NLS-1$
				throw new DeployException(
						InstallerMessages.getString("service.installer.structureCheckFail")); //$NON-NLS-1$
			}
		} else {
			logger.log(noCheck ? InstallerMessages.getString("service.installer.skippedOk") : InstallerMessages.getString("service.installer.OK")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private boolean isInstallMode(IInstallConfiguration conf) {
		return conf.isOptionDefined(InstallerOption.INSTALL)
				|| conf.isOptionDefined(InstallerOption.FULL_INSTALL);
	}

	private boolean needInstall(IInstallConfiguration conf, IDelivery delivery)
			throws InstallerException {
		final IAdminService adminService = ServicesHelper.getAdminService();
		try {
			IRelease currentRelease = adminService.getRelease(conf, delivery);
			return (currentRelease.compareTo(delivery.getRelease()) < 0);
		} catch (RuntimeException e) {
			return true;
		} catch (InstallerException e) {
			return true;
		}
	}

	public void release(IInstallConfiguration configuration) throws InstallerException {
		Connection targetConnection = configuration.getTargetConnection();
		if (targetConnection != null) {
			try {
				targetConnection.close();
			} catch (SQLException e) {
				throw new InstallerException(MessageFormat.format(InstallerMessages
						.getString("service.installer.targetConnectionReleaseFailure"), e //$NON-NLS-1$
						.getMessage()), e);
			}
		}
		Connection adminConnection = configuration.getAdminConnection();
		if (adminConnection != null) {
			try {
				if (!adminConnection.getAutoCommit()) {
					adminConnection.commit();
				}
				adminConnection.close();
			} catch (SQLException e) {
				throw new InstallerException(MessageFormat.format(InstallerMessages
						.getString("service.installer.adminConnectionReleaseFailure"), e //$NON-NLS-1$
						.getMessage()), e);
			}
		}
	}

	/**
	 * Service injection setter, used when the installer is invoked from neXtep designer IDE through
	 * DS injection. This setter registers the service globally on the {@link NextepInstaller}
	 * static bean for compatibility with standalone mode.
	 * 
	 * @param service logging service implementation
	 */
	public void setLoggingService(ILoggingService service) {
		NextepInstaller.registerService(ILoggingService.class, service);
	}

	/**
	 * Service injection setter, used when the installer is invoked from neXtep designer IDE through
	 * DS injection. This setter registers the service globally on the {@link NextepInstaller}
	 * static bean for compatibility with standalone mode.
	 * 
	 * @param service admin service implementation
	 */
	public void setAdminService(IAdminService service) {
		NextepInstaller.registerService(IAdminService.class, service);
	}

	/**
	 * Service injection setter, used when the installer is invoked from neXtep designer IDE through
	 * DS injection. This setter registers the service globally on the {@link NextepInstaller}
	 * static bean for compatibility with standalone mode.
	 * 
	 * @param service connection service implementation
	 */
	public void setConnectionService(IConnectionService service) {
		NextepInstaller.registerService(IConnectionService.class, service);
	}

}
