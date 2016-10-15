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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.VCSImages;

public class VersionHistoryLabelProvider implements ITableLabelProvider {

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //$NON-NLS-1$

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			return VCSImages.ICON_VERSIONTREE;
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		IVersionInfo v = null;
		if (element instanceof IVersionInfo) {
			v = (IVersionInfo) element;
		} else if (element instanceof IVersionable) {
			v = ((IVersionable<?>) element).getVersion();
		}
		if (v == null || v.getMajorRelease() == -1) {
			if (columnIndex == 0) {
				return "Scratch";
			} else {
				return "-";
			}
		}
		switch (columnIndex) {
		case 0:
			return v.getLabel();
		case 1:
			return v.getBranch().getName();
		case 2:
			return v.getStatus().getLabel();
		case 3:
			return dateFormat.format(v.getCreationDate());
		case 4:
			return v.getUser().getName();
		case 5:
			if (v.getActivity() != null) {
				return v.getActivity().getName();
			} else {
				return ""; ////$NON-NLS-1$
			}
		}
		return null;
	}

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

}
