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
package com.nextep.designer.dbgm.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.VersionedDiagram;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.IDiagram;

public class DiagramFactory implements IElementFactory {

	private static final Log log = LogFactory.getLog(DiagramFactory.class);

	@Override
	public IAdaptable createElement(IMemento memento) {
		IElementType type = IElementType.getInstance(memento.getString("TYPE"));
		if (type != IElementType.getInstance("DIAGRAM")) {
			log.warn("Unable to restore editor");
			return null;
		}

		String id = memento.getString("ID");
		UID uid = new UID(Long.valueOf(id));
		IDiagram d = (IDiagram) CorePlugin.getIdentifiableDao().load(VersionedDiagram.class, uid);
		return new DiagramEditorInput(d);
	}

}
