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
package com.nextep.datadesigner.vcs.gui.rcp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.impl.SchedulingRuleVolatile;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.vcs.gef.VersionTreeGUI;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This view shows versioning information on the currently selected object. It listens to the
 * current workbench selection and will show version informations / options to the user if a
 * IVersionable object is the current selection.
 */

public class VersionableViewRCP extends ViewPart implements ISelectionListener, IEventListener {

	VersionTreeGUI versionableGUI;
	private Object currentSelection = null;

	/**
	 * The constructor.
	 */
	public VersionableViewRCP() {
		versionableGUI = new VersionTreeGUI(null, SWT.NONE);
		versionableGUI.setSelectable(true);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		versionableGUI.create(parent);
		versionableGUI.getSWTConnector().addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(
						VersionableViewRCP.this);
			}
		});
		this.getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);
		this.getSite().setSelectionProvider(versionableGUI.getSelectionProvider());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(versionableGUI.getSWTConnector(),
				"com.neXtep.designer.vcs.ui.VersionableViewRCP");

		// Initilizing first selection
		selectionChanged(null, this.getSite().getWorkbenchWindow().getSelectionService()
				.getSelection());
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		versionableGUI.getSWTConnector().setFocus();
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, final ISelection selection) {
		if (part == this)
			return;
		// Stopping all listening operations
		Designer.getListenerService().unregisterListeners(this);
		Designer.getListenerService().unregisterListeners(versionableGUI.getSWTConnector());
		if (selection != null && (selection instanceof IStructuredSelection)) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (currentSelection instanceof IObservable) {
				// Unregistering any previous listener
				Designer.getListenerService().unregisterListener((IObservable) currentSelection,
						this);
			}
			if (!selection.isEmpty()) {
				if (currentSelection == sel.getFirstElement()) {
					return;
				}
				// Asynchronous job for refreshing version tree
				Job refreshJob = createRefreshJob(sel.getFirstElement());
				refreshJob.setRule(SchedulingRuleVolatile.getInstance());
				refreshJob.schedule();
				// Updating selection
				currentSelection = sel.getFirstElement();
				// Listening to model if observable
				if (currentSelection instanceof IObservable) {
					IObservable o = (IObservable) sel.getFirstElement();
					Designer.getListenerService().registerListener(
							versionableGUI.getSWTConnector(), o, this);
				}
			} else {
				versionableGUI.setModel(null);
				versionableGUI.refreshConnector();
				currentSelection = null;
			}
		}
	}

	/**
	 * Creates the Eclipse job which refreshes the version tree for the given object.
	 * 
	 * @param obj object to refresh in version tree
	 * @return an eclipse background job
	 */
	public Job createRefreshJob(final Object obj) {
		return new Job("Refreshing version tree...") {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {

				Object model = null;
				String taskName = null;
				if (obj instanceof IVersionable<?>) {
					IVersionable<?> v = (IVersionable<?>) obj;
					model = v;
					taskName = "Updating version model for " + v.getType() + " '" + v.getName()
							+ "'...";
				} else if (obj instanceof IVersionInfo) {
					IVersionInfo v = (IVersionInfo) obj;
					model = v;
					taskName = "Updating version for " + v.getLabel() + "...";
				} else {
					model = null;
					taskName = "Resetting version model...";
				}
				try {
					monitor.beginTask(taskName, 2);
					// Setting model in the display thread
					final Object myModel = model;
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

						@Override
						public void run() {
							if (versionableGUI.getSWTConnector() != null
									&& !versionableGUI.getSWTConnector().isDisposed()) {
								versionableGUI.setModel(myModel);
							}
						}
					});
					monitor.worked(1);
					monitor.setTaskName("Refreshing visuals...");
					// We refresh in the UI thread
					PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

						@Override
						public void run() {
							if (versionableGUI.getSWTConnector() != null
									&& !versionableGUI.getSWTConnector().isDisposed()) {
								versionableGUI.refreshConnector();
							}
						}
					});
					monitor.worked(1);
					monitor.done();
				} catch (RuntimeException e) {
					e.printStackTrace();
					throw e;
				}

				return Status.OK_STATUS;
			}

		};
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		// if(adapter== ZoomManager.class) {
		// LogFactory.getLog(this.getClass()).debug("ZOOM MANAGER ADAPTER CALL");
		// if(versionableGUI!= null) {
		// if(versionableGUI.getGraphicalGUI()!=null) {
		// return
		// versionableGUI.getGraphicalGUI().getGraphicalViewer().getProperty(ZoomManager.class.toString());
		// }
		// }
		if (versionableGUI != null) {
			return versionableGUI.getAdapter(adapter);
		}
		// }
		return super.getAdapter(adapter);
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case UPDATES_LOCKED:
		case UPDATES_UNLOCKED:
		case CHECKIN:
		case CHECKOUT:
		case MODEL_CHANGED:
			Job refreshJob = createRefreshJob(source);
			refreshJob.schedule();
		}

	}
}
