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
package com.nextep.designer.vcs.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.vcs.gui.VersionInfoEditor;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.IValidatableUI;
import com.nextep.designer.ui.model.base.AbstractUIComponent;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.jface.VersionableNewLabelProvider;
import com.nextep.designer.vcs.ui.jface.VersionableTableContentProvider;

public class VersionSettingsDialog extends AbstractUIComponent implements ITitleAreaComponent,
		IValidatableUI {

	private VersionInfoEditor editor;
	private IVersioningOperationContext context;
	private Composite editorPane;
	private Combo activityCombo;
	private Collection<IVersionable<?>> versionablesToReview = new ArrayList<IVersionable<?>>();

	public VersionSettingsDialog(IVersioningOperationContext context) {
		this.context = context;
	}

	@Override
	public Control create(Composite p) {
		for (IVersionable<?> v : context.getVersionables()) {
			if (v.getContainer() instanceof IWorkspace) {
				versionablesToReview.add(v);
			}
		}
		// Main composite
		Composite parent = new Composite(p, SWT.NONE);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		parent.setLayout(new GridLayout(2, false));
		// Activity controls
		Label activityLabel = new Label(parent, SWT.RIGHT);
		activityLabel.setText(VCSUIMessages.getString("dialog.versionSettings.activity")); //$NON-NLS-1$
		activityLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		activityCombo = new Combo(parent, SWT.BORDER);
		activityCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		activityCombo.setText(getActivityText());
		List<IActivity> activities = VCSPlugin.getService(IVersioningService.class)
				.getRecentActivities();
		for (IActivity a : activities) {
			activityCombo.add(a.getName());
		}
		// Sash form
		SashForm editor = new SashForm(parent, SWT.HORIZONTAL);
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		// Listing of versionables to review
		TableViewer viewer = new TableViewer(editor, SWT.BORDER);
		viewer.setLabelProvider(new DecoratingStyledCellLabelProvider(
				new VersionableNewLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager()
						.getLabelDecorator(), null));
		viewer.setContentProvider(new VersionableTableContentProvider());
		viewer.setInput(versionablesToReview);

		// Editor pane for version selection
		editorPane = new Composite(editor, SWT.BORDER);
		editorPane.setLayout(new GridLayout());
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final ISelection s = event.getSelection();
				if (s instanceof IStructuredSelection && !s.isEmpty()) {
					Object o = ((IStructuredSelection) s).getFirstElement();
					if (o instanceof IVersionable<?>) {
						editVersionable((IVersionable<?>) o);
					}
				}
			}
		});
		viewer.setSelection(new StructuredSelection(versionablesToReview.iterator().next()));
		editor.setWeights(new int[] { 2, 5 });
		return parent;
	}

	/**
	 * Displays the release editor for the specified versionable by creating a new editor or
	 * replacing any current editor.
	 * 
	 * @param v the {@link IVersionable} to edit release for
	 */
	private void editVersionable(IVersionable<?> v) {
		if (v != null) {
			if (editor != null) {
				editor.getSWTConnector().dispose();
			}
			final IVersionInfo editedVersion = context.getTargetVersionInfo(v);
			editor = new VersionInfoEditor(editedVersion, false);
			editor.create(editorPane);
			editor.getSWTConnector().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			editor.refreshConnector();
			editorPane.layout();
		}
	}

	@Override
	public String getAreaTitle() {
		return VCSUIMessages.getString("dialog.versionSettings.title"); //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return VCSUIMessages.getString("dialog.versionSettings.description"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return VCSImages.ICON_VERSION_SETTINGS;
	}

	private String getActivityText() {
		return context.getActivity() == null ? "" : context.getActivity().getName(); //$NON-NLS-1$
	}

	@Override
	public boolean validate() {
		if (context.getActivity() == null
				|| !context.getActivity().getName().equals(activityCombo.getText())) {
			// We set a new activity when selected text differs from context activity
			context.setActivity(getVersioningService().createActivity(activityCombo.getText()));
		} else {
			getVersioningService().setCurrentActivity(context.getActivity());
		}
		dispatchEditedRelease();
		return true;
	}

	@Override
	public void cancel() {
		// Nothing to cancel
	}

	/**
	 * Dispatches any current edition to the context. It is mainly meant to propagate the currently
	 * edited release updates to all children.
	 */
	private void dispatchEditedRelease() {

		getUIComponentContainer().run(true, false, new IRunnableWithProgress() {

			@Override
			public void run(IProgressMonitor m) throws InvocationTargetException,
					InterruptedException {
				SubMonitor monitor = SubMonitor.convert(m);
				monitor.beginTask(
						VCSUIMessages.getString("dialog.versionSettings.adjustReleaseTask"), 100 * versionablesToReview.size()); //$NON-NLS-1$
				for (IVersionable<?> v : versionablesToReview) {
					final IVersionInfo targetVersion = context.getTargetVersionInfo(v);
					targetVersion.setActivity(context.getActivity());
					monitor.subTask(VCSUIMessages
							.getString("dialog.versionSettings.checkoutLookupTask")); //$NON-NLS-1$
					// Initializing checked out elements list
					Collection<IVersionable<?>> checkedOutChildren = new ArrayList<IVersionable<?>>();
					fillCheckedOutChildren((IVersionContainer) v, checkedOutChildren);
					monitor.worked(30);
					// Aligning every checked out children with the target version defined by the
					// user
					alignChildren(checkedOutChildren, targetVersion, monitor.newChild(40));
					monitor.setWorkRemaining(30);
					// Notifying
					monitor.subTask(VCSUIMessages
							.getString("dialog.versionSettings.workbenchNotificationTask")); //$NON-NLS-1$
					v.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				}
				monitor.done();
			}
		});
	}

	/**
	 * Fills the specified list with checked out elements of the provided container. This method
	 * will recurse into sub containers when needed.
	 * 
	 * @param v the container to extract checkouts of
	 * @param checkedOutChildren the collection of checked out elements to fill
	 */
	private void fillCheckedOutChildren(IVersionContainer v,
			Collection<IVersionable<?>> checkedOutChildren) {
		for (IVersionable<?> child : v.getContents()) {
			if (child.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
				checkedOutChildren.add(child);
				if (child instanceof IVersionContainer) {
					fillCheckedOutChildren((IVersionContainer) child, checkedOutChildren);
				}
			}
		}
	}

	/**
	 * Aligns the versions of the specified versionables with the provided reference version.
	 * 
	 * @param versionables the collection of {@link IVersionable} objects to align
	 * @param referenceVersion reference {@link IVersionInfo} that will be used to align elements.
	 *        Informations from this reference release will be use to adjust versionables' version
	 *        information
	 * @param monitor a {@link IProgressMonitor} to report progress to
	 */
	private void alignChildren(Collection<IVersionable<?>> versionables,
			IVersionInfo referenceVersion, IProgressMonitor monitor) {
		monitor.beginTask(
				VCSUIMessages.getString("dialog.versionSettings.alignReleaseTask"), versionables.size()); //$NON-NLS-1$
		final long referenceRelease = VersionHelper.computeVersion(referenceVersion);
		for (IVersionable<?> v : versionables) {
			IVersionInfo targetRelease = context.getTargetVersionInfo(v);
			targetRelease.setRelease(referenceRelease, true);
			targetRelease.setBranch(referenceVersion.getBranch());
			targetRelease.setActivity(context.getActivity());
			v.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			monitor.worked(1);
		}
	}

	private IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}
}
