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
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IVersioningOperationContext;

public class VersionableVersionStatusLabelProvider implements ITableLabelProvider {

	private IVersioningOperationContext context;

	public VersionableVersionStatusLabelProvider() {
		context = null;
	}

	public VersionableVersionStatusLabelProvider(IVersioningOperationContext context) {
		this.context = context;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof ITypedObject) {
			if (columnIndex == 0) {
				return ImageFactory.getImage(((ITypedObject) element).getType().getIcon());
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IVersionable<?>) {
			final IVersionable<?> v = (IVersionable<?>) element;
			IVersionInfo version = v.getVersion();
			if (context != null) {
				version = context.getTargetVersionInfo(v);
			}
			switch (columnIndex) {
			case 0:
				return v.getName();
			case 1:
				return version.getLabel();
			case 2:
				return version.getBranch().getName();
			case 3:
				return DateFormat.getDateTimeInstance().format(v.getVersion().getCreationDate());
			case 4:
				return v.getVersion().getUser().getName();
			case 5:
				final IActivity activity = version.getActivity();
				if (activity != null) {
					return activity.getName();
				} else {
					return ""; //$NON-NLS-1$
				}
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
