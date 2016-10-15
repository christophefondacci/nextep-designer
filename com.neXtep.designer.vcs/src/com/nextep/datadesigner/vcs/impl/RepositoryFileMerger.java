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
package com.nextep.datadesigner.vcs.impl;

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IRepositoryFile;

public class RepositoryFileMerger extends Merger {

	public static final String ATTR_NAME = "Filename";
	public static final String ATTR_SIZE = "File size (kb)";
	public static final String ATTR_ID = "File id";
	@Override
	protected Object fillObject(Object target, IComparisonItem result,
			IActivity activity) {
		IRepositoryFile file = (IRepositoryFile)target;
		file.setName(getStringProposal(ATTR_NAME, result));
		// Checking for file removal
		if(file.getName()==null) {
			return null;
		}
		file.setFileSizeKB(Long.parseLong(getStringProposal(ATTR_SIZE, result)));
		file.setUID(new UID(Long.parseLong(getStringProposal(ATTR_ID, result))));
		return file;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IRepositoryFile src = (IRepositoryFile)source;
		IRepositoryFile tgt = (IRepositoryFile)target;

		ComparisonResult result = new ComparisonResult(src,tgt,getMergeStrategy().getComparisonScope());
		// Only informative
		result.addSubItem(new ComparisonAttribute(ATTR_NAME,src==null ? null : src.getName(),tgt==null?null:tgt.getName()));
		result.addSubItem(new ComparisonAttribute(ATTR_SIZE,src==null ? null : String.valueOf(src.getFileSizeKB()),tgt==null?null:String.valueOf(tgt.getFileSizeKB())));
		// Only useful info since we don't really "merge" files, we only merge their references on datasets
		result.addSubItem(new ComparisonAttribute(ATTR_ID,src==null ? null : String.valueOf(src.getUID().rawId()),tgt==null?null:String.valueOf(tgt.getUID().rawId())));
		return result;
	}

	@Override
	protected Object createTargetObject(IComparisonItem result,
			IActivity mergeActivity) {
		return new RepositoryFile();
	}
	@Override
	protected boolean copyWhenUnchanged() {
		return false;
	}
	@Override
	public boolean isVersionable() {
		return false;
	}
}
