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

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.headless.batch.BatchPlugin;
import com.nextep.designer.headless.batch.helpers.TaskHelper;
import com.nextep.designer.headless.batch.model.BatchConstants;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.batch.model.base.AbstractBatchTask;
import com.nextep.designer.synch.SynchPlugin;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.services.ISynchronizationService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * @author Christophe Fondacci
 */
public class DataSynchronizationTask extends AbstractBatchTask implements IBatchTask,
		ISynchronizationListener {

	private ISynchronizationResult synchResult;

	@Override
	public IStatus execute(IConnection targetConnection, Map<String, String> propertiesMap,
			IProgressMonitor monitor) {
		// Service initialization
		final ISynchronizationService synchService = SynchPlugin
				.getService(ISynchronizationService.class);
		final IWorkspaceService workspaceService = VCSPlugin.getViewService();

		// Switching workspace context
		final String workspaceId = propertiesMap.get(BatchConstants.WORKSPACE_ID_ARG);
		UID id = null;
		if (workspaceId != null) {
			id = new UID(Long.valueOf(workspaceId));
		} else {
			final String workspaceName = propertiesMap.get(BatchConstants.WORKSPACE_NAME_ARG);
			monitor.subTask("Looking for workspace named '" + workspaceName + "'...");
			id = workspaceService.findWorkspaceId(workspaceName);
			if (id == null) {
				return new Status(IStatus.ERROR, BatchPlugin.PLUGIN_ID, MessageFormat.format(
						BatchMessages.getString("task.synch.workspaceNotFound"), workspaceName)); //$NON-NLS-1$
			}
		}
		monitor.setTaskName("Loading workspace...");
		// Switching current workspace
		workspaceService.changeWorkspace(id, monitor);
		final IWorkspace workspace = workspaceService.getCurrentWorkspace();
		monitor.subTask("Extracting tables to synchronize");
		List<IVersionable<?>> tables = VersionHelper.getAllVersionables(workspace,
				IElementType.getInstance(IBasicTable.TYPE_ID));
		// Running synchronization
		synchService.addSynchronizationListener(this);
		monitor.subTask("Starting table data synchronization");
		synchService.synchronizeData(targetConnection, monitor,
				tables.toArray(new IVersionable[tables.size()]));
		if (synchResult != null) {
			monitor.subTask("Building synchronization script...");
			synchService.buildScript(synchResult, monitor);
			monitor.subTask(MessageFormat.format(
					BatchMessages.getString("task.synch.submittingScript"), targetConnection)); //$NON-NLS-1$
			final IGenerationSubmitter submitter = TaskHelper.getSQLSubmitter(
					targetConnection.getDBVendor(), monitor);
			submitter.submit(monitor, synchResult.getGeneratedScript(), targetConnection);
			monitor.subTask(BatchMessages.getString("task.synch.submitOk")); //$NON-NLS-1$
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
