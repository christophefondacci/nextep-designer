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
package com.nextep.installer.model;

import java.sql.Connection;

/**
 * This interface provides access to the install configuration.
 * 
 * @author Christophe Fondacci
 */
public interface IInstallConfiguration {

	/**
	 * Retrieves the target connection to use for deploying the delivery
	 * 
	 * @return the target SQL {@link Connection} to use for delivery deployment
	 */
	Connection getTargetConnection();

	/**
	 * Retrieves the connection to the administration database from which the installer will fetch
	 * installation information and where it will register deployements.
	 * 
	 * @return the SQL {@link Connection} to the neXtep admin database
	 */
	Connection getAdminConnection();

	/**
	 * Indicates whether the admin tables are located in the target database.
	 * 
	 * @return <code>true</code> when the admin tables are stored in target database, else
	 *         <code>false</code> when admin tables are stored in a dedicated standalone database
	 */
	boolean isAdminInTarget();

	/**
	 * Retrieves the neXtep installer home directory.
	 * 
	 * @return the neXtep installer home directory or <code>null</code> if not defined
	 */
	String getNextepHome();

	/**
	 * Retrieves the current delivery to deploy.
	 * 
	 * @return the IDelivery to process
	 */
	IDelivery getDelivery();

	/**
	 * Retrieves the information on target database
	 * 
	 * @return the {@link IDatabaseTarget} information on the target database to deploy to
	 */
	IDatabaseTarget getTarget();

	/**
	 * Retrieves the path where the delivery is located
	 * 
	 * @return the location of the delivery in the local filesystem
	 */
	String getDeliveryPath();

	/**
	 * Retrieves the value for the given {@link InstallerOption}. Note that this method will return
	 * <code>null</code> for non-valued {@link InstallerOption} which will not allow you to
	 * determine whether the option has been set or not. For non-valued installer options, please
	 * use {@link IInstallConfigurator#isOptionDefined(InstallerOption)} instead.
	 * 
	 * @param option the {@link InstallerOption} to retrieve the value for
	 * @return the value of the specified option or <code>null</code> if non-valued option or not
	 *         set
	 */
	String getOption(InstallerOption option);

	/**
	 * Returns whether the specified option has been defined.
	 * 
	 * @param option {@link InstallerOption} to check
	 * @return <code>true</code> if this option has been set, else <code>false</code>
	 */
	boolean isOptionDefined(InstallerOption option);

	/**
	 * A convenience method for checking if any of the specified option is defined. Equivalent to
	 * calling :<br>
	 * <code>
	 * cfg.isOptionDefined(option1) || cfg.isOptionDefined(option2) || ...
	 * </code>
	 * 
	 * @param options options to check.
	 * @return <code>true</code> if <u>at least</u> 1 of the specified options is defined, else
	 *         <code>false</code>
	 */
	boolean isAnyOptionDefined(InstallerOption... options);

	/**
	 * Defines a configuration property.
	 * 
	 * @param property the property to set
	 * @param value the property value
	 */
	void setProperty(String property, String value);

	/**
	 * Retrieves a property value.
	 * 
	 * @param property property to retrieve
	 * @return the value of this property or <code>null</code> if undefined
	 */
	String getProperty(String property);
}
