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

import java.math.BigDecimal;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.vcs.VersionableTestCase;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class SequenceTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<ISequence> seqV = VersionableFactory.createVersionable(ISequence.class);
		assertNotNull("Failed to create versionable from factory", seqV);
		ITypedObjectUIController controller = testController(seqV, ISequence.TYPE_ID);
		seqV.setName("SEQ_DEP_ID");
		
		seqV = (IVersionable<ISequence>)controller.emptyInstance("SEQ_DEP_ID", getFirstContainer());
		assertNotNull("Failed to instantiate sequence from controller",seqV);
		final ISequence seq = seqV.getVersionnedObject().getModel();
		seq.setCached(true);
		seq.setCacheSize(10);
		seq.setCycle(false);
		seq.setIncrement(1L);
		seq.setMaxValue(new BigDecimal("9999999999"));
		seq.setStart(new BigDecimal("1000000001"));
		seq.setMinValue(new BigDecimal("1000000000"));
		testVersioning(seqV);
	}
	@Override
	public String getName() {
		return "Sequence creation / versioning";
	}
}
