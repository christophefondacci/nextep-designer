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
package com.nextep.designer.unittest.base;

import junit.framework.Assert;
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
 * A base class for customizing a table test
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractTableTest extends VersionableTestCase {

	@Override
	protected void runTest() throws Throwable {
		IVersionable<IBasicTable> depTab = VersionableFactory.createVersionable(IBasicTable.class);
		Assert.assertNotNull("Failed to create versionable from factory", depTab);
		testController(depTab, IBasicTable.TYPE_ID);
		depTab.setName(getTableName());
		CorePlugin.getIdentifiableDao().save(depTab);

		final IBasicTable tab = depTab.getVersionnedObject().getModel();
		addTableColumns(tab);
		// Retrieving emp container
		IVersionContainer parent = getFirstContainer();
		parent.addVersionable(depTab, new ImportPolicyAddOnly());
		Assert.assertTrue("Failed to add table to parent container",
				parent.getContents().contains(depTab));
		testTable(tab);
		testVersioning(depTab);
	}

	protected abstract String getTableName();

	protected final IBasicColumn createAndTestColumn(String name, IBasicTable table) {
		// Testing column abilities
		ITypedObjectUIController colCtrl = testController(null, IBasicColumn.TYPE_ID);
		IBasicColumn column = (IBasicColumn) colCtrl.emptyInstance(name, table);
		Assert.assertNotNull("Failed to create column from controller", column);
		Assert.assertNotNull("Failed to save column", column.getUID());
		return column;
	}

	/**
	 * Add columns to the table. Implementors can use the
	 * {@link AbstractTableTest#createAndTestColumn(String, IBasicTable)} to facilitate test column
	 * creation.
	 * 
	 * @param table table to add columns to
	 */
	protected abstract void addTableColumns(IBasicTable table);

	/**
	 * Specific table tests
	 * 
	 * @param table table to test
	 */
	protected abstract void testTable(IBasicTable table);

}
