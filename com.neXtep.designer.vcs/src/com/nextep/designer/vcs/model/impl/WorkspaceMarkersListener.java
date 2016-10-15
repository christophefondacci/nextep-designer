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
package com.nextep.designer.vcs.model.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.base.AbstractWorkspaceListener;

/**
 * This listener will trigger the recomputation of all markers for the new view when the view
 * changes and stops any ongoing computation before workspace closure.
 * 
 * @author Christophe Fondacci
 */
public class WorkspaceMarkersListener extends AbstractWorkspaceListener {

	@Override
	public void workspaceChanged(IWorkspace oldView, final IWorkspace newView,
			IProgressMonitor monitor) {
		final IMarkerService markerService = CorePlugin.getService(IMarkerService.class);
		// Updating marker service
		markerService.setInputContainer(newView);
		markerService.resumeMarkerComputation();
	}

	@Override
	public void workspaceClosed(IWorkspace view) {
		final IMarkerService markerService = CorePlugin.getService(IMarkerService.class);
		markerService.pauseMarkerComputation();
	}
}
