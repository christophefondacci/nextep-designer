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
package com.nextep.installer.services;

import java.util.List;

import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRequirement;

/**
 * The installer service provides method to run the deployment of a database
 * delivery. Callers are responsible of initializing a
 * {@link IInstallConfigurator} to configure the required information describing
 * the installation to perform.<br>
 * After installation, callers need to explicitly call the
 * {@link IInstallerService#release(IInstallConfiguration)} method to release
 * any resource allocated by the installer.
 * 
 * @author Christophe Fondacci
 */
public interface IInstallerService {

	/**
	 * Installs the delivery from the specified location, after all
	 * {@link IRequirement}s are met.
	 * 
	 * @param configurator
	 *            the installer configurator
	 * @param deliveryLocation
	 *            the location (local directory path) where the delivery can be
	 *            found
	 * @param requirements
	 *            requirements to check before installing
	 * @return <code>true</code> when OK, <code>false</code> on failure
	 */
	boolean install(IInstallConfigurator configurator, String deliveryLocation,
			List<IRequirement> requirements) throws InstallerException;

	/**
	 * Installs the delivery from the specified location with default
	 * requirements check.
	 * 
	 * @param configurator
	 *            the installer configurator {@link IInstallConfigurator}
	 * @param deliveryLocation
	 *            the location where the delivery descriptor can be found
	 * @return <code>true</code> when OK, <code>false</code> on failure
	 */
	boolean install(IInstallConfigurator configurator, String deliveryLocation)
			throws InstallerException;

	/**
	 * Checks all specified requirements
	 * 
	 * @param configuration
	 *            install configuration to use for this check
	 * @param requirements
	 *            list of all requirements to check
	 * @throws InstallerException
	 *             whenever we have problems connecting to the target or admin
	 *             database
	 */
	void checkRequirements(IInstallConfigurator configurator, List<IRequirement> requirements)
			throws InstallerException;

	/**
	 * Releases any resource opened in the specified install configuration. Any
	 * client of the installer service should release the configuration after
	 * use so that any underlying opened resource can be safely release.
	 * 
	 * @param configuration
	 *            the configuration to release
	 */
	void release(IInstallConfiguration configuration) throws InstallerException;

	/**
	 * Copies the configuration into a new configurator. This method may be used
	 * to initialize new configuration for recursive deliveries
	 * 
	 * @param configurator
	 *            the {@link IInstallConfiguration} to copy
	 * @return a new {@link IInstallConfigurator} initialized as a copy of the
	 *         input configuration
	 */
	IInstallConfigurator copyConfigurator(IInstallConfiguration configurator);
}
