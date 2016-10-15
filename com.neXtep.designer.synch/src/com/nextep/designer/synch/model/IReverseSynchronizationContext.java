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
package com.nextep.designer.synch.model;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.base.AbstractImportPolicy;

/**
 * This interface defines the context of a reverse synchronization.
 * 
 * @author Christophe Fondacci
 */
public interface IReverseSynchronizationContext {

	/**
	 * Retrieves the mapping between source references and target references for the current
	 * context.
	 * 
	 * @return a map whose key is the source reference and whose value is the target reference
	 */
	Map<IReference, IReference> getSourceReferenceMapping();

	/**
	 * Retrieves the collection of all {@link IVersionable} items to import in this reverse
	 * synchronization context. Import means either add a new element or update existing element.
	 * 
	 * @return a collection of {@link IVersionable} elements to add or update in the current view
	 */
	Collection<IVersionable<?>> getVersionablesToImport();

	/**
	 * Retrieves all existing versionables in the repository which needs to be updated or removed.
	 * This method can help validation of the reverse synchronization as those items need :<br>
	 * - To have a parent module in a CheckedOut state<br>
	 * - Not to be in a checked out state themselves<br>
	 * 
	 * @return a collection of all updated or removed {@link IVersionable}
	 */
	Collection<IVersionable<?>> getVersionablesToUpdateOrRemove();

	/**
	 * Retrieves a collection of all {@link IVersionable} items to remove from the view in this
	 * reverse synchronization context.
	 * 
	 * @return a collection of {@link IVersionable} elements to remove from the current view
	 */
	Collection<IVersionable<?>> getVersionablesToRemove();

	/**
	 * Retrieves the reverse dependencies map to use when importing elements. This specific map has
	 * been built according to user selection of elements to import and could be seen as a "preview"
	 * of the reverse dependencies before import. This map could be used to check for potential
	 * external references which <i>would</i> be created if the elements were exported "as is".
	 * 
	 * @return a map whose key is a reference and value is a collection of objects depending on it
	 */
	MultiValueMap getReverseDependenciesMap();

	/**
	 * Retrieves a map of the {@link IComparisonItem} information hashed by the associated object to
	 * import.
	 * 
	 * @return a map of {@link IComparisonItem} information hashed by the item to import or remove
	 *         to/from the repository
	 */
	Map<IVersionable<?>, IComparisonItem> getVersionableItemsMap();

	/**
	 * Retrieves the list of {@link IComparisonItem} which should be considered during the reverse
	 * synchronization of the repository.
	 * 
	 * @return the collection of {@link IComparisonItem} which should been included in the reverse
	 *         synchronization process
	 */
	Collection<IComparisonItem> getItemsToSynchronize();

	/**
	 * Retrieves the list of referencers which will be deleted if this reverse synchronization is
	 * performed.
	 * 
	 * @return a collection of {@link IReferencer} which will be deleted if this reverse
	 *         synchronization is run.
	 */
	Collection<IReferencer> getDeletedReferencers();

	/**
	 * Returns whether the reverse synchronization should check for external references while
	 * importing elements into the workspace. External check is <b>required</b> when importing into
	 * an existing workspace but might be skipped when building a new workspace from reverse
	 * synchronization as it might be very time-consuming.<br>
	 * If set to <code>true</code> then an exception will be raised when the element to import would
	 * generate external references. Externals are checked against a consistent snapshot of the
	 * dependencies that will be present after all selected elements will be imported.
	 * 
	 * @return <code>true</code> when the reverse synchronization should check external references
	 *         before importing elements, else <code>false</code>
	 */
	boolean shouldCheckForExternals();

	/**
	 * Provides the import policy to use when importing elements into the workspace. A specific
	 * policy may be used to boost performances for specific simple use-case like the initial import
	 * of elements into an empty view.
	 * 
	 * @return the {@link AbstractImportPolicy} to use when elements are added to the workspace
	 */
	IImportPolicy getImportPolicy();

	/**
	 * Gives access to the synchronization result from which this context has been initialized.
	 * 
	 * @return the {@link ISynchronizationResult} of the synchronization from which this context was
	 *         initialized
	 */
	ISynchronizationResult getSynchronizationResult();

	/**
	 * Informs whether the current reverse synchronization is the initial import of a workspace.
	 * This information can be used by the reverse synchronization service to optimize performance
	 * by not performing some steps which do not make sense when dealing with an empty workspace.
	 * 
	 * @return <code>true</code> if the current reverse synchro is the first initial import of the
	 *         workspace, else <code>false</code>
	 */
	boolean isInitialImport();
}
