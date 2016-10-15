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
package com.nextep.designer.vcs.ui.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.vcs.gui.VersionViewCreationWizard;
import com.nextep.datadesigner.vcs.gui.VersionViewEditorGUI;
import com.nextep.datadesigner.vcs.gui.VersionViewNavigator;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.impl.Workspace;

/**
 * @author Christophe Fondacci
 */
public class VersionViewUIController extends AbstractUIController {

	private static final Log log = LogFactory.getLog(VersionViewUIController.class);

	public VersionViewUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.VERSIONABLE_ADDED);
		addSaveEvent(ChangeEvent.VERSIONABLE_REMOVED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new VersionViewEditorGUI((IWorkspace) content);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeGraphical(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeNavigator(java.lang.Object)
	 */
	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new VersionViewNavigator((IWorkspace) model, this, null);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#newInstance(java.lang.Object)
	 */
	@Override
	public Object newInstance(Object parent) {
		IWorkspace view = new Workspace("", "");
		try {
			Shell shell = null;
			if (CoreUiPlugin.getDefault().getWorkbench() != null
					&& CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
				shell = CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow()
						.getShell();
			} else {
				shell = Display.getCurrent().getActiveShell();
			}
			WizardDialog d = new WizardDialog(shell, new VersionViewCreationWizard(
					"View creation wizard", view));
			d.setTitle("View creation wizard");
			d.setBlockOnOpen(true);
			d.open();
			// for(IDisplayConnector conn : pages) {
			//
			// }
			if (d.getReturnCode() == Window.CANCEL) {
				throw new CancelException("Creation has been cancelled by the user.");
			}
			// newWizardEdition("View creation wizard", initializeEditor(view));
		} catch (CancelException e) {
			throw e;
		} catch (Exception e) {
			log.error("View creation wizard exception", e);
		}
		// TODO Auto-generated method stub
		return view;
	}

}
