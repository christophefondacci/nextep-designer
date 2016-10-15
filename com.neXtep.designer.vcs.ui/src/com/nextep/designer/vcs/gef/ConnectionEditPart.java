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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;

/**
 * @author Christophe Fondacci
 *
 */
public class ConnectionEditPart extends AbstractConnectionEditPart {

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub

	}
	/* (non-Javadoc)
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		PolylineConnection connection = (PolylineConnection) super.createFigure();
		connection.setTargetDecoration(new PolygonDecoration()); // arrow at target endpoint
		connection.setConnectionRouter(new ShortestPathConnectionRouter(((GraphicalEditPart)getViewer().getContents()).getFigure()));
	//	connection.setLineStyle(getCastedModel().getLineStyle());  // line drawing style
		return connection;
	}
//
//	protected void refreshVisuals() {
//		// notify parent container of changed position & location
//		// if this line is removed, the XYLayoutManager used by the parent container
//		// (the Figure of the ShapesDiagramEditPart), will not know the bounds of this figure
//		// and will not draw it correctly.
//		IDiagramItem srcItem = (IDiagramItem)getSource().getModel();
//		IDiagramItem dstItem = (IDiagramItem)getTarget().getModel();
//		Rectangle bounds = new Rectangle(item.getXStart(),item.getYStart(),item.getWidth(),item.getHeight());
//		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
//	}
}
