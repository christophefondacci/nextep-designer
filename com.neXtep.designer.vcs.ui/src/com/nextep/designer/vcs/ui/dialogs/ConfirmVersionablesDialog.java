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

import java.util.Collections;
import java.util.List;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.gui.impl.swt.TableColumnSorter;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.base.AbstractUIComponent;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.jface.VersionableTableContentProvider;
import com.nextep.designer.vcs.ui.jface.VersionableVersionStatusLabelProvider;
import com.nextep.designer.vcs.ui.swt.VersionableVersionStatusTable;

public abstract class ConfirmVersionablesDialog extends AbstractUIComponent implements
		ITitleAreaComponent {

	private List<IVersionable<?>> versionablesToConfirm;
	private IVersioningOperationContext context;
	private TableViewer viewer;
	private Combo activityCombo;
	private boolean activityDisplayed;

	public ConfirmVersionablesDialog(IVersioningOperationContext context,
			List<IVersionable<?>> commitsToConfirm, boolean displayActivity) {
		this.versionablesToConfirm = commitsToConfirm;
		this.context = context;
		this.activityDisplayed = displayActivity;
	}

	@Override
	public Control create(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		c.setLayout(layout);
		if (activityDisplayed) {
			Label activityLabel = new Label(c, SWT.NONE);
			activityLabel.setText(VCSUIMessages.getString("dialog.versionSettings.activity")); //$NON-NLS-1$
			activityCombo = new Combo(c, SWT.BORDER);
			activityCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			final IVersioningService versioningService = getVersioningService();
			activityCombo.setText(getCurrentActivity().getName());
			List<IActivity> activities = versioningService.getRecentActivities();
			for (IActivity a : activities) {
				activityCombo.add(a.getName());
			}
		}

		Table table = VersionableVersionStatusTable.create(c, SWT.BORDER, 2);
		viewer = new TableViewer(table);
		Collections.sort(versionablesToConfirm, NameComparator.getInstance());
		viewer.setLabelProvider(new VersionableVersionStatusLabelProvider(context));
		viewer.setContentProvider(new VersionableTableContentProvider(context));
		viewer.setInput(versionablesToConfirm);
		viewer.setComparator(new TableColumnSorter(table, viewer));

		return c;
	}

	private IActivity getCurrentActivity() {
		if (context != null && context.getActivity() != null) {
			return context.getActivity();
		} else {
			return getVersioningService().getCurrentActivity();
		}
	}

	protected IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}

	protected boolean isActivityDisplayed() {
		return activityDisplayed;
	}

	protected List<IVersionable<?>> getVersionablesToConfirm() {
		return versionablesToConfirm;
	}

	protected IVersioningOperationContext getContext() {
		return context;
	}

	protected String getActivityUserText() {
		if (activityCombo != null && !activityCombo.isDisposed()) {
			return activityCombo.getText();
		} else {
			return getCurrentActivity().getName();
		}
	}
}
