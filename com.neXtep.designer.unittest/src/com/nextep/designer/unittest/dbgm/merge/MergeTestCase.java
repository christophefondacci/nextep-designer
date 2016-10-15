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

import junit.framework.TestCase;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.services.IVersioningService;

public class MergeTestCase<T extends IReferenceable> extends TestCase {

	protected void testCopyCompare(IVersionable<T> t1) {
		T t2 = VersionableFactory.copy(t1).getVersionnedObject().getModel();
		assertNotNull("Failed to copy table", t2);

		// Retrieving table merger
		IMerger m = MergerFactory.getMerger(t1.getType(), ComparisonScope.REPOSITORY);
		assertNotNull("Failed to obtain a table merger", m);

		// First comparing raw copy
		IActivity activity = VCSPlugin.getService(IVersioningService.class).getCurrentActivity();
		VersionHelper.getVersionable(t2).setVersion(
				VersionFactory.buildNextVersionInfo(t1.getVersion(), activity));
		IComparisonItem result = m.compare(t1, t2);
		assertEquals("Raw copy returned differing objects", DifferenceType.EQUALS,
				result.getDifferenceType());

		((INamedObject) t2).setName("EMP2");
		result = m.compare(t1, t2);
		assertEquals("Name does not affect comparison", DifferenceType.DIFFER,
				result.getDifferenceType());
		for (IComparisonItem i : result.getSubItems()) {
			if (i instanceof ComparisonAttribute
					&& Merger.ATTR_NAME.equals(((ComparisonAttribute) i).getName())) {
				assertEquals("Name does not affect comparison", DifferenceType.DIFFER,
						i.getDifferenceType());
			} else {
				String name = null;
				if (i instanceof ComparisonAttribute) {
					name = ((ComparisonAttribute) i).getName();
				}
				assertEquals("Other items affected by name change: " + name, DifferenceType.EQUALS,
						i.getDifferenceType());
			}
		}
	}
}
