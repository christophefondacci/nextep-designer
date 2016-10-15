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
/**
 *
 */
package com.nextep.designer.dbgm.gef.commands;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.DiagramItem;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class TableCreateCommand extends Command {

	private IDiagramItem newItem;
	private IDiagram parentDiagram;
	private Rectangle bounds;

	public TableCreateCommand(IBasicTable table, IDiagram parent, Rectangle bounds) {
		// IVersionable<IBasicTable> t =
		// (IVersionable<IBasicTable>)ControllerFactory.getController(IElementType.getInstance("TABLE")).emptyInstance(VersionHelper.getVersionable(parentDiagram).getContainer());
		// newItem = new DiagramItem(t,bounds.x,bounds.y);
		// newItem.setHeight(bounds.height);
		// newItem.setWidth(bounds.width);
		this.parentDiagram = parent;
		this.bounds = bounds;
		setLabel("Table creation");
	}

	public boolean canExecute() {
		return parentDiagram != null && bounds != null
				&& Designer.checkIsModifiable(parentDiagram, false);
	}

	public void execute() {
		ITypedObjectUIController controller = UIControllerFactory.getController(IElementType
				.getInstance(IBasicTable.TYPE_ID));
		IVersionable<IBasicTable> t = (IVersionable<IBasicTable>) controller
				.newInstance(VersionHelper.getVersionable(parentDiagram).getContainer());
		newItem = new DiagramItem(t, bounds.x, bounds.y);
		newItem.setWidth(bounds.width);
		newItem.setHeight(bounds.height);
		// Referencing it
		CorePlugin.getService(IReferenceManager.class).addReferencer(newItem);
		redo();
		controller.defaultOpen(t);
	}

	public void redo() {
		parentDiagram.addItem(newItem);
	}

	public void undo() {
		parentDiagram.removeItem(newItem);
		CorePlugin.getService(IReferenceManager.class).removeReferencer(newItem);
	}
}
