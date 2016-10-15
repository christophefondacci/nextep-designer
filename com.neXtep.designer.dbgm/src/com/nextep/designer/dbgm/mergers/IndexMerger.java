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
package com.nextep.designer.dbgm.mergers;

import java.util.Collections;
import java.util.List;

import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * @author Christophe Fondacci
 */
public class IndexMerger<T> extends MergerWithChildCollections<T> {

	private static final String ATTR_TABLE = "Indexed table";
	private static final String ATTR_TYPE = "Index type";
	public static final String ATTR_COLUMNS = "Indexed columns";
	public static final String ATTR_PHYSICAL = "Physical attributes";
	public static final String ATTR_COLFUNCS = "Function definition";

	/**
	 * @se com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *     com.nextep.designer.vcs.model.IComparisonItem,
	 *     com.nextep.designer.vcs.model.IActivity))
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IIndex tgt = (IIndex) target;
		fillName(result, tgt);
		if (tgt.getName() == null) {
			return null;
		}
		// Filling index type
		IndexType type = IndexType.valueOf(getStringProposal(ATTR_TYPE, result));
		tgt.setIndexType(type);
		// Filling indexed table reference
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),
				getMergeStrategy().getComparisonScope());
		IReference r = (IReference) m.buildMergedObject(result.getSubItems(ATTR_TABLE).iterator()
				.next(), activity);
		tgt.setIndexedTableRef(r);
		// Filling column references
		List<?> colRefs = getMergedList(ATTR_COLUMNS, result, activity);
		for (Object o : colRefs) {
			if (o != null) {
				tgt.addColumnRef((IReference) o);
			}
		}

		// Building physical properties when supported
		if (tgt instanceof IPhysicalObject) {
			final IPhysicalObject physObject = (IPhysicalObject) tgt;
			m = MergerFactory.getMerger(IElementType.getInstance(IIndexPhysicalProperties.TYPE_ID),
					getMergeStrategy().getComparisonScope());
			if (m != null && result.getSubItems(ATTR_PHYSICAL) != null) {
				IIndexPhysicalProperties props = (IIndexPhysicalProperties) m.buildMergedObject(
						result.getSubItems(ATTR_PHYSICAL).iterator().next(), activity);
				physObject.setPhysicalProperties(props);
			}
		}

		// Filling functions map
		fillFunctionDiffs(result, tgt);

		// Saving
		save(tgt);
		// Returning the filled object
		return tgt;
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {

		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		IIndex src = (IIndex) source;
		IIndex tgt = (IIndex) target;

		compareName(result, src, tgt);
		// Index type
		result.addSubItem(new ComparisonAttribute(ATTR_TYPE, src == null ? null : src
				.getIndexType().name(), tgt == null ? null : tgt.getIndexType().name()));
		// Comparing indexed table
		IMerger m = MergerFactory.getMerger(IElementType.getInstance(IReference.TYPE_ID),
				getMergeStrategy().getComparisonScope());
		result.addSubItem(
				ATTR_TABLE,
				m.compare(src == null ? null : src.getIndexedTableRef(),
						tgt == null ? null : tgt.getIndexedTableRef()));
		// Comparing columns
		listCompare(ATTR_COLUMNS, result,
				src == null ? Collections.EMPTY_LIST : src.getIndexedColumnsRef(),
				tgt == null ? Collections.EMPTY_LIST : tgt.getIndexedColumnsRef(), true);

		if (src instanceof IPhysicalObject && tgt instanceof IPhysicalObject) {
			final IPhysicalObject physSrc = (IPhysicalObject) src;
			final IPhysicalObject physTgt = (IPhysicalObject) tgt;

			IPhysicalProperties srcPty = (physSrc == null ? null : physSrc.getPhysicalProperties());
			IPhysicalProperties tgtPty = (physTgt == null ? null : physTgt.getPhysicalProperties());
			if (srcPty != null || tgtPty != null) {
				m = MergerFactory.getMerger(
						IElementType.getInstance(IIndexPhysicalProperties.TYPE_ID),
						getMergeStrategy().getComparisonScope());
				if (m != null) {
					result.addSubItem(ATTR_PHYSICAL, m.compare(srcPty, tgtPty));
				}
			}
		}

		// Function definitions
		buildFunctionDiffs(result, src, tgt);

		// Returning result
		return result;
	}

	private void buildFunctionDiffs(IComparisonItem indexComparison, IIndex src, IIndex tgt) {
		List<IComparisonItem> colItems = indexComparison.getSubItems(ATTR_COLUMNS);
		if (colItems != null) {
			for (IComparisonItem i : colItems) {
				final IReference r = i.getReference();
				i.addSubItem(new ComparisonAttribute(ATTR_COLFUNCS, src == null ? null : src
						.getFunction(r), tgt == null ? null : tgt.getFunction(r)));
			}
		}
	}

	private void fillFunctionDiffs(IComparisonItem result, IIndex index) {
		List<IComparisonItem> colItems = result.getSubItems(ATTR_COLUMNS);
		if (colItems != null) {
			for (IComparisonItem i : colItems) {
				String func = getStringProposal(ATTR_COLFUNCS, result);
				if (func != null && !func.trim().isEmpty()) {
					index.setFunction(i.getReference(), func);
				}
			}
		}
	}
}
