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
package com.nextep.designer.synch.ui.perpectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import com.nextep.designer.synch.ui.navigators.ComparisonNavigator;
import com.nextep.designer.vcs.ui.views.DiffPreviewView;

public class SynchronizationPerspective implements IPerspectiveFactory {

	public final static String PERSPECTIVE_ID = "com.neXtep.designer.synch.ui.perspectiveSynchDb";

	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		layout.addStandaloneView(ComparisonNavigator.VIEW_ID, true, IPageLayout.LEFT, 0.35f,
				layout.getEditorArea());

		// IFolderLayout synchFolder = layout.createFolder("synch", IPageLayout.LEFT, 0.25f,
		// layout.getEditorArea());
		// synchFolder.addView(ComparisonNavigator.VIEW_ID);

		IFolderLayout toolFolder = layout.createFolder("ToolBar", IPageLayout.BOTTOM, 0.7f,
				layout.getEditorArea());
		toolFolder.addView("org.eclipse.ui.console.ConsoleView");
		toolFolder.addView(DiffPreviewView.VIEW_ID);
		toolFolder.addView("com.neXtep.designer.dbgm.ui.connections");

	}

}
