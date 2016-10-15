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
/**
 *
 */
package com.nextep.designer.vcs.ui.handlers;

import java.text.MessageFormat;
import java.util.List;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * This handler is the default remove handler wired on the "Remove" command on generic elements.
 * 
 * @author Christophe Fondacci
 */
public class RemoveHandler extends AbstractVersionHandler {

	/**
	 * @see com.nextep.designer.vcs.ui.handlers.AbstractVersionHandler#isEnabled(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	protected boolean isEnabled(IVersionable<?> v) {
		return true;
	}

	@Override
	protected boolean checkAndConfirm(List<IVersionable<?>> selectedVersions) {
		final boolean confirmed = MessageDialog.openConfirm(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(),
				VCSUIMessages.getString("handler.remove.title"), //$NON-NLS-1$
				MessageFormat.format(VCSUIMessages.getString("handler.remove.confirmMsg"), //$NON-NLS-1$
						selectedVersions.size()));
		return confirmed;
	}

	/**
	 * @see com.nextep.designer.vcs.ui.handlers.AbstractVersionHandler#versionAction(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	protected Object versionAction(IVersionable<?> v) {
		return null;
	}

	@Override
	protected void versionActions(List<IVersionable<?>> selectedVersions) {
		if (!checkAndConfirm(selectedVersions)) {
			return;
		}
		VCSUIPlugin.getService(IWorkspaceUIService.class).remove(
				selectedVersions.toArray(new IVersionable<?>[selectedVersions.size()]));
	}

}
