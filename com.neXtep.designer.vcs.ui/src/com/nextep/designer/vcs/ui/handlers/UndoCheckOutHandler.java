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

import java.util.List;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSUIPlugin;

/**
 * @author Christophe Fondacci
 */
public class UndoCheckOutHandler extends AbstractVersionHandler {

	@Override
	protected void versionActions(List<IVersionable<?>> selectedVersions) {
		if (!checkAndConfirm(selectedVersions)) {
			return;
		}
		VCSUIPlugin.getVersioningUIService().undoCheckOut(null,
				selectedVersions.toArray(new IVersionable<?>[selectedVersions.size()]));
	}

	/**
	 * @see com.nextep.designer.vcs.ui.handlers.CheckInHandler#versionAction(com.nextep.designer.vcs.model.IVersionable)
	 */
	@Override
	protected Object versionAction(IVersionable<?> v) {
		VCSUIPlugin.getVersioningUIService().undoCheckOut(null, v);
		return null;
	}

	@Override
	protected boolean checkAndConfirm(List<IVersionable<?>> selectedVersions) {
		return true;
	}

	@Override
	public boolean isEnabled(IVersionable<?> v) {
		return IVersionStatus.CHECKED_OUT == v.getVersion().getStatus();
	}
}
