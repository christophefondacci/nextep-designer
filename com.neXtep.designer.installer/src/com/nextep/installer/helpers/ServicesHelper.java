/*******************************************************************************
 * Copyright (c) 2017 neXtep Software and contributors.
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
package com.nextep.installer.helpers;

import com.nextep.installer.NextepInstaller;
import com.nextep.installer.services.IAdminService;
import com.nextep.installer.services.IConnectionService;
import com.nextep.installer.services.IInstallerService;
import com.nextep.installer.services.ILoggingService;

/**
 * This helper provides common methods to retrieve available services.
 * 
 * @author Bruno Gautier
 */
public final class ServicesHelper {

	private ServicesHelper() {
	}

	public static final ILoggingService getLoggingService() {
		return NextepInstaller.getService(ILoggingService.class);
	}

	public static final IInstallerService getInstallerService() {
		return NextepInstaller.getService(IInstallerService.class);
	}

	public static final IAdminService getAdminService() {
		return NextepInstaller.getService(IAdminService.class);
	}

	public static final IConnectionService getConnectionService() {
		return NextepInstaller.getService(IConnectionService.class);
	}

}
