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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.headless.batch.model.impl;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.beng.BengPlugin;
import com.nextep.designer.beng.dao.IDeliveryDao;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.services.IDeliveryExportService;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.headless.batch.BatchPlugin;
import com.nextep.designer.headless.batch.model.BatchConstants;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.batch.model.base.AbstractBatchTask;
import com.nextep.designer.headless.exceptions.BatchException;
import com.nextep.designer.repository.services.IRepositoryUpdaterService;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.factories.InstallerFactory;
import com.nextep.installer.impl.req.DeliveryRequirement;
import com.nextep.installer.impl.req.NextepAdminRequirement;
import com.nextep.installer.impl.req.TargetUserRequirement;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.IInstallerService;

/**
 * This batch task can deploy a delivery to a target database.
 * <p>
 * The delivery to deploy is defined through the property {@link BatchConstants#MODULE_ARG}
 * information using the pattern :<br>
 * <code><i>module</i>:<i>versionPrefix</i></code><br>
 * allowing callers to specify version ranges. For example, consider a module <i>dbmod</i> having
 * deliveries for releases 1.0.3.0, 1.0.3.1, 1.0.3.2, 1.0.4.0. Specifying the following pattern :<br>
 * <code>dbmod:1.0.3</code><br>
 * will select deliveries 1.0.3.0, 1.0.3.1 and 1.0.3.2, while :<br>
 * <code>dbmod:1.0</code><br>
 * will select all deliveries (since they all start with 1.0).<br>
 * </p>
 * <p>
 * This task first analyzes the installed release of the module in the target database and computes
 * a delivery chain from this release up to the highest release matching the version prefix pattern
 * and deploys every delivery of this chain.
 * 
 * @author Christophe Fondacci
 */
public class DeployDeliveryTask extends AbstractBatchTask implements IBatchTask {

	private IInstallConfigurator configurator;
	private IInstallerService installerService;

	@Override
	public IStatus execute(IConnection targetConnection, Map<String, String> propertiesMap,
			IProgressMonitor monitor) throws BatchException {
		final IDeliveryExportService exportService = BengPlugin
				.getService(IDeliveryExportService.class);
		final IDeliveryService deliveryService = BengPlugin.getService(IDeliveryService.class);
		final IDeliveryDao deliveryDao = BengPlugin.getService(IDeliveryDao.class);

		String exportDir = propertiesMap.get(BatchConstants.DELIVERY_EXPORT_DIR);
		// We need an export directory
		if (exportDir == null) {
			exportDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		}

		// Retrieving deliveries
		monitor.subTask(BatchMessages.getString("task.deploy.fetchingDeliveries")); //$NON-NLS-1$
		final List<IDeliveryInfo> deliveries = getBatchTaskService().getDeliveries(propertiesMap);
		final String module = getBatchTaskService().getModuleNameFromDeliveries(deliveries);
		if (!deliveries.isEmpty()) {
			monitor.beginTask(
					MessageFormat.format(BatchMessages.getString("task.deploy.deliveriesFound"), //$NON-NLS-1$
							deliveries.size(), module), deliveries.size());
			// Retrieving highest delivery
			Collections.sort(deliveries);
			Collections.reverse(deliveries);
			final IDeliveryInfo highestDelivery = deliveries.iterator().next();
			monitor.subTask(MessageFormat.format(
					BatchMessages.getString("task.deploy.deployTargetReleaseMsg"), //$NON-NLS-1$
					module, highestDelivery.getTargetRelease().getLabel()));

			// Getting current release
			try {
				init(targetConnection, propertiesMap, monitor);
				final IVersionInfo installedRelease = getInstalledRelease(highestDelivery
						.getTargetRelease().getReference());
				monitor.subTask(MessageFormat.format(
						BatchMessages.getString("task.deploy.currentReleaseMsg"), module, //$NON-NLS-1$
						(installedRelease == null ? "[None]" : installedRelease.getLabel()))); //$NON-NLS-1$
				// Computing delivery chain
				List<IDeliveryInfo> deliveriesToDeploy = deliveryService.getDeliveries(
						installedRelease, highestDelivery.getTargetRelease());
				monitor.subTask(MessageFormat.format(
						BatchMessages.getString("task.deploy.deliveryChainResolved"), //$NON-NLS-1$
						(installedRelease == null ? "[None]" : installedRelease.getLabel()), //$NON-NLS-1$
						highestDelivery.getTargetRelease().getLabel()));
				// Listing delivery chain
				int i = 1;
				for (IDeliveryInfo dlvInfo : deliveriesToDeploy) {
					monitor.subTask(MessageFormat.format(BatchMessages.getString("task.deploy.deliverySequence"), i++, (dlvInfo //$NON-NLS-1$
							.getSourceRelease() == null ? BatchMessages.getString("task.deploy.scratch") : dlvInfo.getSourceRelease() //$NON-NLS-1$
							.getLabel()), dlvInfo.getTargetRelease().getLabel()));
				}
				// Deploying the chain
				for (IDeliveryInfo dlvInfo : deliveriesToDeploy) {
					monitor.subTask(MessageFormat.format(
							BatchMessages.getString("task.deploy.loadingDelivery"), //$NON-NLS-1$
							dlvInfo.getName()));
					final IDeliveryModule delivery = deliveryDao.loadModule(dlvInfo);
					try {
						monitor.subTask(MessageFormat.format(
								BatchMessages.getString("task.deploy.exportingDelivery"), //$NON-NLS-1$
								dlvInfo.getName()));
						exportService.exportDelivery(exportDir, delivery, monitor);
						monitor.subTask(MessageFormat.format(
								BatchMessages.getString("task.deploy.deployingDelivery"), //$NON-NLS-1$
								dlvInfo.getName()));
						deploy(exportDir + File.separator + delivery.getName());
						// Freeing memory
						HibernateUtil.getInstance().clearAllSessions();
					} catch (Exception e) {
						return new Status(IStatus.ERROR, BatchPlugin.PLUGIN_ID, e.getMessage(), e);
					}
				}
				release();
			} catch (Exception e) {
				return new Status(IStatus.ERROR, BatchPlugin.PLUGIN_ID, e.getMessage(), e);
			}

		}
		return Status.OK_STATUS;
	}

