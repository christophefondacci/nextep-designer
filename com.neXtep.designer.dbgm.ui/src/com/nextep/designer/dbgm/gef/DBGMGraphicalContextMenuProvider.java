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
package com.nextep.designer.dbgm.gef;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Providing contextual menu on the database model graphical
 * editor.
 * 
 * @author Christophe
 *
 */
public class DBGMGraphicalContextMenuProvider extends ContextMenuProvider {

	private ActionRegistry actionRegistry;

	public DBGMGraphicalContextMenuProvider(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		setActionRegistry(registry);
	}
	
	@Override
	public void buildContextMenu(IMenuManager manager) {
//		manager.add(new GroupMarker("editions"));
		manager.add(new Separator("editions"));
		// Add standard action groups to the menu
		GEFActionConstants.addStandardActionGroups(manager);

		
		// Add actions to the menu
		manager.appendToGroup(
				GEFActionConstants.GROUP_UNDO, // target group id
				getActionRegistry().getAction(ActionFactory.UNDO.getId())); // action to add
		manager.appendToGroup(
				GEFActionConstants.GROUP_UNDO,
				getActionRegistry().getAction(ActionFactory.REDO.getId()));
		manager.appendToGroup(
				GEFActionConstants.GROUP_EDIT,
				getActionRegistry().getAction(ActionFactory.DELETE.getId()));
//		manager.add(new Separator());
//		manager.add(new GroupMarker(
//				IWorkbenchActionConstants.MB_ADDITIONS));
//		
//		manager.add(new Separator());
		// Alignment Actions
//		MenuManager submenu = new MenuManager("Align");
//
//		IAction action = getActionRegistry().getAction(GEFActionConstants.ALIGN_LEFT);
//		if (action.isEnabled())
//			submenu.add(action);
//
//		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_CENTER);
//		if (action.isEnabled())
//			submenu.add(action);
//
//		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_RIGHT);
//		if (action.isEnabled())
//			submenu.add(action);
//			
//		submenu.add(new Separator());
//		
//		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_TOP);
//		if (action.isEnabled())
//			submenu.add(action);
//
//		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_MIDDLE);
//		if (action.isEnabled())
//			submenu.add(action);
//
//		action = getActionRegistry().getAction(GEFActionConstants.ALIGN_BOTTOM);
//		if (action.isEnabled())
//			submenu.add(action);
//		if (!submenu.isEmpty())
//			manager.appendToGroup(GEFActionConstants.GROUP_REST, submenu);
		
//		manager.add(new Separator());
//		manager.add(new GroupMarker("actions"));
//		manager.add(new Separator());
//		manager.add(new GroupMarker("version"));

	}
	private ActionRegistry getActionRegistry() {
		return actionRegistry;
	}

	private void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}
}
