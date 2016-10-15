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

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.dbgm.mergers.TableMerger;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 */
public class MySQLTableMerger extends TableMerger<IMySQLTable> {

	public static final String ATTR_ENGINE = "Engine";
	public static final String ATTR_CHARSET = "Character set";
	public static final String ATTR_COLLATION = "Collation";

	@Override
	protected void fillSpecificComparison(IComparisonItem result, IMySQLTable src, IMySQLTable tgt) {
		// Adding mysql specific attributes
		result.addSubItem(new ComparisonAttribute(ATTR_ENGINE,
				src == null ? null : src.getEngine(), tgt == null ? null : tgt.getEngine()));
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
		return IMySQLTable.class;
	}

	/**
	 * @see com.nextep.designer.dbgm.mergers.TableMerger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		// Filling from default table implementation
		IMySQLTable tab = (IMySQLTable) super.fillObject(target, result, activity);
		if (tab == null)
			return null;
		// Adding mysql specific properties
		tab.setEngine(getStringProposal(ATTR_ENGINE, result));
		tab.setCharacterSet(getStringProposal(ATTR_CHARSET, result));
		tab.setCollation(getStringProposal(ATTR_COLLATION, result));
		return tab;
	}
}
