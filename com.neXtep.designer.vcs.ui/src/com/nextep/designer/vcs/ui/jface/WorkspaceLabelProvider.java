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

import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.helpers.UIHelper;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.ui.VCSImages;

public class WorkspaceLabelProvider implements ITableLabelProvider {

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

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return VCSImages.ICON_VERSION_NAVIGATOR;
		case 1:
			return UIHelper.getVendorIcon(((IWorkspace) element).getDBVendor());
		case 3:
			return ImageFactory.ICON_DELETE;
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IWorkspace) {
			final IWorkspace view = (IWorkspace) element;
			switch (columnIndex) {
			case 0:
				return view.getName();
			case 1:
				if (view.getDBVendor() != null) {
					return view.getDBVendor().toString();
				} else {
					return null;
				}
			case 2:
				return view.getDescription();
			}
		}
		return ""; //$NON-NLS-1$
	}

}
