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
package com.nextep.designer.synch.services;

import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.synch.model.IReverseSynchronizationContext;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * Provides services to reverse engineer a database structure into the repository.
 * 
 * @author Christophe Fondacci
 */
public interface IReverseSynchronizationService {

	/**
	 * Imports the specified synchronization result into the repository. Any checked source element
	 * will be imported in the repository.<br>
	 * A convenience method to perform the import directly from a {@link ISynchronizationResult}.
	 * This method should create the context and then delegate processing to
	 * {@link IReverseSynchronizationService#reverseSynchronize(IReverseSynchronizationContext, IProgressMonitor)}
	 * 
	 * @param result synchronization result
	 */
	void reverseSynchronize(ISynchronizationResult result, IProgressMonitor monitor);

	/**
	 * Executes the reverse synchronization against the specified pre-built context.
	 * 
	 * @param context a {@link IReverseSynchronizationContext} containing all needed information to
	 *        know what to do during this reverse synchronization
	 * @param monitor a non null {@link IProgressMonitor} to report progress
	 */
	void reverseSynchronize(IReverseSynchronizationContext context, IProgressMonitor monitor);

	/**
	 * Creates a context for reverse synchronization using the current state of the specified
	 * {@link ISynchronizationResult}. A context is required for any reverse synchronization action.
	 * Any method which does not need a {@link IReverseSynchronizationContext} will call this method
	 * internally to generate one.
	 * 
	 * @param result a {@link ISynchronizationResult} to create the context from
	 * @return a fresh {@link IReverseSynchronizationContext} based on the state of the
	 *         {@link ISynchronizationResult} at the time this method is called. Any further user
	 *         change on the current {@link ISynchronizationResult} will not be reflected in the
	 *         provided context
	 */
	IReverseSynchronizationContext createContext(ISynchronizationResult result);

	/**
	 * Adds the specified versionable to the specified view / container.
	 * 
	 * @param view the {@link IWorkspace} into which the {@link IVersionable} will be added
	 * @param container the container into which the new element should be placed, or
	 *        <code>null</code> for view root
	 * @param toImport the {@link IVersionable} element to import into the view
	 * @param context the {@link IReverseSynchronizationContext} of this action
	 */
	void addToView(IWorkspace view, IVersionContainer container, IVersionable<?> toImport,
			IReverseSynchronizationContext context);

	/**
	 * Updates the specified element in its current view location.
	 * 
	 * @param a {@link IWorkspace} into which element should be updated
	 * @param item item to update specified by the {@link IComparisonItem} which holds relationship
	 *        between the view element and its corresponding database "version" to import.
	 * @param context the {@link IReverseSynchronizationContext} of this action
	 */
	void updateIntoView(IWorkspace view, IComparisonItem item,
			IReverseSynchronizationContext context);

	/**
	 * Removes the specified versionable element from the view.
	 * 
	 * @param toRemove a {@link IVersionable} element to remove from its container in the current
	 *        view.
	 * @param context the {@link IReverseSynchronizationContext} of this action
	 */
	void removeFromView(IVersionable<?> toRemove, IReverseSynchronizationContext context);

	/**
	 * Lists any problem which would prevent the reverse synchro from completing.
	 * 
	 * @param context current reverse synchronization context
	 * @return a list of {@link IMarker}
	 */
	List<IMarker> getProblems(IReverseSynchronizationContext context);

	void setNewElementsTargetModule(IVersionContainer module);

	IVersionContainer getNewElementsTargetModule();
}
