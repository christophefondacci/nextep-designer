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
import java.util.List;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * A comparison item is an element involved in a comparison. It allows the comparison of 2 objects
 * and is manipulated by the mergers to build a comparison between 2 objects. This is an
 * hierarchical structure.
 * 
 * @author Christophe Fondacci
 */
public interface IComparisonItem extends IReferenceable, ITypedObject, IObservable {

	/**
	 * Defines the new source of the comparison item.<br>
	 * <b>USE WITH EXTREME CARE.</b><br>
	 * Published this method for test purposes to avoid code rewrite for validating generation
	 * merge.
	 * 
	 * @param source
	 */
	// public void setSource(IReferenceable source);
	/**
	 * @return the source item of comparison
	 */
	IReferenceable getSource();

	/**
	 * @return the destination item of comparison
	 */
	IReferenceable getTarget();

	/**
	 * Retrieves the parent of this comparison item
	 * 
	 * @return the parent {@link IComparisonItem} or <code>null</code> for a root item
	 */
	IComparisonItem getParent();

	/**
	 * Defines the parent of a comparison item
	 * 
	 * @param parentItem parent comparison item
	 */
	void setParent(IComparisonItem parentItem);

	/**
	 * @return the kind of difference between source and target
	 */
	DifferenceType getDifferenceType();

	/**
	 * @return sub items of this comparison
	 */
	List<IComparisonItem> getSubItems();

	/**
	 * A convenience method to retrieve an item attribute
	 * 
	 * @param attributeName name of the attribute to retreive
	 * @return the corresponding comparison item
	 */
	IComparisonItem getAttribute(String attributeName);

	/**
	 * A convenience method to retrieve a sub item by its reference. This method will not
	 * recursively look for this reference in its sub item so the item must be a direct children of
	 * this one to be returned by this method call.
	 * 
	 * @param ref reference of the comparison item to retrieve
	 * @return the matching comparison subitem or <code>null</code>
	 */
	IComparisonItem getSubItem(IReference ref);

	/**
	 * Adds a new sub item to this comparison item
	 * 
	 * @param subItem new sub item
	 */
	void addSubItem(IComparisonItem subItem);

	/**
	 * Adds a new sub item in the specified category. This allows to classify comparison items and
	 * to retrieve categories item by calling the {@link IComparisonItem#getSubItems(String)}
	 * method. Items will still be returned when calling the {@link IComparisonItem#getSubItems()}
	 * method.
	 * 
	 * @param category sub item category
	 * @param subItem sub item to add
	 */
	void addSubItem(String category, IComparisonItem subItem);

	/**
	 * Retrieves the list of all sub items of the specified category.
	 * 
	 * @param category category for which the sub items have been defined
	 * @return the list of comparison items contained in this category
	 */
	List<IComparisonItem> getSubItems(String category);

	/**
	 * Sets the merge information of this comparison
	 * 
	 * @param info merge information
	 */
	void setMergeInfo(MergeInfo info);

	/**
	 * @return the merge information of this item
	 */
	MergeInfo getMergeInfo();

	/**
	 * @return the scope of this comparison item
	 * @see ComparisonScope
	 */
	ComparisonScope getScope();

	/**
	 * Defines the scope of a comparison item
	 * 
	 * @param scope scope of the comparison item
	 * @see ComparisonScope
	 */
	void setScope(ComparisonScope scope);

	/**
	 * Provides the list of categories registered as sub items of this comparison items
	 * 
	 * @return the list of sub-categories, as string values
	 */
	Collection<String> getCategories();
}
