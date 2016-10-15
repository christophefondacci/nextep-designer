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

import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.CommandProgress;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.services.VersionUIHelper;

public class ContainerComparisonTest extends VersionableTestCase {

	@Override
	protected void runTest() throws Throwable {
		IVersionContainer c = getFirstContainer();
		IVersionable<IVersionContainer> v = VersionHelper.getVersionable(c);

		IVersionable<?> checkedInTab = VersionHelper
				.getAllVersionables(c, IElementType.getInstance(IBasicTable.TYPE_ID)).iterator()
				.next();
		getVersioningService().commit(new NullProgressMonitor(), v);
		final IVersionable<?> checkedInContainer = v;
		final IVersionable<?> checkedOutContainer = getVersioningService()
				.checkOut(new NullProgressMonitor(), v).iterator().next();
		final IVersionable<?> checkedOutTab = getVersioningService()
				.checkOut(new NullProgressMonitor(), checkedInTab).iterator().next();
		VersionUIHelper.changeView(VersionHelper.getCurrentView().getUID());
		// Sandbox test (delivery generation bug)
		CommandProgress.runWithProgress(new ICommand() {

			@Override
			public Object execute(Object... parameters) {
				IVersionable<?> tempV = (IVersionable<?>) CorePlugin.getIdentifiableDao().load(
						IVersionable.class, checkedOutContainer.getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), true);
				CorePlugin.getService(IReferenceManager.class).flushVolatiles(
						HibernateUtil.getInstance().getSandBoxSession());
				IVersionable<?> tempVPrev = (IVersionable<?>) CorePlugin.getIdentifiableDao().load(
						IVersionable.class, checkedInContainer.getUID(),
						HibernateUtil.getInstance().getSandBoxSession(), false);

				// Retrieving temp versionable by its ref (exception if ko)
				// VersionHelper.getReferencedItem(checkedOut.getReference());
				// VersionHelper.getReferencedItem(v.getReference());
				// assertTrue("Sandboxed versionable is not volatile",tempV.getReference().isVolatile());
				// VersionHelper.getReferencedItem(tempVPrev.getReference());
				// assertTrue("Sandboxed previous versionable is not volatile",tempVPrev.getReference().isVolatile());
				return null;
			}

			@Override
			public String getName() {
				return "Testing thread sandbox versionable load...";
			}
		});
		// VersionUIActions.undoCheckOut(checkedOut);
	}

	@Override
	public String getName() {
		return "Container comparison volatile reference test";
	}
}
