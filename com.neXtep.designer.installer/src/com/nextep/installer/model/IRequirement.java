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

import com.nextep.installer.exception.InstallerException;

/**
 * This interface represents a requirement.
 * 
 * @author Christophe Fondacci
 */
public interface IRequirement {

	int PADDING = 30;

	/**
	 * This method checks the requirement and returns a {@link IStatus} indicating if the
	 * requirement is OK or not. Implementation may return an erroneous status or raise an exception
	 * to indicate that the requirement failed.
	 * 
	 * @param configurator the installer configurator which could be use to get and/or set
	 *        information for this requirement
	 * @return <code>true</code> if the requirement is fullfilled, <code>false</code> otherwise.
	 */
	IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException;

	/**
	 * Retrieves the name of this requirement. This name will be used when displaying the
	 * requirement to the end user while it is being checked.
	 * 
	 * @return the name of the requirement
	 */
	String getName();

}
