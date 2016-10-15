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
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.gui.dialog.VersionHistoryGUI;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.ui.dialogs.DialogGUIWrapper;
import com.nextep.designer.ui.dialogs.IDialogValidator;
import com.nextep.designer.ui.dialogs.TitleAreaDialogWrapper;
import com.nextep.designer.ui.helpers.BlockingJob;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.ui.jobs.NextepSchedulingRules;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.VersioningOperation;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.dialogs.ConfirmUndoCheckoutDialog;
import com.nextep.designer.vcs.ui.services.IVersioningUIService;

public class VersioningUIService implements IVersioningUIService {

	private IVersioningService versioningService;

	@Override
	public IVersionInfo pickPreviousVersion(IVersionable<?> v, String title) {
		return pickPreviousVersion(null, v, title);
	}

	@Override
	public List<IVersionable<?>> checkOut(IProgressMonitor monitor,
			final IVersionable<?>... checkedInVersions) {
		final IVersioningOperationContext context = versioningService.createVersioningContext(
				VersioningOperation.CHECKOUT, Arrays.asList(checkedInVersions));

		validate(context);
		Job j = new BlockingJob(VCSUIMessages.getString("version.ui.checkOutJob")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// listenerService.setDispatchMode(IListenerService.ASYNCHED);
					final List<IVersionable<?>> checkedOuts = versioningService.checkOut(monitor,
							context);
					setSelection(checkedOuts);

				} catch (CancelException e) {
					return Status.CANCEL_STATUS;
					// } finally {
					// listenerService.setDispatchMode(IListenerService.SYNCHED);
				}
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
		// asynchronous UI-oriented process, returning nothing
		return Collections.emptyList();
	}

	@Override
	public void commit(final IProgressMonitor monitor, final IVersionable<?>... checkedOutVersions) {
		final IVersioningOperationContext context = versioningService.createVersioningContext(
				VersioningOperation.COMMIT, Arrays.asList(checkedOutVersions));
		// Validation
		validate(context);
		// Execution
		Job j = new BlockingJob(VCSUIMessages.getString("version.ui.commitJob")) { //$NON-NLS-1$

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// listenerService.setDispatchMode(IListenerService.ASYNCHED);
					versioningService.commit(monitor, context);
				} catch (CancelException e) {
					return Status.CANCEL_STATUS;
					// } finally {
					// listenerService.setDispatchMode(IListenerService.SYNCHED);
				}
				return Status.OK_STATUS;
			}
		};
		j.setUser(true);
		j.schedule();
	}

	@Override
	public List<IVersionable<?>> undoCheckOut(IProgressMonitor monitor,
			final IVersionable<?>... checkedOutVersions) {
		final IWorkspaceService workspaceService = CorePlugin.getService(IWorkspaceService.class);
		// Building confirm text
		final List<IVersionable<?>> elts = new ArrayList<IVersionable<?>>();
		for (IVersionable<?> v : checkedOutVersions) {
			if (v.getVersion().getUser() != workspaceService.getCurrentUser()) {
				throw new ErrorException(
						MessageFormat.format(
								VCSUIMessages.getString("undoCheckoutInvalidUser"), v.getType().getName().toLowerCase(), //$NON-NLS-1$
								v.getName(), v.getVersion().getUser().getName()));
			}
			elts.add(v);
			for (IReferenceable r : v.getReferenceMap().values()) {
				if (r instanceof IVersionable<?>) {
					if (((IVersionable<?>) r).getVersion().getStatus() == IVersionStatus.CHECKED_OUT) {
						elts.add((IVersionable<?>) r);
					}
				}
			}
		}
		// initializing dialog
		ConfirmUndoCheckoutDialog commitDlg = new ConfirmUndoCheckoutDialog(elts);
		Dialog dlg = new TitleAreaDialogWrapper(UIHelper.getShell(), commitDlg, SWT.RESIZE
				| SWT.TITLE | SWT.BORDER);
		dlg.setBlockOnOpen(true);
		dlg.open();

		// If user confirmed, we start undo checkout
		if (dlg.getReturnCode() == Window.OK) {

			final IVersioningOperationContext context = versioningService.createVersioningContext(
					VersioningOperation.UNDO_CHECKOUT, Arrays.asList(checkedOutVersions));
			// Validation
			validate(context);
			// Execution
			Job j = new BlockingJob(VCSUIMessages.getString("version.ui.undoCheckOutJob")) { //$NON-NLS-1$

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						// listenerService.setDispatchMode(IListenerService.ASYNCHED);
						List<IVersionable<?>> undoCheckouts = versioningService.undoCheckOut(
								monitor, context);
						setSelection(undoCheckouts);
					} catch (CancelException e) {
						return Status.CANCEL_STATUS;
						// } finally {
						// listenerService.setDispatchMode(IListenerService.SYNCHED);
					}
					return Status.OK_STATUS;
				}
			};
			j.setUser(true);
			j.schedule();
		}
		return Collections.emptyList();
	}

	public void setVersioningService(IVersioningService service) {
		this.versioningService = service;
	}

	@Override
	public <T> T ensureModifiable(T v) {
		// Passing plates
		return versioningService.ensureModifiable(v);
	}

	public void unlock(boolean parentsOnly, IVersionable<?>... lockedVersionables) {
		versioningService.unlock(parentsOnly, lockedVersionables);
	}

	@Override
	public IVersionInfo pickPreviousVersion(Shell parentShell, IVersionable<?> v, String title) {
		String dialogTitle = title;
		if (title == null) {
			dialogTitle = VCSUIMessages.getString("version.ui.pickPrevious"); //$NON-NLS-1$
		}
		VersionHistoryGUI gui = new VersionHistoryGUI(v);
		DialogGUIWrapper wrapper = new DialogGUIWrapper(parentShell, gui, dialogTitle,
				new IDialogValidator() {

					@Override
					public Object getSelection(IDisplayConnector c) {
						return ((VersionHistoryGUI) c).getSelection();
					}

				});
		wrapper.setBlockOnOpen(true);
		if (wrapper.open() == Window.OK) {
			return (IVersionInfo) wrapper.getResult();
		} else {
			throw new CancelException();
		}
	}

	public List<IMarker> getUnlockMarkers(boolean parentsOnly,
			Collection<?> potentiallyLockedObjects) {
		return versioningService.getUnlockMarkers(parentsOnly, potentiallyLockedObjects);
	};

	private void setSelection(List<IVersionable<?>> selection) {
		final ISelection sel = new StructuredSelection(selection);
		// Setting the selection
		Job j = new UIJob(VCSUIMessages.getString("service.ui.versioning.updateSelectionJob")) { //$NON-NLS-1$

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				yieldRule(monitor);
				if (PlatformUI.getWorkbench() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
								.getActivePart() != null) {
					final ISelectionProvider provider = PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage().getActivePart().getSite()
							.getSelectionProvider();
					provider.setSelection(sel);
				}
				return Status.OK_STATUS;
			}

		};
		j.setRule(NextepSchedulingRules.SELECTIONER);
		j.schedule();
	}

	private IStatus validate(IVersioningOperationContext context) {
		final IStatus status = versioningService.validate(context);
		if (!status.isOK()) {
			if (status.getException() != null) {
				throw new ErrorException(status.getMessage(), status.getException());
			} else {
				throw new CancelException();
			}
		}
		return status;
	}

}
