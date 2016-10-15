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
/**
 *
 */
package com.nextep.datadesigner.vcs.impl;

import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * @author Christophe Fondacci
 */
public class ReferenceTransitiveMerger extends Merger {

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem, com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		return target;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#createTargetObject(com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		// UID id = new UID(Long.valueOf(getStringProposal(ATTR_UID, result)));
		if (result.getMergeInfo().getMergeProposal() != null) {
			IReference ref = (IReference) result.getMergeInfo().getMergeProposal();
			// Depending on the scope:
			// - We want the proposal when importing from database (to avoid externals)
			// - We want a sandboxed reference when merging in repository
			if (getMergeStrategy().getComparisonScope() == ComparisonScope.DB_TO_REPOSITORY) {
				return ref;
			} else {
				return CorePlugin.getIdentifiableDao().load(Reference.class, ref.getReferenceId(),
						HibernateUtil.getInstance().getSandBoxSession(), false);
			}
		} else {
			return null;
		}
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		Reference src = (Reference) source;
		Reference tgt = (Reference) target;
		ComparisonResult r = new ComparisonResult(src, tgt, getMergeStrategy().getComparisonScope());
		// if(getMergeStrategy().getComparisonScope().isCompatible(ComparisonScope.DB_TO_REPOSITORY))
		// {
		if (src != null && tgt != null && src.getInstance() != null && tgt.getInstance() != null
				&& src.getInstance().getClass() == tgt.getInstance().getClass()
				&& src.getType().getInterface().isAssignableFrom(src.getInstance().getClass())) {
			IMerger m = MergerFactory.getMerger(src.getType(), getMergeStrategy()
					.getComparisonScope());
			if (m != null) {
				r.setDifferenceType(m.compare(src.getInstance(), tgt.getInstance())
						.getDifferenceType());
			}
		}
		// }
		return r;
	}

}
