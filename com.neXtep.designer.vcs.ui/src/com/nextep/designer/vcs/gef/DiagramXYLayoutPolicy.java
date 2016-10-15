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
package com.nextep.designer.vcs.gef;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.NonResizableEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IDiagram;
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
	protected Command createChangeConstraintCommand(ChangeBoundsRequest request,
			EditPart child, Object constraint) {
		if (child instanceof DiagramItemPart && constraint instanceof Rectangle) {
			// return a command that can move and/or resize a Shape
//			return new ItemSetConstraintCommand(
//					(IDiagramItem)child.getModel(), request, (Rectangle) constraint);
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
		Object childClass = request.getNewObjectType();
//		if (childClass == IBasicTable.class ) {
//			// return a command that can add a Shape to a ShapesDiagram
//
//			return new TableCreateCommand((IBasicTable)request.getNewObject(),
//					(IDiagram)getHost().getModel(), (Rectangle)getConstraintFor(request));
//		}
		return null;
	}

	/**
	 * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChildEditPolicy(org.eclipse.gef.EditPart)
	 */
	@Override
	protected EditPolicy createChildEditPolicy(EditPart child) {
		// TODO Auto-generated method stub
		NonResizableEditPolicy p = new NonResizableEditPolicy();
		p.setDragAllowed(false);
		return p;
	}

}
