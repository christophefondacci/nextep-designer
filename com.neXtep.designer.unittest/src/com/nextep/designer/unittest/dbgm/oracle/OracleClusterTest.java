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
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class OracleClusterTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IOracleCluster> clusterV = VersionableFactory
				.createVersionable(IOracleCluster.class);
		IOracleCluster cluster = clusterV.getVersionnedObject().getModel();
		Assert.assertNotNull("Failed to create Oracle cluster type from factory", cluster);
		ITypedObjectUIController controller = testController(cluster,
				IOracleCluster.CLUSTER_TYPE_ID);

		// Instantiating from controller
		clusterV = (IVersionable<IOracleCluster>) controller.emptyInstance("EMP_CLUSTER",
				getFirstContainer());
		cluster = clusterV.getVersionnedObject().getModel();

		// Retrieving DEP / EMP tables
		IBasicTable empTab = (IBasicTable) getVersionableByName("EMPLOYEES",
				IElementType.getInstance(IBasicTable.TYPE_ID), getFirstContainer())
				.getVersionnedObject().getModel();
		IBasicTable depTab = (IBasicTable) getVersionableByName("DEPARTMENTS",
				IElementType.getInstance(IBasicTable.TYPE_ID), getFirstContainer())
				.getVersionnedObject().getModel();

		cluster.addClusteredTable(empTab.getReference());
		cluster.addClusteredTable(depTab.getReference());
		final IOracleClusteredTable empClTab = cluster.getClusteredTable(empTab.getReference());
		final IOracleClusteredTable depClTab = cluster.getClusteredTable(depTab.getReference());
		assertNotNull("Failed to add a table to the cluster", empClTab);
		assertNotNull("Failed to add a second table to the cluster", depClTab);

		IBasicColumn depIdClCol = (IBasicColumn) UIControllerFactory.getController(
				IElementType.getInstance(IBasicColumn.TYPE_ID)).emptyInstance("DEP_ID", cluster);
		assertTrue("Failed to add a cluster column", cluster.getColumns().contains(depIdClCol));

		empClTab.setColumnReferenceMapping(depIdClCol.getReference(), getDepIdCol(empTab)
				.getReference());
		depClTab.setColumnReferenceMapping(depIdClCol.getReference(), getDepIdCol(depTab)
				.getReference());
		testVersioning(clusterV);
		// Creating the cluster index
		final ITypedObjectUIController indexController = UIControllerFactory
				.getController(IElementType.getInstance(IIndex.INDEX_TYPE));
		IVersionable<IIndex> empIndV = (IVersionable<IIndex>) indexController.emptyInstance(
				"CLUSTER_I", cluster);
		IIndex clusterIndex = (IIndex) empIndV.getVersionnedObject().getModel();
		clusterIndex.setIndexType(IndexType.UNIQUE);
		clusterIndex.addColumnRef(depIdClCol.getReference());
	}

	private IBasicColumn getDepIdCol(IBasicTable t) {
		for (IBasicColumn c : t.getColumns()) {
			if ("DEP_ID".equals(c.getName())) {
				return c;
			}
		}
		fail("No DEP_ID column in table " + t.getName() + ", table test may have failed");
		return null;
	}

	@Override
	public String getName() {
		return "Oracle cluster creation / versioning";
	}
}
