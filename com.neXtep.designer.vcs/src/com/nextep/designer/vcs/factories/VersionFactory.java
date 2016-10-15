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
package com.nextep.designer.vcs.factories;

import java.util.Date;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.impl.VersionBranch;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionBranch;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.impl.VersionInfo;
import com.nextep.designer.vcs.services.IVersioningService;

/**
 * This class is a factory of {@link IVersionInfo} instances
 * 
 * @author Christophe
 */
public class VersionFactory {

	/**
	 * Creates a new unversionned information. This method should be called when creating a new
	 * unversioned object.
	 * 
	 * @param ref version reference which will be used for this version tree
	 * @return a new unversionned versionInfo object
	 */
	public static IVersionInfo getUnversionedInfo(IReference ref, IActivity activity) {
		return buildVersionInfo(1, 0, 0, 0, VersionBranch.getDefaultBranch(),
				IVersionStatus.NOT_VERSIONED, ref, activity);
	}

	/**
	 * Builds a new {@link IVersionInfo} instance using the specified information. Note that
	 * 
	 * @param majorRelease major release of the version to create
	 * @param minorRelease minor release of the version to create
	 * @param iteration iteration number
	 * @param patch patch number
	 * @param branch {@link IVersionBranch} on which the version should be created
	 * @param status {@link IVersionStatus} of the version to create
	 * @param ref the version reference (will be saved if not yet saved)
	 * @param activity activity to set for the new version
	 */
	public static IVersionInfo buildVersionInfo(int majorRelease, int minorRelease, int iteration,
			int patch, IVersionBranch branch, IVersionStatus status, IReference ref,
			IActivity activity) {
		IVersionInfo v = new VersionInfo();
		v.setMajorRelease(majorRelease);
		v.setMinorRelease(minorRelease);
		v.setIteration(iteration);
		v.setPatch(patch);
		v.setRevision(0);
		v.setBranch(branch);
		v.setStatus(status);
		// Ensuring the reference is always set and saved in a version info object
		// For HashMap optimization on large views, we need safe hashCode on VersionInfo
		// based on the reference id.
		if (ref.getUID() == null || ref.getUID().rawId() == 0) {
			CorePlugin.getIdentifiableDao().save(ref, false,
					HibernateUtil.getInstance().getSandBoxSession(), true);
		}
		v.setReference(ref);
		if (activity == null) {
			activity = VCSPlugin.getService(IVersioningService.class).getCurrentActivity();
		}
		v.setActivity(activity);
		// v.setId(new UID(0));
		v.setCreationDate(new Date());
		v.setUpdateDate(new Date());
		return v;
	}

	/**
	 * This methods builds a brand new version info following the specified one. The new version
	 * will have the specified version as predecessor and will be created with a CHECK_OUT status.
	 * 
	 * @param previousVersion predecessor of the version to create
	 * @param activity activity to set for the new version
	 * @return a new {@link IVersionInfo} instance
	 */
	public static IVersionInfo buildNextVersionInfo(IVersionInfo previousVersion, IActivity activity) {
		VersionInfo v = (VersionInfo) copyVersion(previousVersion);
		v.setPreviousVersion(previousVersion);
		v.setStatus(IVersionStatus.CHECKED_OUT);
		if (activity == null) {
			activity = VCSPlugin.getService(IVersioningService.class).getCurrentActivity();
		}
		v.setActivity(activity);
		return v;
	}

	public static IVersionInfo copyVersion(IVersionInfo version) {
		VersionInfo v = new VersionInfo();
		v.setMajorRelease(version.getMajorRelease());
		v.setMinorRelease(version.getMinorRelease());
		v.setIteration(version.getIteration());
		v.setPatch(version.getPatch());
		v.setRevision(version.getRevision());
		v.setBranch(version.getBranch());
		v.setPreviousVersion(version.getPreviousVersion());
		v.setReference(version.getReference());
		v.setStatus(version.getStatus());
		v.setActivity(version.getActivity());
		v.setCreationDate(new Date());
		v.setUpdateDate(new Date());
		return v;
	}

}
