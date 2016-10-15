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
package com.nextep.designer.sqlclient.ui.model.impl;

import java.util.LinkedList;
import java.util.List;
import com.nextep.designer.sqlclient.ui.model.ISQLRowResult;

/**
 * Default implementation of a {@link ISQLRowResult} holding SQL row values. This implementation
 * will consider new instantiated rows as not pending and requires explicit set of the pending flag
 * if needed.
 * 
 * @author Christophe Fondacci
 */
public class SQLRowResult implements ISQLRowResult {

	private List<Object> values = new LinkedList<Object>();
	private List<Integer> columnTypes = new LinkedList<Integer>();
	private boolean pending = false;

	@Override
	public List<Object> getValues() {
		return values;
	}

	/**
	 * Adds a value to this row
	 * 
	 * @param o
	 */
	public void addValue(Object o) {
		values.add(o);
	}

	@Override
	public void setValue(int index, Object value) {
		values.remove(index);
		values.add(index, value);
	}

	@Override
	public void setPending(boolean pending) {
		this.pending = pending;
	}

	@Override
	public boolean isPending() {
		return pending;
	}

	@Override
	public List<Integer> getSqlTypes() {
		return columnTypes;
	}

	public void addSqlType(int sqlType) {
		columnTypes.add(sqlType);
	}

}
