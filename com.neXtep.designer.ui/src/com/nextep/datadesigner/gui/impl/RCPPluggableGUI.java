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
/**
 *
 */
package com.nextep.datadesigner.gui.impl;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.IPluggableGUI;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.ui.CoreUiPlugin;

/**
 * @author Christophe Fondacci
 *
 */
public class RCPPluggableGUI implements IPluggableGUI {

	/**
	 * @see com.nextep.datadesigner.gui.model.IPluggableGUI#dialogDisplay(com.nextep.datadesigner.gui.model.IDisplayConnector, java.lang.String, java.lang.Object, int, int)
	 */
	@Override
	public Object dialogDisplay(IDisplayConnector connector,
			String dialogTitle, Object argument, int width, int height) {
		GUIWrapper wrapper = new GUIWrapper(connector,dialogTitle,width,height);
		return wrapper.invoke(argument);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IPluggableGUI#plugItemInDebugFolder(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	@Override
	public void plugItemInDebugFolder(IDisplayConnector connector) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IPluggableGUI#plugItemInEditorFolder(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	@Override
	public void plugItemInEditorFolder(IDisplayConnector connector) {
		try {
			CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
					"com.nextep.datadesigner.gui.impl.rcp.RCPViewWrapper",
					((IdentifiedObject)connector.getModel()).getUID().toString(),
					IWorkbenchPage.VIEW_ACTIVATE
					);
		} catch( PartInitException e) {
			throw new ErrorException(e);
		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IPluggableGUI#plugItemInNavigatorFolder(com.nextep.datadesigner.gui.model.IDisplayConnector)
	 */
	@Override
	public void plugItemInNavigatorFolder(IDisplayConnector connector) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IPluggableGUI#unplugModel(java.lang.Object)
	 */
	@Override
	public void unplugModel(Object model) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#getDisplay()
	 */
	@Override
	public Display getDisplay() {
		return CoreUiPlugin.getDefault().getWorkbench().getDisplay();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#getShell()
	 */
	@Override
	public Shell getShell() {
		return CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#initializeGUI(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	public void initializeGUI(Shell parentGUI) {
		// Nonsense in a RCP context

	}

}
