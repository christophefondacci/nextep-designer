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

import java.sql.Connection;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.model.ICheck;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.IRelease;

/**
 * A stateful service providing ways of communicating with the installation information stored in
 * the administration database of the installer.
 * 
 * @author Christophe Fondacci
 */
public interface IAdminService {

	/**
	 * Checks all installed releases.
	 * 
	 * @param configuration configuration information
	 * @param errorsOnly when set to <code>true</code> only errors will be listed, otherwise every
	 *        checked module will be printed out.
	 * @return <code>true</code> if all checks succeeded, else <code>false</code>
	 */
	boolean checkAllForce(IInstallConfiguration configuration, boolean errorsOnly)
			throws InstallerException;

	/**
	 * Checks all installed releases. This method will perform some checks to decide whether checks
	 * really needs to be performed depending on the current configuration.
	 * 
	 * @param configuration configuration information
	 * @param errorsOnly when set to <code>true</code> only errors will be listed, otherwise every
	 *        checked module will be printed out.
	 * @return <code>true</code> if all checks succeeded, else <code>false</code>
	 */
	boolean checkAll(IInstallConfiguration configuration, boolean errorsOnly)
			throws InstallerException;

	/**
	 * Checks a delivery.
	 * 
	 * @param configuration installer configuration
	 * @param deliveryToCheck the delivery to check
	 * @return <code>true</code> if structure check passed, else <code>false</code>
	 */
	boolean check(IInstallConfiguration configuration, IDelivery deliveryToCheck);

	/**
	 * Retrieves the currently installed release for this delivery. This is a convenience method
	 * equivalent to calling :<br>
	 * <code>getRelease(configuration,delivery.getRefUID(),true);</code>
	 * 
	 * @param configuration installer configuration
	 * @param delivery delivery to check release for
	 * @return the currently installed release for this delivery module or <code>null</code> if none
	 * @throws InstallerException whenever we cannot connect to the administration database
	 */
	IRelease getRelease(IInstallConfiguration configuration, IDelivery delivery)
			throws InstallerException;

	/**
	 * Retrieves the currently installed release for a delivery module passed by its id. The release
	 * will be retrieved from the admin database whose connection is stored in the
	 * {@link IInstallConfiguration}
	 * 
	 * @param configuration installer configuration
	 * @param moduleRefId unique reference id of the module to retrieve current release for
	 * @param raise whether or not we should raise any problem
	 * @return the {@link IRelease} of the currently installed module version, or <code>null</code>
	 *         if none
	 * @throws InstallerException
	 */
	IRelease getRelease(IInstallConfiguration configuration, long moduleRefId, boolean raise)
			throws InstallerException;

	/**
	 * Retrieves the currently installed release for a delivery module passed by its id. The release
	 * will be retrieved from the specified database connection. This specific method is used when
	 * looking for the admin database, before the admin connection has been registered in the
	 * configuration.
	 * 
	 * @param configuration installer configuration
	 * @param moduleRefId unique reference id of the module to retrieve current release for
	 * @param raise whether or not we should raise any problem
	 * @return the {@link IRelease} of the currently installed module version, or <code>null</code>
	 *         if none
	 * @throws InstallerException
	 */
	IRelease getRelease(IInstallConfiguration configuration, Connection connection,
			long moduleRefId, boolean raise) throws InstallerException;

	/**
	 * Installs the initial release, optionally performing structural checks.
	 * 
	 * @param configuration installer configuration
	 * @param deliveryToInstall the {@link IDelivery} to install as the initial release
	 */
	void installInitialRelease(IInstallConfiguration configuration, IDelivery deliveryToInstall)
			throws InstallerException;

	/**
	 * Installs a new release.
	 * 
	 * @param configuration installer configuration
	 * @param deliveryToInstall delivery to register
	 * @param success whether the release to install is in success or failure
	 */
	void installRelease(IInstallConfiguration configuration, IDelivery deliveryToInstall,
			boolean success) throws InstallerException;

	/**
	 * Lists all installed elements of the target connection.
	 * 
	 * @param configuration installer configuration
	 */
	void showInstalledReleases(IInstallConfiguration configuration) throws InstallerException;

	/**
	 * Retrieves the check for elements of the specified release.
	 * 
	 * @param configuration current install configuration
	 * @param release the {@link IRelease} to check
	 * @return the {@link ICheck} object, whose execution will check the structure of the specified
	 *         release
	 */
	ICheck getReleaseCheck(IInstallConfiguration configuration, IRelease release)
			throws InstallerException;
}
