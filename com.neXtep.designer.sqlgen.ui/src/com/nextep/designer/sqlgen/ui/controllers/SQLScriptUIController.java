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
package com.nextep.designer.sqlgen.ui.controllers;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.gui.SQLScriptNavigator;
import com.nextep.datadesigner.sqlgen.gui.SQLScriptWizardEditor;
import com.nextep.datadesigner.sqlgen.impl.SQLWrapperScript;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.designer.sqlgen.ui.Activator;
import com.nextep.designer.sqlgen.ui.SQLScriptEditorInput;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;
import com.nextep.designer.sqlgen.ui.editors.SQLMultiEditor;

/**
 * UI controller for {@link ISQLScript}
 * 
 * @author Christophe Fondacci
 */
public class SQLScriptUIController extends VersionableController {

	public SQLScriptUIController() {
		super();
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new SQLScriptWizardEditor((ISQLScript) content);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		return null;
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new SQLScriptNavigator((ISQLScript) model, this);
	}

	// CFO 2011/04/01 Reverted this part since it caused regression on every opened script (such as
	// the edition of a SQL comparison).
	// @Override
	// public String getEditorId() {
	// return MixedSQLEditor.EDITOR_ID;
	// }
	//
	// @Override
	// public IEditorInput getEditorInput(ITypedObject model) {
	// String editorId = SQLEditor.EDITOR_ID;
	// if (model instanceof SQLWrapperScript) {
	// editorId = SQLMultiEditor.EDITOR_ID;
	// }
	// return new MixedSQLEditorInput(model, new SQLScriptEditorInput((ISQLScript) model),
	// editorId);
	// }

	@Override
	public String getEditorId() {
		return SQLEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new SQLScriptEditorInput((ISQLScript) model);
	}

	@Override
	public void defaultOpen(ITypedObject model) {
		try {
			IWorkbenchPage page = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow()
					.getActivePage();
			if (model instanceof SQLWrapperScript) {
				page.openEditor(new SQLScriptEditorInput((ISQLScript) model),
						SQLMultiEditor.EDITOR_ID);
			} else {
				page.openEditor(new SQLScriptEditorInput((ISQLScript) model), SQLEditor.EDITOR_ID);
			}
		} catch (PartInitException e) {
			throw new ErrorException(e);
		}
	}
}
