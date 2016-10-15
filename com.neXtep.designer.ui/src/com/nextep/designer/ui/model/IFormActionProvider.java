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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.ui.model;

import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;

/**
 * This action provider allows implementors to customize the actions of the default master / details
 * form UI implementation. This kind of page contains a "Add", "Remove" and optionally a "Move up"
 * and "Move down" buttons. This action provider provides methods which are called when the user
 * clicks on the corresponding buttons.<br>
 * This interface should generally not be implemented directly as it may change without notice. The
 * abstract base class {@link AbstractFormActionProvider} should be used as a base so that it can
 * absorb API changes transparently.
 * 
 * @author Christophe Fondacci
 */
public interface IFormActionProvider {

	/**
	 * Adds a new element to the specified parent typed object
	 * 
	 * @param parent the {@link ITypedObject} to add a new element to
	 */
	Object add(ITypedObject parent);

	/**
	 * Removes the specified element
	 * 
	 * @param parent the {@link ITypedObject} parent to remove element from
	 * @param toRemove the {@link ITypedObject} to remove
	 */
	void remove(ITypedObject parent, ITypedObject toRemove);

	/**
	 * This method is called when the user wants to reorder the specified child element up. It is
	 * only applicable when working with sortable elements where the order has a meaning. This
	 * action is called to move the specified element up of 1 rank in the items list.
	 * 
	 * @param parent the {@link ITypedObject} parent containing all sorted elements
	 * @param element the element to move up
	 */
	void up(ITypedObject parent, ITypedObject element);

	/**
	 * This method is called when the user wants to reorder the specified child element down. It is
	 * only applicable when working with sortable elements where the order has a meaning. This
	 * action is called to move the specified element down of 1 rank in the items list.
	 * 
	 * @param parent the {@link ITypedObject} parent containing all sorted elements
	 * @param element the element to move down
	 */
	void down(ITypedObject parent, ITypedObject element);

	/**
	 * Informs whether this action provider supports custom edition of the selected element.
	 * Providers which returns <code>true</code> to this method need to implement the
	 * {@link IFormActionProvider#edit(ITypedObject, ITypedObject)} method to provide edition
	 * support.
	 * 
	 * @return <code>true</code> when elements can be edited, else <code>false</code>
	 */
	boolean isEditable();

	/**
	 * Informs whether this action provider supports sorting of the listed elements. Providers which
	 * returns <code>true</code> to this method need to implement the
	 * {@link IFormActionProvider#up(ITypedObject, ITypedObject)} and
	 * {@link IFormActionProvider#down(ITypedObject, ITypedObject)} methods to provide sorting
	 * support.
	 * 
	 * @return <code>true</code> when elements can be sorted, else <code>false</code>
	 */
	boolean isSortable();

	/**
	 * Informs wether actions like <code>Add</code> and <code>Remove</code> buttons are displayed
	 * and exposed to the user.
	 * 
	 * @return <code>true</code> (default) when Add and Remove are displayed, <code>false</code> to
	 *         hide them
	 */
	boolean isAddRemoveEnabled();

	/**
	 * Edits the specfied elements. This method will be called when the user requests edition of one
	 * element.
	 * 
	 * @param parent parent of the currently selected elements
	 * @param element selected element to edit
	 */
	void edit(ITypedObject parent, ITypedObject element);
}
