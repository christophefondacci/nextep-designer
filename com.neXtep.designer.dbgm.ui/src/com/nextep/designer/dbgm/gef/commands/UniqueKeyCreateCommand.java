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
package com.nextep.designer.dbgm.gef.commands;

import org.eclipse.gef.commands.Command;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.GUIWrapper;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.ui.factories.UIControllerFactory;

public class UniqueKeyCreateCommand extends Command {

	private IBasicTable table;

	public UniqueKeyCreateCommand(IBasicTable table) {
		this.table = table;
	}

	@Override
	public boolean canExecute() {
		return Designer.checkIsModifiable(table, false);
	}

	@Override
	public void execute() {
		redo();
	}

	@Override
	public void redo() {

		UniqueKeyConstraint uk = (UniqueKeyConstraint) UIControllerFactory.getController(
				IElementType.getInstance(UniqueKeyConstraint.TYPE_ID)).newInstance(table);
		IDisplayConnector conn = UIControllerFactory.getController(
				IElementType.getInstance(UniqueKeyConstraint.TYPE_ID)).initializeEditor(uk);
		GUIWrapper gui = new GUIWrapper(conn, "Unique key creation", 600, 500);
		gui.invoke();
	}
}
