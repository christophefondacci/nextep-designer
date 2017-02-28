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
package com.nextep.designer.repository.services.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.repository.DeliveryRegistry;
import com.nextep.designer.repository.RepositoryMessages;
import com.nextep.designer.repository.RepositoryStatus;
import com.nextep.designer.repository.exception.NoRepositoryConnectionException;
import com.nextep.designer.repository.exception.NoRepositoryException;
import com.nextep.designer.repository.services.IRepositoryUpdaterService;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.factories.InstallerFactory;
import com.nextep.installer.impl.req.DeliveryRequirement;
import com.nextep.installer.impl.req.NextepAdminRequirement;
import com.nextep.installer.impl.req.TargetUserRequirement;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IInstallerMonitor;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.IInstallerService;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class RepositoryUpdaterService implements IRepositoryUpdaterService {

	private static final Log LOGGER = LogFactory.getLog(RepositoryUpdaterService.class);

	private static final long DESIGNER_REPOSITORY_MODULE_ID = 100000000033l;
	private static final IRelease DESIGNER_REPOSITORY_VERSION = InstallerFactory.buildRelease(1, 0,
			7, 4, 0);

	private IInstallerService installerService;
	private ILoggingService loggingService;
	private IAdminService adminService;

	/**
	 * Returns the release number of the specified module in the specified repository.
	 * 
	 * @param moduleId id of the module for which we need to retrieve the release number
	 * @param repoConnection a connection to the repository
	 * @return a {@link IRelease} object representing the release number of the specified module
	 */
	public IRelease getRelease(long moduleId, IConnection repoConnection)
			throws NoRepositoryConnectionException {
		// Defining target
		final String user = repoConnection.getLogin();
		final String password = repoConnection.getPassword();
		final String database = repoConnection.getDatabase();
		final String host = repoConnection.getServerIP();
		final String port = String.valueOf(repoConnection.getServerPort());
		final String serviceName = repoConnection.getTnsAlias();
		final IDatabaseTarget target = InstallerFactory.createTarget(user, password, database,
				host, port, DBVendor.valueOf(repoConnection.getDBVendor().name()), serviceName);

		// Building installer configurator
		IInstallConfigurator conf = InstallerFactory.createConfigurator();
		conf.setTarget(target);
		conf.setAdminInTarget(true);

		// Preparing database connection
		IDatabaseConnector connector = CorePlugin.getConnectionService().getDatabaseConnector(
				repoConnection.getDBVendor());
		try {
			Connection conn = connector.connect(repoConnection);
			try {
				IRelease currentRel = adminService.getRelease(conf, conn, moduleId, true);
				// Compatibility : assuming 1.0.2.7
				if (currentRel == null) {
					currentRel = InstallerFactory.buildRelease(1, 0, 2, 7, 0);
				}
				return currentRel;

			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			LOGGER.error(e);
			throw new NoRepositoryConnectionException("Unable to connect to repository: "
					+ e.getMessage(), e);
		} catch (InstallerException e) {
			LOGGER.error(e);
			return null;
		}
	}

	@Override
	public RepositoryStatus checkRepository(IConnection repoConn) throws NoRepositoryException,
			NoRepositoryConnectionException {
		IRelease currentRel = getRelease(DESIGNER_REPOSITORY_MODULE_ID, repoConn);
		if (currentRel == null) {
			throw new NoRepositoryException("No installed repository"); //$NON-NLS-1$
		} else {
			final int comparisonResult = currentRel.compareTo(DESIGNER_REPOSITORY_VERSION);
			if (comparisonResult == 0) {
				return RepositoryStatus.OK;
			} else if (comparisonResult < 0) {
				return RepositoryStatus.REPOSITORY_TOO_OLD;
			} else {
				return RepositoryStatus.CLIENT_TOO_OLD;
			}
		}
	}

	@Override
	public void upgrade(final IInstallerMonitor monitor, final IConnection repoConn) {
		final List<String> deliveries = DeliveryRegistry.listDeliveriesForUpgrade(null, null,
				DBVendor.valueOf(repoConn.getDBVendor().name()));
		monitor.mainStart(
				RepositoryMessages.getString("repositoryUpdater.title"), deliveries.size() * 2); //$NON-NLS-1$
		loggingService.setMonitor(monitor);
		// Setting flag
		new Thread(new Runnable() {

			@Override
			public void run() {
				// Otherwise we enter a deadlock (Maybe a RCP bug within startup / splashscreen)
				// UISynchronizer.overrideThread.set(Boolean.TRUE);
				NextepInstaller.printLaunchHeader();
				IInstallConfigurator configurator = InstallerFactory.createConfigurator();
				try {
					// Preparing install configuration
					configurator.setOption(InstallerOption.USER, repoConn.getLogin());
					configurator.setOption(InstallerOption.PASSWORD, repoConn.getPassword());
					configurator.setOption(InstallerOption.DATABASE, repoConn.getDatabase());
					configurator.setOption(InstallerOption.HOST, repoConn.getServerIP());
					configurator.setOption(InstallerOption.PORT, repoConn.getServerPort());
					configurator.setOption(InstallerOption.TNS, repoConn.getTnsAlias());
					configurator.setOption(InstallerOption.VENDOR, repoConn.getDBVendor().name());
					configurator.defineOption(InstallerOption.FULL_INSTALL);
					List<IRequirement> requirements = new ArrayList<IRequirement>();
					requirements.add(new TargetUserRequirement());
					requirements.add(new NextepAdminRequirement());
					installerService.checkRequirements(configurator, requirements);
					for (String dlv : deliveries) {
						submitDelivery(dlv, monitor, configurator);
					}
					// Full check
					adminService.checkAllForce(configurator, false);
					monitor.done();
					// What to do on failure ? Maybe we should warn the user,
					// but we should let him
					// try to connect to repository...
				} catch (Throwable e) {
					LOGGER.error("Unexpected installer exception raised", e);
					monitor.log(RepositoryMessages
							.getString("repositoryUpdater.unexpectedException") + e.getMessage()); //$NON-NLS-1$
					monitor.mainWork(RepositoryMessages
							.getString("repositoryUpdater.installationFailed")); //$NON-NLS-1$
					monitor.done();
				} finally {
					if (configurator != null) {
						try {
							installerService.release(configurator);
						} catch (InstallerException e) {
							LOGGER.error(
									"Unable to release installator resources: " + e.getMessage(), e);
						}
					}
				}
			}
		}).start();
	}

	@Override
	public void install(IInstallerMonitor monitor, IConnection repoConn) {
		upgrade(monitor, repoConn);
	}

	private void submitDelivery(String dlv, IInstallerMonitor monitor,
			IInstallConfigurator configurator) throws InstallerException {
		monitor.mainWork(RepositoryMessages.getString("repositoryUpdater.submittingDelivery") //$NON-NLS-1$
				+ dlv.substring(dlv.lastIndexOf('/') + 1) + "..."); //$NON-NLS-1$

		try {
			String tempDlvLoc = createTempDelivery(dlv);
			List<IRequirement> req = new ArrayList<IRequirement>();
			req.add(new DeliveryRequirement());
			installerService.install(configurator, tempDlvLoc, req);
		} catch (RuntimeException e) {
			monitor.log(RepositoryMessages.getString("repositoryUpdater.unableToLoadDelivery") //$NON-NLS-1$
					+ dlv + RepositoryMessages.getString("repositoryUpdater.pleaseContactNextep")); //$NON-NLS-1$
			LOGGER.error("Exception occurred", e);
			monitor.done();
			throw e;
		}
		monitor.mainWork(RepositoryMessages.getString("repositoryUpdater.delivery") //$NON-NLS-1$
				+ dlv.substring(dlv.lastIndexOf('/') + 1)
				+ RepositoryMessages.getString("repositoryUpdater.installed")); //$NON-NLS-1$
	}

	/**
	 * Creates the specified delivery (ZIP resource file) on the temporary directory of the local
	 * file system.
	 * 
	 * @param deliveryResource java resource zip file
	 * @return a <code>String</code> representing the absolute path to the delivery root directory.
	 */
	private static String createTempDelivery(String deliveryResource) {
		InputStream is = RepositoryUpdaterService.class.getResourceAsStream(deliveryResource);
		if (is == null) {
			throw new ErrorException("Unable to load delivery file: " + deliveryResource); //$NON-NLS-1$
		}

		final String exportLoc = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		ZipInputStream zipInput = new ZipInputStream(is);
		ZipEntry entry = null;
		String rootDeliveryDir = null;
		try {
			while ((entry = zipInput.getNextEntry()) != null) {
				File targetFile = new File(exportLoc, entry.getName());

				if (rootDeliveryDir == null) {
					/*
					 * Initialize the delivery root directory value by searching recursively for the
					 * shallowest directory in the path.
					 */
					rootDeliveryDir = getDeliveryRootPath(targetFile, new File(exportLoc));
				}

				if (entry.isDirectory()) {
					targetFile.mkdirs();
				} else {
					File targetDir = targetFile.getParentFile();
					if (!targetDir.exists()) {
						/*
						 * Creates the directory including any necessary but nonexistent parent
						 * directories.
						 */
						targetDir.mkdirs();
					}

					FileOutputStream outFile = new FileOutputStream(targetFile);
					copyStreams(zipInput, outFile);
					outFile.close();
				}
				zipInput.closeEntry();
			}
		} catch (IOException e) {
			throw new ErrorException(e);
		} finally {
			try {
				zipInput.close();
			} catch (IOException e) {
				throw new ErrorException(e);
			}
		}
		return rootDeliveryDir;
	}

	/**
	 * Recursively search for the directory whose parent is the specified export directory in the
	 * absolute path of the specified archive file. If no such directory is found, return the parent
	 * directory of the specified archive file.
	 * 
	 * @param archiveFile the delivery file for which we need to find the delivery root directory
	 * @param exportDir the directory in which the delivery is exported
	 * @return the root path of the delivery
	 */
	private static String getDeliveryRootPath(File archiveFile, File exportDir) {
		String currPath = archiveFile.getParent();
		File parent = archiveFile;
		while (parent != null && !parent.equals(exportDir)) {
			currPath = parent.getAbsolutePath();
			parent = parent.getParentFile();
		}
		return (parent == null ? archiveFile.getParent() : currPath);
	}

	private static void copyStreams(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[10240];
		int bytesRead;
		while ((bytesRead = in.read(buffer)) >= 0) {
			out.write(buffer, 0, bytesRead);
		}
	}

	public void setInstallerService(IInstallerService installerService) {
		this.installerService = installerService;
	}

	public void setLoggingService(ILoggingService loggingService) {
		this.loggingService = loggingService;
	}

	public void setAdminService(IAdminService adminService) {
		this.adminService = adminService;
	}

	@Override
	public String createNeXtepAdminTempDelivery() {
		return createTempDelivery(DeliveryRegistry.ADMIN_DELIVERY);
	}

}
