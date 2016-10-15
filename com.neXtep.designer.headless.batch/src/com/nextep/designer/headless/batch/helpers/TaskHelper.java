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
package com.nextep.designer.headless.batch.helpers;

import java.text.MessageFormat;
import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.headless.batch.BatchMessages;
import com.nextep.designer.headless.batch.model.BatchConstants;
import com.nextep.designer.headless.batch.model.impl.GenerationHeadlessConsole;
import com.nextep.designer.headless.exceptions.BatchException;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * Provides helper methods commonly used by headless build tasks.
 * 
 * @author Christophe Fondacci
 */
public final class TaskHelper {

	private TaskHelper() {
	}

	/**
	 * Retrieves a generation submitter that can be used headlessly.
	 * 
	 * @param vendor the {@link DBVendor}
	 * @return a {@link IGenerationSubmitter} ready to use
	 */
	public static IGenerationSubmitter getSQLSubmitter(DBVendor vendor, IProgressMonitor monitor) {
		IGenerationSubmitter submitter = SQLGenPlugin.getService(IGenerationService.class)
				.getGenerationSubmitter(vendor);
		submitter.setConsole(new GenerationHeadlessConsole(monitor));
		return submitter;
	}

	public static void loadWorkspace(Map<String, String> propertiesMap, IProgressMonitor monitor)
			throws BatchException {
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
				throw new BatchException(MessageFormat.format(
						BatchMessages.getString("task.synch.workspaceNotFound"), workspaceName)); //$NON-NLS-1$
			}
		}
		monitor.setTaskName("Loading workspace...");
		// Switching current workspace
		workspaceService.changeWorkspace(id, monitor);
	}

	public static void submit(IConnection targetConnection, IProgressMonitor monitor,
			ISQLScript... scripts) {
		// Informing user about what we are about to submit
		monitor.subTask(MessageFormat.format(
				BatchMessages.getString("task.synch.beforeSubmit"), scripts.length, targetConnection)); //$NON-NLS-1$
		final IGenerationSubmitter submitter = TaskHelper.getSQLSubmitter(
				targetConnection.getDBVendor(), monitor);
		// Submitting every script
		for (ISQLScript script : scripts) {
			monitor.subTask(MessageFormat.format(
					BatchMessages.getString("task.synch.submittingScript"), script.getName(), targetConnection)); //$NON-NLS-1$
			// Submitting the script to the target connection
			submitter.submit(monitor, script, targetConnection);
		}
		// Everything is OK
		monitor.subTask(BatchMessages.getString("task.synch.submitOk")); //$NON-NLS-1$
	}
}
