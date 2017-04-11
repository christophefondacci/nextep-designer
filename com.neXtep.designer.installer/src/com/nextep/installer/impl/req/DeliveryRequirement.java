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
import java.text.MessageFormat;
import com.nextep.installer.InstallerMessages;
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
 * This requirement ensures that the delivery descriptor file delivery.xml exists and that it is a
 * valid delivery descriptor. It also checks that the value of the <code>--vendor</code> option, if
 * any specified, is a valid database vendor.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class DeliveryRequirement implements IRequirement {

	public static final String DELIVERY_FILE_NAME = "delivery.xml"; //$NON-NLS-1$

	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		String path = configurator.getDeliveryPath();

		File descriptor = null;
		if (!configurator.isOptionDefined(InstallerOption.INSTALL)) {
			descriptor = new File(path + File.separator + DELIVERY_FILE_NAME);
		} else {
			String home = configurator.getNextepHome();
			path = home + File.separator + "admin" + File.separator + "JDBC"; //$NON-NLS-1$ //$NON-NLS-2$
			descriptor = new File(path + File.separator + DELIVERY_FILE_NAME);
			configurator.setDeliveryPath(path);
		}
		if (!descriptor.exists()) {
			throw new InstallerException(MessageFormat.format(InstallerMessages
					.getString("requirement.delivery.descriptorFileNotFoundException"), //$NON-NLS-1$
					DELIVERY_FILE_NAME, descriptor.getAbsolutePath()));
		}

		// Building delivery
		try {
			String vendor = configurator.getOption(InstallerOption.VENDOR);
			DBVendor dbVendor = null;
			if (vendor != null && !"".equals(vendor)) { //$NON-NLS-1$
				try {
					dbVendor = DBVendor.valueOf(vendor);
				} catch (IllegalArgumentException e) {
					throw new InstallerException(MessageFormat.format(InstallerMessages
							.getString("requirement.delivery.unsupportedDBVendorException"), //$NON-NLS-1$
							vendor), e);
				}
			}
			IDelivery delivery = DescriptorParser.buildDescriptor(path, descriptor, dbVendor);
			configurator.setDelivery(delivery);
		} catch (ParseException e) {
			throw new InstallerException(MessageFormat.format(
					InstallerMessages.getString("requirement.delivery.invalidDescriptorException"), //$NON-NLS-1$
					DELIVERY_FILE_NAME, descriptor.getAbsolutePath()), e);
		}

		// We are clean
		return new Status(true, descriptor.getAbsolutePath());
	}

	public String getName() {
		return "Delivery descriptor"; //$NON-NLS-1$
	}

}
