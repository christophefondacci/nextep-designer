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
package com.nextep.designer.dbgm.oracle.ui.impl;

import com.nextep.datadesigner.dbgm.gui.navigators.ConstraintNavigator;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueConstraint;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class OracleUniqueKeyNavigator extends ConstraintNavigator {

	public OracleUniqueKeyNavigator(OracleUniqueConstraint uk, ITypedObjectUIController controller) {
		super(uk,controller);
	}
	
	@Override
	public void initializeChildConnectors() {
		super.initializeChildConnectors();
		OracleUniqueConstraint uk = (OracleUniqueConstraint)getModel();
		if(uk.getPhysicalProperties()!=null) {
			addConnector(UIControllerFactory.getController(uk.getPhysicalProperties().getType()).initializeNavigator(uk.getPhysicalProperties()));
			// Forcing consistency
			if(uk.getPhysicalProperties().getParent()!=uk) {
				try {
					Observable.deactivateListeners();
					uk.getPhysicalProperties().setParent(uk);
				} finally {
					Observable.activateListeners();
				}
				
			}
		}
	}
	
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case GENERIC_CHILD_ADDED:
			if(data != null) {
				this.addConnector(UIControllerFactory.getController(data).initializeNavigator(data));
			}
			break;
		case GENERIC_CHILD_REMOVED:
			this.removeConnector(this.getConnector(data));
			break;			
		}
		// Super implementation
		super.handleEvent(event, source, data);
	}
}
