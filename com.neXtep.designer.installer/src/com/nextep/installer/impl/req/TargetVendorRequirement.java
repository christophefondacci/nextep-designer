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
package com.nextep.installer.impl.req;

import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.impl.Status;

/**
 * Checks the requirement of the target database vendor.
 * 
 * @author Christophe Fondacci
 */
public class TargetVendorRequirement implements IRequirement {

	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		if (configurator.getTarget() != null) {
			DBVendor vendor = configurator.getTarget().getVendor();

			// Delegating the check to the vendor's dedicated requirement
			if (vendor.getRequirement() != null) {
				return vendor.getRequirement().checkRequirement(configurator);
			}
		}
		return new Status(true, "Skipped");
	}

	public String getName() {
		return "Vendor specific connection";
	}

}
