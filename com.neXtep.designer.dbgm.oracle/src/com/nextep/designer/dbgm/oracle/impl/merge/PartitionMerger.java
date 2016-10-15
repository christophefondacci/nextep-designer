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
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

public abstract class PartitionMerger extends MergerWithChildCollections {

	public static final String ATTR_POSITION 	= "Position";
	public static final String ATTR_PHYSICALS	= "Physical attributes";
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		IPartition part = (IPartition)target;
		if(part==null) return null;
		// Filling name
		fillName(result, part);
		if(part.getName()==null) return null;
		// Filling partition properties
		final String position = getStringProposal(ATTR_POSITION, result);
		if(position != null) {
			part.setPosition(Integer.valueOf(position));
		}
		// Building physical properties
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IPartitionPhysicalProperties.TYPE_ID),getMergeStrategy().getComparisonScope());
		if(m != null && result.getSubItems(ATTR_PHYSICALS)!=null) {
			IPartitionPhysicalProperties props = (IPartitionPhysicalProperties)m.buildMergedObject(result.getSubItems(ATTR_PHYSICALS).iterator().next(), activity);
			part.setPhysicalProperties(props);
		}
		// Our partition is filled
		return part;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = new ComparisonResult(source,target,getMergeStrategy().getComparisonScope());
		IPartition src = (IPartition)source;
		IPartition tgt = (IPartition)target;
		// Name comparison
		this.compareName(result,src,tgt);
		// Partition properties comparison
		result.addSubItem(new ComparisonAttribute(ATTR_POSITION,src == null ? null : String.valueOf(src.getPosition()), tgt == null ? null : String.valueOf(tgt.getPosition()),ComparisonScope.REPOSITORY));
		// Partition comparison
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IPartitionPhysicalProperties.TYPE_ID), getMergeStrategy().getComparisonScope());
		if(m!=null) {
			result.addSubItem(ATTR_PHYSICALS,m.compare(src==null ? null : src.getPhysicalProperties(), tgt == null ? null : tgt.getPhysicalProperties()));
		}
		// Returning
		return result;
	}

	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}
	@Override
	public boolean isVersionable() {
		return false;
	}
	@Override
	protected abstract Object createTargetObject(IComparisonItem result,
			IActivity mergeActivity);
}
