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
package com.nextep.designer.dbgm.gef.editors;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.gef.commands.ConnectionCreateCommand;
import com.nextep.designer.dbgm.gef.commands.ItemSetConstraintCommand;
import com.nextep.designer.dbgm.gef.commands.TableCreateCommand;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 *
 */
public class DiagramXYLayoutPolicy extends XYLayoutEditPolicy {
	private IVersionable<IDiagram> diagram;
	public DiagramXYLayoutPolicy(IDiagram diagram) {
		this.diagram=VersionHelper.getVersionable(diagram);
	}
	/**
	 * @see org.eclipse.gef.editpolicies.XYLayoutEditPolicy#getConstraintFor(org.eclipse.gef.requests.ChangeBoundsRequest, org.eclipse.gef.GraphicalEditPart)
	 */
	@Override
	protected Object getConstraintFor(ChangeBoundsRequest request,
			GraphicalEditPart child) {
		Rectangle r = (Rectangle) super.getConstraintFor(request, child);
		r.x=Math.max(r.x, 0);
		r.y=Math.max(r.y,0);
		return r;
	}
	protected Command createChangeConstraintCommand(ChangeBoundsRequest request,
			EditPart child, Object constraint) {
		if(!VersionHelper.ensureModifiable(diagram, false)) {
			return null;
		}
		if (child instanceof DiagramItemPart && constraint instanceof Rectangle) {
			Rectangle bounds = ((Rectangle)constraint).getCopy();
			// Checking bounds collision with other items on the diagram
//			for(IDiagramItem i : diagram.getVersionnedObject().getModel().getItems()) {
//				if( i!= child.getModel() && bounds.intersects(new Rectangle(i.getXStart(),i.getYStart(),i.getWidth(),i.getHeight())) ) {
//					return null;
//				}
//			}
//			// Checking that we have a positive constraint
//			bounds.x = Math.max(0,bounds.x);
//			bounds.y = Math.max(0, bounds.y);
//			bounds.width = Math.max(0,bounds.width);
//			bounds.height = Math.max(0, bounds.height);
			// return a command that can move and/or resize a Shape
			return new ItemSetConstraintCommand(
					(IDiagramItem)child.getModel(), request, bounds);
		}
		return super.createChangeConstraintCommand(request, child, constraint);
	}
	/**
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	@Override
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreateCommand(org.eclipse.gef.requests.CreateRequest)
	 */
	@Override
	protected Command getCreateCommand(CreateRequest request) {
		if(!VersionHelper.ensureModifiable(diagram, false)) {
			return null;
		}
		Object childClass = request.getNewObjectType();
		if (childClass == IBasicTable.class ) {
			// return a command that can add a Shape to a ShapesDiagram
			if(getHost().getModel() instanceof IDiagram) {
				return new TableCreateCommand((IBasicTable)request.getNewObject(),
					(IDiagram)getHost().getModel(), (Rectangle)getConstraintFor(request));
			}
		} else if( childClass == ForeignKeyConstraint.class) {
			if(getHost().getModel() instanceof IDiagramItem) {
				return new ConnectionCreateCommand((IDiagramItem)getHost().getModel());
			}
		}
		return null;
	}


}

