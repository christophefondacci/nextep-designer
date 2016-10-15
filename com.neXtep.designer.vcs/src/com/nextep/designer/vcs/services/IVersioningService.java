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
package com.nextep.designer.vcs.services;

import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IProblemSolver;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningListener;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.VersioningOperation;

/**
 * This versioning service provides additional internal methods which are not exposed in the UI
 * versioning service since they would not be of any interest. These methods allows deep control
 * over versioning operations by accepting a {@link IVersioningOperationContext} which contains
 * settings about the operation to perform.
 * 
 * @author Christophe Fondacci
 */
public interface IVersioningService extends ICoreVersioningService {

	/**
	 * Check-out operation defined by the {@link IVersioningOperationContext}
	 * 
	 * @param monitor monitor to use to report progress
	 * @param context a {@link IVersioningOperationContext} providing information about what needs
	 *        to be done.
	 * @return a list of checked out elements
	 */
	List<IVersionable<?>> checkOut(IProgressMonitor monitor, IVersioningOperationContext context);

	/**
	 * Commit operation defined by the {@link IVersioningOperationContext}.
	 * 
	 * @param monitor a {@link IProgressMonitor} to report progress to
	 * @param context the {@link IVersioningOperationContext} containing information of what needs
	 *        to be done
	 */
	void commit(IProgressMonitor monitor, IVersioningOperationContext context);

	/**
	 * Undoe-checkout operation defined by the {@link IVersioningOperationContext}
	 * 
	 * @param monitor a {@link IProgressMonitor} to report progress
	 * @param context the {@link IVersioningOperationContext} containing information of what needs
	 *        to be done
	 * @return the list of elements after rollbacking the checkout
	 */
	List<IVersionable<?>> undoCheckOut(IProgressMonitor monitor, IVersioningOperationContext context);

	/**
	 * Lists checkout versionables of the given container.<br>
	 * This method will return checkouts of any sub-container if <code>recurseCotnainers</code> is
	 * set to <code>true</code>
	 * 
	 * @param c container to list checkouts
	 * @param recurseContainers a flag indicating if this method should return checkouts of
	 *        sub-containers.
	 * @return the list of checkouts of the given container
	 */
	List<IVersionable<?>> listCheckouts(IVersionContainer c, boolean recurseContainers);

	/**
	 * Unlocks all specified {@link IVersionable} through the {@link IProblemSolver}. If any element
	 * could not be unlocked, an exception is raised.
	 * 
	 * @param parentsOnly will only unlock parent modules of the specified element
	 * @param lockedVersionables list of {@link IVersionable} to unlock
	 */
	void unlock(boolean parentsOnly, IVersionable<?>... lockedVersionables);

	/**
	 * Builds a list of markers that are able to fully unlock the specified collection of elements.
	 * The returned markers are designed to be processed by the {@link IProblemSolver}. There are
	 * two modes for unlocking :<br>
	 * - <code>parentsOnly</code> : this indicates that the caller want to move or remove the
	 * elements but does not actually need to modify the elements themselves.<br>
	 * - <code>all</code> : everything will be unlocked, including the elements
	 * 
	 * @param parentsOnly a flag indicating whether this method should provide markers to unlock the
	 *        parents elements of the provided objects or the objects themselves as well
	 * @param potentiallyLockedObjects collection of objects which need to be modifiable (maybe they
	 *        already are, in which case this method would return an empty list).
	 * @return a collection of {@link IMarker} able to unlock any locked element
	 */
	List<IMarker> getUnlockMarkers(boolean parentsOnly, Collection<?> potentiallyLockedObjects);

	/**
	 * Creates a version aware "proxy" which will always point to the current workspace element,
	 * handling proper routing after a checkout, undo checkout or commit user actions.
	 * 
	 * @param <T>
	 * @param object workspace element to create a proxy for
	 * @return a {@link IModelOriented} object whose getModel() method will return the current
	 *         workspace object corresponding to the original object
	 */
	<T> IModelOriented<T> createVersionAwareObject(T object);

	/**
	 * Validates the specified context for a versioning event.
	 * 
	 * @param event versioning event (checkout, commit, undo)
	 * @param context the current operation context to validate
	 * @return a status informing about the validation result
	 */
	IStatus validate(IVersioningOperationContext context);

	/**
	 * Initializes and configures a versioning context for the specified operation and versionables.
	 * 
	 * @param operation versioning operation to initialize a context for
	 * @param versionables collection of versionables to consider
	 * @return a corresponding new {@link IVersioningOperationContext}
	 */
	IVersioningOperationContext createVersioningContext(VersioningOperation operation,
			Collection<IVersionable<?>> versionables);

	/**
	 * Creates a new activity with the specified description
	 * 
	 * @param activityText description of the activity
	 * @return the new {@link IActivity}
	 */
	IActivity createActivity(String activityText);

	/**
	 * Retrieves the current activity. An activity will always be provided by this method, but might
	 * provide some default or empty description if no user activity has yet been defined.
	 * 
	 * @return the current {@link IActivity}
	 */
	IActivity getCurrentActivity();

	/**
	 * Defines the current activity.
	 * 
	 * @param currentActivity current activity
	 */
	void setCurrentActivity(IActivity currentActivity);

	/**
	 * Retrieves the list of recently used activities. The list may be empty if the user has not
	 * started to define activities.
	 * 
	 * @return a list of recently used {@link IActivity}
	 */
	List<IActivity> getRecentActivities();

	/**
	 * Lists all known versions for the specified {@link IReference}
	 * 
	 * @param ref reference to look versions for
	 * @return a list of all {@link IVersionInfo} defined for this reference
	 */
	List<IVersionInfo> listVersions(IReference ref);

	/**
	 * Adds a {@link IVersioningListener} to this service which will get notified about version
	 * control operation.
	 * 
	 * @param listener listener to register
	 */
	void addVersioningListener(IVersioningListener listener);

	/**
	 * Removes a {@link IVersioningListener} to this service which will no longer be notified about
	 * version control operation.
	 * 
	 * @param listener listener to unregister
	 */
	void removeVersioningListener(IVersioningListener listener);
}
