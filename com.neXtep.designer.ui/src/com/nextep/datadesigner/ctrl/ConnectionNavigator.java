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
package com.nextep.datadesigner.ctrl;

import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * @author Christophe Fondacci
 */
public class ConnectionNavigator extends TypedNavigator {

	public ConnectionNavigator(IConnection connection, ITypedObjectUIController controller) {
		super(connection, controller);
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();

	}

	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance("CONNECTION"); //$NON-NLS-1$
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#defaultAction()
	 */
	@Override
	public void defaultAction() {
		// if(isInitialized()) {
		AbstractUIController.newWizardEdition("Database connection wizard", getController()
				.initializeEditor(getModel()));
		// }
		// getController().newInstance(getModel());
	}

	// @Override
	// public String getTitle() {
	// IConnection conn = (IConnection)getModel();
	// if(conn==null) return "";
	// return conn.getLogin() + "@" + conn.getSID() + " (" + conn.getServerIP() + ")";
	// }
}
