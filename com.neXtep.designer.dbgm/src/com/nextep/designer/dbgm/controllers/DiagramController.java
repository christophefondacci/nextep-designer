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
package com.nextep.designer.dbgm.controllers;

import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.controllers.base.AbstractVersionableController;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;

public class DiagramController extends AbstractVersionableController {

	@Override
	public void modelChanged(Object content) {
	}

	@Override
	public void modelDeleted(Object content) {
		if (content instanceof IDiagramItem) {
			IDiagramItem item = (IDiagramItem) content;
			IDiagram diagram = (IDiagram) item.getParentDiagram();
			item.setParentDiagram(null);
			diagram.removeItem(item);
			CorePlugin.getIdentifiableDao().delete(item);
			save(diagram);
			CorePlugin.getService(IReferenceManager.class).dereference(item);
		} else {
			super.modelDeleted(content);
		}

	}
}
