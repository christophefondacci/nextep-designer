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
package com.nextep.designer.p2.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * Describes a license information.
 * 
 * @author Christophe Fondacci
 */
public interface ILicenseInformation extends Serializable {

	/**
	 * Retrieves the name of the organization for which this license has been registered.
	 * 
	 * @return the organization name
	 */
	String getOrganization();

	/**
	 * Retrieves the list of IPs which have used this license
	 * 
	 * @return the IP address of machines which have used this license
	 */
	Collection<String> getRegisteredIPs();

	/**
	 * Retrieves the number of installation that this license can support.
	 * 
	 * @return the number of license subscribed
	 */
	int getLicenseCount();

	/**
	 * The location of the update site to contact for this license.
	 * 
	 * @return the URL (as string) that P2 should contact to fetch updates for this license
	 */
	String getUpdateSiteLocation();

	/**
	 * Retrieves the key of this license
	 * 
	 * @return the license key
	 */
	String getLicenseKey();

	/**
	 * Retrieves the expiration date of this license.
	 * 
	 * @return the license expiration date
	 */
	Date getExpirationDate();
}
