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
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * Creates and validates a table in the first container of the current view. The current view must
 * exist and must contain at least 1 container.
 * 
 * @author Christophe
 */
public class BasicTableTest extends VersionableTestCase {

	@Override
	protected void runTest() throws Throwable {
		IVersionable<IBasicTable> depTab = VersionableFactory.createVersionable(IBasicTable.class);
		Assert.assertNotNull("Failed to create versionable from factory", depTab);
		testController(depTab, IBasicTable.TYPE_ID);
		depTab.setName("DEPARTMENTS");
		CorePlugin.getIdentifiableDao().save(depTab);

		// Testing column abilities
		ITypedObjectUIController colCtrl = testController(null, IBasicColumn.TYPE_ID);
		IBasicColumn column = (IBasicColumn) colCtrl.emptyInstance("DEP_ID", depTab);
		Assert.assertNotNull("Failed to create column from controller", column);
		Assert.assertNotNull("Failed to save column", column.getUID());
		column.setNotNull(true);
		// Retrieving emp container
		IVersionContainer parent = getFirstContainer();
		parent.addVersionable(depTab, new ImportPolicyAddOnly());
		Assert.assertTrue("Failed to add table to parent container",
				parent.getContents().contains(depTab));

		// Testing primary key
		ITypedObjectUIController pkCtrl = testController(null, UniqueKeyConstraint.TYPE_ID);
		UniqueKeyConstraint depPK = (UniqueKeyConstraint) pkCtrl.emptyInstance(
				formatPKName("DEP_PK"), depTab);
		Assert.assertNotNull("Unique key creation failed", depPK);
		CorePlugin.getIdentifiableDao().save(depPK);
		depTab.getVersionnedObject().getModel().addConstraint(depPK);
		// IdentifiableDAO.getInstance().save(depTab);

		Assert.assertTrue("Unique key table attachment failed",
				depPK.getConstrainedTable() == depTab);
		depPK.addColumn(column);
		Assert.assertTrue("Unique key addColumn instance failed", depPK.getColumns()
				.contains(column));
		depPK.removeColumn(column);
		Assert.assertTrue("Unique key removeColumn instance failed", !depPK.getColumns()
				.contains(column));
		depPK.addConstrainedReference(column.getReference());
		Assert.assertTrue("Unique key addColumn reference failed", depPK.getColumns()
				.contains(column));
		depPK.setConstraintType(ConstraintType.PRIMARY);
		Assert.assertTrue("Unique key primary switch failed",
				depPK.getConstraintType() == ConstraintType.PRIMARY);

		// Creating a second table with a foreign key to the first one
		IVersionable<IBasicTable> empTab = VersionableFactory.createVersionable(IBasicTable.class);
		empTab.setName("EMPLOYEES");
		CorePlugin.getIdentifiableDao().save(empTab);
		IBasicColumn empIdCol = (IBasicColumn) colCtrl.emptyInstance("EMP_ID", empTab);
		IBasicColumn empDepCol = (IBasicColumn) colCtrl.emptyInstance("DEP_ID", empTab);
		IBasicColumn empDummyCol = (IBasicColumn) colCtrl.emptyInstance("DUMMY_COL", empTab);
		parent.addVersionable(empTab, new ImportPolicyAddOnly());
		UniqueKeyConstraint empPK = (UniqueKeyConstraint) pkCtrl.emptyInstance(
				formatPKName("EMP_PK"), empTab);
		CorePlugin.getIdentifiableDao().save(empPK);
		empTab.getVersionnedObject().getModel().addConstraint(empPK);
		// IdentifiableDAO.getInstance().save(empTab);
		empIdCol.setNotNull(true);
		empPK.addColumn(empIdCol);
		empPK.setConstraintType(ConstraintType.PRIMARY);

		configureAndTestPK(empPK);

		ITypedObjectUIController fkCtrl = testController(null, ForeignKeyConstraint.TYPE_ID);
		ForeignKeyConstraint fk = (ForeignKeyConstraint) fkCtrl.emptyInstance("EMP_DEP_FK", empTab);
		Assert.assertNotNull("Foreign key creation failed", fk);
		fk.setRemoteConstraintRef(depPK.getReference());
		Assert.assertEquals("Foreign key remote constraint setter failed", depPK,
				fk.getRemoteConstraint());
		fk.addConstrainedReference(empDepCol.getReference());
		Assert.assertTrue("Foreign key add constrained column failed", fk.getColumns()
				.contains(empDepCol));
		fk.removeColumn(empDepCol);
		Assert.assertTrue("Foreign key remove constrained column failed", !fk
				.getColumns().contains(empDepCol));
		fk.addColumn(empDepCol);
		Assert.assertTrue("Foreign key add constrained column failed", fk
				.getConstrainedColumnsRef().contains(empDepCol.getReference()));
		fk.checkConsistency();

		// Version test
		testVersioning(depTab);
		testVersioning(empTab);
	}

	@Override
	public String getName() {
		return "Table creation / versioning";
	}

	protected void configureAndTestPK(UniqueKeyConstraint empPK) {

	}

	protected String formatPKName(String name) {
		return name;
	}
}
