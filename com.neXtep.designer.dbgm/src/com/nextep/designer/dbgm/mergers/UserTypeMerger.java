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

import java.util.Collections;
import java.util.List;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 *
 */
public class UserTypeMerger extends MergerWithChildCollections {

	public static final String ATTR_TYPE_COLUMNS = "Type columns";
	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object, com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		IUserType tgt = (IUserType)target;
		
		//Filling name
		fillName(result, tgt);
		// Checking nullity
		if(tgt.getName()==null) {
			return null;
		}
		// Pre-saving type
		save(tgt);
		// Building type columns
		List<?> columns = getMergedList(ATTR_TYPE_COLUMNS, result, activity);
		for(Object o : columns) {
			tgt.addColumn((ITypeColumn)o);
			save((ITypeColumn)o);
		}
		// Returning the built object
		return tgt;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable, com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IUserType src = (IUserType)source;
		IUserType tgt = (IUserType)target;
		
		IComparisonItem result = new ComparisonResult(src,tgt,getMergeStrategy().getComparisonScope());
		
		// Comparing names
		compareName(result, src, tgt);
		// Comparing columns
		listCompare(ATTR_TYPE_COLUMNS, result, src != null ? src.getColumns() : Collections.EMPTY_LIST, tgt != null ? tgt.getColumns() : Collections.EMPTY_LIST);
		// Returning comparison info
		return result;
	}

}
