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
package com.nextep.designer.vcs.model.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.ObjectComparator;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonListener;
import com.nextep.designer.vcs.model.IComparisonManager;
import com.nextep.designer.vcs.model.IMergeStrategy;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Default implementation of the {@link IComparisonManager} interface.
 * 
 * @author Christophe Fondacci
 */
public class ComparisonManager implements IComparisonManager {

	private Collection<IComparisonListener> listeners;

	public ComparisonManager() {
		listeners = new ArrayList<IComparisonListener>();
	}

	@Override
	public void addComparisonListener(IComparisonListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeComparisonListener(IComparisonListener listener) {
		listeners.remove(listener);
	}

	protected Collection<IComparisonListener> getComparisonListeners() {
		return listeners;
	}

	@Override
	public void compare(final IReference refElementToCompare, final IVersionInfo sourceVersion,
			final IVersionInfo targetVersion) {
		final IMerger merger = MergerFactory.getMerger(refElementToCompare.getType(),
				ComparisonScope.REPOSITORY);
		if (merger != null) {
			IComparisonItem result = null;
			// Optimizing performances when source element is in the workspace
			final IReferenceable referenceable = VersionHelper
					.getReferencedItem(refElementToCompare);
			final IVersionable<?> v = VersionHelper.getVersionable(referenceable);
			// if(v!=null && v.getVersion().equals(sourceVersion)) {
			// result = merger.compare(referenceable,targetVersion);
			// } else {
			result = merger.compare(refElementToCompare, sourceVersion, targetVersion, true);
			// }

			// Notifying that this comparison is ready
			final String desc = MessageFormat.format(VCSMessages.getString("comparison.descTitle"),
					sourceVersion != null ? sourceVersion.getLabel() : "",
					targetVersion != null ? targetVersion.getLabel() : "");
			notifyNewComparison(desc, result);
		} else {
			throw new ErrorException("No merger has been found for the selected item type.");
		}
	}

	@Override
	public void notifyNewComparison(String description, IComparisonItem... comparisonItems) {
		for (IComparisonListener l : new ArrayList<IComparisonListener>(listeners)) {
			l.newComparison(description, comparisonItems);
		}
	}

	@Override
	public <V extends IReferenceable> List<IComparisonItem> compare(Collection<V> sourceItems,
			Collection<V> targetItems, IMergeStrategy strategy, boolean noSwap) {
		List<IComparisonItem> comparisons = new ArrayList<IComparisonItem>();

		Collection<V> srcItemList = null;
		Collection<V> tgtItemList = null;
		if (sourceItems instanceof List<?>) {
			srcItemList = (List<V>) sourceItems;
		} else {
			srcItemList = sort(sourceItems);
		}
		if (targetItems instanceof List<?>) {
			tgtItemList = (List<V>) targetItems;
		} else {
			tgtItemList = sort(targetItems);
		}
		// Hashing target items by their reference (only if the noSwap flag is not set)
		strategy.initializeCollectionComparison(sourceItems, targetItems);
		// An array for performed source entries
		List<IReferenceable> processedSources = new ArrayList<IReferenceable>();
		// Iterating synchronously
		Iterator<V> srcColIt = srcItemList.iterator();
		Iterator<V> tgtColIt = tgtItemList.iterator();
		V srcItem = null;
		V tgtItem = null;
		boolean nextSource = true;
		boolean nextTarget = true;
		while (srcColIt.hasNext() || tgtColIt.hasNext() || !nextSource || !nextTarget) {
			if (nextSource)
				srcItem = (srcColIt.hasNext() ? srcColIt.next() : null);
			// Avoiding already processed items
			while (processedSources.contains(srcItem) && srcColIt.hasNext()) {
				srcItem = srcColIt.next();
			}
			if (processedSources.contains(srcItem)) {
				// If the last item has already been processed
				srcItem = null;
			}
			if (nextTarget)
				tgtItem = (tgtColIt.hasNext() ? tgtColIt.next() : null);
			// Progress refresh
			// refreshProgressLabel(srcItem, tgtItem);
			// Resetting flags
			nextSource = nextTarget = true;
			// If we have a strategy match we are on the same item
			if (srcItem != null && tgtItem != null && strategy.match(srcItem, tgtItem)) {
				IMerger merger = getMerger(srcItem, tgtItem, strategy);
				if (merger != null) {
					comparisons.add(merger.compare(srcItem, tgtItem));
				}
			} else {
				// Here, we have 2 different objects in source and target

				// If we have the noSwap then we do not try to make any association
				if (noSwap) {
					IMerger merger = getMerger(tgtItem, srcItem, strategy);
					if (merger != null) {
						comparisons.add(merger.compare(srcItem, tgtItem));
					}
				} else {
					// Does the source item exists in target items ?
					if (srcItem == null || strategy.existsInTargets(srcItem)) {
						if (tgtItem != null) {
							IMerger merger = getMerger(tgtItem, srcItem, strategy);
							if (merger != null) {
								// Does the target item exists in source items ?
								if (strategy.existsInSources(tgtItem)) {
									IReferenceable src = strategy.getMatchingSource(tgtItem);
									// We flag the item as processed
									processedSources.add(src);
									// We perform the matching comparison
									comparisons.add(merger.compare(src, tgtItem));
								} else {
									// If no we consider the target column is a new one
									comparisons.add(merger.compare(null, tgtItem));
								}
							}
							nextSource = (srcItem == null);
						}

					} else {
						IMerger merger = getMerger(srcItem, tgtItem, strategy);
						if (merger != null) {
							comparisons.add(merger.compare(srcItem, null));
						}
						nextTarget = (tgtItem == null); // false;
					}
				}
			}
		}
		return comparisons;

	}

	/**
	 * A convenience method which retrieves the correct merger to process the specified source /
	 * target couple.
	 * 
	 * @param srcItem source item which will be used with the merger
	 * @param tgtItem target item which will be used with the merger
	 * @return the appropriate merger or <code>null</code> if no matching merger has been found.
	 */
	private IMerger getMerger(IReferenceable srcItem, IReferenceable tgtItem,
			IMergeStrategy strategy) {
		return MergerFactory.getMerger(srcItem != null ? srcItem : tgtItem,
				strategy.getComparisonScope());
	}

	/**
	 * Sorting the given collection which may not be a list. Elements will be sorted using an
	 * {@link ObjectComparator} which will base the comparison on the {@link Object#toString()}
	 * method.
	 * 
	 * @param <U>
	 * @param collection collection of elements to sort
	 * @return a sorted list
	 */
	private <U> List<U> sort(Collection<U> collection) {
		// Handling a null collection
		if (collection == null)
			return null;
		List<U> list = new ArrayList<U>(collection);
		// Handling a 0-length collection
		if (collection.size() == 0)
			return list;
		Collections.sort(list, ObjectComparator.getInstance());
		return list;
	}
}
