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
package com.nextep.designer.dbgm.oracle.impl.merge;

import java.util.Collections;
import java.util.List;

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.designer.dbgm.mergers.TableMerger;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.dbgm.model.ICheckConstraintContainer;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * @author Christophe Fondacci
 */
public class OracleTableMerger extends TableMerger {

	public static final String CATEGORY_CHECKS = "Check constraints";

	/**
	 * @see com.nextep.designer.dbgm.mergers.TableMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		// Building basic table comparison
		IComparisonItem result = super.doCompare(source, target);

		// Adding Oracle-specific attributes
		if (source instanceof ICheckConstraintContainer
				&& target instanceof ICheckConstraintContainer) {
			ICheckConstraintContainer src = (ICheckConstraintContainer) source;
			ICheckConstraintContainer tgt = (ICheckConstraintContainer) target;
			listCompare(CATEGORY_CHECKS, result,
					src == null ? Collections.EMPTY_LIST : src.getCheckConstraints(),
					tgt == null ? Collections.EMPTY_LIST : tgt.getCheckConstraints());
		}
		return result;
	}

	/**
	 * @see com.nextep.designer.dbgm.mergers.TableMerger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IOracleTable t = (IOracleTable) super.fillObject(target, result, activity);
		// Checking nullity
		if (t == null)
			return null;
		// Building physical properties
		IMerger m = MergerFactory.getMerger(IElementType
				.getInstance(ITablePhysicalProperties.TYPE_ID), getMergeStrategy()
				.getComparisonScope());
		if (m != null && result.getSubItems(ATTR_PHYSICAL) != null) {
			IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) m
					.buildMergedObject(result.getSubItems(ATTR_PHYSICAL).iterator().next(),
							activity);
			t.setPhysicalProperties(props);
		}
		// Building check constraints
		List<?> cols = getMergedList(CATEGORY_CHECKS, result, activity);

		// Adding merged columns
		for (Object r : cols) {
			ICheckConstraint c = (ICheckConstraint) r;
			c.setConstrainedTable(t);
			// Bugfix: re-setting column index
			save(c);
			t.addCheckConstraint(c);
		}
		// returning our Oracle table
		return t;
	}

}
