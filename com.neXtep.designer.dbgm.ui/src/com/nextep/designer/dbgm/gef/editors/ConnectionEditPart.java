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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.gef.figures.EllipseDecoration;
import com.nextep.designer.dbgm.gef.figures.ForeignKeyConnection;
import com.nextep.designer.dbgm.gef.figures.KeyDecoration;
import com.nextep.designer.vcs.model.IDiagram;

/**
 * @author Christophe Fondacci
 *
 */
public class ConnectionEditPart extends AbstractConnectionEditPart implements IEventListener {

	/** Maximum objects in the diagram to activate proper routing (for performance tuning on large diagrams)*/
	private static final int MAX_OBJECTS_FOR_CONNECTION_ROUTING = 100;
	private ForeignKeyConstraint constraint;
	public ConnectionEditPart(ForeignKeyConstraint constraint) {
		this.constraint=constraint;
		setModel(constraint);
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {
		if (!isActive()) {
			super.activate();
			Designer.getListenerService().registerListener(this,((IObservable) getModel()),this);
		}
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		if (isActive()) {
			super.deactivate();
			((IObservable) getModel()).removeListener(this);
		}
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// Selection handle edit policy.
		// Makes the connection show a feedback, when selected by the user.
		installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE,
				new ConnectionEndpointEditPolicy());

	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		ForeignKeyConnection connection = new ForeignKeyConnection(this,constraint);

		connection.setSourceDecoration(new KeyDecoration(connection)); // arrow at target endpoint
		connection.setTargetDecoration(new EllipseDecoration());
//		connection.setConnectionRouter(new ShortestManhattanRouter(((GraphicalEditPart)getViewer().getContents()).getFigure()));
		EditPart ep = getViewer().getContents();
		if(ep instanceof DiagramEditPart) {
			IDiagram diagram = (IDiagram)((DiagramEditPart)ep).getModel();
			if(diagram!=null && diagram.getItems().size()<MAX_OBJECTS_FOR_CONNECTION_ROUTING) {
				connection.setConnectionRouter(new ShortestPathConnectionRouter(((GraphicalEditPart)getViewer().getContents()).getFigure()));		
			}
		}
		
//		connection.setConnectionRouter(new ManhattanConnectionRouter());
	//	connection.setLineStyle(getCastedModel().getLineStyle());  // line drawing style
		return connection;
	}

	/**
	 * Sets the width of the line when selected
	 */
	public void setSelected(int value)
	{
		super.setSelected(value);
		if (value != EditPart.SELECTED_NONE) {
			((PolylineConnection) getFigure()).setLineWidth(2);
			((ForeignKeyConnection)getFigure()).setLabelVisible(true);
		} else {
			((PolylineConnection) getFigure()).setLineWidth(1);
			((ForeignKeyConnection)getFigure()).setLabelVisible(false);
		}
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if(getParent()!=null && getParent().getChildren()!=null) {
			for(Object p : getParent().getChildren()) {
				((EditPart)p).refresh();
			}
		}
	}
}
