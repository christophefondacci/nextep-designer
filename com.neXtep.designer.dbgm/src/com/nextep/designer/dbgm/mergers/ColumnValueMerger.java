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
package com.nextep.designer.dbgm.mergers;

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.dbgm.model.IColumnValue;
import com.nextep.designer.dbgm.model.impl.ColumnValue;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 *
 */
public class ColumnValueMerger extends Merger {

	private static final String ATTR_VALUE = "Value";
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		IColumnValue v = (IColumnValue)target;
		v.setValue(getStringProposal(ATTR_VALUE, result)); 
		return v;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = new ComparisonResult(source,target,getMergeStrategy().getComparisonScope());
		IColumnValue sourceValue = (IColumnValue)source;
		IColumnValue targetValue = (IColumnValue)target;
		
		result.addSubItem(new ComparisonAttribute(ATTR_VALUE,sourceValue==null?null:sourceValue.getStringValue(), targetValue==null?null:targetValue.getStringValue()));
		// Returning
		return result;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#createTargetObject(com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object createTargetObject(IComparisonItem result,
			IActivity mergeActivity) {
		return new ColumnValue();
	}
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#copyWhenUnchanged()
	 */
	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}
	
}
