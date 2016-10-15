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
package com.nextep.datadesigner.sqlgen.ctrl;

import org.eclipse.ui.IEditorInput;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.designer.sqlgen.ui.dbgm.MixedSQLEditor;
import com.nextep.designer.sqlgen.ui.dbgm.MixedSQLEditorInput;
import com.nextep.designer.sqlgen.ui.dbgm.ViewEditorGUI;
import com.nextep.designer.sqlgen.ui.dbgm.ViewEditorInput;
import com.nextep.designer.sqlgen.ui.dbgm.ViewNavigator;

/**
 * @author Christophe Fondacci
 */
public class ViewController extends VersionableController {

	public ViewController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new ViewEditorGUI((IView) content, this);
		// return null;
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
		return new ViewNavigator((IView) model, this);
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
	 * @see com.nextep.datadesigner.vcs.gui.external.VersionableController#newInstance(java.lang.Object)
	 */
	@Override
	public Object newInstance(Object parent) {
		IView v = (IView) super.newInstance(parent);
		v.setSQLDefinition("select  from  where ");
		return v;
	}

	@Override
	public String getEditorId() {
		return MixedSQLEditor.EDITOR_ID; // ViewEditor.EDITOR_ID;
	}

	@Override
	public IEditorInput getEditorInput(ITypedObject model) {
		return new MixedSQLEditorInput((IView) model, new ViewEditorInput((IView) model));
	}
}
