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
package com.nextep.designer.dbgm.oracle.ui.jface;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.dbgm.ui.model.IColumnBinding;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.vcs.ui.jface.FontStyler;

/**
 * @author Christophe Fondacci
 */
public class ClusteredTableColumnsLabelProvider extends CellLabelProvider implements
		ILabelProvider, IStyledLabelProvider {

	private IStyledLabelProvider rootProvider = new DbgmLabelProvider();
	private Styler ARROW_STYLER = new FontStyler(FontFactory.DDL_COLOR, FontFactory.FONT_BOLD);
	private Styler ITALIC_STYLER = new FontStyler(FontFactory.FUNC_COLOR, FontFactory.FONT_ITALIC);

	@Override
	public StyledString getStyledText(Object element) {
		final StyledString s = new StyledString();
		final IColumnBinding binding = (IColumnBinding) element;

		s.append(rootProvider.getStyledText(binding.getColumn()));
		if (binding.getAssociatedColumn() == null) {
			s.append(" : Unmapped", ITALIC_STYLER);
		} else {
			s.append(" -> ", ARROW_STYLER);
			s.append(rootProvider.getStyledText(binding.getAssociatedColumn()));
		}
		return s;
	}

	@Override
	public Image getImage(Object element) {
		return ImageFactory.getImage(IElementType.getInstance(IBasicColumn.TYPE_ID).getIcon());
	}

	@Override
	public String getText(Object element) {
		return null;
	}

	@Override
	public void update(ViewerCell cell) {

	}

}
