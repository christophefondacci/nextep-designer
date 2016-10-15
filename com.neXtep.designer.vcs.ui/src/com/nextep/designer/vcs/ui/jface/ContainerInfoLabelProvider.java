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

import java.text.SimpleDateFormat;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IVersionContainer;

public class ContainerInfoLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			return ImageFactory.getImage(IElementType.getInstance(IVersionContainer.TYPE_ID)
					.getIcon());
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		ContainerInfo info = (ContainerInfo) element;
		switch (columnIndex) {
		case 0:
			return info.getName();
		case 1:
			return info.getRelease().getLabel();
		case 2:
			return info.getDBVendor() == null ? "Undefined" : info.getDBVendor().toString();
		case 3:
			return info.getRelease().getBranch().getName();
		case 4:
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(info.getRelease()
					.getUpdateDate());
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
