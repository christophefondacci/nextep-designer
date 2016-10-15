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
package com.nextep.designer.unittest.base;

import junit.framework.Assert;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public abstract class AbstractProcedureTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IProcedure> procV = VersionableFactory.createVersionable(IProcedure.class);
		IProcedure proc = procV.getVersionnedObject().getModel();
		Assert.assertNotNull("Failed to create procedure from factory", proc);
		ITypedObjectUIController controller = testController(proc, IProcedure.TYPE_ID);

		// Instantiating from controller
		procV = (IVersionable<IProcedure>) controller.emptyInstance("PROC", getFirstContainer());
		proc = procV.getVersionnedObject().getModel();
		assertNotNull("Failed to create procedure from controller", proc);
		setProcedureSql(proc);

		testVersioning(procV);
	}

	protected abstract void setProcedureSql(IProcedure proc);

	@Override
	public String getName() {
		return "Procedure creation / versioning";
	}
}
