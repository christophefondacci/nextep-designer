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
package com.nextep.designer.vcs.model;

import java.util.Collection;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface represents a graphical diagram
 *
 * @author Christophe Fondacci
 *
 */
public interface IDiagram extends INamedObject, IdentifiedObject, IObservable, ILockable<IDiagram> {

	public static final String TYPE_ID="DIAGRAM";
	/**
	 *
	 * @return a Collection of all items displayed on this diagram
	 */
	public abstract Collection<IDiagramItem> getItems();
	/**
	 * Adds an item to this diagram.
	 *
	 * @param item item to add to this diagram
	 */
	public abstract void addItem(IDiagramItem item);
	/**
	 * Removes an item from this diagram
	 *
	 * @param item item to remove from diagram
	 */
	public abstract void removeItem(IDiagramItem item);
	/**
	 * Retrieves an item displayed on this diagram given
	 * the object model it represents. This method should
	 * return <code>null</code> if no item exists for the
	 * specified object model.
	 *
	 * @param itemModel model represented by the diagram item
	 * @return a <code>IDiagramItem</code> representing the model on this diagram
	 */
	public abstract IDiagramItem getItem(IReferenceable itemModel);

}
