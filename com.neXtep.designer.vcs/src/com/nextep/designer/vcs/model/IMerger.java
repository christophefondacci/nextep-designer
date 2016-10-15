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

/**
 * Interface of an element merger. It provides operations
 * for comparing, merging and rebuilding elements of a
 * given type. Mergers should be registered by the
 * MergerProvider extension point.<br>
 * Implementors must not implement this interface directly
 * by should rather extend the <code>AbstractMerger</code>
 * class which provides merging abilities.
 *
 * @author Christophe Fondacci
 *
 */
public interface IMerger extends IObservable {

	/**
	 * Compares the source & target and returns the comparison
	 * result through a IComparisonItem hierarchy. Implementors
	 * should select attributes to compare. Those attributes
	 * will then be used in the merge process.<br>
	 *
	 * @param source the source item of the comparison (left side)
	 * @param target the target item of the comparison (right side)
	 * @return a <code>IComparisonItem</code> hierarchy
	 */
	IComparisonItem compare(IReferenceable source, IReferenceable target);
	
	/**
	 * Compares an element with another of its version. Typically, the passed element
	 * would be the current workspace element which is being compared with another
	 * of its version
	 * 
	 * @param source source element of the comparison 
	 * @param otherVersion another release of this same element as a {@link IVersionInfo}.
	 * 						passing a version of another element from the source will fail
	 * @return the {@link IComparisonItem} hierarchy root
	 */
	IComparisonItem compare(IReferenceable source, IVersionInfo otherVersion);
	/**
	 * Compares 2 versions of an element which is identified
	 * by its absolute reference. This method is only here
	 * for conveniency and will call the other <code>compare</code>
	 * method when the 2 elements would have been retrieved.
	 *
	 * @param ref absolute element reference
	 * @param source version of this element to use as the comparison source
	 * @param destination version of this element to use as the comparison target
	 * @param sandBoxSession a flag indicating if objects should be loaded in a separate
	 * sandbox hibernate session or in the current view session
	 * @return a <code>IComparisonItem</code> hierarchy
	 * @see IMerger#doCompare(IReferenceable, IReferenceable)
	 */
	IComparisonItem compare(IReference ref, IVersionInfo source, IVersionInfo destination,boolean sandBoxSession);
	
	
	/**
	 * Builds the merge information from the comparison item.
	 *
	 * @param result result of a first comparison of 2 element versions
	 * @param sourceRootDiff result of the comparison of the source element
	 * with the last common ancestor version of the 2 compared elements
	 * @param targetRootDiff result of the comparison of the target element
	 * with the last common ancestor version of the 2 compared elements
	 * @return the merge information resulting of this merge
	 */
	MergeInfo merge(IComparisonItem result, IComparisonItem sourceRootDiff, IComparisonItem targetRootDiff);
	/**
	 * Builds a merged object from the provided comparison result (which
	 * must contain fully resolved merge info). An exception will be thrown
	 * if the provided comparison result contains unresolved or unmerged
	 * data. A version activity should also be provided and should correspond
	 * to the merge activity. It will be used for any versioning operation
	 * which may occur during the build of the new merged object.
	 *
	 * @param result the comparison result of the merged objects containing
	 * 		  resolved merge information.
	 * @param mergeActivity the merge versioning activity
	 * @return the merged object
	 */
	Object buildMergedObject(IComparisonItem result, IActivity mergeActivity);

	/**
	 * Defines the strategy of merge to use.
	 *
	 * @param mergeStrategy
	 */
	void setMergeStrategy(IMergeStrategy mergeStrategy);
	/**
	 * @return the strategy to use for merge
	 */
	IMergeStrategy getMergeStrategy();
	boolean isVersionable();
}

