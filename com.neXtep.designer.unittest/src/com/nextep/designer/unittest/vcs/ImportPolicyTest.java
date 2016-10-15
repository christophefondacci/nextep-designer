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
package com.nextep.designer.unittest.vcs;

import junit.framework.Assert;
import junit.framework.TestCase;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.model.base.AbstractImportPolicy;

public class ImportPolicyTest extends TestCase {

	/**
	 * This policy fails on exist or unexist depending on the constructor flag.
	 * 
	 * @author Christophe Fondacci
	 */
	private class TestPolicy extends AbstractImportPolicy {

		private boolean failOnExist;
		private boolean returnedValue;

		public TestPolicy(boolean failOnExist, boolean returnedValue) {
			this.failOnExist = failOnExist;
			this.returnedValue = returnedValue;

		}

		protected boolean existingObject(IVersionable<?> importing, IVersionable<?> existing,
				IActivity activity) {
			if (failOnExist) {
				Assert.fail("Existing object called");
			}
			return returnedValue;
		};

		@Override
		protected boolean unexistingObject(IVersionable<?> importing,
				IVersionContainer targetContainer) {
			if (!failOnExist) {
				Assert.fail("Unexisting object called");
			}
			targetContainer.getContents().add(importing);
			return returnedValue;
		}

		@Override
		protected void beforeImport(IVersionable<?> v, IVersionContainer c, IActivity activity) {

		}

		@Override
		public void finalizeImport() {

		}
	}

	@Override
	public String getName() {
		return "Import policy test";
	}

	@Override
	protected void runTest() throws Throwable {
		final IImportPolicy existPolicy = new TestPolicy(false, true);
		final IImportPolicy notExistPolicy = new TestPolicy(true, true);
		final IImportPolicy existPolicyNotAdding = new TestPolicy(false, false);
		final IVersionable<IVersionContainer> v = VersionableFactory
				.createVersionable(IVersionContainer.class);
		final IVersionContainer c = v.getVersionnedObject().getModel();

		// Let's add a table
		final IVersionable<IVersionContainer> subV = VersionableFactory
				.createVersionable(IVersionContainer.class);
		final IVersionContainer subC = subV.getVersionnedObject().getModel();
		subV.setName("SUB_CONTAINER");

		// Check the addition
		c.addVersionable(subV, notExistPolicy);
		// Check second addition
		c.addVersionable(subV, existPolicy);
		try {
			c.addVersionable(subV, existPolicyNotAdding);
			Assert.fail("The non-addition from the policy should raise an exception");
		} catch (ErrorException e) {
			// Normal use case
		}

		// Adding tab in sub container
		final IVersionable<IBasicTable> vt = VersionableFactory
				.createVersionable(IBasicTable.class);
		final IBasicTable t = vt.getVersionnedObject().getModel();
		vt.setName("MYTAB");
		subC.addVersionable(vt, notExistPolicy);

		// Adding same table in parent container
		c.addVersionable(vt, existPolicy);
	}
}
