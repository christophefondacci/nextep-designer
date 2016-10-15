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

import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.vcs.gui.UserEditorGUI;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IRepositoryUser;

public class UserController extends AbstractUIController {

	public UserController() {
		// addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new UserEditorGUI((IRepositoryUser) content);
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return null;
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		return null;
	}

	@Override
	public Object newInstance(Object parent) {
		IRepositoryUser user = (IRepositoryUser) emptyInstance(null, parent);
		newWizardEdition("User creation wizard", initializeEditor(user));
		save(user);
		return user;
	}

	@Override
	public Object emptyInstance(String name, Object parent) {
		final IRepositoryUser user = new RepositoryUser();
		return user;
	}

}
