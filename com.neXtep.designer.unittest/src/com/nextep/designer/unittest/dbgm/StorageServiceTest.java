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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import junit.framework.TestCase;
import org.junit.Assert;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.services.IStorageService;
import com.nextep.designer.unittest.helpers.TestHelper;
import com.nextep.designer.vcs.model.IVersionable;

public class StorageServiceTest extends TestCase {

	private static final String RENAMED_HANDLE_NAME = "testhandle";

	@Override
	protected void runTest() throws Throwable {
		final IStorageService storageService = DbgmPlugin.getService(IStorageService.class);

		final IVersionable<?> dataSetV = TestHelper.getVersionableByName(DBGMHelper.getCurrentVendor().getNameFormatter().format("DEPARTMENTS"),
				IElementType.getInstance(IDataSet.TYPE_ID), TestHelper.getFirstContainer());
		final IDataSet dataSet = (IDataSet) dataSetV.getVersionnedObject().getModel();

		// Ensuring we got no storage for this data set
		dataSet.setStorageHandle(null);

		IStorageHandle handle = storageService.createDataSetStorage(dataSet);
		Assert.assertNotNull("Unable to create storage for dataset", handle);

		Connection conn = null;
		Statement stmt = null;
		ResultSet rset = null;
		try {
			conn = storageService.getLocalConnection();
			Assert.assertNotNull("Unable to create local connection", conn);
			String selectStmt = handle.getSelectStatement();
			Assert.assertNotNull(selectStmt);
			Assert.assertNotNull(handle.getInsertStatement());
			stmt = conn.createStatement();
			rset = stmt.executeQuery(selectStmt);
			// Checking we can iterate
			while (rset.next()) {
			}
			rset.close();
			// Renaming dataset handle
			storageService.renameStorageHandle(dataSet, RENAMED_HANDLE_NAME);
			handle = dataSet.getStorageHandle();
			Assert.assertNotNull("Renamed storage handle is null", handle);
			selectStmt = handle.getSelectStatement();
			Assert.assertNotNull("Renamed select statement is null", selectStmt);
			Assert.assertTrue("Renamed select statement does not reference renamed storage",
					selectStmt.contains(RENAMED_HANDLE_NAME));
			String insertStmt = handle.getInsertStatement();
			Assert.assertNotNull(handle.getInsertStatement());
			Assert.assertTrue("Renamed insert statement does not reference renamed storage",
					insertStmt.contains(RENAMED_HANDLE_NAME));
			// Ensuring we can execute query on renamed handle
			rset = stmt.executeQuery(selectStmt);
			while (rset.next()) {
			}
		} finally {
			if (rset != null) {
				rset.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (conn != null) {
				conn.close();
			}
		}
	}

	@Override
	public String getName() {
		return "Storage service test";
	}
}
