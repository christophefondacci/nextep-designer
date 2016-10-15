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
package com.nextep.designer.dbgm.model.base;

import java.util.ArrayList;
import java.util.List;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PartitioningMethod;

/**
 * Base class for physical properties of partitionable database objects.
 * 
 * @author Bruno Gautier
 */
public abstract class AbstractPartitionablePhysicalProperties extends AbstractPhysicalProperties
		implements IPhysicalProperties, IPartitionable {

	private List<IPartition> partitions;
	private PartitioningMethod method = PartitioningMethod.NONE;

	public AbstractPartitionablePhysicalProperties() {
		this.partitions = new ArrayList<IPartition>();
	}

	@Override
	public List<IPartition> getPartitions() {
		return partitions;
	}

	@Override
	public void addPartition(IPartition partition) {
		if (!partitions.contains(partition)) {
			partitions.add(partition);
			partition.setPosition(partitions.indexOf(partition));
			partition.setParent(this);
			notifyListeners(ChangeEvent.PARTITION_ADDED, partition);
		}
	}

	@Override
	public void removePartition(IPartition partition) {
		final int delIndex = partition.getPosition();
		if (partitions.remove(partition)) {
			for (IPartition p : getPartitions()) {
				if (p.getPosition() > delIndex) {
					p.setPosition(p.getPosition() - 1);
				}
			}
			notifyListeners(ChangeEvent.PARTITION_REMOVED, partition);
		}
	}

	@Override
	public PartitioningMethod getPartitioningMethod() {
		return method;
	}

	@Override
	public void setPartitioningMethod(PartitioningMethod method) {
		if (this.method != method) {
			this.method = method;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * Hibernate partition setter.
	 * 
	 * @param partitions a {@link List} of {@link IPartition} objects
	 */
	public void setPartitions(List<IPartition> partitions) {
		int index = 0;
		for (IPartition p : partitions) {
			p.setPosition(index++);
			p.setParent(this);
		}
		this.partitions = partitions;
	}

}
