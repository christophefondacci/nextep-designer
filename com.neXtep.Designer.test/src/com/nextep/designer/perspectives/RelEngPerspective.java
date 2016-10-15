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
package com.nextep.designer.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;


/**
 *  This class is meant to serve as an example for how various contributions 
 *  are made to a perspective. Note that some of the extension point id's are
 *  referred to as API constants while others are hardcoded and may be subject 
 *  to change. 
 */
public class RelEngPerspective implements IPerspectiveFactory {

	private IPageLayout factory;

	public RelEngPerspective() {
		super();
	}

	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
		addViews();
		addActionSets();
		addNewWizardShortcuts();
		addPerspectiveShortcuts();
		addViewShortcuts();
	}

	private void addViews() {
		// Creates the overall folder layout. 
		// Note that each new Folder uses a percentage of the remaining EditorArea.
		
		IFolderLayout bottom =
			factory.createFolder(
				"bottomRight", 
				IPageLayout.BOTTOM,
				0.75f,
				factory.getEditorArea());
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView("org.eclipse.team.ui.GenericHistoryView"); 
//		bottom.addPlaceholder(IConsoleConstants.ID_CONSOLE_VIEW);

		IFolderLayout topLeft =
			factory.createFolder(
				"topLeft", 
				IPageLayout.LEFT,
				0.25f,
				factory.getEditorArea());
		topLeft.addView(IPageLayout.ID_RES_NAV);
		topLeft.addView("org.eclipse.jdt.junit.ResultView"); 
		
		factory.addFastView("org.eclipse.team.ccvs.ui.RepositoriesView",0.50f); 
		factory.addFastView("org.eclipse.team.sync.views.SynchronizeView", 0.50f); 
	}

	private void addActionSets() {
		factory.addActionSet("org.eclipse.debug.ui.launchActionSet"); 
		factory.addActionSet("org.eclipse.debug.ui.debugActionSet"); 
		factory.addActionSet("org.eclipse.debug.ui.profileActionSet"); 
		factory.addActionSet("org.eclipse.jdt.debug.ui.JDTDebugActionSet"); 
		factory.addActionSet("org.eclipse.jdt.junit.JUnitActionSet"); 
		factory.addActionSet("org.eclipse.team.ui.actionSet"); 
		factory.addActionSet("org.eclipse.team.cvs.ui.CVSActionSet"); 
		factory.addActionSet("org.eclipse.ant.ui.actionSet.presentation"); 
//		factory.addActionSet(JavaUI.ID_ACTION_SET);
//		factory.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
		factory.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET); 
	}

	private void addPerspectiveShortcuts() {
		factory.addPerspectiveShortcut("org.eclipse.team.ui.TeamSynchronizingPerspective"); 
		factory.addPerspectiveShortcut("org.eclipse.team.cvs.ui.cvsPerspective"); 
		factory.addPerspectiveShortcut("org.eclipse.ui.resourcePerspective"); 
	}

	private void addNewWizardShortcuts() {
		factory.addNewWizardShortcut("org.eclipse.team.cvs.ui.newProjectCheckout"); 
		factory.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder"); 
		factory.addNewWizardShortcut("org.eclipse.ui.wizards.new.file"); 
	}

	private void addViewShortcuts() {
		factory.addShowViewShortcut("org.eclipse.ant.ui.views.AntView"); 
		factory.addShowViewShortcut("org.eclipse.team.ccvs.ui.AnnotateView"); 
		factory.addShowViewShortcut("org.eclipse.pde.ui.DependenciesView"); 
		factory.addShowViewShortcut("org.eclipse.jdt.junit.ResultView"); 
		factory.addShowViewShortcut("org.eclipse.team.ui.GenericHistoryView"); 
//		factory.addShowViewShortcut(IConsoleConstants.ID_CONSOLE_VIEW);
//		factory.addShowViewShortcut(JavaUI.ID_PACKAGES);
		factory.addShowViewShortcut(IPageLayout.ID_RES_NAV);
		factory.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		factory.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	}

}
