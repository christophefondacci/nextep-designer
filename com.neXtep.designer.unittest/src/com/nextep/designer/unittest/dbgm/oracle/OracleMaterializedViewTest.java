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
package com.nextep.designer.unittest.dbgm.oracle;

import junit.framework.Assert;
import com.nextep.designer.dbgm.oracle.model.BuildType;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.MaterializedViewType;
import com.nextep.designer.dbgm.oracle.model.RefreshTime;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class OracleMaterializedViewTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IMaterializedView> viewV = VersionableFactory
				.createVersionable(IMaterializedView.class);
		IMaterializedView view = viewV.getVersionnedObject().getModel();
		Assert.assertNotNull("Failed to create materialized view from factory", view);
		ITypedObjectUIController controller = testController(view, IMaterializedView.VIEW_TYPE_ID);
		IVersionContainer parent = getFirstContainer();

		// Instantiating from controller
		viewV = (IVersionable<IMaterializedView>) controller.emptyInstance("MAT_EMP", parent);
		view = (IMaterializedView) viewV.getVersionnedObject().getModel();
		assertNotNull("Materialized view creation failed", view);

		view.setBuildType(BuildType.IMMEDIATE);
		view.setDescription("my mat desc");
		view.setNextExpr("sysdate + 1");
		view.setStartExpr("sysdate");
		view.setQueryRewriteEnabled(true);
		view.setRefreshTime(RefreshTime.SPECIFY);
		view.setSql("select * from employees;");
		view.setViewType(MaterializedViewType.ROWID);

		// Versioning test
		testVersioning(viewV);
	}

	@Override
	public String getName() {
		return "Materialized view creation / versioning";
	}
}
