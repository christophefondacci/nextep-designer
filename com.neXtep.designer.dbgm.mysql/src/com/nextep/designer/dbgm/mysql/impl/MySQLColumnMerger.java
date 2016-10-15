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
package com.nextep.designer.dbgm.mysql.impl;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.dbgm.mergers.ColumnMerger;
import com.nextep.designer.dbgm.mysql.model.IMySQLColumn;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class MySQLColumnMerger extends ColumnMerger<IMySQLColumn> {

	private static final String ATTR_AUTOINC = "Auto-increment";
	public static final String ATTR_CHARSET = "Character set";
	public static final String ATTR_COLLATION = "Collation";

	@Override
	protected void fillSpecificComparison(IComparisonItem result, IMySQLColumn src, IMySQLColumn tgt) {
		result.addSubItem(new ComparisonAttribute(ATTR_AUTOINC, src == null ? null : String
				.valueOf(src.isAutoIncremented()), tgt == null ? null : String.valueOf(tgt
				.isAutoIncremented())));

		if (src != null && src.getCharacterSet() != null && !"".equals(src.getCharacterSet())) { //$NON-NLS-1$
			result.addSubItem(new ComparisonAttribute(ATTR_CHARSET, src == null ? null : src
					.getCharacterSet(), tgt == null ? null : tgt.getCharacterSet()));
		}
		if (src != null && src.getCollation() != null && !"".equals(src.getCollation())) { //$NON-NLS-1$
			result.addSubItem(new ComparisonAttribute(ATTR_COLLATION, src == null ? null : src
					.getCollation(), tgt == null ? null : tgt.getCollation()));
		}
	}

	@Override
	protected Class<? extends IReferenceable> getSpecificClass() {
		return MySQLColumn.class;
	}

	@Override
	protected void addDefaultAttribute(IComparisonItem result, IBasicColumn sourceCol,
			IBasicColumn targetCol) {
		// Fix for bug #298 on default expression values '' not given by MySQL
		// JDBC
		if (sourceCol != null && targetCol != null && sourceCol.getDefaultExpr() != null
				&& sourceCol.getDefaultExpr().equals("''")
				&& ("".equals(targetCol.getDefaultExpr()) || targetCol.getDatatype() == null)) {
			result.addSubItem(new ComparisonAttribute(ATTR_DEFAULT, "''", "''"));
		} else {
			super.addDefaultAttribute(result, sourceCol, targetCol);
		}
	}

	/**
	 * @see com.nextep.designer.dbgm.mergers.ColumnMerger#createTargetObject(com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		IMySQLColumn c = new MySQLColumn("", "", null, 0);
		return c;
	}

	/**
	 * @see com.nextep.designer.dbgm.mergers.ColumnMerger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IMySQLColumn col = (IMySQLColumn) super.fillObject(target, result, activity);
		if (col != null) {
			col.setAutoIncremented(Boolean.valueOf(getStringProposal(ATTR_AUTOINC, result)));
			col.setCharacterSet(getStringProposal(ATTR_CHARSET, result));
			col.setCollation(getStringProposal(ATTR_COLLATION, result));
		}
		return col;

	}
}
