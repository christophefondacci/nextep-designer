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
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * A GEF command for removing items on a diagram
 * 
 * @author Christophe Fondacci
 */
public class ItemDeleteCommand extends Command {

	private IDiagram diagram;
	private IDiagramItem item;

	public ItemDeleteCommand(IDiagram diagram, IDiagramItem item) {
		this.diagram = diagram;
		this.item = item;
	}

	/**
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	@Override
	public boolean canExecute() {
		return diagram != null && item != null && diagram.getItems().contains(item)
				&& Designer.checkIsModifiable(diagram, false);
	}

	/**
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	@Override
	public void redo() {
		ControllerFactory.getController(IElementType.getInstance("DIAGRAM")).modelDeleted(item);
	}

	/**
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	@Override
	public void execute() {
		redo();
	}

	/**
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	@Override
	public void undo() {
		diagram.addItem(item);
	}
}
