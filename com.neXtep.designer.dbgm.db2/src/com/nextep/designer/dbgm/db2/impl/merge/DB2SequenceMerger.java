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
package com.nextep.designer.dbgm.db2.impl.merge;

import java.math.BigDecimal;
import com.nextep.datadesigner.dbgm.model.ISequence;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.dbgm.mergers.SequenceMerger;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Bruno Gautier
 */
public final class DB2SequenceMerger extends SequenceMerger {

	private static final BigDecimal MIN_VALUE = BigDecimal.ONE;
	private static final String DB2_DATATYPE_INTEGER = "INTEGER"; //$NON-NLS-1$

	@Override
	protected void compareMinValueAttribute(IComparisonItem result, ISequence source,
			ISequence target) {
		BigDecimal srcMinVal = (null == source ? null : source.getMinValue());
		BigDecimal tgtMinVal = (null == target ? null : target.getMinValue());
		if (MIN_VALUE.equals(tgtMinVal) && null == srcMinVal
				&& getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY) {
			tgtMinVal = null;
		}
		result.addSubItem(new ComparisonAttribute(ATTR_MIN, strVal(srcMinVal), strVal(tgtMinVal)));
	}

	@Override
	protected void compareMaxValueAttribute(IComparisonItem result, ISequence source,
			ISequence target) {
		BigDecimal srcMaxVal = (null == source ? null : source.getMaxValue());
		BigDecimal tgtMaxVal = (null == target ? null : target.getMaxValue());
		// FIXME [BGA]: The IDatatypeProvider should be retrieved by using a service.
		// TODO [BGA]: The DB2_DATATYPE_INTEGER constant should be replaced by a target.getDatatype
		// once the sequence data type is implemented in the DB2 specific model.
		if (DBGMHelper.getDatatypeProvider(DBVendor.DB2).getDatatypeMaxSize(DB2_DATATYPE_INTEGER)
				.equals(tgtMaxVal)
				&& null == srcMaxVal
				&& getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY) {
			tgtMaxVal = null;
		}
		result.addSubItem(new ComparisonAttribute(ATTR_MIN, strVal(srcMaxVal), strVal(tgtMaxVal)));
	}

}
