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
package com.nextep.designer.dbgm.mergers;

import com.nextep.datadesigner.dbgm.impl.BasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.Merger;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * 
 */
public class ColumnMerger<T> extends Merger<T> {

	private static final String ATTR_POSITION = "Position";
	private static final String ATTR_NOTNULL = "Not-Null";
	private static final String ATTR_VIRTUAL = "Virtual";
	protected static final String ATTR_DEFAULT = "Default";

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		IBasicColumn sourceCol = (IBasicColumn) source;
		IBasicColumn targetCol = (IBasicColumn) target;
		this.compareName(result, sourceCol, targetCol);

		addDefaultAttribute(result, sourceCol, targetCol);
		result.addSubItem(new ComparisonAttribute(ATTR_NOTNULL, sourceCol == null ? null : String
				.valueOf(sourceCol.isNotNull()), targetCol == null ? null : String
				.valueOf(targetCol.isNotNull())));
		result.addSubItem(new ComparisonAttribute(ATTR_POSITION, sourceCol == null ? null : String
				.valueOf(sourceCol.getRank()), targetCol == null ? null : String.valueOf(targetCol
				.getRank()), ComparisonScope.REPOSITORY));
		result.addSubItem(new ComparisonAttribute(ATTR_VIRTUAL, sourceCol == null ? null : String
				.valueOf(sourceCol.isVirtual()), targetCol == null ? null : String
				.valueOf(targetCol.isVirtual())));

		IDatatype srcType = sourceCol == null ? null : sourceCol.getDatatype();
		IDatatype tgtType = targetCol == null ? null : targetCol.getDatatype();
		DatatypeMergeHelper.addDatatypeComparison(result, srcType, tgtType, getMergeStrategy()
				.getComparisonScope());
		return result;
	}

	/**
	 * Hook for child implementation to override default expression comparison.
	 * Mainly added for mysql specific processing on default values due to bug
	 * #298.
	 * 
	 * @param result
	 *            result of the comparison
	 * @param sourceCol
	 *            source column being compared
	 * @param targetCol
	 *            target column being compared
	 */
	protected void addDefaultAttribute(IComparisonItem result, IBasicColumn sourceCol,
			IBasicColumn targetCol) {
		result.addSubItem(new ComparisonAttribute(ATTR_DEFAULT, sourceCol == null ? null
				: sourceCol.getDefaultExpr() == null ? "" : sourceCol.getDefaultExpr(),
				targetCol == null ? null : targetCol.getDefaultExpr() == null ? "" : targetCol
						.getDefaultExpr()));
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#copyWhenUnchanged()
	 */
	@Override
	protected boolean copyWhenUnchanged() {
		return true;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#isVersionable()
	 */
	@Override
	public boolean isVersionable() {
		return false;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IBasicColumn c = (IBasicColumn) target;
		fillName(result, c);
		if (c.getName() == null || "".equals(c.getName())) {
			return null;
		}
		if (getMergeStrategy().getComparisonScope() == ComparisonScope.REPOSITORY) {
			c.setRank(Integer.valueOf(getStringProposal(ATTR_POSITION, result)));
		}
		c.setNotNull(Boolean.valueOf(getStringProposal(ATTR_NOTNULL, result)));
		c.setDefaultExpr(getStringProposal(ATTR_DEFAULT, result));
		c.setVirtual(Boolean.valueOf(getStringProposal(ATTR_VIRTUAL, result)));

		c.setDatatype(DatatypeMergeHelper.buildDataTypeFromComparison(result));
		return c;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#createTargetObject(com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		// Creating a new column
		IBasicColumn c = new BasicColumn("", "", null, -1);

		return c;
	}
	// /**
	// * @see
	// com.nextep.designer.vcs.model.IMerger#merge(com.nextep.designer.vcs.model.IReferenceable,
	// com.nextep.designer.vcs.model.IReferenceable)
	// */
	// @Override
	// public void merge(IComparisonItem item) {
	// // TODO Auto-generated method stub
	//
	// }

}
