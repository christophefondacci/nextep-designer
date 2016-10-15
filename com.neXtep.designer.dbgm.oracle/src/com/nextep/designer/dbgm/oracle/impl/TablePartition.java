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

import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;

/**
 * Implementation of a table partition.
 * 
 * @author Christophe Fondacci
 */
public class TablePartition extends AbstractPartition implements ITablePartition {

	/** Partition's high value */
	private String highValue;

	@Override
	public IElementType getType() {
		return IElementType.getInstance(ITablePartition.TYPE_ID);
	}

	@Override
	public String getHighValue() {
		return highValue;
	}

	@Override
	public void setHighValue(String highValue) {
		if (this.highValue != highValue) {
			this.highValue = highValue;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void checkConsistency() throws InconsistentObjectException {
		super.checkConsistency();
		if (getParent().getPartitioningMethod() == PartitioningMethod.RANGE) {
			if (getHighValue() == null || "".equals(getHighValue())) { //$NON-NLS-1$
				throw new InconsistentObjectException("A partition must define a valid high-value");
			}
		}
	}

	@Override
	public IElementType getPhysicalPropertiesType() {
		return IElementType.getInstance(IPartitionPhysicalProperties.TYPE_ID);
	}
}
