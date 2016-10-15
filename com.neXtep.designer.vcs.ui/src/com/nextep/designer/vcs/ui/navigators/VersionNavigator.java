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
package com.nextep.designer.vcs.ui.navigators;

import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersioningListener;
import com.nextep.designer.vcs.services.IVersioningService;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.jface.TextFilter;
import com.nextep.designer.vcs.ui.listeners.NavigatorVersioningListener;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

public class VersionNavigator extends CommonNavigator {

	public final static String VIEW_ID = "com.neXtep.designer.vcs.ui.navigator"; //$NON-NLS-1$
	// private final static Log log = LogFactory.getLog(VersionNavigator.class);
	private IVersioningListener versioningListener;

	public VersionNavigator() {
		setLinkingEnabled(true);
		this.versioningListener = new NavigatorVersioningListener(this);
		VCSPlugin.getService(IVersioningService.class).addVersioningListener(versioningListener);
	}

	@Override
	protected Object getInitialInput() {
		return VersionNavigatorRoot.getInstance();
	}

	@Override
	public void dispose() {
		VCSPlugin.getService(IVersioningService.class).removeVersioningListener(versioningListener);
		super.dispose();
	}

	@Override
	protected CommonViewer createCommonViewer(Composite aParent) {
		// Tweaking common viewer creation to add a search bar on top
		GridLayout parentLayout = new GridLayout(2, false);
		setNoMarginLayout(parentLayout);
		aParent.setLayout(parentLayout);
		Label lbl = new Label(aParent, SWT.RIGHT);
		lbl.setText(VCSUIMessages.getString("navigator.filter")); //$NON-NLS-1$
		lbl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		Text filterText = new Text(aParent, SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
		filterText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Common viewer creation with specific SWT attributes like FULL_SELECTION
		final CommonViewer viewer = new CommonViewer(getViewSite().getId(), aParent, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION) {

			@Override
			protected boolean isSameSelection(List items, Item[] current) {
				return false;
			}
		};
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		// Registering our "static" filter
		TextFilter.handle(viewer, filterText);
		viewer.setComparer(new VersionableNavigatorComparer());
		viewer.setAutoExpandLevel(2);
		// Registering viewer
		CorePlugin.getService(IWorkspaceUIService.class).registerVersionNavigatorViewer(viewer);
		// viewer.setMapper(new VersionableNavigatorMapper(viewer));
		return viewer;
	}

	private void setNoMarginLayout(GridLayout l) {
		l.marginBottom = l.marginHeight = l.marginHeight = l.marginLeft = l.marginRight = l.marginWidth = 0;
		l.marginTop = 3;
		l.verticalSpacing = 3;
	}

}
