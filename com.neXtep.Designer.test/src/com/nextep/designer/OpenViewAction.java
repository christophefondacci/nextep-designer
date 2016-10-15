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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;


public class OpenViewAction extends Action {

	private final static Log log = LogFactory.getLog(OpenViewAction.class);
	private final IWorkbenchWindow window;
	private int instanceNum = 0;
	private final String viewId;
	private final String id;

	public OpenViewAction(IWorkbenchWindow window, String id, String viewId) {
		this.window = window;
		this.viewId = viewId;
		this.id=id;
        //setText(label);
        // The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_OPEN);
        // Associate the action with a pre-defined command, to allow key bindings.
		setActionDefinitionId(ICommandIds.CMD_OPEN);
//		setImageDescriptor(com.nextep.designer.Activator.getImageDescriptor("/icons/sample2.gif"));
	}

	public void run() {
		if(window != null) {
			try {
				log.debug("OpenView action: viewID = <" +viewId + "> | 2nd ID=<"+id + ">");
				window.getActivePage().showView(viewId, id, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:" + e.getMessage());
			}
		}
	}
}
