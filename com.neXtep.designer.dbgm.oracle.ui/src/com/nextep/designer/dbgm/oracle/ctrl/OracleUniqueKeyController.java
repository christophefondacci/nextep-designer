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

import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueConstraint;
import com.nextep.designer.dbgm.oracle.ui.impl.OracleUniqueKeyNavigator;
import com.nextep.designer.dbgm.ui.controllers.UniqueKeyUIController;

public class OracleUniqueKeyController extends UniqueKeyUIController {

	public OracleUniqueKeyController() {
		super();
		addSaveEvent(ChangeEvent.GENERIC_CHILD_ADDED);
		addSaveEvent(ChangeEvent.GENERIC_CHILD_REMOVED);
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		return new OracleUniqueKeyNavigator((OracleUniqueConstraint) model, this);
	}

}
