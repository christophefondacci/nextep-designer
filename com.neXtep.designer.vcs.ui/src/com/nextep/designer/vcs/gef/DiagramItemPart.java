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

import java.util.Collections;
import java.util.List;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.viewers.StructuredSelection;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * @author Christophe Fondacci
 *
 */
public class DiagramItemPart extends AbstractGraphicalEditPart implements NodeEditPart, IEventListener, MouseListener {
	private IDiagramItem item;
	private IVersionInfo version;
	private boolean selectable = false;
	public DiagramItemPart(IDiagramItem item, boolean selectable) {
		this.item = item;
		this.version = (IVersionInfo)item.getItemModel();//.getVersionnedObject().getModel();
		setModel(item);
		this.selectable=selectable;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		IFigure f =new VersionInfoFigure(version,this.getRoot().getViewer(),((VersionTreeDiagram)item.getParentDiagram()).isCurrent(version));
		if(selectable) {
			f.addMouseListener(this);
		}
		return f;
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		//installEditPolicy(EditPolicy.LAYOUT_ROLE,  new DiagramXYLayoutPolicy(item.getParentDiagram()));
		//installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new SelectionPolicy());

	}
	protected void refreshVisuals() {
		// notify parent container of changed position & location
		// if this line is removed, the XYLayoutManager used by the parent container
		// (the Figure of the ShapesDiagramEditPart), will not know the bounds of this figure
		// and will not draw it correctly.
		Rectangle bounds = new Rectangle(item.getXStart(),item.getYStart(),item.getWidth(),item.getHeight());
		final IFigure f = getFigure();
		if(f!=null) {
			((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
		}
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
	 */
	@Override
	protected List<VersionConnection> getModelSourceConnections() {
		List<VersionConnection> l = ((VersionTreeDiagram)item.getParentDiagram()).getVersionSuccessors(version);
		if(l == null) {
			return Collections.emptyList();
		}
		return l;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
	 */
	@Override
	protected List<VersionConnection> getModelTargetConnections() {
		List<VersionConnection> l = ((VersionTreeDiagram)item.getParentDiagram()).getVersionPredecessors(version);
		if(l == null) {
			return Collections.emptyList();
		}
		return l;
	}
	/**
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(
			ConnectionEditPart connection) {
		return new EllipseAnchor(getFigure());
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
	 */
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new EllipseAnchor(getFigure());
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(
			ConnectionEditPart connection) {
		return new EllipseAnchor(getFigure());
	}

	/**
	 * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
	 */
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new EllipseAnchor(getFigure());
	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshVisuals();
	}

	/**
	 * @see org.eclipse.draw2d.MouseListener#mouseDoubleClicked(org.eclipse.draw2d.MouseEvent)
	 */
	@Override
	public void mouseDoubleClicked(MouseEvent me) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.draw2d.MouseListener#mousePressed(org.eclipse.draw2d.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent me) {
		this.getRoot().getViewer().setSelection(new StructuredSelection(this));
	}

	/**
	 * @see org.eclipse.draw2d.MouseListener#mouseReleased(org.eclipse.draw2d.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent me) {
		// TODO Auto-generated method stub

	}

}
