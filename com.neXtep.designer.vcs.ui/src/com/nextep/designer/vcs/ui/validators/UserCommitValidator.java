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

import java.text.MessageFormat;
import java.util.Collection;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.IVersioningValidator;
import com.nextep.designer.vcs.model.VersioningOperation;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * This validator checks the user to which versioned objects belongs and displays a confirmation
 * message when trying to commit elements owned by other users (= not current).
 * 
 * @author Christophe Fondacci
 */
public class UserCommitValidator implements IVersioningValidator {

	@Override
	public boolean isActiveFor(IVersioningOperationContext context) {
		return context.getVersioningOperation() == VersioningOperation.COMMIT;
	}

	@Override
	public IStatus validate(IVersioningOperationContext context) {
		// Building list
		final StringBuilder b = new StringBuilder();
		boolean containsOtherUser = fillOtherUserBuffer(context.getVersionables(), b);
		if (containsOtherUser) {
			// Ask for confirmation
			final boolean force = MessageDialog.openConfirm(
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
					VCSUIMessages.getString("checkInDifferentUserForceTitle"), //$NON-NLS-1$
					MessageFormat.format(
							VCSUIMessages.getString("checkInDifferentUserForce"), b.toString() //$NON-NLS-1$
							));
			// User has canceled the process
			if (!force) {
				return Status.CANCEL_STATUS;
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Fills the string buffer with a list of external users elements and returns a flag indicating
	 * whether external elements had been found.
	 * 
	 * @param versionables collection of {@link IVersionable} to process
	 * @param b the string builder to fill
	 * @return <code>true</code> if some objects do not belong to current user, else
	 *         <code>false</code>
	 */
	private boolean fillOtherUserBuffer(Collection<IVersionable<?>> versionables, StringBuilder b) {
		boolean containsOtherUser = false;
		for (IVersionable<?> v : versionables) {
			if (v.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
				if (v.getVersion().getUser() != VCSPlugin.getViewService().getCurrentUser()) {
					b.append(MessageFormat.format(VCSUIMessages
							.getString("version.commit.externalCheckoutElement"), v.getType() //$NON-NLS-1$
							.getName(), v.getName(), v.getVersion().getUser().getName()));
					containsOtherUser = true;
				}
				if (v instanceof IVersionContainer) {
					final boolean innerContains = fillOtherUserBuffer(
							((IVersionContainer) v).getContents(), b);
					containsOtherUser = containsOtherUser || innerContains;
				}
			}
		}
		return containsOtherUser;
	}
}
