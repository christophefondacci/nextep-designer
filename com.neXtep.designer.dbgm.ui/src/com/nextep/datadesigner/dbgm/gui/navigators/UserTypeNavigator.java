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
package com.nextep.datadesigner.dbgm.gui.navigators;

import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 *
 */
public class UserTypeNavigator extends UntypedNavigator {

	public UserTypeNavigator(IUserType type, ITypedObjectUIController controller) {
		super(type,controller);
	}
	@Override
	public void initializeChildConnectors() {
		IUserType type = (IUserType)getModel();
		for(ITypeColumn c : type.getColumns()) {
			addConnector(UIControllerFactory.getController(c.getType()).initializeNavigator(c));
			// Forcing consistency
			if(c.getParent()!=type) {
				try {
					Observable.deactivateListeners();
					c.setParent(type);
				} finally {
					Observable.activateListeners();
				}
				
			}
		}		
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case COLUMN_ADDED:
			ITypeColumn c = (ITypeColumn)data;
			addConnector(UIControllerFactory.getController(c.getType()).initializeNavigator(c));
			break;
		case COLUMN_REMOVED:
			removeConnector(getConnector(data));
			break;
		}
		refreshConnector();
	}

}
