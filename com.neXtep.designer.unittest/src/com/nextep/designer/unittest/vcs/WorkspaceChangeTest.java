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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;
import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.impl.Workspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

public class WorkspaceChangeTest extends TestCase {

	private DBVendor vendor;
	private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$

	public WorkspaceChangeTest(DBVendor vendor) {
		this.vendor = vendor;
	}

	@Override
	protected void runTest() throws Throwable {
		final IWorkspaceService viewService = VCSPlugin.getViewService();

		IWorkspace view = new Workspace(vendor.name() + "_" + dateFormat.format(new Date()),
				"test");
		view.setDBVendor(vendor);
		CorePlugin.getIdentifiableDao().save(view);

		viewService.changeWorkspace(view.getUID(), new NullProgressMonitor());
	}

	@Override
	public String getName() {
		return "Workspace creation / change";
	}
}