	/**
	 * Retrieves the installed release of the specified module reference as a {@link IVersionInfo}
	 * bean, thus converting the installer {@link IRelease} to a {@link IVersionInfo}. Note that
	 * branch and status will not be properly initialized during this conversion.
	 * 
	 * @param moduleRef the {@link IReference} of the module to get the release for
	 * @return the installed {@link IVersionInfo} or <code>null</code> if none
	 * @throws InstallerException
	 */
	private IVersionInfo getInstalledRelease(IReference moduleRef) throws InstallerException {
		IAdminService adminService = CorePlugin.getService(IAdminService.class);
		final IRelease release = adminService.getRelease(configurator, moduleRef.getUID().rawId(),
				false);
		if (release != null) {
			final IVersionInfo version = VersionFactory.getUnversionedInfo(moduleRef, null);
			version.setMajorRelease(release.getMajor());
			version.setMinorRelease(release.getMinor());
			version.setIteration(release.getIteration());
			version.setPatch(release.getPatch());
			return version;
		} else {
			return null;
		}
	}

	/**
	 * Initializes the installer service and submits an admin delivery to make sure that installer
	 * database schema is properly configured in the target database
	 * 
	 * @param targetConnection target {@link IConnection} of the installation
	 * @param propertiesMap arguments map
	 * @param monitor the progress monitor
	 * @throws InstallerException whenever we have problems initializing the installer
	 */
	private void init(IConnection targetConnection, Map<String, String> propertiesMap,
			IProgressMonitor monitor) throws InstallerException {
		final IRepositoryUpdaterService updaterService = CorePlugin
				.getService(IRepositoryUpdaterService.class);
		installerService = CorePlugin.getService(IInstallerService.class);
		configurator = InstallerFactory.createConfigurator();
		// Preparing install configuration
		configurator.setOption(InstallerOption.USER, targetConnection.getLogin());
		configurator.setOption(InstallerOption.PASSWORD, targetConnection.getPassword());
		configurator.setOption(InstallerOption.DATABASE, targetConnection.getDatabase());
		configurator.setOption(InstallerOption.HOST, targetConnection.getServerIP());
		configurator.setOption(InstallerOption.PORT, targetConnection.getServerPort());
		configurator.setOption(InstallerOption.VENDOR, targetConnection.getDBVendor().name());
		configurator.defineOption(InstallerOption.VERBOSE);
		configurator.defineOption(InstallerOption.FULL_INSTALL);
		List<IRequirement> requirements = new ArrayList<IRequirement>();
		requirements.add(new TargetUserRequirement());
		requirements.add(new NextepAdminRequirement());
		installerService.checkRequirements(configurator, requirements);

		// First installing neXtep admin delivery
		final String adminDeliveryLocation = updaterService.createNeXtepAdminTempDelivery();

		deploy(adminDeliveryLocation);
	}

	/**
	 * Releases the installer resources.
	 * 
	 * @throws InstallerException
	 */
	private void release() throws InstallerException {
		installerService.release(configurator);
	}

	/**
	 * Deploys the artefact of the specified location to the target database.
	 * 
	 * @param deliveryLoc location of the delivery on the local filesystem
	 * @throws InstallerException
	 */
	private void deploy(String deliveryLoc) throws InstallerException {
		List<IRequirement> req = new ArrayList<IRequirement>();
		req.add(new DeliveryRequirement());
		installerService.install(configurator, deliveryLoc, req);
	}

}
