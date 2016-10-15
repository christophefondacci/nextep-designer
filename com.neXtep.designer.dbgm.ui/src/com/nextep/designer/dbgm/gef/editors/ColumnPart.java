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
import org.eclipse.draw2d.Label;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.helper.DatatypeHelper;


/**
 * @author Christophe Fondacci
 *
 */
public class ColumnPart extends AbstractGraphicalEditPart implements IEventListener {
	private IBasicColumn column;
	public ColumnPart(IBasicColumn c) {
		this.column=c;
		this.setModel(c);
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
	 */
	@Override
	public void activate() {
		if(!isActive()) {
			super.activate();
			Designer.getListenerService().registerListener(this,column,this);
			if(column.getParent() instanceof IObservable) {
				Designer.getListenerService().registerListener(this,(IObservable)column.getParent(),this);
			}
		}

	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
	 */
	@Override
	public void deactivate() {
		if(isActive()) {
			super.deactivate();
			column.removeListener(this);
			if(column.getParent() instanceof IObservable) {
				((IObservable)column.getParent()).removeListener(this);
			}
		}
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	@Override
	protected IFigure createFigure() {
		IBasicColumn column = (IBasicColumn) getModel();
		String label = column.getName();
		Label columnLabel = new Label(label);
		columnLabel.setIcon(DatatypeHelper.getDatatypeIcon(column.getDatatype(), true));
		columnLabel.setForegroundColor(FontFactory.CHECKIN_COLOR);
		final IKeyConstraint pk = DBGMHelper.getPrimaryKey(column.getParent());
		if(pk!=null && pk.getConstrainedColumnsRef().contains(column.getReference())) {
			columnLabel.setFont(FontFactory.FONT_BOLD);
		} else if(!column.isNotNull()) {
			columnLabel.setFont(FontFactory.FONT_ITALIC);
		}
		return columnLabel;
	}

	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
	 */
	@Override
	protected void refreshVisuals() {
		Label lbl = (Label)getFigure();
		lbl.setText(column.getName());
		lbl.setIcon(DatatypeHelper.getDatatypeIcon(column.getDatatype(),true));
	}
	/**
	 * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
	 */
	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub

	}
	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent, com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		refreshVisuals();

	}
@Override
public boolean isSelectable() {
	// TODO Auto-generated method stub
	return false;
}
}
