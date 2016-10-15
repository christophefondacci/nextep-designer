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
package com.nextep.designer.dbgm.oracle.impl;

import com.nextep.datadesigner.dbgm.impl.SynchronizableNamedObservable;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public abstract class AbstractPartition extends SynchronizableNamedObservable implements IPartition {

	/** Physical properties which define this partition */
	private IPartitionable parent;
	/** Partition position among all defined partitions */
	private int position;
	/** Locking support */
	private boolean lock;
	/** Partition's physical properties */
	private IPartitionPhysicalProperties properties;

	public AbstractPartition() {
		setReference(new Reference(this.getType(), null, this));
		nameHelper.setFormatter(IFormatter.UPPERCASE);
	}

	@Override
	public IPartitionable getParent() {
		return parent;
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public void setParent(IPartitionable parent) {
		this.parent = parent;
	}

	@Override
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public IPartition getModel() {
		return this;
	}

	@Override
	public void lockUpdates() {
		this.lock = true;
	}

	@Override
	public void unlockUpdates() {
		this.lock = false;
	}

	@Override
	public boolean updatesLocked() {
		return CorePlugin.getService(ICoreService.class).isLocked(getParent());
	}

	@Override
	public IPhysicalProperties getPhysicalProperties() {
		return properties;
	}

	@Override
	public void setPhysicalProperties(IPhysicalProperties properties) {
		if (properties != this.properties) {
			final IPartitionPhysicalProperties oldProps = this.properties;
			this.properties = (IPartitionPhysicalProperties) properties;
			if (properties == null) {
				notifyListeners(ChangeEvent.GENERIC_CHILD_REMOVED, this.properties);
			} else {
				this.properties.setParent(this);
				notifyIfChanged(oldProps, this.properties, ChangeEvent.GENERIC_CHILD_ADDED);
			}
		}
	}

}
