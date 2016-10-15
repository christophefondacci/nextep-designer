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
package com.nextep.designer.unittest.dbgm.merge;

import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.MergeInfo;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.services.IVersioningService;

public class TableMergerTest extends MergeTestCase<IBasicTable> {

	@Override
	protected void runTest() throws Throwable {

		IBasicTable t1 = (IBasicTable) VersionableTestCase
				.getVersionableByName("EMPLOYEES", IElementType.getInstance(IBasicTable.TYPE_ID),
						VersionableTestCase.getFirstContainer()).getVersionnedObject().getModel();
		assertNotNull("Failed to retrieve Employees table", t1);

		// Testing copy / compare / name change
		testCopyCompare(VersionHelper.getVersionable(t1));

		// Processing table changes for merge test
		IVersionContainer container = VersionableTestCase.getFirstContainer();
		getVersioningService().commit(new NullProgressMonitor(),
				VersionHelper.getVersionable(container));
		IVersionable<?> v = VersionHelper.getVersionable(container);

		// Checking out
		IVersionable<?> coContainer = VCSPlugin.getService(IVersioningService.class)
				.checkOut(new NullProgressMonitor(), v).iterator().next();
		// Our table merge target
		IVersionable<?> empV = VersionableTestCase.getVersionableByName("EMPLOYEES", IElementType
				.getInstance(IBasicTable.TYPE_ID), (IVersionContainer) coContainer
				.getVersionnedObject().getModel());
		// Checkouting
		final IBasicTable initialT = (IBasicTable) empV.getVersionnedObject().getModel();
		IVersionable<?> tgtV = VCSPlugin.getService(IVersioningService.class)
				.checkOut(new NullProgressMonitor(), empV).iterator().next();
		IBasicTable tgt = (IBasicTable) tgtV.getVersionnedObject().getModel();
		IVersionable<?> srcV = VersionableFactory.copy(tgtV);

		IActivity activity = VCSPlugin.getService(IVersioningService.class).getCurrentActivity();

		srcV.setVersion(VersionFactory.buildNextVersionInfo(empV.getVersion(), activity));
		IBasicTable src = (IBasicTable) srcV.getVersionnedObject().getModel();
		CorePlugin.getIdentifiableDao().save(srcV);

		// Altering target
		tgt.setDescription("Target desc");
		IBasicColumn c = tgt.getColumns().get(2);
		ControllerFactory.getController(c).modelDeleted(c);
		UIControllerFactory.getController(IElementType.getInstance(IBasicColumn.TYPE_ID))
				.emptyInstance("MERGE_TGT_COL", tgt);

		c = tgt.getColumns().iterator().next();
		c.setDefaultExpr("123");
		c.setName(c.getName() + "_MERGED_TGT");

		// Altering source
		src.setName("EMPLOYEES_SRC_MRG");
		c = src.getColumns().get(0);
		c.setDescription("Source merge desc");
		c.setNotNull(!c.isNotNull());
		boolean expectedNotNull = c.isNotNull();
		UIControllerFactory.getController(IElementType.getInstance(IBasicColumn.TYPE_ID))
				.emptyInstance("MERGE_SRC_COL", src);

		// Merging
		IMerger m = MergerFactory.getMerger(src.getType(), ComparisonScope.REPOSITORY);
		IComparisonItem i = m.compare(src, tgt);
		IComparisonItem srcRoot = m.compare(initialT, src);
		IComparisonItem tgtRoot = m.compare(initialT, tgt);
		MergeInfo info = m.merge(i, srcRoot, tgtRoot);
		IBasicTable t = (IBasicTable) m.buildMergedObject(i, activity);

		// Asserting
		final IFormatter f = DBGMHelper.getCurrentVendor().getNameFormatter();
		String expectedName = f.format("EMPLOYEES_SRC_MRG");
		assertEquals("Name merge failed", expectedName, t.getName());
		assertEquals("Columns count failed", 4, t.getColumns().size());
		c = t.getColumns().iterator().next();
		assertEquals("Column description merge failed", "Source merge desc", c.getDescription());
		expectedName = f.format("EMP_ID_MERGED_TGT"); 
		assertEquals("Column name merge failed", expectedName, c.getName());
		assertEquals("Column notNull merge failed", expectedNotNull, c.isNotNull());
		assertEquals("Column defaultExpr merge failed", "123", c.getDefaultExpr());
		boolean srcCol = false, tgtCol = false;
		for (IBasicColumn col : t.getColumns()) {
			if ("MERGE_SRC_COL".equalsIgnoreCase(col.getName())) {
				srcCol = true;
			}
			if ("MERGE_TGT_COL".equalsIgnoreCase(col.getName())) {
				tgtCol = true;
			}
		}
		assertTrue("Column source addition merge failed", srcCol);
		assertTrue("Column target addition merge failed", tgtCol);
		// final UID previousView = VersionHelper.getCurrentView().getUID();
		//
		// // Creating temp merge view
		// IVersionView newView = new VersionView("JUnit merge",new Date().toString());
		// IdentifiableDAO.getInstance().save(newView);
		// VersionUIHelper.changeView(newView.getUID());
		//
		// IVersionContainer ciContainer = VersionableTestCase.getFirstContainer();
		// IVersionable<?> srcContainer =
		// VersionUIActions.checkOut(VersionHelper.getVersionable(ciContainer),
		// Activity.getDefaultActivity());
	}

	@Override
	public String getName() {
		return "Table merge test";
	}

	protected IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}
}
