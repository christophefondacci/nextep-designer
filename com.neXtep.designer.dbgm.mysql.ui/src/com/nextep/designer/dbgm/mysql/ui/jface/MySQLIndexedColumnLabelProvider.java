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
package com.nextep.designer.dbgm.mysql.ui.jface;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;
import com.nextep.designer.helper.DatatypeHelper;
import com.nextep.designer.vcs.ui.jface.VersionableNewLabelProvider;

/**
 * @author Christophe Fondacci
 */
public class MySQLIndexedColumnLabelProvider extends VersionableNewLabelProvider {

	private IMySQLIndex index;

	public MySQLIndexedColumnLabelProvider(IMySQLIndex index) {
		this.index = index;
	}

	public void setIndex(IMySQLIndex index) {
		this.index = index;
	}

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

			// Appending prefix length if defined
			if (index != null) {
				final Integer prefixLength = index.getColumnPrefixLength(col.getReference());
				if (prefixLength != null) {
					s.append(" (" + prefixLength.toString() + ")", StyledString.COUNTER_STYLER);
				}
			}
			final String datatype = DBGMHelper.getDatatypeLabel(((IBasicColumn) element)
					.getDatatype());
			s.append(" : " + datatype, StyledString.QUALIFIER_STYLER); //$NON-NLS-1$
		}
		return s;

	}
}
