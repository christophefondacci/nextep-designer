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
package com.nextep.designer.dbgm.gef.editors;

import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.CreateRequest;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.designer.dbgm.gef.commands.UniqueKeyCreateCommand;
import com.nextep.designer.dbgm.gef.figures.ColumnsFigure;
import com.nextep.designer.vcs.model.IDiagramItem;

public class DiagramItemXYLayoutPolicy extends XYLayoutEditPolicy {

	@Override
	protected Command createChangeConstraintCommand(EditPart child,
			Object constraint) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Command getCreateCommand(CreateRequest request) {
		Object childClass = request.getNewObjectType();
		 if( childClass == UniqueKeyConstraint.class && getHost().getModel() instanceof IDiagramItem) {
//				if(getHost().getModel() instanceof IDiagramItem) {
					return new UniqueKeyCreateCommand( (IBasicTable)((IDiagramItem)getHost().getModel()).getItemModel());
//				}
			}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected XYLayout getXYLayout() {
//		try {
			return super.getXYLayout();
//		} catch(ClassCastException e) {
//			return null;
//		}
	}
	
	@Override
	protected Command getAddCommand(Request generic) {
		if(getLayoutContainer() instanceof ColumnsFigure) {
			return null;
		}
		// TODO Auto-generated method stub
		return super.getAddCommand(generic);
	}
}
