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
package com.nextep.designer.p2.model.impl;

import java.util.Collection;
import java.util.Date;
import com.nextep.designer.p2.model.ILicenseInformation;

/**
 * Default implementation
 * 
 * @author Christophe Fondacci
 */
public class LicenseInformation implements ILicenseInformation {

	/** Serialization UID */
	private static final long serialVersionUID = 1L;
	private String organization;
	private Collection<String> registeredIPs;
	private int licenseCount;
	private String updateSiteLocation;
	private String licenseKey;
	private Date expirationDate;

	@Override
	public String getOrganization() {
		return organization;
	}

	@Override
	public Collection<String> getRegisteredIPs() {
		return registeredIPs;
	}

	@Override
	public int getLicenseCount() {
		return licenseCount;
	}

	@Override
	public String getUpdateSiteLocation() {
		return updateSiteLocation;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public void setRegisteredIPs(Collection<String> registeredIPs) {
		this.registeredIPs = registeredIPs;
	}

	public void setLicenseCount(int licenseCount) {
		this.licenseCount = licenseCount;
	}

	public void setUpdateSiteLocation(String updateSiteLocation) {
		this.updateSiteLocation = updateSiteLocation;
	}

	@Override
	public String getLicenseKey() {
		return licenseKey;
	}

	public void setLicenseKey(String licenseKey) {
		this.licenseKey = licenseKey;
	}

	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}

	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

}
