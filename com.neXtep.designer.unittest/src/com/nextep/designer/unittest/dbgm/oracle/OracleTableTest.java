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

import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueConstraint;
import com.nextep.designer.dbgm.oracle.impl.external.OracleIndexPhysicalProperties;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.dbgm.BasicTableTest;

public class OracleTableTest extends BasicTableTest {

	@Override
	protected void configureAndTestPK(UniqueKeyConstraint empPK) {
		// Testing physical properties
		ITypedObjectUIController physCtrl = testController(null, IIndexPhysicalProperties.TYPE_ID);
		assertNotNull("Unable to retrieve physical properties controller", physCtrl);
		OracleIndexPhysicalProperties phys = (OracleIndexPhysicalProperties) physCtrl.emptyInstance("EMP_PK",
				empPK);
		assertNotNull("Failed to instantiate physical properties", phys);
		phys.setTablespaceName("APPL_DATA");
		phys.setAttribute(PhysicalAttribute.PCT_FREE, new Integer(5));
		((OracleUniqueConstraint) empPK).setPhysicalProperties(phys);
		assertEquals("Failed to attach physical properties to unique key", phys,
				((OracleUniqueConstraint) empPK).getPhysicalProperties());

	}
}
