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

import com.nextep.datadesigner.gui.impl.navigators.UntypedNavigator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.oracle.impl.merge.RefMapping;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class RefMappingNavigator extends UntypedNavigator {

	public RefMappingNavigator(RefMapping r, ITypedObjectUIController controller) {
		super(r,controller);
	}
	@Override
	public void initializeChildConnectors() {
		RefMapping r = (RefMapping)getModel();
		if(r.getClusterCol()!=null) addConnector(UIControllerFactory.getController(IElementType.getInstance(IReference.TYPE_ID)).initializeNavigator(r.getClusterCol()));
		if(r.getTableCol()!=null) addConnector(UIControllerFactory.getController(IElementType.getInstance(IReference.TYPE_ID)).initializeNavigator(r.getTableCol()));
	}
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshConnector();
	}

}
