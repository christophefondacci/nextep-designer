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

import junit.framework.Assert;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * This test case dependes on {@link BasicTableTest} which should have been executed successfully
 * before this one
 * 
 * @author Christophe
 */
public class IndexTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IIndex> empIndV = VersionableFactory.createVersionable(IIndex.class);
		IIndex empInd = (IIndex) empIndV.getVersionnedObject().getModel();
		Assert.assertNotNull("Failed to create index from factory", empInd);
		ITypedObjectUIController controller = testController(empInd, IIndex.INDEX_TYPE);
		IVersionContainer parent = getFirstContainer();

		// Retrieving parent table
		IVersionable<?> empV = getVersionableByName(DBGMHelper.getCurrentVendor().getNameFormatter().format("EMPLOYEES"),
				IElementType.getInstance(IBasicTable.TYPE_ID), parent);
		IBasicTable empTab = (IBasicTable) empV.getVersionnedObject().getModel();
		empIndV = (IVersionable<IIndex>) controller.emptyInstance("EMP_DEP_FK_I", empTab);
		empInd = empIndV.getVersionnedObject().getModel();
		assertNotNull("Index creation failed", empInd);

		// Retrieving column to index
		IBasicColumn empDepCol = null;
		for (IBasicColumn col : empTab.getColumns()) {
			if ("DEP_ID".equalsIgnoreCase(col.getName())) {
				empDepCol = col;
			}
		}
		assertNotNull("Column DEP_ID not found", empDepCol);
		empInd.addColumnRef(empDepCol.getReference());
		assertTrue("Failed to add index column", empInd.getColumns().contains(empDepCol));
		assertTrue("Failed to add index column reference",
				empInd.getIndexedColumnsRef().contains(empDepCol.getReference()));
		testVersioning(empIndV);
	}

	@Override
	public String getName() {
		return "Index creation / versioning";
	}
}
