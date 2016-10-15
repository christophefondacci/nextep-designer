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

import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface represents any item displayed on a diagram.
 * 
 * @author Christophe Fondacci
 */
public interface IDiagramItem extends IdentifiedObject, IObservable, IReferencer, ITypedObject,
		IReferenceable {

	public static final String TYPE_ID = "DIAGRAM_ITEM"; //$NON-NLS-1$

	/**
	 * @return the X position of this item on the diagram
	 */
	public int getXStart();

	/**
	 * @return the Y position of this item ont the diagram
	 */
	public int getYStart();

	/**
	 * @return the width of this item
	 */
	public int getWidth();

	/**
	 * Sets the width of this graphical item
	 * 
	 * @param width new graphical width of the item
	 */
	public void setWidth(int width);

	/**
	 * @return the graphical height of this item
	 */
	public int getHeight();

	/**
	 * Sets the graphical height of this item
	 * 
	 * @param height new graphical height of this item
	 */
	public void setHeight(int height);

	/**
	 * Sets the X position of this item
	 * 
	 * @param x new X position
	 */
	public void setXStart(int x);

	/**
	 * Sets the Y position of this item
	 * 
	 * @param y new Y position
	 */
	public void setYStart(int y);

	/**
	 * Sets the object model represented by this item.
	 * 
	 * @param itemModel model represented by this item
	 */
	// public void setItemModel(IVersionable itemModel);
	/**
	 * @return the object model represented by this item
	 */
	public IReferenceable getItemModel();

	/**
	 * @return the absolute reference of this item, whatever the version.
	 */
	public IReference getItemReference();

	/**
	 * Defines the reference of this item
	 * 
	 * @param itemRef new item reference
	 */
	public void setItemReference(IReference itemRef);

	/**
	 * Defines the parent diagram of this item
	 * 
	 * @param parent parent diagram containing this item
	 */
	public void setParentDiagram(IDiagram parent);

	/**
	 * @return the {@link IDiagram} containing this item
	 */
	public IDiagram getParentDiagram();
}
