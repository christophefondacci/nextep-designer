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
package com.nextep.datadesigner.vcs.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * A base class for mergers which handles element types which contain columns
 * (table, view, index, keys). It provide a convenience method to merge 2
 * collection of columns.
 * 
 * @author Christophe Fondacci
 */
public abstract class MergerWithChildCollections<T> extends Merger<T> {

	private final IMerger forcedChildMerger = null;

	/**
	 * The default listCompare call with the noSwap flag set to false. See
	 * complete method
	 * {@link MergerWithChildCollections#listCompare(String, IComparisonItem, Collection, Collection, boolean)}
	 * for more information.
	 * 
	 * @see MergerWithChildCollections#listCompare(String, IComparisonItem,
	 *      Collection, Collection, boolean)
	 */
	protected final <V extends IReferenceable> IComparisonItem listCompare(String category,
			IComparisonItem result, Collection<V> sourceItems, Collection<V> targetItems) {
		return listCompare(category, result, sourceItems, targetItems, false);
	}

	/**
	 * This method provides a comparison algorithm for lists. Any mergeable
	 * object which contain any mergeable collection of child items should use
	 * this method to recursively compare its child collections. <br>
	 * For example, a Table merger should call this method with the column
	 * collections of the source and target table which is being compared.<br>
	 * The items compared by this method could then be merged by calling the
	 * {@link MergerWithChildCollections#getMergedList(String, IComparisonItem, IActivity)}
	 * method which will recursively merge the generated list.
	 * 
	 * @param <V>
	 *            method parameter to ensure source and destination collections
	 *            contains the same element type.
	 * @param category
	 *            a string attribute used to categorized this list within the
	 *            current comparison result
	 * @param result
	 *            the comparison result currently being processed
	 * @param sourceItems
	 *            collection of mergeable childs of the source item
	 * @param targetItems
	 *            collection of mergeable childs of the target item
	 * @param noSwap
	 *            a flag indicating whether the algorithm will try to match any
	 *            item of the source collection with any item of the target
	 *            collection. If set to true then source & target collection
	 *            will be compared in an order-dependent way.
	 * @return the specified comparison result (result) for convenience
	 *         purposes.
	 */
	protected final <V extends IReferenceable> IComparisonItem listCompare(String category,
			IComparisonItem result, Collection<V> sourceItems, Collection<V> targetItems,
			boolean noSwap) {
		// Delegating to the IComparisonManager
		List<IComparisonItem> comparisons = VCSPlugin.getComparisonManager().compare(sourceItems,
				targetItems, getMergeStrategy(), noSwap);
		// Injecting results into our input comparison item
		for (IComparisonItem item : comparisons) {
			result.addSubItem(category, item);
		}

		return result;
	}

	/**
	 * This method merges all items which have been compared using the
	 * {@link MergerWithChildCollections#listCompare(String, IComparisonItem, Collection, Collection, boolean)}
	 * method. It will recursively invoke the mergers for any object contained
	 * in the list.
	 * 
	 * @param category
	 *            a String attribute used to retrieve the collection within the
	 *            compared list. Should be the same as the category specified in
	 *            the <code>listCompare</code> method call.
	 * @param result
	 *            the comparison result in which the comparison information will
	 *            be found. This result must have been compared using the
	 *            listCompare method.
	 * @param activity
	 *            version activity to use if a merger needs to perform a version
	 *            action which requires an activity.
	 * @return a list of the merged object built from the comparison
	 *         information.
	 */
	@SuppressWarnings("unchecked")
	protected List<Object> getMergedList(String category, IComparisonItem result, IActivity activity) {
		List<Object> list = new ArrayList<Object>();
		List<IComparisonItem> items = result.getSubItems(category);
		if (items == null)
			return Collections.EMPTY_LIST;
		for (IComparisonItem i : items) {
			IMerger m = null;
			if (forcedChildMerger != null) {
				m = forcedChildMerger;
			} else {
				m = MergerFactory.getMerger(i.getType(), getMergeStrategy().getComparisonScope());
			}
			if (m != null) {
				Object mergedObject = m.buildMergedObject(i, activity);
				if (mergedObject != null) {
					list.add(mergedObject);
				}
			} else {
				throw new ErrorException("The merger for <" + i.getType().getId()
						+ "> is missing! Cannot complete merge, operation has been aborted.");
			}
		}
		return list;
	}
}
