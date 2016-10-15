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
package com.nextep.designer.p2.services.impl;

import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.RepositoryTracker;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import com.nextep.designer.p2.P2Messages;
import com.nextep.designer.p2.exceptions.UnavailableLicenseServerException;
import com.nextep.designer.p2.services.ILicenseService;

public class NextepRepositoryTracker extends RepositoryTracker {

	private final static Log log = LogFactory.getLog(NextepRepositoryTracker.class);
	private ILicenseService licenseService;

	@Override
	public URI[] getKnownRepositories(ProvisioningSession session) {
		log.debug("getKnownRepositories"); //$NON-NLS-1$
		try {
			return new URI[] { licenseService.getUpdateRepository() };
		} catch (UnavailableLicenseServerException e) {
			log.error(P2Messages.getString("tracker.unavailableServerError") + e.getMessage(), e); //$NON-NLS-1$
			return new URI[] {};
		}
	}

	@Override
	public void addRepository(URI location, String nickname, ProvisioningSession session) {
		log.debug("addRepository"); //$NON-NLS-1$
		// Doing nothing, we lock repository additions
	}

	@Override
	public void removeRepositories(URI[] locations, ProvisioningSession session) {
		log.debug("removeRepositories"); //$NON-NLS-1$
		// Doing nothing, we lock repository removal
	}

	@Override
	public void refreshRepositories(URI[] locations, ProvisioningSession session,
			IProgressMonitor monitor) {
		log.debug("refreshRepositories"); //$NON-NLS-1$
		// What to do ?
		ProvisioningUI ui = ProvisioningUI.getDefaultUI();
		ui.signalRepositoryOperationStart();
		SubMonitor mon = SubMonitor.convert(monitor, locations.length * 100);
		for (int i = 0; i < locations.length; i++) {
			try {
				getArtifactRepositoryManager(ui).refreshRepository(locations[i], mon.newChild(50));
				getMetadataRepositoryManager(ui).refreshRepository(locations[i], mon.newChild(50));
			} catch (ProvisionException e) {
				// ignore problematic repositories when refreshing
			}
		}
		// We have no idea how many repos may have been added/removed as a result of
		// refreshing these, this one, so we do not use a specific repository event to represent it.
		ui.signalRepositoryOperationComplete(null, true);
	}

	IMetadataRepositoryManager getMetadataRepositoryManager(ProvisioningUI ui) {
		return ProvUI.getMetadataRepositoryManager(ui.getSession());
	}

	IArtifactRepositoryManager getArtifactRepositoryManager(ProvisioningUI ui) {
		return ProvUI.getArtifactRepositoryManager(ui.getSession());
	}

	public void setLicenseService(ILicenseService licenseService) {
		this.licenseService = licenseService;
	}
}
