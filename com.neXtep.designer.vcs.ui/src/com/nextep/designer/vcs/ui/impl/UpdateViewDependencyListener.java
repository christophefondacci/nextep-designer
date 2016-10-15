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
package com.nextep.designer.vcs.ui.impl;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import com.nextep.designer.vcs.ui.model.IDependencySearchRequest;
import com.nextep.designer.vcs.ui.model.IDependencyServiceListener;
import com.nextep.designer.vcs.ui.navigators.DependenciesNavigator;

public class UpdateViewDependencyListener implements IDependencyServiceListener {

	@Override
	public void newDependencyRequest(IDependencySearchRequest request) {
		// Displaying the dependency view
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage();
			CommonNavigator dependenciesView = (CommonNavigator) page
					.findView(DependenciesNavigator.VIEW_ID);
			if (dependenciesView != null) {
				dependenciesView.getCommonViewer().setInput(request);
			}
		}
	}

}
