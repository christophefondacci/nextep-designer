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

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.designer.dbgm.oracle.impl.OracleClusteredTable;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

public class OracleClusteredTableMerger extends MergerWithChildCollections {

	public static final String ATTR_TABLEREF = "Table reference";
	public static final String ATTR_COLPREFIX ="Column mappings";
	
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		final IOracleClusteredTable t = (IOracleClusteredTable)target;
		
		// Filling clustered table reference
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		IReference r = (IReference)m.buildMergedObject(result.getSubItems(ATTR_TABLEREF).iterator().next(), activity);
		t.setTableReference(r);
		if(r==null) return null;
		
		// Filling mappings
		List<?> mappings = getMergedList(ATTR_COLPREFIX, result, activity);
		for(Object map : mappings) {
			RefMapping refMap = (RefMapping)map;
			t.setColumnReferenceMapping(refMap.getClusterCol(), refMap.getTableCol());
		}
		
		// Returning filled object
		return t;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		final IOracleClusteredTable src = (IOracleClusteredTable)source;
		final IOracleClusteredTable tgt = (IOracleClusteredTable)target;
		
		// Comparing indexed table
		IComparisonItem result = new ComparisonResult(source,target,getMergeStrategy().getComparisonScope());
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		result.addSubItem(ATTR_TABLEREF,m.compare(src == null ? null : src.getTableReference(), tgt == null ? null : tgt.getTableReference()));
		
		// Building an ordered cluster column mapping list
		List<RefMapping> srcMappings = new ArrayList<RefMapping>();
		if(src!=null) {
			final Map<IReference,IReference> srcMap = new IdentityHashMap<IReference, IReference>(src ==null ? Collections.EMPTY_MAP : src.getColumnMappings());
			for(IReference r : srcMap.keySet()) {
				srcMappings.add(new RefMapping(r,srcMap.get(r)));
			}
		}
		List<RefMapping> tgtMappings = new ArrayList<RefMapping>();
		if(src!=null) {
			final Map<IReference,IReference> tgtMap = new IdentityHashMap<IReference, IReference>(tgt ==null ? Collections.EMPTY_MAP : tgt.getColumnMappings());
			for(IReference r : tgtMap.keySet()) {
				tgtMappings.add(new RefMapping(r,tgtMap.get(r)));
			}
		}
		// Merging mappings collection
		listCompare(ATTR_COLPREFIX, result, srcMappings, tgtMappings);
		// Returning comparison
		return result;
	}
	
	@Override
	protected Object createTargetObject(IComparisonItem result,
			IActivity mergeActivity) {
		return new OracleClusteredTable();
	}

	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}
}
