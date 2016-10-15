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
package com.nextep.designer.vcs.ui.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.service.GUIService;
import com.nextep.datadesigner.model.IListenerService;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IProblemSolver;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.core.model.ResourceConstants;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.ui.helpers.BlockingJob;
import com.nextep.designer.ui.jobs.NextepSchedulingRules;
import com.nextep.designer.vcs.marker.impl.RemoveHint;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

public class WorkspaceUIService implements IWorkspaceUIService {

	private static final Log LOGGER = LogFactory.getLog(WorkspaceUIService.class);
	private static final int REFRESH_INTERVAL = 500;

	private IWorkspaceService workspaceService;
	private IListenerService listenerService;
	private IVersioningService versioningService;
	private IProblemSolver problemSolver;
	private ICoreFactory coreFactory;
	private ICoreService coreService;
	private IMarkerService markerService;
	private StructuredViewer viewer;
	private Collection<IReference> dirtyItems;
	private NavigatorRefreshJob refreshJob;

	public WorkspaceUIService() {
		dirtyItems = new HashSet<IReference>();
		refreshJob = new NavigatorRefreshJob();
	}

	/**
	 * This job handles the refresh of pending "dirty" elements of the version navigator.
	 */
	private class NavigatorRefreshJob extends UIJob {

		public NavigatorRefreshJob() {
			super(VCSUIMessages.getString("provider.versionable.refreshJob")); //$NON-NLS-1$
			setRule(NextepSchedulingRules.REFRESHER);
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			Collection<IReference> toProcess;
			synchronized (dirtyItems) {
				toProcess = dirtyItems;
				dirtyItems = new HashSet<IReference>();
			}
			// As long as there is something to process, we process and reschedule
			if (!toProcess.isEmpty()) {
				// Refreshing dirty elements
				for (IReference dirtyReference : toProcess) {
					try {
						final IReferenceable dirtyObj = VersionHelper
								.getReferencedItem(dirtyReference);
						if (dirtyObj != null) {
							viewer.update(dirtyObj, null);
							viewer.refresh(dirtyObj, true);
						}
					} catch (ErrorException e) {
						LOGGER.debug(
								"Cannot refresh dirty object referenced " + dirtyReference
										+ ", probably not present in replacing version : "
										+ e.getMessage(), e);
					} catch (RuntimeException e) {
						LOGGER.error("Cannot refresh dirty object referenced " + dirtyReference
								+ " : " + e.getMessage(), e);
					}
				}
				// Rescheduling (always)
				schedule(REFRESH_INTERVAL);
			} else {
				// When nothing to do, we make a full refresh and stop
				viewer.refresh();
			}
			return Status.OK_STATUS;
		}
	}

	/**
	 * A marker hint extending regular remove hint to close currently opened editors on removed
	 * elements.
	 */
	private class RemoveUIHint extends RemoveHint {

		@Override
		public void execute(Object element) {
			super.execute(element);
			closeEditors(null, (IReferenceable) element);
		}
	}

