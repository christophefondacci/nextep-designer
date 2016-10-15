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
package com.nextep.datadesigner.dbgm.ctrl;

import com.nextep.datadesigner.dbgm.gui.UserTypeEditorGUI;
import com.nextep.datadesigner.dbgm.gui.navigators.UserTypeNavigator;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.gui.external.VersionableController;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class UserTypeController extends VersionableController {

	/**
	 * 
	 */
	public UserTypeController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
		addSaveEvent(ChangeEvent.COLUMN_ADDED);
		addSaveEvent(ChangeEvent.COLUMN_REMOVED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new UserTypeEditorGUI((IUserType) content, this);
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
		return new UserTypeNavigator((IUserType) model, this);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeProperty(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeEdition(IVersionable<?> v, IVersionContainer container, Object parent) {
		final String prefix = "TYPE";
		String name = prefix;
		int index = 1;
		try {
			while (CorePlugin.getService(IReferenceManager.class).findByTypeName(
					IElementType.getInstance(IUserType.TYPE_ID), name) != null) {
				name = prefix + String.valueOf(index++);
			}
		} catch (ReferenceNotFoundException e) {
			// That's what we are looking for
			v.setName(name);
		}
		CorePlugin.getIdentifiableDao().save(v);
	}

}
