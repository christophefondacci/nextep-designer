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
package com.nextep.designer.vcs.policies;

import java.text.MessageFormat;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.VersionPolicy;
import com.nextep.designer.vcs.model.VersionableFactory;
import com.nextep.designer.vcs.services.IVersioningService;

/**
 * A simple release strategy based on a decimal increment of each version number element. Activity
 * selection is totally handled by this version policy.<br>
 * This strategy will increment from within the PRECISION modulo and will then turn back to 0
 * incrementing the next version number.<br>
 * Version numbers are incremented from patch to iteration to minor release to major releases.<br>
 * E.g. : A call to increment() with a precision of 100 and a version number 1.1.4.9 will return
 * 1.1.5.0 and so on.
 * 
 * @author Christophe Fondacci
 */
public class DefaultVersionPolicy implements VersionPolicy {

	// private static final Log log = LogFactory.getLog(DefaultVersionPolicy.class);

	private static VersionPolicy instance = null;

	protected DefaultVersionPolicy() {
	}

	/**
	 * @return the default policy instance
	 */
	public static VersionPolicy getInstance() {
		if (instance == null) {
			instance = new DefaultVersionPolicy();
		}
		return instance;
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionPolicy#checkIn(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionInfo, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	public void checkIn(IVersionable<?> v, IVersioningOperationContext context) {
		IVersionInfo version = v.getVersion();
		if (v.getVersion().getStatus() == IVersionStatus.CHECKED_IN) {
			throw new ErrorException(MessageFormat.format(
					VCSMessages.getString("alreadyCheckedIn"), v.getName()));
		}
		// First of all we set the activity since the user can cancel
		// the operation
		version.setActivity(getActivity(context.getActivity(), version));
		// Updating arbitrary reference name
		version.getReference().setArbitraryName(v.getName());
		// Aligning version to owning container
		IVersionInfo targetVersion = context.getTargetVersionInfo(v);
		long versionNumber = VersionHelper.computeVersion(targetVersion);
		version.setRelease(versionNumber, true);
		version.setBranch(targetVersion.getBranch());
		// alignVersion(v, version);
		version.setStatus(IVersionStatus.CHECKED_IN);
	}

	/**
	 * Implements the default checkout behaviour.<br>
	 * <b>Important, this method assumes the following:</b><br>
	 * <code>
	 * IVersionable\<V\>.getType().getDefaultClass()
	 * </code> will return the Class V. This should always be true, otherwise the checkout would
	 * cause a <code>ClassCastException</code> at runtime since unchecked conversions are made.
	 * 
	 * @see com.nextep.designer.vcs.model.VersionPolicy#checkOut(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionInfo, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	public <V> IVersionable<V> checkOut(IVersionable<V> source, IVersioningOperationContext context) {
		// Retrieving source version
		IVersionInfo version = source.getVersion();
		// Checking if we can checkout
		if (version.getStatus() != IVersionStatus.CHECKED_IN) {
			throw new ErrorException(MessageFormat.format(VCSMessages.getString("mustBeCheckedIn"),
					source.getName()));
		}
		// Creating new child version with specified activity
		IVersionInfo newVersion = context.getTargetVersionInfo(source);
		if (newVersion == null) {
			newVersion = VersionFactory.buildNextVersionInfo(version,
					getActivity(context.getActivity(), null));
		}

		// Aligning version to owning container
		alignVersion(source, newVersion);
		// Copying object
		IVersionable<V> destination = createCheckedOutObject(source);
		// Now working only with destination version which
		// will be our checked out object.
		destination.setVersion(newVersion);

		return destination;
	}

	/**
	 * Creates the checked out object from the initial checked in object. This default
	 * implementation will create a new object and copy everything from source to the new object.
	 * 
	 * @param source source versionable of this check out
	 * @return the new checked out object
	 */
	protected <V> IVersionable<V> createCheckedOutObject(IVersionable<V> source) {
		return VersionableFactory.copy(source);
	}

	/**
	 * Aligns the version info to the owning container of the versionable
	 * 
	 * @param v versionable which is being modified
	 * @param version version to align
	 */
	protected void alignVersion(IVersionable<?> v, IVersionInfo version) {
		// Aligning version to owning container version
		if (v.getContainer() != null && !(v.getContainer() instanceof IWorkspace)) {
			IVersionInfo parentVersion = VersionHelper.getVersionable(v.getContainer())
					.getVersion();
			// The resetRevision flag indicates whether the specified version
			// was already aligned on the owning container version
			boolean wasAligned = true;
			if (version.getMajorRelease() != parentVersion.getMajorRelease()) {
				wasAligned = false;
				version.setMajorRelease(parentVersion.getMajorRelease());
			}
			if (version.getMinorRelease() != parentVersion.getMinorRelease()) {
				wasAligned = false;
				version.setMinorRelease(parentVersion.getMinorRelease());
			}
			if (version.getIteration() != parentVersion.getIteration()) {
				wasAligned = false;
				version.setIteration(parentVersion.getIteration());
			}
			if (version.getPatch() != parentVersion.getPatch()) {
				wasAligned = false;
				version.setPatch(parentVersion.getPatch());
			}
			if (version.getBranch() != parentVersion.getBranch()) {
				wasAligned = false;
				version.setBranch(parentVersion.getBranch());
			}
			// If the release was not aligned, we reset the revision count to 0
			if (!wasAligned) {
				version.setRevision(0);
			}
		}
		// Avoiding version conflicts by incrementing revisions
		while (!VersionHelper.isVersionAvailable(version)) {
			// Increment the version number on checkin
			VersionHelper.incrementRelease(version, VersionHelper.REVISION);
		}
	}

	/**
	 * @see com.nextep.designer.vcs.model.VersionPolicy#debranch(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IVersionInfo,
	 *      com.nextep.designer.vcs.model.IVersionBranch)
	 */
	@Override
	public void debranch(IVersionable<?> v, IVersionBranch branch) {
		IVersionInfo version = v.getVersion();
		// TODO: Version may not be available on new branch, must check availability
		version.setBranch(branch);
		// Notifying IVersionable listeners of debranch
		v.notifyListeners(ChangeEvent.DEBRANCH, branch);
	}

	/**
	 * Gets a non-null activity given the initial activity set for the versioning operation. Should
	 * this initial activity be null, it will query the user for activity selection. Otherwise, the
	 * initial activity will be returned
	 * 
	 * @param initialActivity initial activity defined by the user for the versioning operation or
	 *        <code>null</code>
	 * @param version version on which we perform a versioning operation
	 * @return a non-null activity
	 */
	private IActivity getActivity(IActivity initialActivity, IVersionInfo version) {
		if (initialActivity == null) {
			return VCSPlugin.getService(IVersioningService.class).getCurrentActivity();
		} else {
			return initialActivity;
		}
	}

}
