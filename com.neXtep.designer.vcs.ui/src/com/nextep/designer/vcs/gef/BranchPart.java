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
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * @author Christophe Fondacci
 *
 */
public class BranchPart extends AbstractGraphicalEditPart {

	private IDiagramItem item;
	private BranchReferenceAdapter branchRef;

	public BranchPart(IDiagramItem item) {
		this.item = item;
		branchRef = (BranchReferenceAdapter)item.getItemModel();
		setModel(item);
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		Label l = new Label(branchRef.getReference().getArbitraryName());
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);

		layout.setVertical(true);
		layout.setStretchMinorAxis(true);

		l.setFont(FontFactory.FONT_BOLD);
		l.setLayoutManager(layout);
		return l;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub

	}
	protected void refreshVisuals() {
		// notify parent container of changed position & location
		// if this line is removed, the XYLayoutManager used by the parent container
		// (the Figure of the ShapesDiagramEditPart), will not know the bounds of this figure
		// and will not draw it correctly.
		Rectangle bounds = new Rectangle(item.getXStart(),item.getYStart(),item.getWidth(),item.getHeight()); //getFigure().getPreferredSize().width, getFigure().getPreferredSize().height); //
		((GraphicalEditPart) getParent()).setLayoutConstraint(this, getFigure(), bounds);
	}
}
