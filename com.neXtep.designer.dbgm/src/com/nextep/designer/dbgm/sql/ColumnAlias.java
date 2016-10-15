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
package com.nextep.designer.dbgm.sql;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;

/**
 * This class represent the alias of a column referenced in a SQL statement. A column of a SQL
 * statement is defined by the optional table alias prefix, by the column name and by the optional
 * column alias.
 * 
 * @author Christophe Fondacci
 */
public class ColumnAlias {

	private String columnName;
	private String columnAlias;
	private String tableAlias;
	private IBasicColumn column;

	public IBasicColumn getColumn() {
		return column;
	}

	public void setColumn(IBasicColumn column) {
		this.column = column;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public void setColumnAlias(String columnAlias) {
		this.columnAlias = columnAlias;
	}

	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	/**
	 * @return the column
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the selName
	 */
	public String getColumnAlias() {
		return columnAlias;
	}

	/**
	 * @return the tableAlias
	 */
	public String getTableAlias() {
		return tableAlias;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ColumnAlias) {
			ColumnAlias nc = (ColumnAlias) obj;
			if (getColumnAlias() != null) {
				return getColumnAlias().equals(nc.getColumnAlias());
			} else if (nc.getColumnAlias() != null) {
				return false;
			} else if (getColumnName() != null) {
				return getColumnName().equals(nc.getColumnName());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 1;
	}

}
