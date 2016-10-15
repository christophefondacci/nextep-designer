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

import java.util.List;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;

/**
 * An interface for database objects which support partitioning. Provides methods to access the
 * partitions and the partitioning method.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface IPartitionable extends IObservable {

	/**
	 * @return the list of all partitions defined for this partitionable object.
	 */
	List<IPartition> getPartitions();

	/**
	 * Adds a partition to this partitionable object.
	 * 
	 * @param partition partition to add
	 */
	void addPartition(IPartition partition);

	/**
	 * Removes a partition from this partitionable object.
	 * 
	 * @param partition partition to remove
	 */
	void removePartition(IPartition partition);

	/**
	 * @return the type of contained partitions
	 */
	IElementType getPartitionType();

	/**
	 * Defines the partitioning method of this partitionable object.
	 * 
	 * @param method partitioning method
	 */
	void setPartitioningMethod(PartitioningMethod method);

	/**
	 * @return the partitioning method of this partitionable object
	 */
	PartitioningMethod getPartitioningMethod();

}
