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
package com.nextep.designer.core.model;

/**
 * Interface for elements that could be specific to a specific vendor. This interface exposes
 * accessors to the vendor information.
 * 
 * @author Christophe Fondacci
 */
public interface IVendorOriented {

	/**
	 * The database vendor for which this object is designed.
	 * 
	 * @return the {@link DBVendor}
	 */
	DBVendor getDBVendor();

	/**
	 * Defines the database vendor for which the object is designed
	 * 
	 * @param vendor the {@link DBVendor}
	 */
	void setDBVendor(DBVendor vendor);
}