	@Override
	public void changeWorkspace(final UID viewId) {
		Job job = new Job(VCSUIMessages.getString("service.ui.view.loadView")) { //$NON-NLS-1$

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					// listenerService.setDispatchMode(IListenerService.ASYNCHED);
					dirtyItems.clear();
					workspaceService.changeWorkspace(viewId, monitor);
				} finally {
					// listenerService.setDispatchMode(IListenerService.SYNCHED);
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}

	public void setWorkspaceService(IWorkspaceService service) {
		this.workspaceService = service;
	}

	public void setListenerService(IListenerService service) {
		this.listenerService = service;
	}

	@Override
	public void move(final Collection<IVersionable<?>> elementsToMove,
			final IVersionContainer targetContainer) {
		// Performing drop here
		// boolean confirmed = MessageDialog.openConfirm(PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow().getShell(), VCSUIMessages
		//				.getString("movingVersionableConfirmTitle"), MessageFormat.format(VCSUIMessages //$NON-NLS-1$
		//				.getString("movingVersionableConfirm"), targetContainer.getName())); //$NON-NLS-1$
		// if (confirmed) {
		Job job = new BlockingJob(VCSUIMessages.getString("service.ui.view.loadView")) { //$NON-NLS-1$

			@Override
			public IStatus run(IProgressMonitor monitor) {
				try {
					markerService.pauseMarkerComputation();
					workspaceService.move(elementsToMove, targetContainer, monitor);
				} finally {
					markerService.resumeMarkerComputation();
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		// }
	}

	@Override
	public void remove(IReferenceable... elementsToRemove) {
		final MultiValueMap invRefMap = CorePlugin.getService(IReferenceManager.class).getReverseDependenciesMap();
		// Building a collection of proxy object to keep reference to workspace object
		final List<IModelOriented<IReferenceable>> proxiesToRemove = new ArrayList<IModelOriented<IReferenceable>>();
		for (IReferenceable r : elementsToRemove) {
			proxiesToRemove.add(versioningService.createVersionAwareObject(r));
		}

		// We are at the root level of dependency computation, so no dependency is deleted so far
		List<IReferencer> deletedDependencies = Collections.emptyList();
		// Computing all dependencies which would remain in the chain of deletion
		List<IReferencer> remainingDependencies = workspaceService.getRemainingDependencies(
				deletedDependencies, invRefMap, elementsToRemove);
		// Here we know exactly what dependency is a problem so now we need to compute any locking
		// problem which would prevent these objects from being deleted
		List<IMarker> markers = new ArrayList<IMarker>();
		// We need to unlock the parent of initial elements to remove as well
		List<Object> elementsToUnlock = new ArrayList<Object>(remainingDependencies);
		elementsToUnlock.addAll(Arrays.asList(elementsToRemove));
		// Getting unlock markers
		markers.addAll(versioningService.getUnlockMarkers(true, elementsToUnlock));

		// Converting this list into problems
		for (IReferencer r : remainingDependencies) {
			final ITypedObject typedObj = (ITypedObject) r;
			IModelOriented<?> proxiedObj = null;
			proxiedObj = versioningService.createVersionAwareObject(typedObj);
			final IMarker marker = coreFactory
					.createMarker(
							proxiedObj,
							MarkerType.ERROR,
							VCSUIMessages.getString("service.ui.view.dependencyProblemMarker"), new RemoveUIHint()); //$NON-NLS-1$
			marker.setIcon(coreService.getResource(ResourceConstants.ICON_DROP));
			markers.add(marker);
		}
		if (!markers.isEmpty()) {
			problemSolver.solve(markers.toArray(new IMarker[markers.size()]));
		}

		// Creating the removal job
		Job j = new BlockingJob(VCSUIMessages.getString("service.ui.view.removeJob")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					final SubMonitor m = SubMonitor.convert(monitor);
					m.beginTask(VCSUIMessages.getString("service.ui.view.removeTask"), 220); //$NON-NLS-1$
					// Rebuilding a proper IReferenceable list from the proxy
					List<IReferenceable> toRemove = new ArrayList<IReferenceable>();
					for (IModelOriented<IReferenceable> r : proxiesToRemove) {
						toRemove.add(r.getModel());
					}
					m.worked(20);
					final IReferenceable[] toRemoveArray = toRemove
							.toArray(new IReferenceable[toRemove.size()]);
					workspaceService.remove(m.newChild(100), toRemoveArray);
					m.setWorkRemaining(100);
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							closeEditors(m.newChild(100), toRemoveArray);
						}
					});
					m.done();
					return Status.OK_STATUS;
				} catch (CancelException e) {
					return Status.CANCEL_STATUS;
				}
			}
		};
		j.schedule();
	}

	private void closeEditors(IProgressMonitor m, IReferenceable... removedElts) {
		SubMonitor monitor = SubMonitor.convert(m);
		monitor.beginTask(
				VCSUIMessages.getString("service.ui.view.closeEditorsTask"), removedElts.length); //$NON-NLS-1$
		for (IReferenceable r : removedElts) {
			// Retrieving opened dependent GUI editors
			List<IEditorReference> editorsToClose = GUIService.getDependentEditors(r);
			if (!editorsToClose.isEmpty()) {
				// Closing editors
				PlatformUI
						.getWorkbench()
						.getActiveWorkbenchWindow()
						.getActivePage()
						.closeEditors(
								editorsToClose.toArray(new IEditorReference[editorsToClose.size()]),
								false);
			}
			monitor.worked(1);
		}
		monitor.done();
	}

	/**
	 * Injects the {@link IVersioningService} implementation
	 * 
	 * @param versioningService
	 */
	public void setVersioningService(IVersioningService versioningService) {
		this.versioningService = versioningService;
	}

	/**
	 * Injects the {@link IProblemSolver} implementation
	 * 
	 * @param problemSolver
	 */
	public void setProblemSolver(IProblemSolver problemSolver) {
		this.problemSolver = problemSolver;
	}

	/**
	 * @param coreFactory the coreFactory to set
	 */
	public void setCoreFactory(ICoreFactory coreFactory) {
		this.coreFactory = coreFactory;
	}

	/**
	 * @param coreService the coreService to set
	 */
	public void setCoreService(ICoreService coreService) {
		this.coreService = coreService;
	}

	/**
	 * @param markerService the markerService to set
	 */
	public void setMarkerService(IMarkerService markerService) {
		this.markerService = markerService;
	}

	@Override
	public void registerVersionNavigatorViewer(StructuredViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void refreshNavigatorFor(Object o) {
		if (o instanceof IReferenceable) {
			synchronized (dirtyItems) {
				dirtyItems.add(((IReferenceable) o).getReference());
			}
			if (refreshJob.getState() == Job.NONE) {
				refreshJob.schedule(REFRESH_INTERVAL);
			}
		} else if (o instanceof IWorkspace) {
			synchronized (dirtyItems) {
				dirtyItems.clear();
			}
			refreshJob.schedule();
		} else {
			LOGGER.warn(MessageFormat.format(
					VCSUIMessages.getString("provider.versionable.refreshProblem"), o)); //$NON-NLS-1$
		}
	}
}
