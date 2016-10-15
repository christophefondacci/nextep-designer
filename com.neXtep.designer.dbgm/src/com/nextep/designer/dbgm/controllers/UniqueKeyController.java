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

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.base.AbstractController;
import com.nextep.designer.vcs.exception.UnmodifiableObjectException;

public class UniqueKeyController extends AbstractController {

	@Override
	public void modelChanged(Object content) {

	}

	@Override
	public void modelDeleted(Object content) {
		IKeyConstraint uk = (IKeyConstraint) content;
		IBasicTable t = uk.getConstrainedTable();
		if (t.updatesLocked()) {
			throw new UnmodifiableObjectException(VersionHelper.getVersionable(t));
		}
		t.removeConstraint(uk);
		uk.getConstrainedColumnsRef().clear();
		// c.setReference(null);
		// Updating table
		CorePlugin.getIdentifiableDao().save(t);
		// Removing constraint in database
		CorePlugin.getIdentifiableDao().delete(uk);
		// Removing reference
		CorePlugin.getService(IReferenceManager.class).dereference(uk);
	}

	@Override
	public void save(IdentifiedObject content) {
		if (content instanceof IKeyConstraint) {
			IKeyConstraint c = (IKeyConstraint) content;
			if (c != null) {
				CorePlugin.getIdentifiableDao().save(c.getReference());
			}
			super.save(content);
		}
	}
}
