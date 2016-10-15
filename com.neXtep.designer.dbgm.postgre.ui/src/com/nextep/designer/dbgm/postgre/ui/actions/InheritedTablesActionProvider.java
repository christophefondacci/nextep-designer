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
package com.nextep.designer.dbgm.postgre.ui.actions;

import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlInheritedTable;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;
import com.nextep.designer.ui.model.base.AbstractFormActionProvider;

/**
 * @author Christophe Fondacci
 */
public class InheritedTablesActionProvider extends AbstractFormActionProvider {

	@Override
	public Object add(ITypedObject parent) {
		final IPostgreSqlTable table = (IPostgreSqlTable) parent;
		table.addInheritance(table);
		return table;
	}

	@Override
	public void remove(ITypedObject parent, ITypedObject toRemove) {
		final IPostgreSqlInheritedTable inheritedTable = (IPostgreSqlInheritedTable) toRemove;
		final IPostgreSqlTable parentTable = inheritedTable.getParent();
		final IPostgreSqlTable table = inheritedTable.getTable();

		table.removeInheritance(parentTable);
	}

}
