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

import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.designer.core.model.IMarkerHint;
import com.nextep.designer.util.Assert;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.services.IVersioningService;

public class UndoCheckOutHint implements IMarkerHint {

	@Override
	public void execute(Object element) {
		Assert.instanceOf(element, IVersionable.class, VCSMessages
				.getString("hint.undoCheckOut.nonVersionableError")); //$NON-NLS-1$
		final IVersionable<?> v = (IVersionable<?>) element;
		getVersioningService().undoCheckOut(new NullProgressMonitor(), v);
	}

	@Override
	public String getDescription() {
		return VCSMessages.getString("hint.undoCheckOut.description"); //$NON-NLS-1$
	}

	private IVersioningService getVersioningService() {
		return VCSPlugin.getService(IVersioningService.class);
	}
}
