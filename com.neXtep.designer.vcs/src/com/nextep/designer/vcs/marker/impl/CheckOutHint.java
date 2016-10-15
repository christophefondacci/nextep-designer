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
package com.nextep.designer.vcs.marker.impl;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.designer.core.model.IMarkerHint;
import com.nextep.designer.util.Assert;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;
import com.nextep.designer.vcs.model.VersioningOperation;
import com.nextep.designer.vcs.services.IVersioningService;

/**
 * A marker hint which, when executed, will checkout the marked element.
 * 
 * @author Christophe Fondacci
 */
public class CheckOutHint implements IMarkerHint {

	@Override
	public void execute(Object element) {
		Assert.instanceOf(element, IVersionable.class,
				VCSMessages.getString("hint.checkout.nonVersionableError")); //$NON-NLS-1$
		final IVersionable<?> v = (IVersionable<?>) element;
		Assert.equals(IVersionStatus.CHECKED_IN, v.getVersion().getStatus(),
				VCSMessages.getString("hint.checkout.alreadyCheckedoutError")); //$NON-NLS-1$
		final IVersioningService versioningService = getVersioningService();
		final List<IVersionable<?>> list = new ArrayList<IVersionable<?>>();
		list.add(v);
		final IVersioningOperationContext context = versioningService.createVersioningContext(
				VersioningOperation.CHECKOUT, list);
		validate(context);
		getVersioningService().checkOut(new NullProgressMonitor(), context);
	}

	@Override
	public String getDescription() {
		return VCSMessages.getString("hint.checkout.description"); //$NON-NLS-1$
	}

	private IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}

	private IStatus validate(IVersioningOperationContext context) {
		final IStatus status = getVersioningService().validate(context);
		if (!status.isOK()) {
			if (status.getException() != null) {
				throw new ErrorException(status.getMessage(), status.getException());
			} else {
				throw new CancelException();
			}
		}
		return status;
	}
}
