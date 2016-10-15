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
package com.nextep.designer.vcs.ui.listeners;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.navigator.CommonViewer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningListener;
import com.nextep.designer.vcs.model.VersioningOperation;
import com.nextep.designer.vcs.ui.navigators.VersionNavigator;

/**
 * This navigator is in charge of maintaining the version navigator appearance after versioning
 * operations. Instead of listening to every event, this listener allows a single-pass refresh of
 * the tree after versioning operation.
 * 
 * @author Christophe Fondacci
 */
public class NavigatorVersioningListener implements IVersioningListener {

	private static final Log LOGGER = LogFactory.getLog(NavigatorVersioningListener.class);
	private VersionNavigator navigator;

	public NavigatorVersioningListener(VersionNavigator navigator) {
		this.navigator = navigator;
	}

	@Override
	public void handleBeforeVersionableOperation(VersioningOperation operation, IVersionable<?> v) {
	}

	@Override
	public void handleAfterVersionableOperation(VersioningOperation operation, IVersionable<?> v) {
	}

	@Override
	public void handleBeforeServiceOperation(VersioningOperation operation,
			Collection<IVersionable<?>> versionables) {

	}

	@Override
	public void handleAfterServiceOperation(VersioningOperation operation,
			final Collection<IVersionable<?>> versionables) {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				uiThreadRefresh(versionables);
			}
		});
	}

	private void uiThreadRefresh(Collection<IVersionable<?>> versionables) {
		final CommonViewer viewer = getVersionNavigatorViewer();
		// Security enforcement for bud DES-443, we refresh the whole navigator viewer, no matter
		// which versionables are passed
		// viewer.refresh();
	}

	private CommonViewer getVersionNavigatorViewer() {
		return navigator.getCommonViewer();
	}
}
