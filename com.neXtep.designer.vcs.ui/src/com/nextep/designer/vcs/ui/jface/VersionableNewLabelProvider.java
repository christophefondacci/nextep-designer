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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.model.ITypedNode;

public class VersionableNewLabelProvider extends CellLabelProvider implements ILabelProvider,
		IStyledLabelProvider {

	protected Styler CHECKIN_STYLER = new FontStyler(FontFactory.CHECKIN_COLOR, null);
	protected Styler BOLD_STYLER = new FontStyler(null, FontFactory.FONT_BOLD);

	@Override
	public void update(ViewerCell cell) {

	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ITypedObject) {
			return ImageFactory.getImage(((ITypedObject) element).getType().getIcon());
		} else if (element instanceof ITypedNode) {
			return ImageFactory.getImage(((ITypedNode) element).getNodeType().getIcon());
		}
		return null;
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public StyledString getStyledText(Object element) {
		StyledString str = new StyledString();
		if (element instanceof IVersionable<?>) {
			Styler nameStyler = null;
			final IVersionable<?> v = (IVersionable<?>) element;
			if (v.getVersion() != null) {
				switch (v.getVersion().getStatus()) {
				case CHECKED_IN:
					nameStyler = CHECKIN_STYLER;
					break;
				default:
					nameStyler = null;
					break;
				}
			}
			str.append(((INamedObject) element).getName(), nameStyler);
			str.append(" - " + ((IVersionable<?>) element).getVersion().getLabel(),
					StyledString.DECORATIONS_STYLER);
		} else if (element instanceof INamedObject) {
			str.append(((INamedObject) element).getName());
		} else if (element instanceof ITypedNode) {
			final ITypedNode node = (ITypedNode) element;
			str.append(node.getName());
			str.append(" (" + node.getChildren().size() + ")", StyledString.COUNTER_STYLER);
		}
		return str;
	}
}
