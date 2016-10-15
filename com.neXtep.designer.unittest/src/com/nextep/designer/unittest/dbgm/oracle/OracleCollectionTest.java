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
import com.nextep.designer.dbgm.model.IUserCollection;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class OracleCollectionTest extends VersionableTestCase {
	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IUserCollection> typeV = VersionableFactory.createVersionable(IUserCollection.class);
		IUserCollection type = typeV.getVersionnedObject().getModel();
		Assert.assertNotNull("Failed to create user collection type from factory", type);
		ITypedObjectUIController controller = testController(type, IUserCollection.TYPE_ID);
		
		// Instantiating from controller
		typeV = (IVersionable<IUserCollection>)controller.emptyInstance("CUSTOM_COLL",getFirstContainer());
		
			// Retrieving former user type
		type = typeV.getVersionnedObject().getModel();
		assertNotNull("User-Collection creation failed",type);
		
		type.setDatatype(new Datatype("VARCHAR2",200));
		
		// Versioning test
		testVersioning(typeV);
	}
	
	@Override
	public String getName() {
		return "User-collection creation / versioning";
	}

}
