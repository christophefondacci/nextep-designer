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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.headless.batch.model.impl;

import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.headless.batch.helpers.TaskHelper;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.batch.model.base.AbstractBatchTask;
import com.nextep.designer.headless.exceptions.BatchException;
import com.nextep.designer.synch.SynchPlugin;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.services.ISynchronizationService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 */
public class SynchronizationTask extends AbstractBatchTask implements IBatchTask,
		ISynchronizationListener {

	private ISynchronizationResult synchResult;

	@Override
	public IStatus execute(IConnection targetConnection, Map<String, String> propertiesMap,
			IProgressMonitor monitor) throws BatchException {
		// Service initialization
		final ISynchronizationService synchService = SynchPlugin
				.getService(ISynchronizationService.class);
		final IWorkspaceService workspaceService = VCSPlugin.getViewService();

		// Loading the workspace specified in the properties
		TaskHelper.loadWorkspace(propertiesMap, monitor);

		// Running synchronization
		synchService.addSynchronizationListener(this);
		synchService.synchronize(workspaceService.getCurrentWorkspace(), targetConnection, monitor);
		if (synchResult != null) {
			TaskHelper.submit(targetConnection, monitor, synchResult.getGeneratedScript());
		}
		return Status.OK_STATUS;

	}

	@Override
	public void newSynchronization(ISynchronizationResult synchronizationResult) {
		this.synchResult = synchronizationResult;
	}

	@Override
	public void scopeChanged(ComparisonScope scope) {

	}

}
