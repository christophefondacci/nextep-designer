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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.vcs.model.impl;

import java.text.MessageFormat;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.IVersioningValidator;

/**
 * This validator checks wether every versionable of the versioning operation belongs to the
 * appropriate user.
 * 
 * @author Christophe Fondacci
 */
public class UserVersioningValidator implements IVersioningValidator {

	@Override
	public boolean isActiveFor(IVersioningOperationContext context) {
		return true;
	}

	@Override
	public IStatus validate(IVersioningOperationContext context) {
		for (IVersionable<?> v : context.getVersionables()) {
			final IVersionInfo version = v.getVersion();
			switch (version.getStatus()) {
			case CHECKED_OUT:
			case NOT_VERSIONED:
				if (version.getUser() != VCSPlugin.getViewService().getCurrentUser()) {
					return new Status(IStatus.ERROR, VCSPlugin.PLUGIN_ID, MessageFormat.format(
							VCSMessages.getString("versioning.lockedByOtherUser"), //$NON-NLS-1$
							v.getType(), v.getName(), version.getUser().getName()),
							new ErrorException(""));
				}
			}
		}
		return Status.OK_STATUS;
	}

}
