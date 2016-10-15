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
package com.nextep.datadesigner.model;

/**
 * This interface describes objects which are shared through the repository database. These objects
 * can be modified from different client machines / applications and neXtep designer should never
 * assume the current loaded version is up to date with the repository.<br>
 * When objects implement this interface, special actions will occur on them :<br>
 * - They may be synchronized during a save if they are out of synch<br>
 * - They may be synchronized during a checkin / checkout / undo checkout actions<br>
 * - Out of date checks will be made frequently<br>
 * - Any action will fail with a warning message when the object needs to be synchronized<br>
 * 
 * @author Christophe
 */
public interface ISharedRepositoryObject {

	/**
	 * Indicates if this object is out of date with the repository and therefore needs to be
	 * synchronized.
	 * 
	 * @return the out of date flag
	 */
	boolean isOutOfDate();

	/**
	 * Synchronizes the current object with repository information.
	 */
	void resyncWithRepository();

	/**
	 * Increments the revision number in repository database.
	 */
	void incrementRevision();

	/**
	 * @return the revision number of the loaded object information
	 */
	long getRevision();

	/**
	 * Defines the revision number of current information.<br>
	 * <b>Internal: call {@link ISharedRepositoryObject#incrementRevision()} instead</b>
	 * 
	 * @param revision new revision number
	 */
	void setRevision(long revision);

}
