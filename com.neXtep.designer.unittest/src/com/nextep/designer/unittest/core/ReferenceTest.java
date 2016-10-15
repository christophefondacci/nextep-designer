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
package com.nextep.designer.unittest.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import com.nextep.datadesigner.dbgm.impl.VersionedTable;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class ReferenceTest extends TestCase {

	Map<IReference, IReferenceable> refMap;
	Collection<IReference> refCol;

	@Override
	protected void setUp() throws Exception {
		refMap = new HashMap<IReference, IReferenceable>();
		refCol = new ArrayList<IReference>();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {

		// First creating a referenceable entity
		IVersionable<IBasicTable> v = VersionableFactory.createVersionable(IBasicTable.class);
		assertNotNull("Unable to create versionable for reference test", v);
		v.setName("REFTEST_TAB");
		assertTrue("Referenceable initialized with unpersisted reference", v.getReference()
				.getUID() != null);
		assertNull("Factory versionable has been persisted", v.getUID());
		assertTrue("Unsaved referenceable should have volatile reference", v.getReference()
				.isVolatile());
		// Saving reference
		final IReference ref = v.getReference();
		// Map & collection addition
		refMap.put(v.getReference(), v);
		refCol.add(v.getReference());

		// Saving table
		CorePlugin.getIdentifiableDao().save(v);
		assertFalse("Unsaved referenceable should have volatile reference", v.getReference()
				.isVolatile());
		// Trying to retrieve object
		assertNotNull("Unable to retrieve persisted reference in non persisted map",
				refMap.get(v.getReference()));
		assertTrue("Unable to retrieve persisted reference in non-persisted collection",
				refCol.contains(v.getReference()));

		// Volatile test
		IVersionable<IBasicTable> volatileTable = (IVersionable<IBasicTable>) CorePlugin
				.getIdentifiableDao().load(VersionedTable.class, v.getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), true);
		assertTrue("Loaded volatile table should have a volatile reference", volatileTable
				.getReference().isVolatile());

		assertTrue("Non-volatile reference equals() failed with volatile ref",
				ref.equals(volatileTable.getReference()));
		assertTrue("Volatile reference equals() failed with non-volatile ref", volatileTable
				.getReference().equals(ref));
		assertTrue("Volatile reference hashCode differs from non-volatile hashCode",
				ref.hashCode() == volatileTable.getReference().hashCode());

		assertNotNull("Unable to retrieve map reference from volatile",
				refMap.get(volatileTable.getReference()));
		assertTrue("Unable to retrieve collection reference from volatile",
				refCol.contains(volatileTable.getReference()));

		// Finally checking reference manager
		IVersionable<IBasicTable> refVolTab = (IVersionable<IBasicTable>) VersionHelper
				.getReferencedItem(volatileTable.getReference());
		assertEquals("Volatile referenceable cannot be retrieved from ReferenceManager",
				volatileTable, refVolTab);

		IVersionable<IBasicTable> refTab = (IVersionable<IBasicTable>) VersionHelper
				.getReferencedItem(ref);
		assertEquals("Referenceable cannot be retrieved from ReferenceManager", v, refTab);

	}

	@Override
	public String getName() {
		return "Reference test";
	}
}
