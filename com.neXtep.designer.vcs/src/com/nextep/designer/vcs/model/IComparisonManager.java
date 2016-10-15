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
import java.util.List;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.MergeStrategy;

/**
 * Interface of the comparison manager which is able to start different kind of comparisons
 * 
 * @author Christophe Fondacci
 */
public interface IComparisonManager {

	/**
	 * Compares 2 versions of a given element. The element to compare is specified by its repository
	 * reference. The 2 versions to consider in the comparisons are represented by 2
	 * {@link IVersionInfo} instances which should exists. <code>Null</code> may be specified in any
	 * of the {@link IVersionInfo} parameters to compare a version to nothing.
	 * 
	 * @param refElementToCompare {@link IReference} of the repository element to compare
	 * @param sourceVersion {@link IVersionInfo} of the source version to compare
	 * @param targetVersion {@link IVersionInfo} of the target version to compare
	 */
	void compare(IReference refElementToCompare, IVersionInfo sourceVersion,
			IVersionInfo targetVersion);

	/**
	 * Compares two sets of versionables elements. Elements will be matched depending on the
	 * {@link MergeStrategy} specified. This method will return a collection of
	 * {@link IComparisonItem} resulting from the comparison. Any {@link IComparisonListener} will
	 * be notified when the comparison is done.
	 * 
	 * @param sources a collection of {@link IVersionable} elements to process as the <u>source</u>
	 *        elements of this comparison
	 * @param targets a collection of {@link IVersionable} elements to process as the <u>target</u>
	 *        elements of this comparison
	 * @param strategy strategy to use for associating elements together
	 * @param noSwap indicate that the comparison should not swap or sort items from input lists
	 * @return a collection of {@link IComparisonItem} containing the comparison information
	 */
	<V extends IReferenceable> List<IComparisonItem> compare(Collection<V> sources,
			Collection<V> targets, IMergeStrategy strategy, boolean noSwap);

	/**
	 * Adds a {@link IComparisonListener} which will be notified when comparisons are performed.
	 * 
	 * @param listener a listener to notify when comparisons are made
	 */
	void addComparisonListener(IComparisonListener listener);

	/**
	 * Removes a {@link IComparisonListener} from the listeners which should be notified of
	 * comparisons. This method will do nothing if the listener is not registered to this manager
	 * 
	 * @param listener listener to remove from notifications
	 */
	void removeComparisonListener(IComparisonListener listener);

	/**
	 * Notifies all listeners that a comparison is available.
	 * 
	 * @param description description to display which informs about this comparison
	 * @param comparisonItems the {@link IComparisonItem} to notify
	 */
	void notifyNewComparison(String description, IComparisonItem... comparisonItems);
}
