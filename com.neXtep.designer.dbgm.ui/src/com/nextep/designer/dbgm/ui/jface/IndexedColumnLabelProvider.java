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

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.helper.DatatypeHelper;
import com.nextep.designer.vcs.ui.jface.FontStyler;
import com.nextep.designer.vcs.ui.jface.VersionableNewLabelProvider;

/**
 * @author Christophe Fondacci
 */
public class IndexedColumnLabelProvider extends VersionableNewLabelProvider {

	private IIndex index;
	private final Styler ITALIC_STYLER = new FontStyler(null, FontFactory.FONT_ITALIC);

	public IndexedColumnLabelProvider(IIndex index) {
		this.index = index;
	}

	public void setIndex(IIndex index) {
		this.index = index;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IBasicColumn) {
			final IBasicColumn col = (IBasicColumn) element;
			final String function = index.getFunction(col.getReference());
			if (function != null) {
				return DBGMImages.ICON_FUNC;
			}
			return DatatypeHelper.getDatatypeIcon(((IBasicColumn) element).getDatatype());
		}
		return super.getImage(element);
	}

	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof IBasicColumn) {
			StyledString s = new StyledString();
			final IBasicColumn col = (IBasicColumn) element;
			// Appending function if defined
			final String function = index.getFunction(col.getReference());
			if (function != null) {
				s.append(function, ITALIC_STYLER);
			} else {
				s.append(col.getName());
				if (col.getParent() != null) {
					final IKeyConstraint pk = DBGMHelper.getPrimaryKey(col.getParent());
					if (pk != null) {
						if (pk.getConstrainedColumnsRef().contains(col.getReference())) {
							s.setStyle(0, s.getString().length(), BOLD_STYLER);
						}
					}
				}

				final String datatype = DBGMHelper.getDatatypeLabel(((IBasicColumn) element)
						.getDatatype());
				s.append(" : " + datatype, StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
			}
			return s;
		} else {
			return super.getStyledText(element);
		}

	}
}
