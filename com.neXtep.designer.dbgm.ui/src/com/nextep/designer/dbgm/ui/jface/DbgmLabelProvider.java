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
package com.nextep.designer.dbgm.ui.jface;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.designer.helper.DatatypeHelper;
import com.nextep.designer.vcs.ui.jface.VersionableNewLabelProvider;

public class DbgmLabelProvider extends VersionableNewLabelProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof IBasicColumn) {
			return DatatypeHelper.getDatatypeIcon(((IBasicColumn) element).getDatatype());
		}
		return super.getImage(element);
	}

	@Override
	public StyledString getStyledText(Object element) {
		final StyledString s = super.getStyledText(element);
		if (element instanceof IBasicColumn) {
			final IBasicColumn col = (IBasicColumn) element;
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
			s.append(col.isNotNull() ? " NOT NULL" : "", StyledString.QUALIFIER_STYLER); //$NON-NLS-1$//$NON-NLS-2$
		}
		return s;

	}
}
