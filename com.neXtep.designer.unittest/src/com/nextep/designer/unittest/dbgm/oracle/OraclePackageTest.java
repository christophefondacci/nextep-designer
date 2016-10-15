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
import com.nextep.datadesigner.sqlgen.model.IPackage;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class OraclePackageTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IPackage> packageV = VersionableFactory.createVersionable(IPackage.class);
		IPackage pkg = packageV.getVersionnedObject().getModel();
		Assert.assertNotNull("Failed to create Oracle package from factory", pkg);
		ITypedObjectUIController controller = testController(pkg, IPackage.TYPE_ID);

		// Instantiating from controller
		packageV = (IVersionable<IPackage>) controller.emptyInstance("EMP_UTILS",
				getFirstContainer());
		pkg = packageV.getVersionnedObject().getModel();
		assertNotNull("Failed to create package from controller", pkg);

		pkg.setBodySourceCode("PACKAGE BODY EMP_UTILS IS\n\tPROCEDURE CREATE_EMP(DEP_ID IN NUMBER) IS\r\n\tbegin\n\t\tinsert into employees (dep_id,emp_id) values (dep_id,SEQ_DEP_ID.nextval);\n\tend;\nend;\n");
		pkg.setSpecSourceCode("PACKAGE EMP_UTILS IS\n\tPROCEDURE CREATE_EMP(DEP_ID IN NUMBER);\r\nend;\n");
		testVersioning(packageV);
	}

	@Override
	public String getName() {
		return "Oracle package creation / versioning";
	}
}
