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
package com.nextep.designer.dbgm.oracle.controllers;

import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.base.AbstractController;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;
import com.nextep.designer.vcs.exception.UnmodifiableObjectException;

public class PartitionPhysicalPropertiesController extends AbstractController {

	@Override
	public void modelChanged(Object content) {
	}

	@Override
	public void modelDeleted(Object content) {
		IPartitionPhysicalProperties props = (IPartitionPhysicalProperties) content;
		IPartition i = (IPartition) props.getParent();
		if (i.updatesLocked()) {
			throw new UnmodifiableObjectException(VersionHelper.getVersionable(i));
		}
		i.setPhysicalProperties(null);
		// Updating table
		CorePlugin.getIdentifiableDao().save(i);
		// Removing constraint in database
		CorePlugin.getIdentifiableDao().delete(props);
		// Removing reference
		CorePlugin.getService(IReferenceManager.class).dereference(props);
	}

}
