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
package com.nextep.designer.dbgm.postgre.model.impl;

import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;

/**
 * @author Christophe Fondacci
 */
public class PostgreSqlUniqueKey extends UniqueKeyConstraint implements IPhysicalObject {

	private IPhysicalProperties physicalProperties;

	@Override
	public void setPhysicalProperties(IPhysicalProperties properties) {
		final IPhysicalProperties oldProperties = physicalProperties;
		this.physicalProperties = properties;
		if (this.physicalProperties != null) {
			this.physicalProperties.setParent(this);
			notifyIfChanged(oldProperties, physicalProperties, ChangeEvent.GENERIC_CHILD_ADDED);
		} else {
			notifyIfChanged(oldProperties, physicalProperties, ChangeEvent.GENERIC_CHILD_REMOVED);
		}
	}

	@Override
	public IPhysicalProperties getPhysicalProperties() {
		return physicalProperties;
	}

	@Override
	public IElementType getPhysicalPropertiesType() {
		return IElementType.getInstance(IIndexPhysicalProperties.TYPE_ID);
	}
}
