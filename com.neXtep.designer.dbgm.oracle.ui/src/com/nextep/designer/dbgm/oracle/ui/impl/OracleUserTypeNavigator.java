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

import java.util.ArrayList;
import com.nextep.datadesigner.dbgm.gui.navigators.ProcedureNavigator;
import com.nextep.datadesigner.dbgm.gui.navigators.UserTypeNavigator;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.IProcedureContainer;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class OracleUserTypeNavigator extends UserTypeNavigator {

	public OracleUserTypeNavigator(IOracleUserType type, ITypedObjectUIController controller) {
		super(type,controller);
	}
	
	@Override
	public void initializeChildConnectors() {
		final IProcedureContainer type = (IProcedureContainer)getModel();
		for(IProcedure p : type.getProcedures()) {
			addConnector(new ProcedureNavigator(p));
		}
	}
	
	/**
	 * @see com.nextep.datadesigner.gui.model.AbstractNavigator#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		// Removing connectors
		for(INavigatorConnector c : new ArrayList<INavigatorConnector>(getConnectors())) {
			removeConnector(c);
		}
		initializeChildConnectors();
		super.refreshConnector();

	}
}
