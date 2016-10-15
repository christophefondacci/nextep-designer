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
import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

public class ContainerTest extends VersionableTestCase {

	@SuppressWarnings("unchecked")
	@Override
	protected void runTest() throws Throwable {
		IVersionable<IVersionContainer> container = VersionableFactory
				.createVersionable(IVersionContainer.class);
		Assert.assertNotNull("Container factory creation fails", container);
		container.setName("Junit Test");
		ITypedObjectUIController instanceController = testController(container,
				IVersionContainer.TYPE_ID);

		// Here we start to create top-level functional container
		container = (IVersionable<IVersionContainer>) instanceController.emptyInstance("Employees",
				VersionHelper.getCurrentView());
		Assert.assertNotNull("Container empty instantiation failed", container);
		container.setDescription("JUnit test description");
		Assert.assertEquals("Container update failed", "JUnit test description",
				container.getDescription());

		// Versioning features
		testVersioning(container);
		getVersioningService().checkOut(new NullProgressMonitor(), container);
	}

	@Override
	public String getName() {
		return "Container creation / versioning";
	}
}
