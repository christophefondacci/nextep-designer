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
package com.nextep.designer.vcs.ui.jface;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.UserRight;
import com.nextep.designer.vcs.ui.VCSImages;

public class UsersLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		final IRepositoryUser user = (IRepositoryUser) element;
		if (columnIndex == 0) {
			if (user.isEnabled()) {
				return VCSImages.ICON_USER_TINY;
			} else {
				return VCSImages.ICON_USER_DIS_TINY;
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IRepositoryUser) {
			final IRepositoryUser user = (IRepositoryUser) element;
			switch (columnIndex) {
			case 0:
				return user.getLogin();
			case 1:
				return user.getName();
			case 2:
				return user.getUserRights() != null
						&& user.getUserRights().contains(UserRight.ADMIN) ? "X" : "";
			}
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

}
