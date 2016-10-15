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
package com.nextep.designer.dbgm.oracle.ui.controllers;

import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.vcs.gui.external.VersionableDisplayDecorator;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.ui.impl.CheckConstraintEditor;
import com.nextep.designer.dbgm.oracle.ui.impl.CheckConstraintNavigator;
import com.nextep.designer.ui.model.base.AbstractUIController;

public class OracleCheckConstraintUIController extends AbstractUIController {

	public OracleCheckConstraintUIController() {
		super();
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		final ICheckConstraint c = (ICheckConstraint) content;
		return new VersionableDisplayDecorator(new CheckConstraintEditor(c, this),
				VersionHelper.getVersionable(c.getConstrainedTable()));
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new CheckConstraintNavigator((ICheckConstraint) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object newInstance(Object parent) {
		final IOracleTable t = (IOracleTable) parent;
		final ICheckConstraint c = CorePlugin.getTypedObjectFactory()
				.create(ICheckConstraint.class);
		c.setConstrainedTable(t);

		newWizardEdition("Check constraint creation wizard...", initializeEditor(c));
		save(c);
		t.addCheckConstraint(c);
		CorePlugin.getIdentifiableDao().save(t);

		return c;
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		final IOracleTable t = (IOracleTable) parent;
		final ICheckConstraint c = CorePlugin.getTypedObjectFactory()
				.create(ICheckConstraint.class);
		c.setConstrainedTable(t);
		c.setName(name);

		save(c);
		t.addCheckConstraint(c);
		CorePlugin.getIdentifiableDao().save(t);

		return c;
	}

}
