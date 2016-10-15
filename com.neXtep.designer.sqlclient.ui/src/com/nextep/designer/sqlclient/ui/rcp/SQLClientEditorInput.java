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
package com.nextep.designer.sqlclient.ui.rcp;

import org.eclipse.ui.IPersistableElement;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.ui.SQLEditorInput;

public class SQLClientEditorInput extends SQLEditorInput {

	public SQLClientEditorInput(ISQLScript script, IConnection conn) {
		super(script);
		setConnection(conn);
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		final boolean b = super.equals(obj);
		return b && (obj instanceof SQLClientEditorInput);
	}

	@Override
	public void setSql(String sql) {
		super.setSql(sql);
		// Notifying here in the editor input to avoid unexpected regressions
		// TODO: Check if OK to notify in ISQLScript.setSQL(String)
		getModel().notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}

}
