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

import com.nextep.datadesigner.dbgm.model.IColumnable;

/**
 * @author Christophe Fondacci
 */
public class TableAlias {

	String tableName, tableAlias;
	IColumnable table;

	public TableAlias(String tableName) {
		this.tableName = tableName;
	}

	public void setAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	public void setTable(IColumnable t) {
		this.table = t;
	}

	public IColumnable getTable() {
		return table;
	}

	public String getTableName() {
		return tableName;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TableAlias) {
			TableAlias nc = (TableAlias) obj;
			if (getTableAlias() != null) {
				return getTableAlias().equals(nc.getTableAlias());
			} else if (nc.getTableAlias() != null) {
				return false;
			} else {
				return getTableName().equals(nc.getTableName());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return 1;
	}
}
