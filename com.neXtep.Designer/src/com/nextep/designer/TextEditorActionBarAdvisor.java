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

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Adds actions to a workbench window.
 */
public final class TextEditorActionBarAdvisor extends ActionBarAdvisor {

	private IAction showHelpAction; // NEW
	private IAction searchHelpAction; // NEW
	private IAction dynamicHelpAction; // NEW
	private IAction preferencesAction;
	private IAction newEditorAction;
	private IAction introAction;

	private MenuManager openPerspectiveManager;
	private IContributionItem perspectiveShortList;

	private MenuManager showViewManager;
	private IContributionItem viewShortList;

	/**
	 * Constructs a new action builder.
	 * 
	 * @param actionBarConfigurer the configurer
	 */
	public TextEditorActionBarAdvisor(IActionBarConfigurer actionBarConfigurer) {
		super(actionBarConfigurer);
	}

	protected void fillCoolBar(ICoolBarManager cbManager) {
		cbManager.add(new GroupMarker("group.file")); //$NON-NLS-1$
		{ // File Group
			IToolBarManager fileToolBar = new ToolBarManager(cbManager.getStyle());
			fileToolBar.add(new Separator(IWorkbenchActionConstants.NEW_GROUP));
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.OPEN_EXT));
			fileToolBar.add(new GroupMarker(IWorkbenchActionConstants.SAVE_GROUP));
			fileToolBar.add(getAction(ActionFactory.SAVE.getId()));
			fileToolBar.add(getAction(ActionFactory.PRINT.getId()));

			fileToolBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

