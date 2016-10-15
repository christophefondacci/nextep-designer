/*******************************************************************************
 * Copyright (c) 2013 neXtep Software and contributors.
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
package com.nextep.designer.dbgm.postgre.model.impl;

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlInheritedTable;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;

/**
 * Default implementation
 * 
 * @author Christophe Fondacci
 */
public class PostgreSqlInheritedTable implements IPostgreSqlInheritedTable, INamedObject {

	private IPostgreSqlTable parent;
	private IPostgreSqlTable table;

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public IPostgreSqlTable getParent() {
		return parent;
	}

	@Override
	public void setParent(IPostgreSqlTable parent) {
		this.parent = parent;
	}

	@Override
	public IPostgreSqlTable getTable() {
		return table;
	}

	@Override
	public void setTable(IPostgreSqlTable table) {
		this.table = table;
	}

	@Override
	public String getName() {
		return parent.getName();
	}

	@Override
	public String getDescription() {
		return parent.getDescription();
	}

	@Override
	public void setDescription(String description) {

	}

	@Override
	public void setName(String name) {

	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IPostgreSqlInheritedTable) {
			final IPostgreSqlInheritedTable otherTab = (IPostgreSqlInheritedTable) obj;
			if (parent != null && table != null) {
				return parent.getReference().equals(otherTab.getParent().getReference())
						&& table.getReference().equals(otherTab.getTable().getReference());
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return table != null ? table.getName().hashCode() : 1;
	}
}
