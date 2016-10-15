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
package com.nextep.installer.model;

/**
 * Represents a required delivery.<br>
 * A required delivery is a sub delivery which should be submitted
 * before the current one to upgrade dependent components.
 * 
 * @author Christophe
 *
 */
public interface IRequiredDelivery {

	/**
	 * @return the name of the required delivery
	 */
	public String getName();
	/**
	 * Defines the name of the required delivery
	 * @param name required delivery name
	 */
	public void setName(String name);
}

