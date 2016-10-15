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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionInfo;

/**
 * @author Christophe Fondacci
 *
 */
public class VersionEditPartFactory implements EditPartFactory {

	private Map<VersionConnection,ConnectionEditPart> connectionsMap = new HashMap<VersionConnection,ConnectionEditPart>();
	private boolean selectable = false;
	public VersionEditPartFactory(boolean selectable) {
		this.selectable=selectable;
	}
	public VersionEditPartFactory() {
		this.selectable=false;
	}
	/**
	 * @see org.eclipse.gef.EditPartFactory#createEditPart(org.eclipse.gef.EditPart, java.lang.Object)
	 */
	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		if(model instanceof IDiagram) {
			return new DiagramEditPart((IDiagram)model);
		} else if(model instanceof IDiagramItem) {
			IDiagramItem item = (IDiagramItem)model;
			if(item.getItemModel() instanceof IVersionInfo) {
				return new DiagramItemPart(item,selectable);
			} else {
				return new BranchPart(item);
			}
		} else if(model instanceof VersionConnection) {
			ConnectionEditPart conn = connectionsMap.get(model);
			if(conn == null) {
				conn = new ConnectionEditPart();
				connectionsMap.put((VersionConnection)model, conn);
			}
			return conn;
		}
		// TODO Auto-generated method stub
		return null;
	}

}
