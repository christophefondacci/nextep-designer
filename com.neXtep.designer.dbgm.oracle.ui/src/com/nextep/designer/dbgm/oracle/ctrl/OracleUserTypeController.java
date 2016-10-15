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
package com.nextep.designer.dbgm.oracle.ctrl;

import com.nextep.datadesigner.dbgm.ctrl.UserTypeController;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.dbgm.oracle.ui.impl.OracleUserTypeEditorGUI;
import com.nextep.designer.dbgm.oracle.ui.impl.OracleUserTypeNavigator;

public class OracleUserTypeController extends UserTypeController {

	public OracleUserTypeController() {
		super();
		addSaveEvent(ChangeEvent.SOURCE_CHANGED);
	}
	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new OracleUserTypeNavigator((IOracleUserType)model,this);
	}
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new OracleUserTypeEditorGUI((IOracleUserType)content,this);
	}
	@Override
	public String getEditorId() {
		return "com.neXtep.designer.sqlgen.oracle.ui.oracleTypeEditor";
	}
}
