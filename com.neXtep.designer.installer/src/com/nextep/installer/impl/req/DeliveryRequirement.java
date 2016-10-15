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

import java.io.File;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.exception.ParseException;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRequirement;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.InstallerOption;
import com.nextep.installer.model.impl.Status;
import com.nextep.installer.parsers.DescriptorParser;

/**
 * @author Christophe Fondacci
 */
public class DeliveryRequirement implements IRequirement {

	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		String path = configurator.getDeliveryPath();

		File descriptor = null;
		// NextepInstaller.out("Delivery descriptor",PADDING);
		if (!configurator.isOptionDefined(InstallerOption.INSTALL)) {
			descriptor = new File(path + File.separator + "delivery.xml");
		} else {
			String home = configurator.getNextepHome();
			path = home + File.separator + "admin" + File.separator + "JDBC";
			descriptor = new File(path + File.separator + "delivery.xml");
			configurator.setDeliveryPath(path);
		}
		if (!descriptor.exists()) {
			throw new InstallerException("No delivery descriptor has been found in '"
					+ descriptor.getAbsolutePath() + "'.");
		}
		// Building delivery
		try {
			String vendor = configurator.getOption(InstallerOption.VENDOR);
			DBVendor dbVendor = null;
			if (vendor != null && !"".equals(vendor)) {
				try {
					dbVendor = DBVendor.valueOf(vendor);
				} catch (IllegalArgumentException e) {
					throw new InstallerException("Unsupported database vendor '" + vendor + "'", e);
				}
			}
			IDelivery delivery = DescriptorParser.buildDescriptor(path, descriptor, dbVendor);
			configurator.setDelivery(delivery);
			// NextepInstaller.log("OK.");
		} catch (ParseException e) {
			throw new InstallerException("Exception during parse of delivery.xml in '"
					+ descriptor.getAbsolutePath() + "'", e);
		}
		// We are clean
		return new Status(true, descriptor.getAbsolutePath());
	}

	public String getName() {
		return "Delivery descriptor";
	}
}
