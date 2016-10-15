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
package com.nextep.designer.ui.controllers;

import com.nextep.datadesigner.ctrl.ConnectionNavigator;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IConnectionContainer;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.editors.ConnectionEditorGUI;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * @author Christophe Fondacci
 */
public class ConnectionUIController extends AbstractUIController {

	/**
	 *
	 */
	public ConnectionUIController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	/**
	 * @see com.nextep.designer.ui.model.ITypedObjectUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new ConnectionEditorGUI((IConnection) content, this);
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
		return new ConnectionNavigator((IConnection) model, this);
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
		IConnection conn = CorePlugin.getTypedObjectFactory().create(IConnection.class);
		newWizardEdition(UIMessages.getString("connection.newWizard"), initializeEditor(conn)); //$NON-NLS-1$
		if (parent instanceof IConnectionContainer) {
			((IConnectionContainer) parent).addConnection(conn);
		}
		save(conn);
		return conn;
	}

	/**
	 * @see com.nextep.designer.ui.model.base.AbstractUIController#emptyInstance(java.lang.String,
	 *      java.lang.Object)
	 */
	@Override
	public Object emptyInstance(String name, Object parent) {
		IConnection conn = CorePlugin.getTypedObjectFactory().create(IConnection.class);
		conn.setName(name);
		return conn;
	}

	@Override
	public void defaultOpen(ITypedObject model) {
		AbstractUIController
				.newWizardEdition("Database connection wizard", initializeEditor(model));
	}
}
