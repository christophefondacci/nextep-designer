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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;



/**
 * @author Christophe Fondacci
 *
 */
public class DiagramEditPart extends AbstractGraphicalEditPart implements IEventListener {
	private IDiagram diagram;
	public DiagramEditPart(IDiagram diagram) {
		this.diagram=diagram;
		Designer.getListenerService().registerListener(this,diagram,this);
		this.setModel(diagram);
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {
		if(!isActive()) {
			super.activate();
			Designer.getListenerService().registerListener(this,diagram,this);
		}
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		if(isActive()) {
			super.deactivate();
			diagram.removeListener(this);
		}
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		Figure f = new SchemaFigure();
		f.setLayoutManager(new FreeformLayout());
		return f;
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
	 */
	@Override
	protected List<IDiagramItem> getModelChildren() {
		IDiagram diag = (IDiagram)getModel();
		return new ArrayList<IDiagramItem>(diag.getItems());
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE,  new DiagramXYLayoutPolicy(diagram));
	//	installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new SelectionPolicy());
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if(getParent()!=null) {
			refreshChildren();
			refreshVisuals();
		}
	}

}
