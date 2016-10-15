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
package com.nextep.designer.vcs.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * Handler for the show history command.<br>
 * Displays a dialog box with the history of all versions for the currently selected element.
 * 
 * @author Christophe Fondacci
 */
public class VersionHistoryHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection s = HandlerUtil.getCurrentSelection(event);
		if (s instanceof IStructuredSelection && !s.isEmpty()) {
			final Object o = ((IStructuredSelection) s).iterator().next();
			final IVersionInfo version = VersionHelper.getVersionInfo(o);
			if (version != null) {
				final IVersionable<?> v = VersionHelper.getVersionable(VersionHelper
						.getReferencedItem(version.getReference()));
				VCSUIPlugin.getVersioningUIService().pickPreviousVersion(v,
						VCSUIMessages.getString("dialog.version.history")); ////$NON-NLS-1$
			}
		}

		return null;
	}
}
