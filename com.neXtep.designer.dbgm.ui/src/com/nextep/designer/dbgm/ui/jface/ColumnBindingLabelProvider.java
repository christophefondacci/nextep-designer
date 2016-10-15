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
package com.nextep.designer.dbgm.ui.jface;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.dbgm.ui.model.IColumnBinding;
import com.nextep.designer.ui.UIImages;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * @author Christophe Fondacci
 */
public class ColumnBindingLabelProvider implements ITableLabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {

	}

	@Override
	public void dispose() {

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {

	}

	@Override
	public Image getColumnImage(Object element, int columnIndex) {
		final IColumnBinding binding = (IColumnBinding) element;
		final Image columnIcon = ImageFactory.getImage(IElementType.getInstance(
				IBasicColumn.TYPE_ID).getTinyIcon());
		switch (columnIndex) {
		case 0:
			if (binding.getColumn() == null) {
				return ImageFactory.ICON_ERROR_TINY;
			} else {
				return columnIcon;
			}
		case 1:
			if (binding.getAssociatedColumn() == null) {
				return ImageFactory.ICON_ERROR_TINY;
			} else {
				return columnIcon;
			}
		case 2:
			final IMarker marker = binding.getMarker();
			if (marker == null) {
				return null;
			} else {
				switch (marker.getMarkerType()) {
				case WARNING:
					return UIImages.ICON_MARKER_WARNING;
				case ERROR:
					return UIImages.ICON_MARKER_ERROR;
				}
				return null;
			}
		}
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex) {
		final IColumnBinding binding = (IColumnBinding) element;
		switch (columnIndex) {
		case 0:
			final IBasicColumn src = binding.getColumn();
			if (src != null) {
				return src.getName();
			} else {
				return "[No column defined]";
			}
		case 1:
			final IBasicColumn tgt = binding.getAssociatedColumn();
			if (tgt != null) {
				return tgt.getName();
			} else {
				return "";
			}
		case 2:
			final IMarker marker = binding.getMarker();
			if (marker != null) {
				return marker.getMessage();
			} else {
				return "";
			}
		}
		return null;
	}
}
