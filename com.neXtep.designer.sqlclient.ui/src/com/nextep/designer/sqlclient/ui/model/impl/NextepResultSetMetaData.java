/*******************************************************************************
 * Copyright (c) 2012 neXtep Software and contributors.
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
package com.nextep.designer.sqlclient.ui.model.impl;

import java.util.HashMap;
import java.util.Map;
import com.nextep.designer.sqlclient.ui.model.INextepMetadata;

/**
 * @author Christophe Fondacci
 */
public class NextepResultSetMetaData implements INextepMetadata {

	private int columnsCount;
	private Map<Integer, String> columnNames = new HashMap<Integer, String>();
	private Map<Integer, String> tableNames = new HashMap<Integer, String>();
	private Map<Integer, Integer> columnTypes = new HashMap<Integer, Integer>();

	@Override
	public int getColumnCount() {
		return columnsCount;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames.get(column);
	}

	@Override
	public int getColumnType(int column) {
		return columnTypes.get(column);
	}

	@Override
	public String getTableName(int column) {
		return tableNames.get(column);
	}

	public void setTableName(int column, String name) {
		tableNames.put(column, name);
	}

	public void setColumnName(int column, String name) {
		columnNames.put(column, name);
	}

	public void setColumnType(int column, int type) {
		columnTypes.put(column, type);
	}

	public void setColumnCount(int columns) {
		this.columnsCount = columns;
	}
}
