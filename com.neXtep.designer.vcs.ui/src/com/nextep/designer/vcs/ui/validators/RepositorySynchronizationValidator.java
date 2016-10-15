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
package com.nextep.designer.vcs.ui.validators;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.IVersioningValidator;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.services.VersionUIHelper;

public class RepositorySynchronizationValidator implements IVersioningValidator {

	@Override
	public boolean isActiveFor(IVersioningOperationContext context) {
		return true;
	}

	@Override
	public IStatus validate(IVersioningOperationContext context) {
		for (IVersionable<?> checkedOutVersion : context.getVersionables()) {
			if (checkAndApplySynchronization(checkedOutVersion)) {
				throw new CancelException(
						VCSUIMessages.getString("versioningService.ui.unsynchronizedWorkspace")); //$NON-NLS-1$
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * This method checks wether the specified versionable is still synched with the repository. If
	 * not, it will automatically be refreshed. <br>
	 * A versionable may not be synched for the following reasons :<br>
	 * - The current workspace view has been updated by another client<br>
	 * - The owning container of this versionable has been updated by another client<br>
	 * - The specified versionable has been updated by another client<br>
	 * 
	 * @param v versionable to check synchronization
	 * @return <code>true</code> if synchronization has been made, <code>false</code> if synched
	 */
	protected boolean checkAndApplySynchronization(IVersionable<?> v) {
		// checking that we have the latest updates on this versionable in our view
		if (!VersionHelper.isContainerUpToDate(VCSPlugin.getViewService().getCurrentWorkspace())) {
			Designer.getInstance().invokeSelection("prompt.reloadView"); //$NON-NLS-1$
			return true;
		} else if (!VersionHelper.isContainerUpToDate(v.getContainer())) {
			Designer.getInstance().invokeSelection("prompt.reloadView"); //$NON-NLS-1$
			return true;
		} else if (!VersionHelper.isUpToDate(v)) {
			if (v instanceof IVersionContainer) {
				Designer.getInstance().invokeSelection("prompt.reloadView"); //$NON-NLS-1$
			} else {
				IVersionable<?> newVersionable = VersionHelper.refresh(v);
				VersionUIHelper.promptObjectSynched(newVersionable);
			}
			return true;
		}
		return false;
	}

}
