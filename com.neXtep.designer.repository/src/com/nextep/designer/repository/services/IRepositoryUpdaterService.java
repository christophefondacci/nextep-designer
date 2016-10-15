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
package com.nextep.designer.repository.services;

import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.repository.RepositoryStatus;
import com.nextep.designer.repository.exception.NoRepositoryConnectionException;
import com.nextep.designer.repository.exception.NoRepositoryException;
import com.nextep.installer.model.IInstallerMonitor;

/**
 * This service provides methods to install, upgrade and administrate the neXtep repository. It is
 * the main entry-point when working with bundled neXtep deliveries.
 * 
 * @author Christophe Fondacci
 */
public interface IRepositoryUpdaterService {

	/**
	 * Upgrades the specified neXtep repository database connection to the latest repository release
	 * available.
	 * 
	 * @param monitor the {@link IInstallerMonitor} to report progress to
	 * @param repoConn the neXtep repository database {@link IConnection} descriptor
	 */
	void upgrade(final IInstallerMonitor monitor, IConnection repoConn);

	/**
	 * Installs the latest release of the neXtep repository on the specified database connection
	 * available.
	 * 
	 * @param monitor the {@link IInstallerMonitor} to report progress to
	 * @param repoConn the neXtep repository database {@link IConnection} descriptor
	 */
	void install(IInstallerMonitor monitor, IConnection repoConn);

	/**
	 * Checks whether the repository defined by the specified connection properties is up to date
	 * with the expected repository release.
	 * 
	 * @param user repository db user
	 * @param password repository db password
	 * @param database repository database name
	 * @param host repository db server
	 * @param port repository db port
	 * @param vendor repository db vendor
	 * @return a {@link RepositoryStatus} indicating the current state
	 * @throws NoRepositoryException when no repository exists
	 */
	RepositoryStatus checkRepository(IConnection repoConn) throws NoRepositoryException,
			NoRepositoryConnectionException;

	/**
	 * Creates the neXtep admin delivery in a temporary location on the local filesystemand returns
	 * the delivery root folder.
	 * 
	 * @return the folder where the delivery has been generated
	 */
	String createNeXtepAdminTempDelivery();
}
