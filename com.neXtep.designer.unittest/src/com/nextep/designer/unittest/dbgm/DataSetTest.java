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
import junit.framework.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.dbgm.DbgmPlugin;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.IDataLine;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.dbgm.services.IStorageService;
import com.nextep.designer.sqlclient.ui.SQLClientPlugin;
import com.nextep.designer.sqlclient.ui.model.INextepMetadata;
import com.nextep.designer.sqlclient.ui.model.ISQLQuery;
import com.nextep.designer.sqlclient.ui.model.ISQLQueryListener;
import com.nextep.designer.sqlclient.ui.services.ISQLClientService;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class DataSetTest extends VersionableTestCase {

	@Override
	protected void runTest() throws Throwable {
		IVersionable<IDataSet> depData = VersionableFactory.createVersionable(IDataSet.class);
		Assert.assertNotNull("Failed to create versionable from factory", depData);
		final IDataSet set = depData.getVersionnedObject().getModel();
		testController(depData, IDataSet.TYPE_ID);
		depData.setName("DEPARTMENTS");
		// Filling parent table
		final IBasicTable depTab = (IBasicTable) CorePlugin.getService(IReferenceManager.class)
				.findByTypeName(IElementType.getInstance(IBasicTable.TYPE_ID), "DEPARTMENTS", true);
		set.setTableReference(depTab.getReference());
		// Filling columns
		for (IBasicColumn c : depTab.getColumns()) {
			set.addColumn(c);
		}
		// Saving
		CorePlugin.getIdentifiableDao().save(depData);
		// Retrieving emp container
		IVersionContainer parent = getFirstContainer();
		parent.addVersionable(depData, new ImportPolicyAddOnly());
		Assert.assertTrue("Failed to add table to parent container",
				parent.getContents().contains(depData));

		IDataService dataService = DbgmPlugin.getService(IDataService.class);
		ITypedObjectFactory factory = CorePlugin.getService(ITypedObjectFactory.class);
		IDataLine line = factory.create(IDataLine.class);
		IDataLine line2 = factory.create(IDataLine.class);
		Assert.assertNotNull("Failed to create dataline from factory", line);
		line.setDataSet(set);
		line.setRowId(set.getCurrentRowId());
		for (IReference c : set.getColumnsRef()) {
			IColumnValue val = factory.create(IColumnValue.class);
			IColumnValue val2 = factory.create(IColumnValue.class);
			Assert.assertNotNull("Failed to create data column value from factory", val);
			val.setColumnRef(c);
			val.setValue(1l);
			line.addColumnValue(val);
			val2.setColumnRef(c);
			val2.setValue(2l);
			line2.addColumnValue(val2);
		}
		// Adding line
		dataService.addDataline(set, line, line2);
		// Pushing set contents to repository
		dataService.saveDataLinesToRepository(set, new NullProgressMonitor());
		// Testing data set versioning
		testVersioning(depData);

		IVersionable<?> checkedOutSetV = getVersioningService().checkOut(getMonitor(), depData)
				.iterator().next();
		final IDataSet checkedOutSet = (IDataSet) checkedOutSetV.getVersionnedObject().getModel();
		dataService.loadDataLinesFromRepository(checkedOutSet, getMonitor());
		// Now we will manually alter the dataset
		final ISQLClientService clientService = SQLClientPlugin.getService(ISQLClientService.class);
		final IStorageService storageService = DbgmPlugin.getService(IStorageService.class);
		final Connection localConnection = storageService.getLocalConnection();
		IStorageHandle handle = checkedOutSet.getStorageHandle();
		Assert.assertNotNull("The data set should have a valid storage handle at this point",
				handle);
		final IFormatter f = DBGMHelper.getCurrentVendor().getNameFormatter();
		final String tableName = f.format("DEPARTMENTS");
		final String columnName = "\"" + f.format("DEP_ID") + "\"";
		clientService.runQuery(localConnection, "update " + tableName + " set " + columnName
				+ "=1111 where " + columnName + "=1", new ISQLQueryListener() {

			@Override
			public void queryStarted(ISQLQuery query) {
			}

			@Override
			public void queryResultMetadataAvailable(ISQLQuery query, long executionTime,
					INextepMetadata md) {
			}

			@Override
			public void queryFinished(ISQLQuery query, long execTime, long totalTime,
					int resultCount, boolean isResultSet) {
			}

			@Override
			public void queryFailed(ISQLQuery query, Exception e) {
				Assert.fail("Update dataset query '" + query + "' failed with message : "
						+ e.getMessage());
			}
		});
		// Sleeping to make sure query is properly executed since asynchronous
		Thread.sleep(1000);
		dataService.saveDataLinesToRepository(checkedOutSet, getMonitor());
		// Resetting local storage
		checkedOutSet.setStorageHandle(null);
		dataService.loadDataLinesFromRepository(checkedOutSet, getMonitor());
		handle = checkedOutSet.getStorageHandle();
		Assert.assertNotNull("Storage handle should have been re-initialized at this point", handle);
		final String dataSelect = handle.getSelectStatement();
		Statement stmt = null;
		ResultSet rset = null;
		try {
			stmt = localConnection.createStatement();
			rset = stmt.executeQuery(dataSelect);
			int linesCount = 0;
			while (rset.next()) {
				linesCount++;
				long idVal = rset.getLong(1);
				if (idVal != 2 && idVal != 1111) {
					Assert.fail("Expected new value failure, should be 1111 or 2 and was " + idVal);
				}
			}
			Assert.assertEquals("Only 2 lines expected", 2, linesCount);
		} finally {
			if (rset != null) {
				rset.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	private IProgressMonitor getMonitor() {
		return new NullProgressMonitor();
	}

	@Override
	public String getName() {
		return "Dataset test";
	}
}
