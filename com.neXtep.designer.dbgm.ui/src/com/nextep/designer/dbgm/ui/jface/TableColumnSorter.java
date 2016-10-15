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

import java.text.Collator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;

public class TableColumnSorter extends ViewerSorter {

	public TableColumnSorter() {
	}

	public TableColumnSorter(Collator collator) {
		super(collator);
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof IBasicColumn) || !(e2 instanceof IBasicColumn)) {
			return super.compare(viewer, e1, e2);
		} else {
			final IBasicColumn c1 = (IBasicColumn) e1;
			final IBasicColumn c2 = (IBasicColumn) e2;
			return c1.getRank() - c2.getRank();
		}
	}
}
