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
package com.nextep.designer.sqlgen.mysql.strategies;

import java.util.Collection;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.sqlgen.strategies.UniqueKeyGenerateDropsStrategy;

/**
 * A strategy which drops dependent foreign keys when this unique key needs to be dropped.
 * 
 * @author Christophe Fondacci
 */
public class MySqlUniqueKeyGenerateDropsStrategy extends UniqueKeyGenerateDropsStrategy {

	@Override
	protected void augmentForeignKeys(IKeyConstraint droppedUk,
			Collection<ForeignKeyConstraint> repoFks, Collection<ForeignKeyConstraint> dbFks) {
		// Checking foreign key constraints enforced by this uk
		final IBasicTable t = droppedUk.getConstrainedTable();
		if (t != null) {
			for (IKeyConstraint key : t.getConstraints()) {
				switch (key.getConstraintType()) {
				case FOREIGN:
					if (key.getEnforcingIndex().contains(droppedUk)) {
						dbFks.add((ForeignKeyConstraint) key);
						repoFks.add((ForeignKeyConstraint) key);
					}
				}
			}
		}
	}
}
