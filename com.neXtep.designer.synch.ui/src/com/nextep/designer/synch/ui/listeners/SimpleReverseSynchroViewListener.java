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
package com.nextep.designer.synch.ui.listeners;

import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.dbgm.ui.services.DBGMUIHelper;
import com.nextep.designer.synch.model.IReverseSynchronizationContext;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.services.IReverseSynchronizationService;
import com.nextep.designer.synch.services.ISynchronizationService;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.model.base.AbstractWorkspaceListener;
import com.nextep.designer.vcs.ui.navigators.VersionNavigator;

public class SimpleReverseSynchroViewListener extends AbstractWorkspaceListener {

	@Override
	public void workspaceChanged(IWorkspace oldView, IWorkspace newView, IProgressMonitor monitor) {
		final IReverseSynchronizationService reverseService = CorePlugin
				.getService(IReverseSynchronizationService.class);
		if (newView.isImportOnOpenNeeded()) {
			IVersionContainer targetContainer = null;
			// Checking if initial root container exists
			List<IVersionable<?>> containers = VersionHelper.getAllVersionables(newView,
					IElementType.getInstance(IVersionContainer.TYPE_ID));
			if (!containers.isEmpty()) {
				targetContainer = (IVersionContainer) containers.get(0).getVersionnedObject()
						.getModel();
			} else {
				targetContainer = (IVersionContainer) UIControllerFactory
						.getController(IElementType.getInstance(IVersionContainer.TYPE_ID))
						.emptyInstance(newView.getName(), newView);
			}
			final IVersionContainer module = targetContainer;
			Job j = new Job(SynchUIMessages.getString("synch.simpleReverse.job")) { //$NON-NLS-1$

				@Override
				protected IStatus run(IProgressMonitor parentMonitor) {
					SubMonitor monitor = SubMonitor.convert(parentMonitor,
							SynchUIMessages.getString("synch.simpleReverse.dbSynch"), 100); //$NON-NLS-1$
					IWorkspace newView = getWorkspaceService().getCurrentWorkspace();
					IConnection conn = DBGMUIHelper
							.getConnection(SQLGenUtil.getDefaultTargetType());
					if (conn == null) {
						return Status.CANCEL_STATUS;
					}
					try {
						reverseService.setNewElementsTargetModule(module);
						monitor.worked(10);
						final ISynchronizationResult result = CorePlugin
								.getService(ISynchronizationService.class)
								.buildSynchronizationResult(newView, conn,
										ComparisonScope.DB_TO_REPOSITORY, monitor.newChild(50));
						// Flushing session
						HibernateUtil.getInstance().clearAllSessions();
						final IReverseSynchronizationContext context = reverseService
								.createContext(result);
						monitor.subTask(
								SynchUIMessages.getString("synch.simpleReverse.reverseSynch")); //$NON-NLS-1$
						reverseService.reverseSynchronize(context, monitor.newChild(40));
						newView.setImportOnOpenNeeded(false);
						// Workaround a display bug, we force the full refresh of our navigation
						// view
						Display.getDefault().asyncExec(new Runnable() {

							@Override
							public void run() {
								if (PlatformUI.isWorkbenchRunning() && PlatformUI.getWorkbench()
										.getActiveWorkbenchWindow() != null) {
									IViewPart view = PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow().getActivePage()
											.findView(VersionNavigator.VIEW_ID);
									if (view instanceof VersionNavigator) {
										((VersionNavigator) view).getCommonViewer().refresh();
									}

								}
							}
						});
						monitor.done();
						return Status.OK_STATUS;
					} finally {
						reverseService.setNewElementsTargetModule(null);
					}
				}
			};
			j.setUser(true);
			scheduleJob(j);
		}
	}

	private void scheduleJob(final Job j) {
		if (PlatformUI.isWorkbenchRunning()
				&& PlatformUI.getWorkbench().getWorkbenchWindowCount() != 0) {
			j.schedule();
		} else {
			PlatformUI.getWorkbench().addWindowListener(new IWindowListener() {

				@Override
				public void windowOpened(IWorkbenchWindow window) {
					j.schedule(500);
					PlatformUI.getWorkbench().removeWindowListener(this);
				}

				@Override
				public void windowDeactivated(IWorkbenchWindow window) {

				}

				@Override
				public void windowClosed(IWorkbenchWindow window) {

				}

				@Override
				public void windowActivated(IWorkbenchWindow window) {

				}
			});
		}
	}

	@Override
	public int getPriority() {
		return 5;
	}
}
