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
package com.nextep.designer.dbgm.mysql.ctrl;

import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;
import com.nextep.designer.dbgm.mysql.ui.impl.MySQLTableEditorGUI;
import com.nextep.designer.dbgm.ui.controllers.TableUIController;
import com.nextep.designer.dbgm.ui.rcp.TypedFormRCPEditor;

/**
 * @author Christophe Fondacci
 */
public class MySQLTableController extends TableUIController {

	/**
	 * @see com.nextep.designer.dbgm.ui.controllers.TableUIController#initializeEditor(java.lang.Object)
	 */
	@Override
	public IDisplayConnector initializeEditor(Object content) {
		return new MySQLTableEditorGUI((IMySQLTable) content, this);
	}

	@Override
	public String getEditorId() {
		return TypedFormRCPEditor.EDITOR_ID;
	}
}
