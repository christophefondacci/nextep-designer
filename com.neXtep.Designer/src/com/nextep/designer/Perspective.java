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
package com.nextep.designer;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import com.nextep.designer.vcs.ui.navigators.DependenciesNavigator;
import com.nextep.designer.vcs.ui.navigators.VersionNavigator;
import com.nextep.designer.vcs.ui.views.DiffPreviewView;

/**
 * The default "database design" neXtep perspective definition
 * 
 * @author Christophe Fondacci
 */
public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		layout.addPerspectiveShortcut("com.neXtep.designer.synch.ui.perspectiveSynchDb"); //$NON-NLS-1$

		IFolderLayout navFolder = layout.createFolder(
				"Navigation", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
		navFolder.addView(VersionNavigator.VIEW_ID);

		// Version section (upper right)
		IFolderLayout versionFolder = layout.createFolder("Version", IPageLayout.RIGHT, 0.70f, //$NON-NLS-1$
				layout.getEditorArea());
		versionFolder.addView("com.nextep.datadesigner.vcs.gui.rcp.VersionableViewRCP"); //$NON-NLS-1$
		versionFolder.addView("com.nextep.designer.beng.ui.views.DeliveriesView"); //$NON-NLS-1$

		// Connection section (lower right)
		IFolderLayout dbFolder = layout.createFolder("Database", IPageLayout.BOTTOM, 0.7f, //$NON-NLS-1$
				"Version"); //$NON-NLS-1$
		dbFolder.addView("com.neXtep.designer.dbgm.ui.connections"); //$NON-NLS-1$

		// Main toolbar (central bottom)
		IFolderLayout toolFolder = layout.createFolder("ToolBar", IPageLayout.BOTTOM, 0.8f, layout //$NON-NLS-1$
				.getEditorArea());
		toolFolder.addView("org.eclipse.ui.console.ConsoleView"); //$NON-NLS-1$
		toolFolder.addView("com.neXtep.designer.ui.markersView"); //$NON-NLS-1$
		toolFolder.addView("org.eclipse.search.ui.views.SearchView"); //$NON-NLS-1$
		toolFolder.addView(DiffPreviewView.VIEW_ID);
		toolFolder.addView(DependenciesNavigator.VIEW_ID);
		toolFolder.addView("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$
		IPlaceholderFolderLayout sqlFolder = layout.createPlaceholderFolder(
				"SQL", IPageLayout.BOTTOM, //$NON-NLS-1$
				0.65f, layout.getEditorArea());
		toolFolder.addPlaceholder("com.neXtep.designer.sqlclient.ui.rcp.SQLResultsView:*"); //$NON-NLS-1$

		// Control section (lower left)
		IFolderLayout controlFolder = layout.createFolder(
				"ControlToolBox", IPageLayout.BOTTOM, 0.60f, "Navigation"); //$NON-NLS-1$//$NON-NLS-2$
		controlFolder.addView("com.neXtep.designer.ui.properties"); //$NON-NLS-1$
		controlFolder.addView("org.eclipse.ui.views.ContentOutline"); //$NON-NLS-1$

		// Shortcuts for the Show view menu
		layout.addShowViewShortcut(VersionNavigator.VIEW_ID);
		layout.addShowViewShortcut("com.neXtep.designer.dbgm.ui.connections"); //$NON-NLS-1$
		layout.addShowViewShortcut("com.neXtep.designer.ui.markersView"); //$NON-NLS-1$
		layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView"); //$NON-NLS-1$
		layout.addShowViewShortcut("com.nextep.designer.beng.ui.views.DeliveriesView"); //$NON-NLS-1$
		layout.addShowViewShortcut("com.neXtep.designer.ui.properties"); //$NON-NLS-1$
		layout.addShowViewShortcut("com.nextep.datadesigner.vcs.gui.rcp.VersionableViewRCP"); //$NON-NLS-1$

//		layout.getViewLayout("com.neXtep.datadesigner.vcs.gui.rcp.VersionViewRCP").setCloseable(false); //$NON-NLS-1$
	}

}
