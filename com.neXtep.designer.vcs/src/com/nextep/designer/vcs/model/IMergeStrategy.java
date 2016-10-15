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
package com.nextep.designer.vcs.model;

import java.util.Collection;
import com.nextep.datadesigner.model.IReferenceable;

/**
 * Defines the strategy for the merge process.
 * Depending on the source of the merged items (repository,
 * database target), the mechanisms of the merge might
 * differ, mostly for the match / selection process.
 *
 * The abstract merger will delegate the merge process to
 * the appropriate strategy.
 *
 * @author Christophe Fondacci
 *
 */
public interface IMergeStrategy {

	/**
	 * Performs the merge operation of the specified comparison item.
	 * This method is a delegate to the method {@link IMerger#merge(IComparisonItem, IComparisonItem, IComparisonItem)}
	 *
	 * @param merger the merger which has delegated the process to this strategy
	 * @param result the comparison item to merge
	 * @param sourceRootDiff (optional, depending on the strategy) comparison between source
	 * 		  				 and last common ancestor
	 * @param targetRootDiff (optional, depending on the strategy) comparison between target
	 * 		  				 and last common ancestor
	 * @return the generated merge information
	 * @see IMerger#merge(IComparisonItem, IComparisonItem, IComparisonItem)
	 */
	public MergeInfo merge(IMerger merger, IComparisonItem result, IComparisonItem sourceRootDiff,
            IComparisonItem targetRootDiff);


	/**
	 * Defines how 2 compared elements match. Depending on the strategy,
	 * 2 elements might match on reference, or on their names/types, etc.<br>
	 * <b>IMPORTANT : a match does not mean the 2 objects are equal!</b><br>
	 * If 2 elements match, they will be compared together (on the same
	 * compare / merge line). If they don't match they will never be
	 * compared each other and will appear on separate compare / merge line.
	 *
	 * @param source source element
	 * @param target target element
	 * @return <code>true</code> if the 2 elements match, else <code>target</code>
	 */
	public boolean match(IReferenceable source, IReferenceable target);
	/**
	 * Initializes a comparison of 2 collections of sources and targets.
	 * @param <V> collection type
	 * @param sources list of source elements
	 * @param targets list of taget elements
	 */
	public <V extends IReferenceable> void initializeCollectionComparison(Collection<V> sources, Collection<V> targets);
	/**
	 * Indicates whether the given element exists in the targets
	 * collection or not.
	 *
	 * @param source source item for which we want to know the existence
	 * 				 in targets collection.
	 * @return <code>true</code> if a matching item is found in the targets
	 * 		   collection
	 */
	public boolean existsInTargets(IReferenceable source);
	/**
	 * Indicates whether the given element exists in the sources
	 * collection or not.
	 *
	 * @param target target item for which we want to know the existence
	 * 				 in sources collection.
	 * @return <code>true</code> if a matching item is found in the sources
	 * 		   collection
	 */
	public boolean existsInSources(IReferenceable target);
	/**
	 * Retrieves the element in the targets collection which matches
	 * the specified source element. This method might not be called
	 * if {@link IMergeStrategy#existsInTargets(IReferenceable)} has
	 * returned <code>false</code> on the same source.
	 *
	 * @param source source element to look for a target match
	 * @return the matching target element
	 */
	public IReferenceable getMatchingTarget(IReferenceable source);
	/**
	 * Retrieves the element in the sources collection which matches
	 * the specified target element. This method might not be called
	 * if {@link IMergeStrategy#existsInSources(IReferenceable)} has
	 * returned <code>false</code> on the same target.
	 *
	 * @param source source element to look for a target match
	 * @return the matching target element
	 */
	public IReferenceable getMatchingSource(IReferenceable target);
	/**
	 * A merge strategy defines the scope of a merge / comparison
	 * process. This method indicates the scope of the current
	 * merge strategy.
	 *
	 * @return the comparison scope of this merge strategy
	 * @see ComparisonScope
	 */
	public ComparisonScope getComparisonScope();
}
