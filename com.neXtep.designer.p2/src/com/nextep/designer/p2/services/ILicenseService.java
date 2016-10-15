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
package com.nextep.designer.p2.services;

import java.net.URI;
import com.nextep.designer.p2.exceptions.InvalidLicenseException;
import com.nextep.designer.p2.exceptions.UnavailableLicenseServerException;
import com.nextep.designer.p2.model.ILicenseInformation;

public interface ILicenseService {

	/**
	 * Registers the specified license key.
	 * 
	 * @param licenseKey
	 */
	ILicenseInformation registerLicenseKey(String licenseKey) throws InvalidLicenseException,
			UnavailableLicenseServerException;

	/**
	 * Retrieves the URI which the current installation should contact to fetch product updates for
	 * P2.
	 * 
	 * @return the URI of the neXtep update site
	 */
	URI getUpdateRepository() throws UnavailableLicenseServerException;

	/**
	 * Retrieves the current license information this installation is working with.
	 * 
	 * @return a {@link ILicenseInformation} bean
	 */
	ILicenseInformation getCurrentLicense() throws UnavailableLicenseServerException;

	/**
	 * Checks for updates of the currently installed features and triggers installation when an
	 * update is found.
	 * 
	 * @param isSilent a flag to specify slient behaviour or user interaction
	 */
	void checkForUpdates(boolean isSilent) throws UnavailableLicenseServerException;
}
