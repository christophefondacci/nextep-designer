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

import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.designer.dbgm.mergers.KeyMerger;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.impl.OracleUniqueConstraint;
import com.nextep.designer.dbgm.preferences.PreferenceConstants;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * @author Christophe Fondacci
 */
public class OracleUniqueKeyMerger extends KeyMerger<OracleUniqueConstraint> {

	public static final String ATTR_PHYSICAL = "Physical attributes";

	@Override
	protected void fillSpecificComparison(IComparisonItem result, OracleUniqueConstraint src,
			OracleUniqueConstraint tgt) {
		// Adding Oracle-specific physicals
		// We STOP HERE on non-repository comparison depending on the physicals
		// synchronization flag
		if (getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY
				&& !SQLGenUtil.getPreferenceBool(PreferenceConstants.COMPARE_PHYSICALS)) {
			return;
		}

		IPhysicalProperties srcPty = (src == null ? null : src.getPhysicalProperties());
		IPhysicalProperties tgtPty = (tgt == null ? null : tgt.getPhysicalProperties());
		if (srcPty != null || tgtPty != null) {
			IMerger m = MergerFactory.getMerger(IElementType
					.getInstance(IIndexPhysicalProperties.TYPE_ID), getMergeStrategy()
					.getComparisonScope());
			if (m != null) {
				result.addSubItem(ATTR_PHYSICAL, m.compare(srcPty, tgtPty));
			}
		}
	}

	@Override
	protected Class<? extends IReferenceable> getSpecificClass() {
		return OracleUniqueConstraint.class;
	}

	/**
	 * @see com.nextep.designer.dbgm.mergers.TableMerger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IKeyConstraint key = (IKeyConstraint) super.fillObject(target, result, activity);
		// If null or non unique key we have finished
		if (key == null || key.getType() != IElementType.getInstance(UniqueKeyConstraint.TYPE_ID)) {
			return key;
		}
		// Building physical properties
		IMerger m = MergerFactory.getMerger(IElementType
				.getInstance(IIndexPhysicalProperties.TYPE_ID), getMergeStrategy()
				.getComparisonScope());
		if (m != null && result.getSubItems(ATTR_PHYSICAL) != null) {
			IPhysicalProperties props = (IPhysicalProperties) m.buildMergedObject(result
					.getSubItems(ATTR_PHYSICAL).iterator().next(), activity);
			((OracleUniqueConstraint) key).setPhysicalProperties(props);
		}
		// returning our Oracle unique key
		return key;
	}

	@Override
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		return new OracleUniqueConstraint();
	}
}
