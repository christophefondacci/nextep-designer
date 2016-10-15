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

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.base.AbstractController;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.oracle.DBOMMessages;
import com.nextep.designer.vcs.exception.UnmodifiableObjectException;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class TablePartitionController extends AbstractController {

	@Override
	public void modelChanged(Object content) {
	}

	@Override
	public void modelDeleted(Object content) {
		final ITablePartition part = (ITablePartition) content;
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);

		if (coreService.isLocked(part)) {
			throw new UnmodifiableObjectException(VersionHelper.getVersionable(part));
		}

		IPartitionable parent = part.getParent();
		if (parent == null) {
			throw new ErrorException(DBOMMessages.getString("modelAlreadyDeleted"));
		}

		parent.removePartition(part);
		// Updating table
		CorePlugin.getIdentifiableDao().save((IdentifiedObject) parent);
		// Removing constraint in database
		part.setParent(null);
		CorePlugin.getIdentifiableDao().delete(part);
		// Removing reference
		CorePlugin.getService(IReferenceManager.class).dereference(part);
	}

}
