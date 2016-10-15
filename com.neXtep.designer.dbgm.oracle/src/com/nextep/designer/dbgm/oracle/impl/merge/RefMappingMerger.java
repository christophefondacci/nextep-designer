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

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

public class RefMappingMerger extends Merger {

	public static final String ATTR_SOURCE_REF = "Cluster column ref";
	public static final String ATTR_TARGET_REF = "Table column ref";
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		final RefMapping refMap = (RefMapping)target;
		
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		IReference clusterRef = (IReference)m.buildMergedObject(result.getSubItems(ATTR_SOURCE_REF).iterator().next(), activity);
		IReference tableRef = (IReference)m.buildMergedObject(result.getSubItems(ATTR_SOURCE_REF).iterator().next(), activity);
		if(clusterRef==null) return null;
		
		refMap.setClusterCol(clusterRef);
		refMap.setTableCol(tableRef);
		
		return refMap;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		RefMapping src = (RefMapping)source;
		RefMapping tgt = (RefMapping)target;
		
		IComparisonItem result = new ComparisonResult(src,tgt,getMergeStrategy().getComparisonScope());
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		result.addSubItem(ATTR_SOURCE_REF,m.compare(src == null ? null : src.getClusterCol(), tgt == null ? null : tgt.getClusterCol()));
		result.addSubItem(ATTR_TARGET_REF,m.compare(src == null ? null : src.getTableCol(), tgt == null ? null : tgt.getTableCol()));
		return result;
	}

	@Override
	protected Object createTargetObject(IComparisonItem result,
			IActivity mergeActivity) {
		return new RefMapping(null,null);
	}
	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}
}
