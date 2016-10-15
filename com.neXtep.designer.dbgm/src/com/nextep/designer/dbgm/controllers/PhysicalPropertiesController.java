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
package com.nextep.designer.dbgm.controllers;

import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.base.AbstractController;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.vcs.exception.UnmodifiableObjectException;

/**
 * Generic controller for {@link IPhysicalProperties}
 * 
 * @author Christophe Fondacci
 */
public class PhysicalPropertiesController extends AbstractController {

	@Override
	public void modelChanged(Object content) {
	}

	@Override
	public void modelDeleted(Object content) {

		IPhysicalProperties props = (IPhysicalProperties) content;
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);

		if (coreService.isLocked(props)) {
			throw new UnmodifiableObjectException(VersionHelper.getVersionable(props));
		}
		final IPhysicalObject t = props.getParent();
		t.setPhysicalProperties(null);
		// Updating table
		if (t instanceof IdentifiedObject) {
			CorePlugin.getIdentifiableDao().save((IdentifiedObject) t);
		}
		// Removing constraint in database
		props.setParent(null);
		CorePlugin.getIdentifiableDao().delete(props);
		// Removing reference
		CorePlugin.getService(IReferenceManager.class).dereference(props);
	}

}
