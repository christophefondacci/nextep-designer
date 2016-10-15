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
package com.nextep.installer.handlers;

import java.text.MessageFormat;
import com.neXtep.shared.model.ArtefactType;
import com.nextep.installer.InstallerMessages;
import com.nextep.installer.exception.DeployException;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IDeployHandler;

public class DeployHandlerManager {

	public static IDeployHandler getDeployHandler(DBVendor vendor, IDelivery delivery,
			ArtefactType type) throws DeployException {
		if (type == ArtefactType.SQL) {
			return vendor.getDeployHandler();
		} else if (type == ArtefactType.DELIVERY) {
			return new DeliveryDeployHandler();
		} else if (type == ArtefactType.SYSDBA) {
			return new OracleSysdbaDeployHandler();
		} else if (type == ArtefactType.SQLLOAD) {
			return new OracleSQLLoaderDeployHandler();
		} else if (type == ArtefactType.MYSQLLOAD) {
			return new MySQLLoadDeployHandler();
		} else if (type == ArtefactType.RESOURCE) {
			return new DummyDeployhandler();
		}
		throw new DeployException(MessageFormat.format(
				InstallerMessages.getString("installer.exceptions.unsupportedArtefact"), type //$NON-NLS-1$
						.name()));
	}
}
