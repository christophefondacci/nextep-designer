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
package com.nextep.designer.dbgm.ui.actions;

import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.IFormActionProvider;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * The {@link IFormActionProvider} implementing actions to manage table partitions.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractPartitionsActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		final IPhysicalObject physObj = (IPhysicalObject) parent;
		IPhysicalProperties props = physObj.getPhysicalProperties();
		// If our parent physical object does not yet have physical properties defined, we create
		// them
		if (props == null) {
			final ITypedObjectUIController propsController = UIControllerFactory
					.getController(physObj.getPhysicalPropertiesType());
			props = (IPhysicalProperties) propsController.emptyInstance(
					((INamedObject) physObj).getName(), physObj);
		}
		// Invoking the Table Partition controller to create our new partition
		final ITypedObjectUIController controller = UIControllerFactory
				.getController(getPartitionType());
		// Creating a new partition
		if (controller != null && props instanceof IPartitionable) {
			final IPartitionable partitionable = (IPartitionable) props;
			final int partitionsCount = partitionable.getPartitions().size();
			return controller.emptyInstance("PARTITION_" + partitionsCount, partitionable);
		}
		return null;
	}

	/**
	 * Retrieves the {@link IElementType} of the partition to create
	 * 
	 * @return the {@link IElementType} of partitions to create
	 */
	protected abstract IElementType getPartitionType();

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		VCSUIPlugin.getService(IWorkspaceUIService.class).remove((IReferenceable) toRemove);
	}

	@Override
	public boolean isSortable() {
		return true;
	}

	@Override
	public void up(ITypedObject parent, ITypedObject element) {
		final IPhysicalObject physObj = (IPhysicalObject) parent;
		final IPhysicalProperties props = physObj.getPhysicalProperties();
		if (props != null && props instanceof IPartitionable) {
			final IPartitionable partitionable = (IPartitionable) props;
			final IPartition partition = (IPartition) element;
			if (partition != null) {
				final List<IPartition> partitions = partitionable.getPartitions();
				int partIndex = partitions.indexOf(partition);
				if (partIndex > 0) {
					final IPartition swappedPartition = partitions.get(partIndex - 1);
					Collections.swap(partitions, partIndex, partIndex - 1);
					partition.setPosition(partIndex - 1);
					swappedPartition.setPosition(partIndex);
					partitionable.notifyListeners(ChangeEvent.MODEL_CHANGED, partition);
					partition.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
					swappedPartition.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				}
			}
		}
	}

	@Override
	public void down(ITypedObject parent, ITypedObject element) {
		final IPhysicalObject physObj = (IPhysicalObject) parent;
		final IPhysicalProperties props = physObj.getPhysicalProperties();
		if (props != null && props instanceof IPartitionable) {
			final IPartitionable partitionable = (IPartitionable) props;
			final IPartition partition = (IPartition) element;
			if (partition != null) {
				final List<IPartition> partitions = partitionable.getPartitions();
				int partIndex = partitions.indexOf(partition);
				if (partIndex < partitions.size() - 1) {
					final IPartition swappedPartition = partitions.get(partIndex + 1);
					Collections.swap(partitions, partIndex, partIndex + 1);
					partition.setPosition(partIndex + 1);
					swappedPartition.setPosition(partIndex);
					partitionable.notifyListeners(ChangeEvent.MODEL_CHANGED, partition);
					partition.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
					swappedPartition.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				}
			}
		}

	}

}
