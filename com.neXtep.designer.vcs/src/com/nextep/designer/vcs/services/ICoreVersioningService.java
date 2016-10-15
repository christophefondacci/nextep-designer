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

import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.exception.LockedElementException;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * The core versioning service provides common facilities to version control elements. Any
 * versioning service needs to implement at least these core methods.
 * 
 * @author Christophe Fondacci
 */
public interface ICoreVersioningService {

	/**
	 * Checks out the specified committed versions.
	 * 
	 * @param activity activity to use for check-out
	 * @param monitor a {@link IProgressMonitor} to report progress
	 * @param checkedInVersions version(s) to check out
	 * @return the list of checked out elements, in the same order as input elements
	 */
	List<IVersionable<?>> checkOut(IProgressMonitor monitor, IVersionable<?>... checkedInVersions);

	/**
	 * Commits the specified checked out versions.
	 * 
	 * @param activity the {@link IActivity} to use for committing or null to use current
	 * @param monitor a {@link IProgressMonitor} to report progress
	 * @param checkedOutVersions version(s) to commit
	 */
	void commit(IProgressMonitor monitor, IVersionable<?>... checkedOutVersions);

	/**
	 * Undoes the checkout of the specified checked-out versions.
	 * 
	 * @param monitor a {@link IProgressMonitor} to report progress
	 * @param checkedOutVersions version(s) to undo the check-out of
	 * @return the list of elements after rollbacking the checkout
	 */
	List<IVersionable<?>> undoCheckOut(IProgressMonitor monitor,
			IVersionable<?>... checkedOutVersions);

	/**
	 * Validates all specified elements. The specified event argument indicates which versioning
	 * task needs validation along with the moment (before or after). Depending on the
	 * implementation, versioning methods (checkout, commit, undo) may or may not perform
	 * validation. <br>
	 * For example, the UI implementation of this service will orchestrate calls to validation
	 * methods and calls to versioning methods while the core service will perform atomic tasks.
	 * This allows callers to choose whether they want fine-grained control over operations or only
	 * standard validation strategy.
	 * 
	 * @param event the {@link VersioningEvent} indicating what task is being performed
	 * @param elementsToValidate a list of elements to consider for validation
	 * @return a {@link IStatus} indicating the success or failure of the validation
	 */
	// IStatus validate(VersioningEvent event, IVersionable<?>... elementsToValidate);

	/**
	 * Ensures that the specified versionable is modifiable. This method may perform the required
	 * operations to put the specified element in a modifiable state. The returned object will
	 * always be modifiable. If no modifiable element could be obtained, this method will throw a
	 * {@link LockedElementException}.<br>
	 * <b>Important:</b> the returned object may differ from the input one. This method only
	 * guarantees that the <b><u>returned</u></b> object is modifiable, not the argument.<br>
	 * 
	 * @param v object to ensure modifiable state for
	 * @return the modifiable object
	 */
	<T> T ensureModifiable(T v);

}
