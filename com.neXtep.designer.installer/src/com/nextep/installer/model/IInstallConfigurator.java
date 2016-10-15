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
 * This interface is a configurator of the neXtep installer. The configurator is an element which
 * can exchange information with the installer.
 * 
 * @author Christophe Fondacci
 */
public interface IInstallConfigurator extends IInstallConfiguration {

	/**
	 * Defines the target connection to use for deploying the delivery.
	 * 
	 * @param target target SQL {@link Connection} to use for deployment of the delivery
	 */
	void setTargetConnection(Connection target);

	/**
	 * Defines the connection to the administration database from which the installer will fetch
	 * installation information and where it will register deployements.
	 * 
	 * @param admin the SQL {@link Connection} to the neXtep admin database
	 */
	void setAdminConnection(Connection admin);

	/**
	 * Defines whether the admin schema is located in the target database or not.
	 * 
	 * @param adminIsTarget should be set to <code>true</code> when the admin tables are stored in
	 *        the target database, or else <code>false</code>
	 */
	void setAdminInTarget(boolean adminIsTarget);

	/**
	 * Defines an installation option value.
	 * 
	 * @param option the {@link InstallerOption} to set
	 * @param value the value of this option
	 */
	void setOption(InstallerOption option, String value);

	/**
	 * A convenience method to set a non-valued {@link InstallerOption}. This method is equivalent
	 * to calling :<br>
	 * <code>setOption(option,null);</code>
	 * 
	 * @param option the option to set
	 */
	void defineOption(InstallerOption option);

	/**
	 * Defines the neXtep home directory location.
	 * 
	 * @param nextepHome neXtep installer's home directory
	 */
	void setNextepHome(String nextepHome);

	/**
	 * Defines the delivery to deploy.
	 * 
	 * @param delivery the {@link IDelivery} to deploy to the target database
	 */
	void setDelivery(IDelivery delivery);

	/**
	 * Defines the target database information
	 * 
	 * @param target target database information
	 */
	void setTarget(IDatabaseTarget target);

	/**
	 * Defines the local directory where to look for a delivery descriptor
	 * 
	 * @param path the location of the delivery to install
	 */
	void setDeliveryPath(String path);
}
