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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.ITablePhysicalProperties;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * The merger class for IBasicTable entities.
 * 
 * @author Christophe Fondacci
 */
public class TableMerger<T> extends MergerWithChildCollections<T> {

	private static final Log log = LogFactory.getLog(TableMerger.class);
	public static final String ATTR_SHORTNAME = "Short name";
	public static final String CATEGORY_COLUMNS = "Columns";
	public static final String ATTR_PHYSICAL = "Physical attributes";
	public static final String CATEGORY_KEYS = "Keys";
	public static final String CATEGORY_INDEXES = "Indexes";
	public static final String ATTR_TEMPORARY = "Temporary";

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(java.lang.Object,
	 *      java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		IBasicTable sourceTable = (IBasicTable) source;
		IBasicTable targetTable = (IBasicTable) target;
		// Map<VersionReference,IBasicColumn> srcColRefs =
		// hashByRef(source.getColumns());
		compareName(result, sourceTable, targetTable);
		result.addSubItem(new ComparisonAttribute(ATTR_TEMPORARY, sourceTable == null ? null
				: String.valueOf(sourceTable.isTemporary()), targetTable == null ? null : String
				.valueOf(targetTable.isTemporary())));

		result.addSubItem(new ComparisonAttribute(ATTR_SHORTNAME, sourceTable == null ? null
				: notNull(sourceTable.getShortName()), targetTable == null ? null
				: notNull(targetTable.getShortName()), ComparisonScope.REPOSITORY));
		// Comparing contents
		listCompare(CATEGORY_COLUMNS, result, sourceTable == null ? Collections.EMPTY_LIST
				: sourceTable.getColumns(), targetTable == null ? Collections.EMPTY_LIST
				: targetTable.getColumns());
		// Ordering keys
		List<IKeyConstraint> srcKeyList = new ArrayList<IKeyConstraint>(
				sourceTable == null ? Collections.EMPTY_LIST : sourceTable.getConstraints());
		List<IKeyConstraint> tgtKeyList = new ArrayList<IKeyConstraint>(
				targetTable == null ? Collections.EMPTY_LIST : targetTable.getConstraints());
		Comparator<IKeyConstraint> c = new KeyComparator();
		Collections.sort(srcKeyList, c);
		Collections.sort(tgtKeyList, c);
		listCompare(CATEGORY_KEYS, result, srcKeyList, tgtKeyList);

		// Handling physical tables
		// We STOP HERE on non-repository comparison depending on the physicals
		// synchronization flag
		// if(getMergeStrategy().getComparisonScope()!=ComparisonScope.REPOSITORY
		// &&
		// !SQLGenUtil.getPreferenceBool(PreferenceConstants.SYNCHRONIZE_PHYSICALS))
		// {
		// return result;
		// }
		if (sourceTable instanceof IPhysicalObject && targetTable instanceof IPhysicalObject) {
			final IPhysicalObject src = (IPhysicalObject) sourceTable;
			final IPhysicalObject tgt = (IPhysicalObject) targetTable;
			final ITablePhysicalProperties srcPty = (ITablePhysicalProperties) (src == null ? null
					: src.getPhysicalProperties());
			final ITablePhysicalProperties tgtPty = (ITablePhysicalProperties) (tgt == null ? null
					: tgt.getPhysicalProperties());
			if (srcPty != null || tgtPty != null) {
				IMerger m = MergerFactory.getMerger(
						IElementType.getInstance(ITablePhysicalProperties.TYPE_ID),
						getMergeStrategy().getComparisonScope());
				if (m != null) {
					result.addSubItem(ATTR_PHYSICAL, m.compare(srcPty, tgtPty));
				}
			}
		}
		// returning our comparison result
		return result;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IBasicTable t = (IBasicTable) target;
		log.debug("Starting table <" + t.getName() + "> fill");
		fillName(result, t);

		// Filling temporary flag
		final boolean isTemporary = Boolean.valueOf(getStringProposal(ATTR_TEMPORARY, result));
		t.setTemporary(isTemporary);

		// Monitoring removed tables
		if (t.getName() == null || "".equals(t.getName())) {
			return null;
		}
		t.setShortName(getStringProposal(ATTR_SHORTNAME, result));
		save(t);
		log.debug("Merging columns...");
		List<?> cols = getMergedList(CATEGORY_COLUMNS, result, activity);

		// Adding merged columns
		log.debug("Adding merged columns to table...");
		for (Object r : cols) {
			IBasicColumn c = (IBasicColumn) r;
			c.setParent(t);
			// Bugfix: re-setting column index
			c.setRank(cols.indexOf(c));
			save(c);
			t.addColumn(c);
		}
		log.debug("Merging constraints...");
		List<?> keys = getMergedList(CATEGORY_KEYS, result, activity);

		// Adding merged constraints
		log.debug("Adding merged constraints to table...");
		for (Object r : keys) {
			IKeyConstraint c = (IKeyConstraint) r;
			c.setConstrainedTable(t);
			save(c);
			t.addConstraint(c);
		}
		log.debug("Saving merged table " + t.getName() + ":" + t.getUID());
		save(t);
		return t;
	}

	/**
	 * A simple comparator which places UniqueKey before Foreign Keys. Since
	 * foreign keys can reference unique keys, unique keys should be merged
	 * first.
	 * 
	 * @author Christophe Fondacci
	 */
	private class KeyComparator implements Comparator<IKeyConstraint> {

		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(IKeyConstraint o1, IKeyConstraint o2) {
			if (o1 instanceof UniqueKeyConstraint && !(o2 instanceof UniqueKeyConstraint)) {
				return -1;
			} else if (!(o1 instanceof UniqueKeyConstraint) && o2 instanceof UniqueKeyConstraint) {
				return 1;
			} else {
				return o1.getName().compareTo(o2.getName());
			}
		}

	}
}
