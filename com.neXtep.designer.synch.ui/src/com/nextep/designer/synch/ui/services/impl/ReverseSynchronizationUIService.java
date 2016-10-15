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
package com.nextep.designer.synch.ui.services.impl;

import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.service.GUIService;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.synch.model.IReverseSynchronizationContext;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.services.IReverseSynchronizationService;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.synch.ui.SynchUIPlugin;
import com.nextep.designer.synch.ui.services.IReverseSynchronizationUIService;
import com.nextep.designer.synch.ui.services.ISynchronizationUIService;
import com.nextep.designer.ui.helpers.BlockingJob;
import com.nextep.designer.ui.wizards.ResolveProblemsWizard;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IDependencyService;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * This class is the UI accessor for the {@link IReverseSynchronizationService} features.<br>
 * It wraps calls to {@link IReverseSynchronizationService} in some UI jobs and handles all
 * UI-related synchronization stuff.
 * 
 * @author Christophe Fondacci
 */
public class ReverseSynchronizationUIService implements IReverseSynchronizationUIService {

	private IReverseSynchronizationService reverseSynchronizationService;

	public IVersionContainer getNewElementsTargetModule() {
		return reverseSynchronizationService.getNewElementsTargetModule();
	}

	public void setNewElementsTargetModule(IVersionContainer module) {
		reverseSynchronizationService.setNewElementsTargetModule(module);
	}

	private ISynchronizationUIService synchronizationService;
	private IListenerService listenerService;

	private class SynchFinishedListener extends JobChangeAdapter {

		@Override
		public void done(final IJobChangeEvent event) {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					if (event.getResult() == Status.OK_STATUS) {
						synchronizationService.clearSynchronization();
						MessageDialog.openInformation(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								SynchUIMessages.getString("synch.reverse.successTitle"), //$NON-NLS-1$
								SynchUIMessages.getString("synch.reverse.successMsg")); //$NON-NLS-1$
					} else if (event.getResult() != Status.CANCEL_STATUS) {
						MessageDialog.openError(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								SynchUIMessages.getString("synch.reverse.failureTitle"), //$NON-NLS-1$
								SynchUIMessages.getString("synch.reverse.failureMsg")); //$NON-NLS-1$
					}
				}
			});
		}
	}

	@Override
	public void reverseSynchronize(ISynchronizationResult result, IProgressMonitor monitor) {
		final IReverseSynchronizationContext context = reverseSynchronizationService
				.createContext(result);
		if (!validateContext(context)) {
			return;
		}
		reverseSynchronize(context, monitor);
	}

	@Override
	public void reverseSynchronize(final IReverseSynchronizationContext context,
			IProgressMonitor monitor) {
		Job synchJob = new BlockingJob(SynchUIMessages.getString("synch.reverse.job.title")) { //$NON-NLS-1$

			@Override
			public IStatus run(IProgressMonitor monitor) {
				SubMonitor subMonitor = SubMonitor.convert(monitor,
						SynchUIMessages.getString("synch.reverse.job.task"), 100); //$NON-NLS-1$
				try {
					// listenerService.blockNotifications();
					// listenerService.setDispatchMode(IListenerService.ASYNCHED);
					reverseSynchronizationService.reverseSynchronize(context,
							subMonitor.newChild(97));
					subMonitor.setWorkRemaining(3);
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							refreshVersionNavigator();
						}
					});
					subMonitor.setWorkRemaining(1);

				} catch (CancelException e) {
					return Status.CANCEL_STATUS;
				} catch (RuntimeException e) {
					return new Status(Status.ERROR, SynchUIPlugin.PLUGIN_ID,
							"Error during reverse synchronization: " + e.getMessage(), e);
				} finally {
					subMonitor.setTaskName(SynchUIMessages
							.getString("synch.reverse.unblockNotifications")); //$NON-NLS-1$
					subMonitor.subTask(""); //$NON-NLS-1$
					// listenerService.setDispatchMode(IListenerService.SYNCHED);
					// listenerService.unblockNotifications(true, subMonitor.newChild(20));
				}
				return Status.OK_STATUS;
			}
		};
		synchJob.setUser(true);
		synchJob.addJobChangeListener(new SynchFinishedListener());
		synchJob.schedule();
	}

	@Override
	public void removeFromView(IVersionable<?> v, IReverseSynchronizationContext context) {
		VCSPlugin.getService(IDependencyService.class).checkDeleteAllowed(v);
		// Retrieving opened dependent GUI editors
		List<IEditorReference> editorsToClose = GUIService.getDependentEditors(v);
		// Closing editors
		PlatformUI
				.getWorkbench()
				.getActiveWorkbenchWindow()
				.getActivePage()
				.closeEditors(editorsToClose.toArray(new IEditorReference[editorsToClose.size()]),
						false);
		// Removing
		reverseSynchronizationService.removeFromView(v, context);
	}

	@Override
	public void addToView(IWorkspace view, IVersionContainer container, IVersionable<?> toImport,
			IReverseSynchronizationContext context) {
		reverseSynchronizationService.addToView(view, container, toImport, context);
	}

	@Override
	public IReverseSynchronizationContext createContext(ISynchronizationResult result) {
		return reverseSynchronizationService.createContext(result);
	}

	@Override
	public void updateIntoView(IWorkspace view, IComparisonItem item,
			IReverseSynchronizationContext context) {
		reverseSynchronizationService.updateIntoView(view, item, context);
	}

	public void setReverseSynchronizationService(IReverseSynchronizationService reverseService) {
		this.reverseSynchronizationService = reverseService;
	}

	public void setSynchronizationService(ISynchronizationUIService synchService) {
		this.synchronizationService = synchService;
	}

	private boolean validateContext(IReverseSynchronizationContext context) {
		final List<IMarker> markers = reverseSynchronizationService.getProblems(context);
		if (markers.size() == 0) {
			return true;
		} else {
			ResolveProblemsWizard wiz = new ResolveProblemsWizard(markers);
			WizardDialog dlg = new WizardDialog(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), wiz);
			dlg.setBlockOnOpen(true);
			dlg.open();
			if (dlg.getReturnCode() == Window.OK) {
				return validateContext(context);
			} else {
				return false;
			}
		}
	}

	@Override
	public List<IMarker> getProblems(IReverseSynchronizationContext context) {
		return reverseSynchronizationService.getProblems(context);
	}

	public void setListenerService(IListenerService listenerService) {
		this.listenerService = listenerService;
	}

	/**
	 * Forces the version navigator to refresh
	 */
	private void refreshVersionNavigator() {
		// Since we may not be in the version navigator perspective, we cannot find the navigator
		// view. Instead, we fire an event notification on the current IWorkspace so that it will
		// trigger navigator refresh if the navigator exists (fix for bug DES-767)
		IWorkspaceService workspaceService = VCSPlugin.getViewService();
		workspaceService.getCurrentWorkspace().notifyListeners(ChangeEvent.MODEL_CHANGED, null);
	}
}
