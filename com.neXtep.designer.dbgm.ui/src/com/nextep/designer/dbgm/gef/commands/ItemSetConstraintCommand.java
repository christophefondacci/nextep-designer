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
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import com.nextep.datadesigner.Designer;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * @author Christophe Fondacci
 */
public class ItemSetConstraintCommand extends Command {

	/** Stores the new size and location. */
	private final Rectangle newBounds;
	/** Stores the old size and location. */
	private Rectangle oldBounds;
	/** A request to move/resize an edit part. */
	private final ChangeBoundsRequest request;

	/** Item to manipulate. */
	private final IDiagramItem item;

	/**
	 * Create a command that can resize and/or move a shape.
	 * 
	 * @param shape the shape to manipulate
	 * @param req the move and resize request
	 * @param newBounds the new size and location
	 * @throws IllegalArgumentException if any of the parameters is null
	 */
	public ItemSetConstraintCommand(IDiagramItem item, ChangeBoundsRequest req, Rectangle newBounds) {
		if (item == null || req == null || newBounds == null) {
			throw new IllegalArgumentException();
		}
		this.item = item;
		this.request = req;
		this.newBounds = newBounds.getCopy();
		setLabel("move / resize");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#canExecute()
	 */
	public boolean canExecute() {
		Object type = request.getType();
		// make sure the Request is of a type we support:
		return (RequestConstants.REQ_MOVE.equals(type)
				|| RequestConstants.REQ_MOVE_CHILDREN.equals(type)
				|| RequestConstants.REQ_RESIZE.equals(type) || RequestConstants.REQ_RESIZE_CHILDREN
				.equals(type))
				&& Designer.checkIsModifiable(item.getParentDiagram(), false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#execute()
	 */
	public void execute() {
		oldBounds = new Rectangle(item.getXStart(), item.getYStart(), item.getWidth(), item
				.getHeight());
		redo();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#redo()
	 */
	public void redo() {
		item.setXStart(newBounds.x);
		item.setYStart(newBounds.y);
		item.setWidth(newBounds.width);
		item.setHeight(newBounds.height);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.commands.Command#undo()
	 */
	public void undo() {
		item.setXStart(oldBounds.x);
		item.setYStart(oldBounds.y);
		item.setWidth(oldBounds.width);
		item.setHeight(oldBounds.height);
	}

}
