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
package com.nextep.designer.beng.model;

import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * Represents a release increment. This class does not represent a delivery. It should be used when
 * trying to define a release increment of a container, this increment may then correspond to one or
 * more deliveries.
 * 
 * @author Christophe
 */
public interface IDeliveryIncrement {

	/**
	 * The start release of the increment
	 * 
	 * @return the {@link IVersionInfo} indicating the release from which the increment starts
	 */
	IVersionInfo getFromRelease();

	/**
	 * The target release of this increment
	 * 
	 * @return the {@link IVersionInfo} indicating the release to which the increment ends
	 */
	IVersionInfo getToRelease();

	/**
	 * Provides the module that this increment references
	 * 
	 * @return the {@link IVersionContainer} (=the module instance)
	 */
	IVersionContainer getModule();

}
