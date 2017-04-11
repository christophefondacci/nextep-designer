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
package com.nextep.installer;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.exception.InvalidOptionException;
import com.nextep.installer.impl.req.DeliveryRequirement;
import com.nextep.installer.impl.req.NextepAdminRequirement;
import com.nextep.installer.impl.req.NextepHomeRequirement;
import com.nextep.installer.impl.req.TargetUserRequirement;
import com.nextep.installer.impl.req.TargetVendorRequirement;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.impl.DefaultInstallerConfigurator;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.IConnectionService;
import com.nextep.installer.services.IInstallerService;
import com.nextep.installer.services.ILoggingService;
import com.nextep.installer.services.impl.AdminService;
import com.nextep.installer.services.impl.ConnectionService;
import com.nextep.installer.services.impl.InstallerService;
import com.nextep.installer.services.impl.LoggingService;

/**
 * Entry-point for the neXtep installer in standalone mode.<br>
 * Users of the installer in the non-standalone mode should only use the {@link IInstallerService}. <br>
 * The installer always works with files so you must have a delivery persisted on the file system in
 * order for the installer to work.<br>
 * <br>
 * <b>This class needs to be Java 1.5 compliant</b> <br>
 * Another constraint of the installer is to be lightweight as the installer is exported with every
 * delivery generated through neXtep designer. That means no spring, no hibernate, no apache
 * commons, etc. It should be kept as small as possible.<br>
 * This leads to some raw code which is not very elegant, sorry for this constraint.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class NextepInstaller {

	/** Environment variable for home of installer */
	public static final String NXTP_HOME = "NXTP_HOME"; //$NON-NLS-1$
	public static final String PROP_BINARY = "admin.nextep.bin.location"; //$NON-NLS-1$

	/**
	 * Since we are lightweight, we cannot use spring for dependency injection so we register
	 * services manually using this map.
	 */
	private static final Map<Class<?>, Object> SERVICE_REGISTRY = new HashMap<Class<?>, Object>();

	/**
	 * Displays program header with legal mentions & versions.
	 */
	public static void printLaunchHeader() {
		final ILoggingService logger = getService(ILoggingService.class);

		Date d = new Date();
		SimpleDateFormat f = new SimpleDateFormat("EEE d MMM yyyy"); //$NON-NLS-1$
		logger.log(""); //$NON-NLS-1$
		logger.log(MessageFormat.format(
				InstallerMessages.getString("installer.headerLine1"), f.format(d))); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("installer.headerLine2")); //$NON-NLS-1$
		logger.log(""); //$NON-NLS-1$
	}

	/**
	 * Displays the installer help with the help of the {@link ILoggingService}.
	 */
	private static void displayHelp() {
		final ILoggingService logger = getService(ILoggingService.class);

		logger.log(InstallerMessages.getString("installer.usageLine1")); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("installer.usageLine2")); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("installer.usageLine3")); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("installer.usageLine4")); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("installer.usageLine5")); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("installer.usageLineOptions")); //$NON-NLS-1$
		logger.pad();
		for (InstallerOption option : InstallerOption.values()) {
			String argument = null;
			if (option.isMandatory()) {
				argument = MessageFormat
						.format(InstallerMessages.getString("installer.mandatoryOptionPattern"), option.getName(), option.getValueName()); //$NON-NLS-1$
			} else {
				argument = MessageFormat.format(InstallerMessages
						.getString("installer.nonValuedOptionPattern"), option.getName()); //$NON-NLS-1$
			}
			logger.out(argument, 20);
			logger.log(option.getDescription());
		}
		logger.unpad();
		logger.log(""); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("installer.usageFooterLine1")); //$NON-NLS-1$
		logger.log(InstallerMessages.getString("installer.usageFooterLine2")); //$NON-NLS-1$
		logger.log(""); //$NON-NLS-1$
	}

	/**
	 * Installer entry point.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		// Service registration
		registerService(ILoggingService.class, new LoggingService());
		registerService(IAdminService.class, new AdminService());
		registerService(IInstallerService.class, new InstallerService());
		registerService(IConnectionService.class, new ConnectionService());

		final ILoggingService logger = getService(ILoggingService.class);
		final IInstallerService installer = getService(IInstallerService.class);

		// Launching header
		printLaunchHeader();
		IInstallConfigurator configurator = new DefaultInstallerConfigurator();
		try {
			initOptionsMap(args, configurator);
		} catch (InvalidOptionException e) {
			logger.log(e.getMessage());
			logger.log(""); //$NON-NLS-1$
			displayHelp();
			System.exit(-1);
		}

		// If help option is active, we display help and exit
		if (configurator.isOptionDefined(InstallerOption.HELP)) {
			displayHelp();
			return;
		}

		// Configuring logger for verbose mode if verbose option has been defined
		logger.configure(configurator);

		// Only info has been requested
		if (configurator.isOptionDefined(InstallerOption.INFO)) {
			try {
				showInfo(configurator);
			} catch (InstallerException e) {
				logger.error(
						InstallerMessages.getString("installer.showInfoFail") + e.getMessage(), e); //$NON-NLS-1$
				System.exit(-1);
			}
			return;
		}

		// Building prerequisites list
		List<IRequirement> requirements = buildStandaloneRequirements();

		// Installation
		boolean success = false;
		try {
			success = installer.install(configurator, ".", requirements); //$NON-NLS-1$

			logger.log(""); //$NON-NLS-1$
			logger.log(InstallerMessages.getString("installer.installationTerminated")); //$NON-NLS-1$
			logger.log(""); //$NON-NLS-1$
			logger.log(InstallerMessages.getString("installer.byeMsg")); //$NON-NLS-1$
		} catch (InstallerException e) {
			logger.error("Problems during installation: " + e.getMessage(), e); //$NON-NLS-1$
			System.exit(-1);
		} finally {
			try {
				installer.release(configurator);
				System.exit(success ? 0 : -1);
			} catch (InstallerException e) {
				logger.error(e.getMessage(), e);
				System.exit(-2);
			}
		}
	}

	/**
	 * Builds the list of requirements to check in standalone installer mode. The requirement order
	 * is important since requirements contribute to the configuration.
	 * 
	 * @return a list of {@link IRequirement} to use in standalone installer mode.
	 */
	public static List<IRequirement> buildStandaloneRequirements() {
		List<IRequirement> requirements = new ArrayList<IRequirement>();
		requirements.add(new NextepHomeRequirement());
		requirements.add(new DeliveryRequirement());
		requirements.add(new TargetUserRequirement());
		requirements.add(new NextepAdminRequirement());
		requirements.add(new TargetVendorRequirement());
		return requirements;
	}

	/**
	 * Displays the information on currently installed releases to the user and performs a full
	 * structure check.
	 * 
	 * @param login login of target db
	 * @param password password to connect to target db
	 * @param sid database name of target
	 * @throws DeployException if we fail to perform the structure check
	 */
	private static void showInfo(IInstallConfigurator configurator) throws InstallerException {
		final ILoggingService logger = getService(ILoggingService.class);
		final IAdminService adminService = getService(IAdminService.class);
		final IInstallerService installerService = getService(IInstallerService.class);

		// Preparing info requirements
		List<IRequirement> reqs = new ArrayList<IRequirement>();
		reqs.add(new NextepHomeRequirement());
		reqs.add(new TargetUserRequirement());
		reqs.add(new NextepAdminRequirement());
		installerService.checkRequirements(configurator, reqs);
		logger.log(""); //$NON-NLS-1$
		adminService.showInstalledReleases(configurator);
		logger.log(""); //$NON-NLS-1$
		adminService.checkAll(configurator, false);
	}

	/**
	 * Captures the input from the user and returns it as a string. The input is captured when the
	 * user type the return key.
	 * 
	 * @param defaultResponse for non interactive mode, default is always used
	 * @return the string entered by the user
	 */
	public static String getUserInput(String defaultResponse) {
		return getUserInput(defaultResponse, false);
	}

	/**
	 * Captures the input from the user and returns it as a string. The input is captured when the
	 * user type the return key.
	 * 
	 * @param defaultResponse for non interactive mode, default is always used
	 * @param password is this a password field
	 * @return the string entered by the user
	 */
	public static String getUserInput(String defaultResponse, boolean password) {
		getService(ILoggingService.class).log(defaultResponse);
		return defaultResponse;
	}

	/**
	 * Fills the configurator with command line arguments converted into {@link InstallerOption}.
	 * Any unrecognized option will fire an {@link InvalidOptionException}.
	 * 
	 * @param args command line arguments
	 * @param configurator the installer configurator as {@link IInstallConfigurator}
	 * @throws InvalidOptionException when invalid options are parsed
	 */
	private static void initOptionsMap(String[] args, IInstallConfigurator configurator)
			throws InvalidOptionException {
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) { //$NON-NLS-1$
				InstallerOption option = null;
				int valueSeparator = args[i].indexOf('=');
				String optionName = null;
				String optionValue = null;
				if (valueSeparator == -1) {
					optionName = args[i].substring(1);
				} else {
					optionName = args[i].substring(1, valueSeparator);
					optionValue = args[i].substring(valueSeparator + 1);
				}
				try {
					option = InstallerOption.parse(optionName);
				} catch (RuntimeException e) {
					throw new InvalidOptionException(args[i]);
				}
				configurator.setOption(option, optionValue);
			}
		}
	}

	/**
	 * Retrieves a given service implementation. This is a home-made service registration as we need
	 * to stay lightweight in this installer to ensure that we can embed it into every delivery.
	 * 
	 * @param service service interface to get implementation for
	 * @return the corresponding implementation
	 * @throws NoSuchElementException when no implementation has been registered for this service
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> service) {
		Object serviceImpl = SERVICE_REGISTRY.get(service);
		if (serviceImpl != null && service.isAssignableFrom(serviceImpl.getClass())) {
			return (T) serviceImpl;
		}
		throw new NoSuchElementException("No service registered for interface: " //$NON-NLS-1$
				+ service.toString());
	}

	/**
	 * Registers the given service.
	 * 
	 * @param service service interface
	 * @param serviceImpl service implementation
	 */
	public static <T> void registerService(Class<T> service, T serviceImpl) {
		SERVICE_REGISTRY.put(service, serviceImpl);
	}

}
