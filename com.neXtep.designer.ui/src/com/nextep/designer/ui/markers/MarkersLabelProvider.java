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
package com.nextep.designer.ui.markers;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.ui.UIImages;

public class MarkersLabelProvider implements ITableLabelProvider {

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			switch (((IMarker) element).getMarkerType()) {
			case ERROR:
				return UIImages.ICON_MARKER_ERROR;
			case WARNING:
				return UIImages.ICON_MARKER_WARNING;
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		IMarker m = (IMarker) element;
		switch (columnIndex) {
		case 0:
			return m.getMessage();
			// case 1:
			// return m.getMarkerType().name();
		case 1:
			if (m.getRelatedObject() instanceof INamedObject) {
				final String name = ((INamedObject) m.getRelatedObject()).getName();
				return name == null ? "" : name;
			}
			return "";
		case 2:
			if (m.getRelatedObject() instanceof ITypedObject) {
				return ((ITypedObject) m.getRelatedObject()).getType().getName();
			}
			break;
		case 3:
			final Object line = (Object) m.getAttribute(IMarker.ATTR_LINE);
			if (line != null) {
				return line.toString();
			}
		case 4:
			final Object context = (Object) m.getAttribute(IMarker.ATTR_CONTEXT);
			if (context != null) {
				return context.toString();
			}
		}
		return "";
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
