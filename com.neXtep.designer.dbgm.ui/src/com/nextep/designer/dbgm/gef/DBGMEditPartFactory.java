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
package com.nextep.designer.dbgm.gef;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.designer.dbgm.gef.editors.ColumnPart;
import com.nextep.designer.dbgm.gef.editors.ConnectionEditPart;
import com.nextep.designer.dbgm.gef.editors.DiagramEditPart;
import com.nextep.designer.dbgm.gef.editors.DiagramItemPart;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;

/**
 * @author Christophe Fondacci
 *
 */
public class DBGMEditPartFactory implements EditPartFactory {

	private Map<ForeignKeyConstraint,ConnectionEditPart> keysMap = new HashMap<ForeignKeyConstraint, ConnectionEditPart>();
	/**
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		if(model instanceof IBasicColumn) {
			return new ColumnPart((IBasicColumn)model);
		} else if(model instanceof IDiagram) {
			return new DiagramEditPart((IDiagram)model);
		} else if(model instanceof IDiagramItem) {
			return new DiagramItemPart((IDiagramItem)model);
		} else if(model instanceof ForeignKeyConstraint) {
			ConnectionEditPart editPart = keysMap.get(model);
			if(editPart == null) {
				editPart =new ConnectionEditPart((ForeignKeyConstraint)model);
				keysMap.put((ForeignKeyConstraint)model,editPart);
			}
			return editPart;
		}
		// TODO Auto-generated method stub
		return null;
	}

}
