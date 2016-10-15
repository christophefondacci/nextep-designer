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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.beng.dao;

import java.util.Collection;
import java.util.List;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.core.model.DBVendor;

/**
 * A data access object specification providing repository interactions to deal with deliveries
 * 
 * @author Christophe Fondacci
 */
public interface IDeliveryDao {

	/**
	 * Loads all available deliveries matching the specified vendor as lightweight delivery
	 * information beans.
	 * 
	 * @param vendor the {@link DBVendor} to load deliveries for
	 * @return a list of {@link IDeliveryInfo}
	 */
	List<IDeliveryInfo> getDeliveriesForVendor(DBVendor vendor);

	/**
	 * Loads the module referenced by the specified delivery information.
	 * 
	 * @param info the {@link IDeliveryInfo} of the module to load
	 * @return the {@link IDeliveryModule} corresponding to the delivery information
	 */
	IDeliveryModule loadModule(IDeliveryInfo info);

	/**
	 * Retrieves all defined deliveries information regarding the specified module reference.
	 * 
	 * @param moduleRef the {@link IReference} of a database module to get deliveries info for
	 * @return the list of {@link IDeliveryInfo} on available deliveries for this module
	 */
	List<IDeliveryInfo> getAvailableDeliveries(IReference moduleRef);

	/**
	 * Retrieves the module reference matching the specified name.
	 * 
	 * @param moduleName the name to look for
	 * @return the {@link IReference} of matching modules. There could be more than one matching
	 *         module as there is no global unicity of module names in the neXtep repository
	 */
	Collection<IReference> lookupModuleName(String moduleName);
}
