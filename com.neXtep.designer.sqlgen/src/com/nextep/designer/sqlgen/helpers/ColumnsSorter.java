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
package com.nextep.designer.sqlgen.helpers;

import java.util.Comparator;
import java.util.TreeSet;
import org.apache.commons.collections.keyvalue.MultiKey;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;

/**
 * Convenient class to sort the columns of a table according to their specified position.
 * 
 * @author Bruno Gautier
 */
public final class ColumnsSorter {

	private TreeSet<MultiKey> columns = new TreeSet<MultiKey>(new Comparator<MultiKey>() {

		@Override
		public int compare(MultiKey o1, MultiKey o2) {
			return ((Integer) o1.getKey(1) > (Integer) o2.getKey(1) ? 1 : -1);
		}
	});

	public void addColumn(IBasicColumn column, short position) {
		columns.add(new MultiKey(column, (int) position));
	}

	public void addColumn(IBasicColumn column, int position) {
		columns.add(new MultiKey(column, position));
	}

	public IBasicColumn[] getColumnsSortedArray() {
		IBasicColumn[] columnsArray = new IBasicColumn[columns.size()];

		int pos = 0;
		for (MultiKey key : columns) {
			columnsArray[pos++] = (IBasicColumn) key.getKey(0);
		}

		return columnsArray;
	}

}
