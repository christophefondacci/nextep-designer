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
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.TypeColumn;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class OracleUserTypeTest extends VersionableTestCase {

	
	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IUserType> typeV = VersionableFactory.createVersionable(IUserType.class);
		IOracleUserType type = (IOracleUserType)typeV.getVersionnedObject().getModel();
		Assert.assertNotNull("Failed to create user type from factory", type);
		ITypedObjectUIController controller = testController(type, IUserType.TYPE_ID);
		IVersionContainer parent = getFirstContainer();
		
		// Instantiating from controller
		typeV = (IVersionable<IUserType>)controller.emptyInstance("CUSTOM_TYPE",parent);
		type = (IOracleUserType)typeV.getVersionnedObject().getModel();
		assertNotNull("User-Type creation failed",type);
		
		// Type column
		ITypedObjectUIController colController = testController(new TypeColumn(), ITypeColumn.TYPE_ID);
		ITypeColumn col = (ITypeColumn)colController.emptyInstance("TYPE_COL", type);
		assertTrue("Failed to add a user-type column", type.getColumns().contains(col));
		col.setDatatype(Datatype.getDefaultDatatype());

		// Versioning test
		testVersioning(typeV);
	}
	
	@Override
	public String getName() {
		return "User-type creation / versioning";
	}
}
