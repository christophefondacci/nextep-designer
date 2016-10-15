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
package com.nextep.designer.vcs.factories;

import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.vcs.impl.DiagramItem;
import com.nextep.datadesigner.vcs.impl.VersionedDiagram;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.IDiagram;
import com.nextep.designer.vcs.model.IDiagramItem;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 *
 */
public class DiagramFactory extends VersionableFactory {
	private static DiagramFactory instance = null;
	private static final Log log = LogFactory.getLog(DiagramFactory.class);
	public DiagramFactory() { }
	public static VersionableFactory getInstance() {
		if(instance == null) {
			instance = new DiagramFactory();
		}
		return instance;
	}
	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#createVersionable()
	 */
	@Override
	public IVersionable createVersionable() {
		return new VersionedDiagram();
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionableFactory#rawCopy(com.nextep.designer.vcs.model.IVersionable, com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	public void rawCopy(IVersionable source, IVersionable destination) {
		IDiagram copy = (IDiagram)destination.getVersionnedObject().getModel();
		IDiagram sourceDiagram = (IDiagram)source.getVersionnedObject().getModel();

		for(IDiagramItem i : sourceDiagram.getItems()) {
			try {
				IDiagramItem copiedItem = new DiagramItem(i.getItemModel(),i.getXStart(),i.getYStart());
				copiedItem.setWidth(i.getWidth());
				copiedItem.setHeight(i.getHeight());
				copy.addItem(copiedItem);
			} catch( UnresolvedItemException e ) {
				log.warn(MessageFormat.format(VCSMessages.getString("checkOutUnresolvedRemoved"), sourceDiagram.getType().getName(), sourceDiagram.getName(),sourceDiagram.getType().getName()));
			}
		}
		versionCopy(source, destination);

	}

}
