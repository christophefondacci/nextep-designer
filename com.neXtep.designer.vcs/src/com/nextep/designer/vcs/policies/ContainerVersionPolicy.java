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

import java.util.List;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.vcs.services.VersionActions;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.factories.VersionFactory;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.VersionPolicy;
import com.nextep.designer.vcs.model.VersionableFactory;

/**
 * @author Christophe Fondacci
 */
public class ContainerVersionPolicy extends DefaultVersionPolicy {

	private static ContainerVersionPolicy instance = null;

	protected ContainerVersionPolicy() {
	}

	public static VersionPolicy getInstance() {
		if (instance == null) {
			instance = new ContainerVersionPolicy();
		}
		return instance;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.DefaultVersionPolicy#checkIn(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	public void checkIn(IVersionable<?> v, IVersioningOperationContext context) {
		IVersionContainer c = (IVersionContainer) v;
		// Prompts user for version selection / activity setting, only if parent is the view
		if (v.getContainer() instanceof IWorkspace) {
			final IVersionInfo version = v.getVersion();
			// Checking in sub elements
			List<IVersionable<?>> checkedOutItems = VersionActions.listCheckouts(c, false);
			if (checkedOutItems.size() > 0) {
				throw new ErrorException("Checkin failed: all items contained in <" + c.getName()
						+ "> must be checked in. Check in all sub items and try again.");
			}
			version.setStatus(IVersionStatus.CHECKED_IN);
			// Aligning version to owning container
			final IVersionInfo targetVersion = context.getTargetVersionInfo(v);
			final long versionNumber = VersionHelper.computeVersion(targetVersion);
			version.setRelease(versionNumber, true);
			version.setBranch(targetVersion.getBranch());
			version.setActivity(context.getActivity());
			// Updating arbitrary reference name
			v.getReference().setArbitraryName(c.getName());
		} else {
			super.checkIn(v, context);
		}

	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.DefaultVersionPolicy#checkOut(com.nextep.designer.vcs.model.IVersionable,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	public <V> IVersionable<V> checkOut(IVersionable<V> source, IVersioningOperationContext context) {
		// If we don't have root containers, consider them like standard checkout
		if (!(source.getContainer() instanceof IWorkspace)) {
			return super.checkOut(source, context);
		} else {
			// Prompts user for version selection / activity setting
			IVersionInfo version = context.getTargetVersionInfo(source);
			if (version == null) {
				version = VersionFactory.buildNextVersionInfo(source.getVersion(),
						context.getActivity());
				VersionHelper.incrementRelease(version, VersionHelper.PATCH);
			}
			try {
				// We deactivate listeners here because incrementRelease should
				// never fire a database save(), otherwise it will create a
				// neverending loop.
				Observable.deactivateListeners();
				while (!VersionHelper.isVersionAvailable(version)) {
					VersionHelper.incrementRelease(version, VersionHelper.PATCH);
				}
			} finally {
				Observable.activateListeners();
			}
			// Copying object
			IVersionable<V> destination = VersionableFactory.copy(source);
			// Setting the version of our new copy
			destination.setVersion(version);

			return destination;
		}
	}

}
