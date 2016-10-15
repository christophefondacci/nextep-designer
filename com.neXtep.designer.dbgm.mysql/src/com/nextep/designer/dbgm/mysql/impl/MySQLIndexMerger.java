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

import java.util.List;

import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.designer.dbgm.mergers.IndexMerger;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * The MySQL-specific index merger. It overloads default merger by merging
 * mysql-specific index attributes such as column prefix length.
 * 
 * @author Christophe Fondacci
 */
public class MySQLIndexMerger extends IndexMerger<IMySQLIndex> {

	public final static String ATTR_COL_PREFIX = "Column prefix length"; //$NON-NLS-1$

	@Override
	protected void fillSpecificComparison(IComparisonItem result, IMySQLIndex src, IMySQLIndex tgt) {
		buildPrefixLengthDiffs(result, src, tgt);
	}

	@Override
	protected Class<? extends IReferenceable> getSpecificClass() {
		return IMySQLIndex.class;
	}

	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IMySQLIndex index = (IMySQLIndex) super.fillObject(target, result, activity);
		if (index != null) {
			fillPrefixLengthDiffs(result, index);
		}
		return index;
	}

	private void buildPrefixLengthDiffs(IComparisonItem indexComparison, IMySQLIndex src,
			IMySQLIndex tgt) {
		List<IComparisonItem> colItems = indexComparison.getSubItems(ATTR_COLUMNS);
		if (colItems != null) {
			for (IComparisonItem i : colItems) {
				final IReference r = i.getReference();
				i.addSubItem(new ComparisonAttribute(ATTR_COL_PREFIX, src == null ? null
						: getPrefixLength(src, r), tgt == null ? null : getPrefixLength(tgt, r)));
			}
		}
	}

	private String getPrefixLength(IMySQLIndex index, IReference column) {
		final Integer prefix = index.getColumnPrefixLength(column);
		if (prefix == null) {
			return null;
		} else {
			return prefix.toString();
		}
	}

	private void fillPrefixLengthDiffs(IComparisonItem result, IMySQLIndex index) {
		List<IComparisonItem> colItems = result.getSubItems(ATTR_COLUMNS);
		if (colItems != null) {
			for (IComparisonItem i : colItems) {
				final String prefixLength = getStringProposal(ATTR_COL_PREFIX, result);
				if (prefixLength != null && !prefixLength.trim().isEmpty()) {
					index.setColumnPrefixLength(i.getReference(), Integer.valueOf(prefixLength));
				}
			}
		}
	}
}
