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
package com.nextep.datadesigner.gui.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.CommandFinishException;
import com.nextep.datadesigner.exception.DesignerException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.impl.SchedulingRuleVolatile;
import com.nextep.datadesigner.model.ICommand;
import com.nextep.datadesigner.model.ICommandWithProgress;
import com.nextep.designer.ui.CoreUiPlugin;

/**
 * @author Christophe Fondacci
 */
public class CommandProgress implements IRunnableWithProgress {

	private ICommand[] commands;
	private boolean fork;
	private String mainName = null;
	private Shell shell;
	private List<Object> results = new ArrayList<Object>();
	private static boolean waiting = false;

	public CommandProgress(boolean fork, String name, Shell shell, ICommand... commands) {
		this.commands = commands;
		mainName = name;
		this.shell = shell;
		this.fork = fork;
	}

	public static List<?> runWithProgress(ICommand... commands) {
		return runWithProgress(true, commands);
	}

	public static List<?> runWithProgress(boolean fork, ICommand... commands) {
		return runWithProgress(fork, null, false, commands);
	}

	public static List<?> runWithProgress(boolean fork, String name, ICommand... commands) {
		return runWithProgress(fork, name, false, commands);
	}

	public static List<?> runWithProgress(final boolean fork, final String name,
			final boolean cancellable, final ICommand... commands) {
		if (CoreUiPlugin.getDefault().getWorkbench() != null
				&& CoreUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow() != null) {
			return runWithProgress(fork, name, CoreUiPlugin.getDefault().getWorkbench()
					.getActiveWorkbenchWindow().getShell(), cancellable, commands);
		} else {
			final List<Object> results = new ArrayList<Object>();
			Display.getDefault().syncExec(new Runnable() {

				public void run() {
					final List<?> tempResults = runWithProgress(fork, name, Display.getDefault()
							.getActiveShell(), cancellable, commands);
					results.addAll(tempResults);
				}
			});
			return results;
		}
	}

	public static List<?> runWithProgress(boolean fork, String name, Shell shell,
			ICommand... commands) {
		return runWithProgress(fork, name, shell, false, commands);
	}

	public static List<?> runWithProgress(boolean fork, String name, Shell shell,
			boolean cancellable, ICommand... commands) {
		waitVolatileJobs();
		ProgressMonitorDialog pd = new ProgressMonitorDialog(shell);
		try {
			CommandProgress cmdProgress = new CommandProgress(fork, name, shell, commands);
			pd.run(fork, cancellable, cmdProgress);
			return cmdProgress.getResults();
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof DesignerException) {
				throw (DesignerException) e.getCause();
			} else if (e.getCause() instanceof SWTException
					&& e.getCause().getCause() instanceof DesignerException) {
				// In case of Display.syncExec or asyncExec call, we will fall here
				// with our exception wrapped into a SWTException
				throw (DesignerException) e.getCause().getCause();
			} else {
				throw new ErrorException(e.getCause());
			}
		} catch (InterruptedException e) {
			throw new ErrorException(e);
		}
	}

	/**
	 * This method waits for all jobs using the volatile reference pool to terminate before
	 * returning.
	 */
	private static synchronized void waitVolatileJobs() {
		waiting = true;
		Job j = new Job("Waiting for other jobs to terminate") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask("Waiting for other jobs to terminate...",
						IProgressMonitor.UNKNOWN);
				waiting = false;
				return Status.OK_STATUS;
			}
		};
		j.setRule(SchedulingRuleVolatile.getInstance());
		j.setUser(true);
		j.schedule();
		// boolean showJobs = false;
		while (waiting && !Designer.getTerminationSignal()) {
			while (PlatformUI.getWorkbench().getDisplay().readAndDispatch()) {
			}
			try {
				Thread.sleep(300);
				// if(!showJobs) {
				// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().
				// }
			} catch (InterruptedException e) {
				return;
			}
		}
		if (Designer.getTerminationSignal()) {
			throw new CancelException("Termination signal received");
		}
	}

	/**
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		Designer.setProgressMonitor(monitor);
		monitor.beginTask("Initializing...", commands.length == 1 ? IProgressMonitor.UNKNOWN
				: commands.length);
		if (mainName != null) {
			monitor.setTaskName(mainName);
		}
		refresh();
		try {
			Object lastResult = null;
			for (ICommand c : commands) {
				// Handling progressable commands
				if (c instanceof ICommandWithProgress) {
					((ICommandWithProgress) c).setProgressMonitor(monitor);
				}
				refresh();
				if (monitor.isCanceled())
					return;
				if (mainName == null) {
					monitor.setTaskName(c.getName());
				} else {
					monitor.subTask(c.getName());
				}
				refresh();
				lastResult = c.execute(lastResult);
				results.add(lastResult);
				monitor.worked(1);
				if (monitor.isCanceled())
					return;
				refresh();
			}
		} catch (CommandFinishException e) {
			if (mainName == null) {
				monitor.setTaskName(e.getMessage());
			} else {
				monitor.subTask(e.getMessage());
			}
		}
		monitor.done();
		Designer.setProgressMonitor(null);
	}

	public void refresh() {
		if (!fork) {
			while (shell.getDisplay().readAndDispatch())
				;
		}
	}

	public List<Object> getResults() {
		return results;
	}

}
