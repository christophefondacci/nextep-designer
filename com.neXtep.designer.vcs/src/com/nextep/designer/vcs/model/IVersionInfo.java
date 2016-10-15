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

import java.util.Date;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface provides information regarding the version such as the release number, the version
 * branch and the version status.<br>
 * A version pattern looks like the following :<br>
 * <b>MA.MI.IT.PA_RE</b><br>
 * Where MA = Major, MI = minor, IT = Iteration, PA = Patch, and RE = Revision.<br>
 * IVersionInfo should be provided by an IVersionable. <br>
 * <br>
 * A VersionInfo element should be manipulated via a VersionPolicy for safe behaviours.
 * 
 * @author Christophe Fondacci
 */
public interface IVersionInfo extends IdentifiedObject, IObservable, IReferenceable,
		Comparable<IVersionInfo> {

	int PRECISION = 100;
	long REVISION = 1;
	long PATCH = 100;
	long ITERATION = 10000;
	long MINOR = 1000000;
	long MAJOR = 100000000;

	/**
	 * @return the version label
	 */
	String getLabel();

	/**
	 * @return this version's major release number
	 */
	int getMajorRelease();

	/**
	 * Sets the major release number
	 * 
	 * @param majorRelease new major release number
	 */
	void setMajorRelease(int majorRelease);

	/**
	 * @return the minor release number
	 */
	int getMinorRelease();

	/**
	 * Sets the minor release number
	 * 
	 * @param minorRelease the new minor release number
	 */
	void setMinorRelease(int minorRelease);

	/**
	 * @return the iteration number
	 */
	int getIteration();

	/**
	 * Sets the iteration number
	 * 
	 * @param iteration new iteration number
	 */
	void setIteration(int iteration);

	/**
	 * @return the patch number
	 */
	int getPatch();

	/**
	 * Sets the patch number
	 * 
	 * @param patch new patch number
	 */
	void setPatch(int patch);

	/**
	 * @return the revision number or -1 if none
	 */
	int getRevision();

	/**
	 * Sets the revision number.
	 * 
	 * @param revision new version revision number
	 */
	void setRevision(int revision);

	/**
	 * @return the branch of the versioned element
	 */
	IVersionBranch getBranch();

	/**
	 * Defines a new version branch
	 * 
	 * @param newBranch new branch to define
	 */
	void setBranch(IVersionBranch newBranch);

	/**
	 * The status will indicate if this version is checked out, checked in or unversioned.
	 * 
	 * @return the version status
	 */
	IVersionStatus getStatus();

	/**
	 * Changes the status of this version. No consistency check should be made while changing status
	 * since this only contains information, no logic. The logic of state changes should be made by
	 * the IVersionable implementation.
	 * 
	 * @param status new version status
	 */
	void setStatus(IVersionStatus status);

	/**
	 * @return the previous release from which this version has been created
	 */
	IVersionInfo getPreviousVersion();

	/**
	 * @return the source version of a merge operation, or <code>null</code> if this version is not
	 *         a merge
	 */
	IVersionInfo getMergedFromVersion();

	void setMergedFromVersion(IVersionInfo mergedFromVersion);

	/**
	 * Retrieves the activity associated to this version
	 * 
	 * @return the current version activity
	 */
	IActivity getActivity();

	/**
	 * Defines the activity to associate to this version. Note that this method should only be
	 * called on checked out objects. No check should be made in implementors wether to set or not
	 * the value depending on the version status. The caller has the responsibility to check things
	 * before calling this method.
	 * 
	 * @param activity new activity to associate to this version
	 */
	void setActivity(IActivity activity);

	/**
	 * @return the repository user which has created this version
	 */
	IRepositoryUser getUser();

	/**
	 * @return the date when this version has been created
	 */
	Date getCreationDate();

	/**
	 * Defines the date of creation of this version
	 * 
	 * @param creationDate the date when this version has been created
	 */
	void setCreationDate(Date creationDate);

	/**
	 * @return the last date when this version has been updated
	 */
	Date getUpdateDate();

	/**
	 * Defines the last date when this version has been updated
	 * 
	 * @param updateDate last update date
	 */
	void setUpdateDate(Date updateDate);

	/**
	 * Defines the update revision number for this versionable. This number ensures proper
	 * synchronization between all opened neXtep clients of same components
	 * 
	 * @param revision the new revision number
	 */
	void setUpdateRevision(long revision);

	/**
	 * @return the update revision number of this versionable
	 */
	long getUpdateRevision();

	/**
	 * @return the "drop" status of this version. A version is "dropped" when we should have removed
	 *         it but kept it for recycling
	 */
	boolean isDropped();

	/**
	 * Defines the new drop status of this version information. Should be called when deleting a
	 * version (like undo-checkout) or when recycling a deleted version.
	 * 
	 * @param dropped new dropped state.
	 */
	void setDropped(boolean dropped);

	/**
	 * A convenience method allowing to set the whole release number in a single pass, deciding
	 * whether notifications should be fired or not. This is an internal method which should
	 * generally not be called directly. It has been implemented for performance improvements of the
	 * version info editors which would otherwise need to set release fragments one by one, thus
	 * firing 5 notifications.
	 * 
	 * @param releaseNumber release number to set
	 * @param notify whether or not notifications should be fired
	 */
	void setRelease(long releaseNumber, boolean notify);

	/**
	 * Retrieves the version tag corresponding to current settings
	 * 
	 * @return the version tag which is the numeric representation of the version number or
	 *         <code>null</code>
	 */
	Long getVersionTag();

	/**
	 * Defines the version tag, which is the numeric representation of the version number. This
	 * method should not be called externally.
	 * 
	 * @param tag the new version tag to define.
	 */
	void setVersionTag(Long tag);
}
