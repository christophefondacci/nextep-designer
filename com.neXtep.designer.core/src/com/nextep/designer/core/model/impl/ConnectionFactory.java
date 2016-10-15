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
package com.nextep.designer.core.model.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.ITypedObjectFactory;

public class ConnectionFactory implements ITypedObjectFactory {

	private final static Log LOGGER = LogFactory.getLog(ConnectionFactory.class);

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ITypedObject> T create(Class<T> classToCreate) {
		final IConnection connection = (IConnection) new Connection();
		try {
			final String context = Designer.getInstance().getContext();
			if (context != null) {
				connection.setDBVendor(DBVendor.valueOf(context));
			}
			final DBVendor vendor = connection.getDBVendor();
			final int port = vendor == null ? 0 : vendor.getDefaultPort();
			if (port > 0) {
				connection.setServerPort(String.valueOf(port));
			}
		} catch (RuntimeException e) {
			LOGGER.debug("Couldn't find current vendor", e); //$NON-NLS-1$
		}
		return (T) connection;
	}

}
