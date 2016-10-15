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
package com.nextep.designer.repository;

import java.util.Arrays;
import java.util.List;

import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IRelease;

/**
 * @author Christophe Fondacci
 */
public class DeliveryRegistry {

	public static final String ADMIN_DELIVERY = "/resource/nextep_admin_1.0.0.1.zip"; //$NON-NLS-1$
	public static final String ADMIN_DELIVERY_1_0_0_2 = "/resource/nextep_admin_1.0.0.2.zip"; //$NON-NLS-1$
	public static final String ADMIN_DELIVERY_1_0_0_3 = "/resource/nextep_admin_1.0.0.3.zip"; //$NON-NLS-1$

	public static List<String> listDeliveriesForUpgrade(IRelease from, IRelease to, DBVendor vendor) {
		final String filePath = "/resource/" + "nextep_full_repository_"; //$NON-NLS-1$ //$NON-NLS-2$
		final String fileExt = ".zip"; //$NON-NLS-1$
		return Arrays.asList(ADMIN_DELIVERY, ADMIN_DELIVERY_1_0_0_2, ADMIN_DELIVERY_1_0_0_3,
				filePath + "1.0.6.2" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.3" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.4" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.5" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.6" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.7" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.8" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.9" + fileExt //$NON-NLS-1$
				, filePath + "1.0.7.0" + fileExt //$NON-NLS-1$
				, filePath + "1.0.7.1" + fileExt //$NON-NLS-1$
				, filePath + "1.0.7.2" + fileExt //$NON-NLS-1$
				, filePath + "1.0.7.3" + fileExt //$NON-NLS-1$
				, filePath + "1.0.7.4" + fileExt); //$NON-NLS-1$
	}

	public static List<String> listDeliveriesForFullInstall(DBVendor vendor) {
		final String filePath = "/resource/" + "nextep_full_repository_"; //$NON-NLS-1$ //$NON-NLS-2$
		final String fileExt = ".zip"; //$NON-NLS-1$
		return Arrays.asList(ADMIN_DELIVERY, ADMIN_DELIVERY_1_0_0_2, ADMIN_DELIVERY_1_0_0_3,
				filePath + "1.0.6.2" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.3" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.4" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.5" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.6" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.7" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.8" + fileExt //$NON-NLS-1$
				, filePath + "1.0.6.9" + fileExt //$NON-NLS-1$
				, filePath + "1.0.7.0" + fileExt //$NON-NLS-1$
				, filePath + "1.0.7.1" + fileExt //$NON-NLS-1$
				, filePath + "1.0.7.2" + fileExt); //$NON-NLS-1$
	}

}
