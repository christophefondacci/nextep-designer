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
import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.unittest.helpers.TestHelper;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.services.IVersioningService;

public abstract class VersionableTestCase extends TestCase {

	protected ITypedObjectUIController testController(ITypedObject o, String typeId) {
		IElementType type = IElementType.getInstance(typeId);
		Assert.assertNotNull(typeId + " type not defined", type);
		ITypedObjectUIController typeController = UIControllerFactory.getController(type);
		Assert.assertNotNull(typeId + " type controller not defined", typeController);
		if (o != null) {
			ITypedObjectUIController instanceController = UIControllerFactory.getController(o);
			Assert.assertEquals(typeId + " dynamic controller not found", typeController,
					instanceController);
			return instanceController;
		} else {
			return typeController;
		}
	}

	/**
	 * Tests versioning capabilities of the given versionable. The specified versionable will be
	 * checked in, checked out, and undone if the test succeeds, leaving the view as before.
	 * 
	 * @param <T>
	 * @param v
	 * @return the checked out versionable
	 */
	protected void testVersioning(final IVersionable<?> v) {
		// First we open editor
		UIControllerFactory.getController(v).defaultOpen(v);
		// Versioning features
		getVersioningService().commit(new NullProgressMonitor(), v);
		assertTrue(v.getType().getName() + " commit failed", v.getVersionnedObject()
				.updatesLocked());
		assertEquals(v.getType().getName() + " commit failed", v.getVersion().getStatus(),
				IVersionStatus.CHECKED_IN);

		final IVersionable<?> checkedOut = getVersioningService()
				.checkOut(new NullProgressMonitor(), v).iterator().next();
		assertTrue(v.getType().getName() + " check out failed", checkedOut.getUID().rawId() != v
				.getUID().rawId());
		assertTrue(v.getType().getName() + " check out failed", !checkedOut.getVersionnedObject()
				.updatesLocked());
		assertEquals(v.getType().getName() + " check out failed", checkedOut.getVersion()
				.getStatus(), IVersionStatus.CHECKED_OUT);

		// Comparing objects
		IMerger m = MergerFactory.getMerger(checkedOut, ComparisonScope.REPOSITORY);
		assertNotNull("Cannot find " + checkedOut.getType().getName() + " merger", m);
		IComparisonItem result = m.compare(v, checkedOut);
		assertEquals("Checked out object differs from initial object", DifferenceType.EQUALS,
				result.getDifferenceType());
		// Checking view alignment
		assertTrue("View does not contain checked out " + v.getType().getName(), VersionHelper
				.getCurrentView().getReferenceMap().values().contains(checkedOut));

		// Sandbox test (delivery generation bug)
		CommandProgress.runWithProgress(new ICommand() {

			@Override
			public Object execute(Object... parameters) {
				IVersionable<?> tempV = (IVersionable<?>) CorePlugin.getIdentifiableDao().load(
						IVersionable.class, checkedOut.getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), true);
				CorePlugin.getService(IReferenceManager.class).flushVolatiles(
						HibernateUtil.getInstance().getSandBoxSession());
				IVersionable<?> tempVPrev = (IVersionable<?>) CorePlugin.getIdentifiableDao().load(
						IVersionable.class, v.getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), true);

				// Retrieving temp versionable by its ref (exception if ko)
				VersionHelper.getReferencedItem(tempV.getReference());
				assertTrue("Sandboxed versionable is not volatile", tempV.getReference()
						.isVolatile());
				VersionHelper.getReferencedItem(tempVPrev.getReference());
				assertTrue("Sandboxed previous versionable is not volatile", tempVPrev
						.getReference().isVolatile());
				return null;
			}

			@Override
			public String getName() {
				return "Testing thread sandbox versionable load...";
			}
		});
		// Retrieving view versionable by its ref (exception if ko)
		VersionHelper.getReferencedItem(v.getReference());
		assertFalse("View versionable is volatile", v.getReference().isVolatile());

		// Undoing checkout
		getVersioningService().undoCheckOut(new NullProgressMonitor(), checkedOut);
		assertTrue("Failed to undo checkout: previous item has not been restored in current view",
				VersionHelper.getCurrentView().getReferenceMap().values().contains(v));

	}

	/**
	 * @return the first container of the current view. If the container is checked in, it will be
	 *         checked out automatically
	 */
	public static IVersionContainer getFirstContainer() {
		return TestHelper.getFirstContainer();
	}

	/**
	 * Retrieves a versionable in the specified container from its name.<br>
	 * Test will fail if no such versionable can be found.
	 * 
	 * @param name name of the versionable to look for
	 * @param type the type of element to look for
	 * @param container container to search into
	 * @return the corresponding versionable
	 */
	public static IVersionable<?> getVersionableByName(String name, IElementType type,
			IVersionContainer container) {
		return TestHelper.getVersionableByName(name, type, container);
	}

	protected IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}
}
