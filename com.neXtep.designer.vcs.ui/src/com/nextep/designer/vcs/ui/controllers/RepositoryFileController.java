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

import java.text.MessageFormat;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.RepositoryFileNavigator;
import com.nextep.datadesigner.vcs.gui.rcp.TextEditorInput;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IRepositoryFile;
import com.nextep.designer.vcs.ui.VCSUIMessages;

public class RepositoryFileController extends AbstractUIController {

	public RepositoryFileController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new RepositoryFileNavigator((IRepositoryFile) model, this);
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object newInstance(Object parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEditorId() {
		return "com.neXtep.designer.vcs.gui.rcp.FileTextEditor";
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new TextEditorInput((IRepositoryFile) model);
	}

	@Override
	public void defaultOpen(ITypedObject model) {
		IRepositoryFile file = (IRepositoryFile) model;
		if (file.getFileSizeKB() > 20000) {
			boolean isOK = MessageDialog.openQuestion(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), "Large file warning", MessageFormat
					.format(VCSUIMessages.getString("repositoryFileLargeWarn"), String.valueOf(file
							.getFileSizeKB())));
			if (!isOK) {
				return;
			}
		}
		super.defaultOpen(model);
	}
}
