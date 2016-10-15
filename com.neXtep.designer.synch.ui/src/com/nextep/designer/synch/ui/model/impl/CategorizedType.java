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
package com.nextep.designer.synch.ui.model.impl;

import java.util.Collection;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.synch.ui.model.ICategorizedType;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;

public class CategorizedType implements ICategorizedType {

	private IElementType type;
	private Collection<IComparisonItem> comparisonItems;
	private int changedItems;

	public CategorizedType(IElementType type, Collection<IComparisonItem> items) {
		this.type = type;
		this.comparisonItems = items;
		changedItems = 0;
		if (items != null) {
			for (IComparisonItem i : items) {
				if (i.getDifferenceType() != DifferenceType.EQUALS) {
					changedItems++;
				}
			}
		}

	}

	@Override
	public int getChangedItems() {
		return changedItems;
	}

	@Override
	public Collection<IComparisonItem> getItems() {
		return comparisonItems;
	}

	@Override
	public IElementType getType() {
		return type;
	}

}
