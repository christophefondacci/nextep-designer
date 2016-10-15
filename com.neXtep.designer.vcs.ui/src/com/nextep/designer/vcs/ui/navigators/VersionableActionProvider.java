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

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;
import com.nextep.designer.vcs.ui.handlers.OpenTypedObjectAction;

public class VersionableActionProvider extends CommonActionProvider {

	private IAction openAction;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);

		final ICommonViewerSite viewerSite = aSite.getViewSite();
		if (viewerSite instanceof ICommonViewerWorkbenchSite) {
			final ICommonViewerWorkbenchSite workbenchSite = (ICommonViewerWorkbenchSite) viewerSite;
			openAction = new OpenTypedObjectAction(workbenchSite.getPage(), workbenchSite
					.getSelectionProvider());
		}
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		if (openAction != null && openAction.isEnabled()) {
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, openAction);
		}
	}
}
