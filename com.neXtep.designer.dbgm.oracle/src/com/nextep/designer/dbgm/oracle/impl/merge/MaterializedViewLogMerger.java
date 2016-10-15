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
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

public class MaterializedViewLogMerger extends Merger {

	public static final String ATTR_TABLE = "Table";
	public static final String ATTR_PK = "Include PK";
	public static final String ATTR_ROWID = "Include rowIDs";
	public static final String ATTR_SEQUENCE = "Include sequence";
	public static final String ATTR_NEWVALS = "Include new values";
	public static final String ATTR_PROPS = "Physical properties";
	
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		IMaterializedViewLog tgt = (IMaterializedViewLog)target;
		// No "real" name attribute here, we check this to know whether the merge removed the object
		if(getStringProposal(ATTR_NAME, result)==null) {
			return null;
		}
		// Filling table reference
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		IReference r = (IReference)m.buildMergedObject(result.getSubItems(ATTR_TABLE).iterator().next(), activity);
		tgt.setTableReference(r);
		
		tgt.setPrimaryKey(Boolean.valueOf(getStringProposal(ATTR_PK, result)));
		tgt.setRowId(Boolean.valueOf(getStringProposal(ATTR_ROWID, result)));
		tgt.setSequence(Boolean.valueOf(getStringProposal(ATTR_SEQUENCE, result)));
		tgt.setIncludingNewValues(Boolean.valueOf(getStringProposal(ATTR_NEWVALS, result)));
		
		return tgt;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		final IMaterializedViewLog src= (IMaterializedViewLog)source;
		final IMaterializedViewLog tgt = (IMaterializedViewLog)target;
		
		IComparisonItem result = new ComparisonResult(source,target,getMergeStrategy().getComparisonScope());
		compareName(result, src, tgt);
		// Comparing log table
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),getMergeStrategy().getComparisonScope());
		result.addSubItem(ATTR_TABLE,m.compare(src == null ? null : src.getTableReference(), tgt == null ? null : tgt.getTableReference()));
		
		
		result.addSubItem(new ComparisonAttribute(ATTR_PK,src==null?null:String.valueOf(src.isPrimaryKey()),tgt==null?null:String.valueOf(tgt.isPrimaryKey())));
		result.addSubItem(new ComparisonAttribute(ATTR_ROWID,src==null?null:String.valueOf(src.isRowId()),tgt==null?null:String.valueOf(tgt.isRowId())));
		result.addSubItem(new ComparisonAttribute(ATTR_SEQUENCE,src==null?null:String.valueOf(src.isSequence()),tgt==null?null:String.valueOf(tgt.isSequence())));
		result.addSubItem(new ComparisonAttribute(ATTR_NEWVALS,src==null?null:String.valueOf(src.isIncludingNewValues()),tgt==null?null:String.valueOf(tgt.isIncludingNewValues())));

		return result;
	}

}
