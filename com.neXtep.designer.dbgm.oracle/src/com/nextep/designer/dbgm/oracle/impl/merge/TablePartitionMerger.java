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
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.oracle.impl.TablePartition;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

public class TablePartitionMerger extends PartitionMerger {

	public static final String ATTR_HIGHVALUE 	= "High value";
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		ITablePartition part = (ITablePartition)super.fillObject(target, result, activity);
		if(part==null) return null;
		part.setHighValue(getStringProposal(ATTR_HIGHVALUE, result));
		// Our partition is filled
		return part;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = (IComparisonItem)super.doCompare(source, target);
		ITablePartition src = (ITablePartition)source;
		ITablePartition tgt = (ITablePartition)target;
		// Partition properties comparison
		result.addSubItem(new ComparisonAttribute(ATTR_HIGHVALUE,src == null ? null : src.getHighValue(),tgt == null ? null : tgt.getHighValue()));
		// Returning
		return result;
	}

	@Override
	protected Object createTargetObject(IComparisonItem result,
			IActivity mergeActivity) {
		return new TablePartition();
	}
}
