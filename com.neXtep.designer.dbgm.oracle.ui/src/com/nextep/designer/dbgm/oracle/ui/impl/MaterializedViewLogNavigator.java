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

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.navigators.TypedNavigator;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class MaterializedViewLogNavigator extends TypedNavigator {

	public MaterializedViewLogNavigator(IMaterializedViewLog log, ITypedObjectUIController controller) {
		super(log,controller); 
	}
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch(event) {
		case GENERIC_CHILD_ADDED:
			addConnector(UIControllerFactory.getController(data).initializeNavigator(data));
			break;
		case GENERIC_CHILD_REMOVED:
			removeConnector(getConnector(data));
			break;
		default:
			refreshConnector();
		}
		
	}

	@Override
	public String getTitle() {
		try {
			IBasicTable t = (IBasicTable)VersionHelper.getReferencedItem(((IMaterializedViewLog)getModel()).getTableReference());
			return t.getName();
		} catch( ErrorException e ) {
			return "Unresolved parent table";
		}
	}
	@Override
	public void initializeChildConnectors() {
		super.initializeChildConnectors();
		IMaterializedViewLog log = (IMaterializedViewLog)getModel();
		if(log.getPhysicalProperties()!=null) {
			addConnector(UIControllerFactory.getController(log.getPhysicalProperties().getType()).initializeNavigator(log.getPhysicalProperties()));
			// Forcing consistency
			if(log.getPhysicalProperties().getParent()!=log) {
				try {
					Observable.deactivateListeners();
					log.getPhysicalProperties().setParent(log);
				} finally {
					Observable.activateListeners();
				}
				
			}
		}
	}
	
}
