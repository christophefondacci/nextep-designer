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

import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.dbgm.mergers.DatatypeMergeHelper;
import com.nextep.designer.dbgm.model.CollectionType;
import com.nextep.designer.dbgm.model.IUserCollection;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class UserCollectionMerger extends Merger {

	private static final String ATTR_TYPE = "Collection type"; //$NON-NLS-1$

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IUserCollection tgt = (IUserCollection) target;
		// Filling name
		fillName(result, tgt);
		// Checking nullity
		if (tgt.getName() == null) {
			return null;
		}
		// Filling collection type
		tgt.setCollectionType(CollectionType.valueOf(getStringProposal(ATTR_TYPE, result)));
		// Filling datatype
		IDatatype d = DatatypeMergeHelper.buildDataTypeFromComparison(result);
		tgt.setDatatype(d);
		// Returning the filled object
		return tgt;
	}

	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IUserCollection src = (IUserCollection) source;
		IUserCollection tgt = (IUserCollection) target;

		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		compareName(result, src, tgt);

		// Comparing collection type
		result.addSubItem(new ComparisonAttribute(ATTR_TYPE, src == null ? null : src
				.getCollectionType().name(), tgt == null ? null : tgt.getCollectionType().name()));
		// Comparing datatype
		DatatypeMergeHelper.addDatatypeComparison(result, src == null ? null : src.getDatatype(),
				tgt == null ? null : tgt.getDatatype(), getMergeStrategy().getComparisonScope());
		// Returning the comparison result
		return result;
	}

}
