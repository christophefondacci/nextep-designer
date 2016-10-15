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
package com.nextep.designer.vcs.ui.model;

import java.util.Collection;
import org.eclipse.core.runtime.IAdaptable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * This interface represents a typed node used to categorize visual elements belonging to a same
 * {@link IElementType} for a same parent element.
 * 
 * @author Christophe Fondacci
 */
public interface ITypedNode extends IAdaptable {

	/**
	 * Retrieves the children elements of the current node
	 * 
	 * @return a collection of {@link ITypedObject} elements which all verify the condition :<br>
	 *         elt.getType() == this.getNodeType
	 */
	Collection<ITypedObject> getChildren();

	/**
	 * Retrieves the parent of this node. All children elements all have the same parent represented
	 * by the {@link ITypedObject} returned by this method.
	 * 
	 * @return the "real" parent of all children elements of this node.
	 */
	ITypedObject getParent();

	/**
	 * Adds a child element to this node.
	 * 
	 * @param child the {@link ITypedObject} child element to add to this node.
	 */
	void addChild(ITypedObject child);

	/**
	 * Defineds the whole set of children elements.
	 * 
	 * @param children collection of children of this node
	 */
	void setChildren(Collection<ITypedObject> children);

	/**
	 * Defines the parent element for this node.
	 * 
	 * @param parent the {@link ITypedObject} parent for this node.
	 */
	void setParent(ITypedObject parent);

	/**
	 * Retrieves the type of this node.
	 * 
	 * @return the {@link IElementType} represented by this node which "groups" all children of a
	 *         same parent with a same type.
	 */
	IElementType getNodeType();

	/**
	 * Retrieves the name of this node, it should typically be the node type category name.
	 * 
	 * @return the name of this node.
	 */
	String getName();
}
