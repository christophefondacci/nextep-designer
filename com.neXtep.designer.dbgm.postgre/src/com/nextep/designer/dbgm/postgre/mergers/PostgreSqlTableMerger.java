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
package com.nextep.designer.dbgm.postgre.mergers;

import java.util.Collections;
import java.util.List;

import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.dbgm.mergers.TableMerger;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.ICheckConstraintContainer;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PostgreSqlTableMerger extends TableMerger<IPostgreSqlTable> {

	public static final String CATEGORY_INHERITANCE = "Inherited tables"; //$NON-NLS-1$
	public static final String CATEGORY_CHECKS = "Check constraints"; //$NON-NLS-1$

	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		final IComparisonItem result = super.doCompare(source, target);

		if (isClass(ICheckConstraintContainer.class, source, target)) {
			final ICheckConstraintContainer src = (ICheckConstraintContainer) source;
			final ICheckConstraintContainer tgt = (ICheckConstraintContainer) target;
			// Comparing check constraints
			listCompare(CATEGORY_CHECKS, result,
					src == null ? Collections.EMPTY_LIST : src.getCheckConstraints(),
					tgt == null ? Collections.EMPTY_LIST : tgt.getCheckConstraints());
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillSpecificComparison(IComparisonItem result, IPostgreSqlTable src,
			IPostgreSqlTable tgt) {
		// Comparing PostgreSql-specific attributes

		// Comparing inherited tables
		listCompare(CATEGORY_INHERITANCE, result,
				src == null ? Collections.EMPTY_LIST : src.getInheritances(),
				tgt == null ? Collections.EMPTY_LIST : tgt.getInheritances(), false);
	}

	@Override
	protected Class<? extends IReferenceable> getSpecificClass() {
		return IPostgreSqlTable.class;
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		final IPostgreSqlTable table = (IPostgreSqlTable) super
				.fillObject(target, result, activity);

		// If null, we are in a removal case
		if (table == null) {
			return null;
		}

		// Building check constraints
		List<?> cols = getMergedList(CATEGORY_CHECKS, result, activity);

		// Adding merged check constraints
		for (Object r : cols) {
			ICheckConstraint c = (ICheckConstraint) r;
			c.setConstrainedTable(table);
			// Bugfix: re-setting collection index
			save(c);
			table.addCheckConstraint(c);
		}

		// Filling inherited tables
		List<?> colRefs = getMergedList(CATEGORY_INHERITANCE, result, activity);
		for (Object o : colRefs) {
			if (o != null) {
				table.addInheritanceRef((IReference) o);
			}
		}
		return table;
	}

}
