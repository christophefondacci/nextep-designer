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
 * along with neXtep designer. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.headless.batch;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.headless.batch.helpers.Assert;
import com.nextep.designer.headless.batch.model.IBatchOption;
import com.nextep.designer.headless.batch.model.IBatchTask;
import com.nextep.designer.headless.batch.services.IBatchTaskService;
import com.nextep.designer.headless.exceptions.BatchException;
import com.nextep.designer.headless.helpers.HeadlessHelper;
import com.nextep.designer.headless.model.HeadlessConstants;
import com.nextep.designer.headless.model.impl.HeadlessProgressMonitor;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * This class is the entry point of the nextep command-line build program. It is in charge of neXtep
 * headless initialization, command line arguments registration and starts the task engine.
 * 
 * @author Christophe Fondacci
 */
public class BatchApplication implements IApplication {

	private static final Log STDOUT_LOGGER = LogFactory.getLog("OUT"); //$NON-NLS-1$
	private static final Log LOGGER = LogFactory.getLog(BatchApplication.class);
	private static final int PAD_SIZE = 22;
	private static final String HELP_OPTION = "help";
	private static final String VERBOSE_OPTION = "verbose";
	private Map<String, String> argsMap = Collections.emptyMap();

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) {
		STDOUT_LOGGER.info("\n"); //$NON-NLS-1$
		STDOUT_LOGGER.info(MessageFormat.format(BatchMessages.getString("application.headerLine1"), //$NON-NLS-1$
				new Date()));
		STDOUT_LOGGER.info(BatchMessages.getString("application.headerLine2")); //$NON-NLS-1$
		STDOUT_LOGGER.info("\n"); //$NON-NLS-1$
		final IWorkspaceService viewService = VCSPlugin.getViewService();
		final IRepositoryService repositoryService = CorePlugin.getRepositoryService();
		final IBatchTaskService actionsService = Designer.getService(BatchPlugin.getContext(),
				IBatchTaskService.class);

		// Processing command line arguments
		try {
			argsMap = HeadlessHelper.processArgs(context);
			if (argsMap.containsKey(HELP_OPTION)) {
				for (String option : argsMap.keySet()) {
					if (!HELP_OPTION.equals(option)) {
						showUsage(option);
						return IApplication.EXIT_OK;
					}
				}
				// We fall here when help requested with no argument
				showUsage(null);
				return IApplication.EXIT_OK;
			}
			final IConnection conn = HeadlessHelper.getConnection(HeadlessConstants.TARGET_CONTEXT,
					argsMap);
			final IConnection repoConnection = HeadlessHelper.getConnection(
					HeadlessConstants.REPOSITORY_CONTEXT, argsMap);
			Assert.notNull(repoConnection, "A repository connection must always be specified");
			repositoryService.setRepositoryConnection(repoConnection);

			// Initializing dummy view for headless mode to be properly configured
			final IWorkspace workspace = viewService.createWorkspace();
			workspace.setDBVendor(conn == null ? DBVendor.getDefaultVendor() : conn.getDBVendor());
			Designer.getInstance().setContext(workspace.getDBVendor().name());
			viewService.setCurrentWorkspace(workspace);
			final IRepositoryUser user = new RepositoryUser();
			user.setUID(new UID(1));
			viewService.setCurrentUser(user);

			final String tasksList = argsMap.get(HeadlessConstants.TARGET_TASKS);
			Assert.notNull(tasksList, "No task specified");
			final String[] tasks = tasksList.split(","); //$NON-NLS-1$

			// Processing tasks
			final HeadlessProgressMonitor monitor = new HeadlessProgressMonitor(false);
			for (String task : tasks) {
				monitor.setContext(task);
				final IBatchTask action = actionsService.getTask(task);
				if (action != null) {
					monitor.subTask(BatchMessages.getString("application.startTaskEngine")); //$NON-NLS-1$
					final IStatus status = action.execute(conn, argsMap, monitor);
					if (!status.isOK()) {
						fail(MessageFormat.format(
								BatchMessages.getString("application.taskFailed"), task, //$NON-NLS-1$
								status.getMessage()), status.getException());
						break;
					}
					monitor.subTask(BatchMessages.getString("application.finishedTaskEngine")); //$NON-NLS-1$
				} else {
					LOGGER.error(MessageFormat.format(
							BatchMessages.getString("application.invalidTask"), task)); //$NON-NLS-1$
					break;
				}
			}
		} catch (BatchException e) {
			fail(MessageFormat.format(
					BatchMessages.getString("application.taskExecutionException"), //$NON-NLS-1$
					e.getMessage()), e);
		} catch (RuntimeException e) {
			fail(MessageFormat.format(
					BatchMessages.getString("application.taskExecutionException"), //$NON-NLS-1$
					e.getMessage()), e);
		}
		STDOUT_LOGGER.info("\n"); //$NON-NLS-1$
		return IApplication.EXIT_OK;
	}

	private void fail(String message, Throwable cause) {
		if (argsMap.containsKey(VERBOSE_OPTION)) {
			LOGGER.error(message, cause);
		} else {
			LOGGER.error(message);
		}
		showUsage(null);
	}

	private void showUsage(String helpOptions) {
		final IBatchTaskService service = CorePlugin.getService(IBatchTaskService.class);
		IBatchTask helpedTask = null;
		Collection<IBatchOption> options;
		if (helpOptions != null) {
			try {
				helpedTask = service.getTask(helpOptions);
			} catch (BatchException e) {
				STDOUT_LOGGER.error("Invalid task id provided : " + helpOptions + "\n\n");
			}
		}
		// If no task specified in help, displaying generic all-purpose usage info
		if (helpedTask == null) {
			STDOUT_LOGGER.info("\nUsage : neXtep-cmd -tasks=taskId[,taskId]* [options]\n\n");
			STDOUT_LOGGER.info("Available tasks :\n");
			for (IBatchTask task : service.getAllTasks()) {
				STDOUT_LOGGER.info(MessageFormat.format("   {0}{1}\n", pad(task.getId(), PAD_SIZE),
						task.getDescription()));
			}
			options = service.getAvailableOptions();
		} else {
			// When task is specified we customize help by showing task description
			STDOUT_LOGGER.info("\nUsage : neXtep-cmd -tasks=" + helpOptions
					+ "[,taskId]* [options]\n");
			STDOUT_LOGGER.info("Description : " + helpedTask.getDescription() + "\n");
			// Building task specific options
			options = new ArrayList<IBatchOption>();
			for (String groupId : helpedTask.getUsedOptionGroups()) {
				options.addAll(service.getOptionsFor(groupId));
			}
		}

		// Displaying available options (either all or task-specific)
		STDOUT_LOGGER.info("\nAvailable options ");
		if (helpedTask != null) {
			STDOUT_LOGGER.info("for " + helpedTask.getId() + " task ");
		}
		STDOUT_LOGGER.info(":\n");

		// Listing options
		for (IBatchOption option : options) {
			STDOUT_LOGGER.info("   " + pad(option.getName(), PAD_SIZE) + option.getDescription()
					+ "\n");
		}

		// Help usage message
		STDOUT_LOGGER.info("\nDisplay task-specific help through : neXtep-cmd help [taskId]\n");
	}

	private String pad(String str, int pad) {
		StringBuffer buf = new StringBuffer(str);
		if (str.length() < pad) {
			for (int i = str.length(); i < pad; i++) {
				buf.append(' ');
			}
		}
		return buf.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// nothing to do
	}
}
