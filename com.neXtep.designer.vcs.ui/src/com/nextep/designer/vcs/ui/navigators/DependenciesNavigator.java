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
package com.nextep.designer.vcs.ui.navigators;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

public class DependenciesNavigator extends CommonNavigator {

	public final static String VIEW_ID = "com.neXtep.designer.vcs.ui.dependenciesView"; //$NON-NLS-1$

	@Override
	protected Object getInitialInput() {
		return super.getInitialInput(); // DependenciesNavigatorRoot.getInstance();
	}

	@Override
	protected CommonViewer createCommonViewer(Composite aParent) {
		final CommonViewer viewer = super.createCommonViewer(aParent);
		viewer.setAutoExpandLevel(2);
		return viewer;
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return new CommonViewer(getViewSite().getId(), aParent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.FULL_SELECTION);
	}
}
