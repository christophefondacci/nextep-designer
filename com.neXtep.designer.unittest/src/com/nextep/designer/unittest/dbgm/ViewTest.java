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
package com.nextep.designer.unittest.dbgm;

import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class ViewTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IView> viewV = VersionableFactory.createVersionable(IView.class);
		assertNotNull("Failed to create versionable from factory", viewV);
		ITypedObjectUIController controller = testController(viewV, IView.TYPE_ID);
		viewV.setName("VIEW_TEST");

		viewV = (IVersionable<IView>) controller.emptyInstance("VIEW_TEST", getFirstContainer());
		assertNotNull("Failed to instantiate view from controller", viewV);
		final IView view = viewV.getVersionnedObject().getModel();
		view.setSQLDefinition("select 1 as col1 from employees;");
		testVersioning(viewV);
	}

	@Override
	public String getName() {
		return "View creation / versioning";
	}
}
