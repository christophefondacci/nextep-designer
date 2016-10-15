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

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

public class CheckConstraintMerger extends Merger {

	public static final String ATTR_CONDITION = "Check condition";

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		ICheckConstraint c = (ICheckConstraint) target;

		fillName(result, c);
		if (c.getName() == null) {
			return null;
		}
		c.setCondition(getStringProposal(ATTR_CONDITION, result));
		return c;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		ICheckConstraint src = (ICheckConstraint) source;
		ICheckConstraint tgt = (ICheckConstraint) target;

		compareName(result, src, tgt);
		// Index type
		result.addSubItem(new ComparisonAttribute(ATTR_CONDITION, src == null ? null : src
				.getCondition(), tgt == null ? null : tgt.getCondition()));

		return result;
	}

	@Override
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		return CorePlugin.getTypedObjectFactory().create(ICheckConstraint.class);
	}

	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}

}
