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
package com.nextep.designer.dbgm.model;

import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.designer.core.model.IParentable;

/**
 * This class represents an abstract partition.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface IPartition extends IDatabaseObject<IPartition>, IPhysicalObject,
		IParentable<IPartitionable> {

	String TYPE_ID = "PARTITION"; //$NON-NLS-1$

	/**
	 * @return the position of this partition among all defined partitions
	 */
	int getPosition();

	/**
	 * Defines the position of this partition among all defined partitions.
	 * 
	 * @param position this partition position
	 */
	void setPosition(int position);

}
