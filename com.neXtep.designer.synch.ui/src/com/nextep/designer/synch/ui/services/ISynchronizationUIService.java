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
package com.nextep.designer.synch.ui.services;

import java.util.Collection;

import org.apache.commons.collections.map.MultiValueMap;
import org.eclipse.ui.IEditorPart;

import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.services.ISynchronizationService;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This interface is an extension to the {@link ISynchronizationService} which
 * brings some convenience methods for manipulation of the
 * {@link ISynchronizationResult} elements by the user.
 * 
 * @author Christophe Fondacci
 */
public interface ISynchronizationUIService extends ISynchronizationService {

	/**
	 * Selects the specified proposal in the given item. Dependencies will be
	 * considered in the selection depending on the state of the user toggle
	 * button.
	 * 
	 * @param item
	 *            item to change proposal for
	 * @param selection
	 *            proposal to select
	 */
	void selectProposal(IComparisonItem item, ComparedElement selection);

	/**
	 * Selects the specified proposal in the given item. Dependencies will be
	 * considered in the selection depending on the state of the user toggle
	 * button. <br>
	 * This method should be preferred to
	 * {@link ISynchronizationUIService#selectProposal(IComparisonItem, ComparedElement)}
	 * when iteratively selecting items because it will not recompute the
	 * dependencies each time.
	 * 
	 * @param item
	 *            item to change proposal for
	 * @param selection
	 *            proposal to select
	 * @param reverseDependenciesMap
	 *            the map of reverse dependencies obtained from
	 *            {@link IReferenceManager#getReverseDependenciesMap()}
	 */
	void selectProposal(IComparisonItem item, ComparedElement selection,
			MultiValueMap reverseDependenciesMap);

	/**
	 * Selects the specified proposal in the given item. This method will make
	 * sure that every child selection is consistent with its parent when the
	 * <code>recurseDependentElements</code> parameter is set to
	 * <code>true</code>. For example, selecting a SOURCE element of a
	 * {@link IComparisonItem} will select the SOURCE element of every child
	 * item.
	 * 
	 * @param item
	 *            item to change proposal for
	 * @param selection
	 *            proposal to select
	 * @param recurseDependentElements
	 *            a flag indicating whether we should recursively select
	 *            dependencies
	 */
	void selectProposal(IComparisonItem item, ComparedElement selection,
			boolean recurseDependentElements);

	/**
	 * Adjusts the parents selection of this item. This method will recursively
	 * browse the parents of this element to detect any need to update their
	 * status. For example, unselecting the last selected child item of an
	 * element will unselect the parent, etc.
	 * 
	 * @param item
	 *            item to compute parent selection for
	 */
	void adjustParents(IComparisonItem item);

	/**
	 * Opens the appropriate editor on the current script corresponding to the
	 * synchronization result. This method only opens editor on the current
	 * result. If you want to regenerate the script <b>and</b> open the editor
	 * afterwards, you should rather call
	 * {@link ISynchronizationService#buildScript(ISynchronizationResult)} on
	 * the UI service instead.
	 * 
	 * @param synchResult
	 *            the result to show
	 * @return the editor part which has been opened to show the current
	 *         synchronization script
	 */
	IEditorPart showScript(ISynchronizationResult synchResult);

	/**
	 * Submits the current synchronization script to the current synchronization
	 * target.
	 * 
	 * @param result
	 *            a {@link ISynchronizationResult} of the synchronization to
	 *            submit
	 */
	void submit(ISynchronizationResult result);

	/**
	 * Retrieves all tables which are defined as synchronizable for the data
	 * synch.
	 * 
	 * @return a collection of tables to synchronize, or an empty collection
	 */
	Collection<IVersionable<?>> getDataSynchronizationTables();

	/**
	 * Builds the script for the given synchronization result
	 * 
	 * @param synchResult
	 *            the {@link ISynchronizationResult} to generate SQL script from
	 */
	void buildScript(final ISynchronizationResult synchResult);
}
