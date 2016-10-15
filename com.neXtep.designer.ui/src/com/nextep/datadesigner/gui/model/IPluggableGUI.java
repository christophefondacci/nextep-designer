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
package com.nextep.datadesigner.gui.model;


/**
 * Interface defining a graphical user interface
 * which can integrate pluggable folders in 3
 * distinct panes : navigation, edition and debug.
 *
 * @author Christophe_Fondacci
 *
 */
public interface IPluggableGUI extends IDesignerGUI {

	/**
	 * Adds a new folder to the navigator pane.
	 * The caller should provide the connector which
	 * will be used to render on screen and the item
	 * to be displayed through the connector.
	 *
	 * @param newFolder folder to add to the navigator pane
	 */
	public void plugItemInNavigatorFolder(IDisplayConnector connector);
	/**
	 * Adds a new folder to the editor pane
	 * The caller should provide the connector which
	 * will be used to render on screen and the item
	 * to be displayed through the connector.
	 *
	 * @param newFolder folder to add to the editor pane
	 */
	public void plugItemInEditorFolder(IDisplayConnector connector);
	/**
	 * Adds a new folder to the debugger pane
	 * The caller should provide the connector which
	 * will be used to render on screen and the item
	 * to be displayed through the connector.
	 *
	 * @param newFolder folder to add to the debugger pane
	 */
	public void plugItemInDebugFolder(IDisplayConnector connector);

	/**
	 * Plugs the specified display connector as an application modal
	 * dialog box.
	 *
	 * @param connector connector to display in the dialog
	 * @param dialogTitle title of the dialog box
	 * @param argument any argument to pass
	 * @param width initial dialog width
	 * @param height initial dialog height
	 * @return the object returned by the dialog box
	 */
	public Object dialogDisplay(IDisplayConnector connector, String dialogTitle, Object argument, int width, int height);

	/**
	 * This method will remove any existing plugged connectors which
	 * are displaying the specified model. It should be called before
	 * model removal to avoid unexpected exceptions
	 *
	 * @param model
	 */
	public void unplugModel(Object model);
}
