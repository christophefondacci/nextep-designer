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

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.ITrigger;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class TriggerTest extends VersionableTestCase {

	@Override
	protected void runTest() throws Throwable {
		IVersionable<ITrigger> trgV = VersionableFactory.createVersionable(ITrigger.class);
		assertNotNull("Failed to create versionable from factory", trgV);
		@SuppressWarnings("unused")
		ITypedObjectUIController controller = testController(trgV, ITrigger.TYPE_ID);
		trgV.setName("BIU_EMP");

		IVersionContainer container = getFirstContainer();
		IVersionable<?> empV = getVersionableByName("EMPLOYEES",
				IElementType.getInstance(IBasicTable.TYPE_ID), container);
		IBasicTable empTab = (IBasicTable) empV.getVersionnedObject().getModel();
		final ITrigger t = trgV.getVersionnedObject().getModel();
		trgV.setContainer(container);
		t.setTriggableRef(empTab.getReference());
		CorePlugin.getIdentifiableDao().save(t);
		container.addVersionable(trgV, new ImportPolicyAddOnly());
		empTab.addTrigger(t);
		t.setCustom(true);
		// t.setTime(TriggerTime.BEFORE);
		// t.addEvent(TriggerEvent.INSERT);
		// assertTrue("Triggable event not added properly", t.getEvents()
		// .contains(TriggerEvent.INSERT));
		// t.addEvent(TriggerEvent.UPDATE);
		t.setSourceCode("TRIGGER BIU_EMP BEFORE INSERT ON EMPLOYEES\nFOR EACH ROW\ndeclare\n\tmyvar number;\nbegin\n\tselect 1 into myvar from dual;\nend;\n");
		testVersioning(trgV);
	}

	@Override
	public String getName() {
		return "Trigger creation / versioning";
	}

}