			// Add to the cool bar manager
			cbManager.add(new ToolBarContributionItem(fileToolBar,
					IWorkbenchActionConstants.TOOLBAR_FILE));
		}

		cbManager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));

		cbManager.add(new GroupMarker(IWorkbenchActionConstants.GROUP_EDITOR));
	}

	protected void fillMenuBar(IMenuManager menubar) {
		menubar.add(createFileMenu());
		menubar.add(createEditMenu());
		menubar.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		menubar.add(createWindowMenu());
		menubar.add(createHelpMenu());
	}

	private MenuManager createWindowMenu() {
		MenuManager windowMenu = new MenuManager(DesignerMessages
				.getString("designer.menu.windowMenu"), IWorkbenchActionConstants.M_WINDOW); //$NON-NLS-1$
		windowMenu.add(newEditorAction);
		windowMenu.add(new Separator());
		openPerspectiveManager.add(perspectiveShortList);
		windowMenu.add(openPerspectiveManager);
		showViewManager.add(viewShortList);
		windowMenu.add(showViewManager);
		windowMenu.add(preferencesAction);
		return windowMenu;
	}

	/**
	 * Creates and returns the File menu.
	 */
	private MenuManager createFileMenu() {
		MenuManager menu = new MenuManager(
				DesignerMessages.getString("designer.menu.file"), IWorkbenchActionConstants.M_FILE); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_START));

		menu.add(new GroupMarker(IWorkbenchActionConstants.NEW_EXT));
		menu.add(getAction(ActionFactory.CLOSE.getId()));
		menu.add(getAction(ActionFactory.CLOSE_ALL.getId()));
		// menu.add(closeAllSavedAction);
		menu.add(new GroupMarker(IWorkbenchActionConstants.CLOSE_EXT));
		menu.add(new Separator());
		menu.add(getAction(ActionFactory.SAVE.getId()));
		menu.add(getAction(ActionFactory.SAVE_AS.getId()));
		menu.add(getAction(ActionFactory.SAVE_ALL.getId()));

		menu.add(getAction(ActionFactory.REVERT.getId()));
		menu.add(new Separator());
		menu.add(getAction(ActionFactory.PRINT.getId()));
		menu.add(new Separator());
		menu.add(ContributionItemFactory.REOPEN_EDITORS.create(getActionBarConfigurer()
				.getWindowConfigurer().getWindow()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.MRU));
		menu.add(new Separator());
		menu.add(getAction(ActionFactory.QUIT.getId()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.FILE_END));
		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createEditMenu() {
		MenuManager menu = new MenuManager(
				DesignerMessages.getString("designer.menu.edit"), IWorkbenchActionConstants.M_EDIT); //$NON-NLS-1$
		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_START));

		menu.add(getAction(ActionFactory.UNDO.getId()));
		menu.add(getAction(ActionFactory.REDO.getId()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.UNDO_EXT));

		menu.add(getAction(ActionFactory.CUT.getId()));
		menu.add(getAction(ActionFactory.COPY.getId()));
		menu.add(getAction(ActionFactory.PASTE.getId()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.CUT_EXT));

		menu.add(getAction(ActionFactory.SELECT_ALL.getId()));
		menu.add(new Separator());

		menu.add(getAction(ActionFactory.FIND.getId()));
		menu.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));

		menu.add(new GroupMarker(IWorkbenchActionConstants.ADD_EXT));

		menu.add(new GroupMarker(IWorkbenchActionConstants.EDIT_END));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		return menu;
	}

	/**
	 * Creates and returns the Edit menu.
	 */
	private MenuManager createHelpMenu() {
		MenuManager menu = new MenuManager(
				DesignerMessages.getString("designer.menu.help"), IWorkbenchActionConstants.M_HELP); //$NON-NLS-1$
		if (introAction != null) {
			menu.add(introAction);
			menu.add(new Separator());
		}
		menu.add(showHelpAction); // NEW
		menu.add(searchHelpAction); // NEW
		menu.add(dynamicHelpAction); // NEW
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_START));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(new GroupMarker(IWorkbenchActionConstants.HELP_END));
		menu.add(new Separator("about")); //$NON-NLS-1$
		menu.add(getAction(ActionFactory.ABOUT.getId()));
		return menu;
	}

	protected void makeActions(IWorkbenchWindow window) {
		registerAsGlobal(ActionFactory.SAVE.create(window));
		registerAsGlobal(ActionFactory.SAVE_AS.create(window));
		registerAsGlobal(ActionFactory.ABOUT.create(window));
		registerAsGlobal(ActionFactory.SAVE_ALL.create(window));
		registerAsGlobal(ActionFactory.PRINT.create(window));
		registerAsGlobal(ActionFactory.UNDO.create(window));
		registerAsGlobal(ActionFactory.REDO.create(window));
		registerAsGlobal(ActionFactory.CUT.create(window));
		registerAsGlobal(ActionFactory.COPY.create(window));
		registerAsGlobal(ActionFactory.PASTE.create(window));
		registerAsGlobal(ActionFactory.SELECT_ALL.create(window));
		registerAsGlobal(ActionFactory.FIND.create(window));
		registerAsGlobal(ActionFactory.CLOSE.create(window));
		registerAsGlobal(ActionFactory.CLOSE_ALL.create(window));
		registerAsGlobal(ActionFactory.CLOSE_ALL_SAVED.create(window));
		registerAsGlobal(ActionFactory.REVERT.create(window));
		registerAsGlobal(ActionFactory.QUIT.create(window));
		showHelpAction = ActionFactory.HELP_CONTENTS.create(window); // NEW
		register(showHelpAction); // NEW

		searchHelpAction = ActionFactory.HELP_SEARCH.create(window); // NEW
		register(searchHelpAction); // NEW

		dynamicHelpAction = ActionFactory.DYNAMIC_HELP.create(window); // NEW
		register(dynamicHelpAction); // NEW

		newEditorAction = ActionFactory.NEW_EDITOR.create(window);
		register(newEditorAction);

		openPerspectiveManager = new MenuManager(DesignerMessages
				.getString("designer.menu.openPerspective"), "openPerspective"); //$NON-NLS-1$//$NON-NLS-2$
		perspectiveShortList = ContributionItemFactory.PERSPECTIVES_SHORTLIST.create(window);

		showViewManager = new MenuManager(
				DesignerMessages.getString("designer.menu.showView"), "showView"); //$NON-NLS-1$//$NON-NLS-2$
		viewShortList = ContributionItemFactory.VIEWS_SHORTLIST.create(window);

		preferencesAction = ActionFactory.PREFERENCES.create(window);
		register(preferencesAction);

		if (window.getWorkbench().getIntroManager().hasIntro()) {
			introAction = ActionFactory.INTRO.create(window);
			register(introAction);
		}
	}

	/**
	 * Registers the action as global action and registers it for disposal.
	 * 
	 * @param action the action to register
	 */
	private void registerAsGlobal(IAction action) {
		getActionBarConfigurer().registerGlobalAction(action);
		register(action);
	}
}
