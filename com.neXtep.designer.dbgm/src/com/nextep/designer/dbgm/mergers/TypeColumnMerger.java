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

import com.nextep.datadesigner.dbgm.impl.TypeColumn;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * This merger is not declared and is only internally
 * used by the UserType Merger.
 * 
 * @author Christophe Fondacci
 *
 */
public class TypeColumnMerger extends Merger {

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		ITypeColumn tgt = (ITypeColumn)target;
		// Filling name
		fillName(result, tgt);
		// Checking nullity
		if(tgt.getName()==null) {
			return null;
		}
		// Filling datatype
		tgt.setDatatype(DatatypeMergeHelper.buildDataTypeFromComparison(result));
		// Object built
		return tgt;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		ITypeColumn src = (ITypeColumn)source;
		ITypeColumn tgt = (ITypeColumn)target;
		
		// Initializing comparison result
		IComparisonItem result = new ComparisonResult(src,tgt,getMergeStrategy().getComparisonScope());
		// Comparing names
		compareName(result, src, tgt);
		DatatypeMergeHelper.addDatatypeComparison(result, src != null ? src.getDatatype() : null, tgt != null ? tgt.getDatatype() : null, getMergeStrategy().getComparisonScope());
		// Returning result
		return result;
	}
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#copyWhenUnchanged()
	 */
	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#isVersionable()
	 */
	@Override
	public boolean isVersionable() {
		return false;
	}
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#createTargetObject(com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object createTargetObject(IComparisonItem result,
			IActivity mergeActivity) {
		return new TypeColumn();
	}
}
