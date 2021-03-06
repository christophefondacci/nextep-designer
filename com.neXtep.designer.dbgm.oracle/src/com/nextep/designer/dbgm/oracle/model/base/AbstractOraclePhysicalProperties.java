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
package com.nextep.designer.dbgm.oracle.model.base;

import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.base.AbstractPartitionablePhysicalProperties;
import com.nextep.designer.dbgm.model.base.AbstractPhysicalProperties;

/**
 * @author Bruno Gautier
 * @see AbstractPhysicalProperties
 */
public abstract class AbstractOraclePhysicalProperties extends
		AbstractPartitionablePhysicalProperties implements IPhysicalProperties, IPartitionable {

	/*
	 * FIXME [BGA] This abstract base class is meant to handle, among other things, the logging
	 * attribute of an Oracle object, but to avoid a too heavy refactoring on the repository side,
	 * the logging attribute has been left in the PhysicalProperties abstract base class for the
	 * moment.
	 */

}
