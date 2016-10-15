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

/**
 * This interface allows to define custom policies of versioning operations. This includes numeric
 * version incrementors, debranch strategy and checkin related operations.
 * 
 * @author Christophe Fondacci
 */
public interface VersionPolicy {

	/**
	 * Checks in the provided version specifying the activity.
	 * 
	 * @param v the versionable object to check in
	 * @param context the {@link IVersioningOperationContext}
	 */
	void checkIn(IVersionable<?> v, IVersioningOperationContext context);

	/**
	 * Checks out the provided version specifying the activity. A check out will always be a new
	 * object since the checked in object cannot be modified. <br>
	 * The caller must provide a source object (which is the object to checkout) which will remain
	 * unchange by the operations.
	 * 
	 * @param source the versionable object to check out which will not be modified
	 * @param context the {@link IVersioningOperationContext}
	 * @return a new checked out object newly created
	 */
	<V> IVersionable<V> checkOut(final IVersionable<V> source, IVersioningOperationContext context);

	/**
	 * Changes the version branch of the provided version.
	 * 
	 * @param v the versionable object to debranch
	 */
	void debranch(IVersionable<?> v, IVersionBranch branch);

}
