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

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.base.AbstractController;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IVersioningService;

public class ColumnController extends AbstractController {

	@Override
	public void modelChanged(Object content) {
		save((IdentifiedObject) content);
	}

	@Override
	public void modelDeleted(Object content) {
		IVersioningService versioningService = VCSPlugin.getService(IVersioningService.class);
		// TODO Check if column has been removed from table
		IBasicColumn c = (IBasicColumn) content;
		IColumnable t = c.getParent();
		t = versioningService.ensureModifiable(t);
		t.removeColumn(c);
		// Deleting the column in db
		CorePlugin.getIdentifiableDao().delete(c);
		// Updating the parent table
		CorePlugin.getIdentifiableDao().save(t);
	}

	@Override
	public void save(IdentifiedObject content) {
		if (content instanceof ITypedObject) {
			final ITypedObject typed = (ITypedObject) content;
			// Delegating to appropriate controller if we have not a COLUMN object
			if (typed.getType() != IElementType.getInstance(IBasicColumn.TYPE_ID)) {
				ControllerFactory.getController(typed).save(content);
				return;
			}
		}
		CorePlugin.getIdentifiableDao().save(content);
	}

}
