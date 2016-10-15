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
package com.nextep.designer.synch.ui.jface;

import java.text.Collator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.designer.vcs.model.IComparisonItem;


public class ComparisonItemSorter extends ViewerSorter {

	public ComparisonItemSorter() {
	}

	public ComparisonItemSorter(Collator collator) {
		super(collator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if(e1 instanceof IComparisonItem && e2 instanceof IComparisonItem) {
			final IComparisonItem first = (IComparisonItem)e1;
			final IComparisonItem other = (IComparisonItem)e2;
			return getComparator().compare(getName(first), getName(other));
		}
		return super.compare(viewer, e1, e2);
	}
	
	private String getName(IComparisonItem item) {
		return item.getSource() != null ? ((INamedObject)item.getSource()).getName() : ((INamedObject)item.getTarget()).getName();
	}
}
