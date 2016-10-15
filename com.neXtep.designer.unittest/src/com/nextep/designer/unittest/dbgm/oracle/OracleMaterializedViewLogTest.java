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
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.model.PhysicalAttribute;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLogPhysicalProperties;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class OracleMaterializedViewLogTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IMaterializedViewLog> logV = VersionableFactory
				.createVersionable(IMaterializedViewLog.class);
		IMaterializedViewLog log = logV.getVersionnedObject().getModel();
		Assert.assertNotNull("Failed to create materialized view log from factory", log);
		ITypedObjectUIController controller = testController(log, IMaterializedViewLog.TYPE_ID);
		IVersionContainer parent = getFirstContainer();

		// Instantiating from controller
		IBasicTable empTab = (IBasicTable) getVersionableByName("EMPLOYEES",
				IElementType.getInstance(IBasicTable.TYPE_ID), getFirstContainer())
				.getVersionnedObject().getModel();
		logV = (IVersionable<IMaterializedViewLog>) controller.emptyInstance("MAT_EMP_LOG", empTab);
		log = (IMaterializedViewLog) logV.getVersionnedObject().getModel();
		assertNotNull("Materialized view log creation failed", log);

		log.setTableReference(empTab.getReference());
		log.setDescription("my mat desc");
		log.setIncludingNewValues(true);
		log.setPrimaryKey(true);
		log.setSequence(true);
		log.setRowId(true);

		controller = testController(null, IMaterializedViewLogPhysicalProperties.TYPE_ID);
		IMaterializedViewLogPhysicalProperties props = (IMaterializedViewLogPhysicalProperties) controller
				.emptyInstance(log.getTable().getName(), log);
		props.setTablespaceName("APPL_DATA");
		props.setCompressed(true);
		props.setLogging(true);
		props.setAttribute(PhysicalAttribute.PCT_FREE, 2);
		props.setAttribute(PhysicalAttribute.MAX_TRANS, 123);

		log.setPhysicalProperties(props);
		// Versioning test
		testVersioning(logV);
	}

	@Override
	public String getName() {
		return "Materialized view log creation / versioning";
	}
}
