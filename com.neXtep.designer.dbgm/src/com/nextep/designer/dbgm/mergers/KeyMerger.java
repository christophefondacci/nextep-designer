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

import java.util.List;

import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.ForeignKeyAction;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.impl.ComparisonAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonNameAttribute;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.MergeStrategy;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.MergerWithChildCollections;
import com.nextep.datadesigner.vcs.impl.ReferenceTransitiveMerger;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPhysicalObject;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IActivity;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;

/**
 * @author Christophe Fondacci
 */
public class KeyMerger<T> extends MergerWithChildCollections<T> {

	private static final String ATTR_UKREF = "Remote constraint"; //$NON-NLS-1$
	private static final String ATTR_PRIMARY = "Primary"; //$NON-NLS-1$
	private static final String ATTR_TABNAME = "Table name"; //$NON-NLS-1$
	private static final String ATTR_ONUPDATE = "On update"; //$NON-NLS-1$
	private static final String ATTR_ONDELETE = "On delete"; //$NON-NLS-1$
	private static final String CATEGORY_COLUMNS = "Columns"; //$NON-NLS-1$
	public static final String ATTR_PHYSICAL = "Physical attributes"; //$NON-NLS-1$

	/**
	 * @see com.nextep.designer.vcs.model.IMerger#doCompare(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IComparisonItem doCompare(IReferenceable source, IReferenceable target) {
		IKeyConstraint srcKey = (IKeyConstraint) source;
		IKeyConstraint tgtKey = (IKeyConstraint) target;
		IComparisonItem result = new ComparisonResult(source, target, getMergeStrategy()
				.getComparisonScope());
		if (srcKey == null) {
			srcKey = (IKeyConstraint) createTargetObject(result, null);
		}
		if (tgtKey == null) {
			tgtKey = (IKeyConstraint) createTargetObject(result, null);
		}
		compareName(result, srcKey, tgtKey);
		// We need this one for db scopes to detect when keys parent changes
		if (getMergeStrategy().getComparisonScope() != ComparisonScope.REPOSITORY) {
			result.addSubItem(new ComparisonNameAttribute(ATTR_TABNAME, srcKey == null
					|| srcKey.getConstrainedTable() == null ? null : srcKey.getConstrainedTable()
					.getName(), tgtKey == null || tgtKey.getConstrainedTable() == null ? null
					: tgtKey.getConstrainedTable().getName(), MergeStrategy.isCaseSensitive()));
		}
		// result.addSubItem(new ComparisonAttribute(ATTR_TYPE,srcKey == null ?
		// null :
		// srcKey.getType().getCode(),tgtKey == null ? null :
		// tgtKey.getType().getCode()));
		listCompare(CATEGORY_COLUMNS, result, srcKey.getConstrainedColumnsRef(),
				tgtKey.getConstrainedColumnsRef(), true);

		if (result.getType() == IElementType.getInstance("FOREIGN_KEY")) { //$NON-NLS-1$
			ForeignKeyConstraint srcFK = (ForeignKeyConstraint) srcKey;
			ForeignKeyConstraint tgtFK = (ForeignKeyConstraint) tgtKey;
			// IMerger m =
			// MergerFactory.getMerger(IElementType.getInstance("REFERENCE"),getMergeStrategy().getComparisonScope());
			// We want to recursively compare inner PK reference to make sure it
			// is the same
			final IMerger m = new ReferenceTransitiveMerger();
			m.setMergeStrategy(MergeStrategy.create(getMergeStrategy().getComparisonScope()));
			result.addSubItem(ATTR_UKREF, m.compare(
					srcFK == null ? null : srcFK.getRemoteConstraintRef(), tgtFK == null ? null
							: tgtFK.getRemoteConstraintRef()));
			result.addSubItem(new ComparisonAttribute(ATTR_ONUPDATE, srcFK == null ? null : srcFK
					.getOnUpdateAction().name(), tgtFK == null ? null : tgtFK.getOnUpdateAction()
					.name()));
			result.addSubItem(new ComparisonAttribute(ATTR_ONDELETE, srcFK == null ? null : srcFK
					.getOnDeleteAction().name(), tgtFK == null ? null : tgtFK.getOnDeleteAction()
					.name()));
		} else {
			result.addSubItem(new ComparisonAttribute(ATTR_PRIMARY, srcKey == null ? null : String
					.valueOf(srcKey.getConstraintType() == ConstraintType.PRIMARY),
					tgtKey == null ? null
							: String.valueOf(tgtKey.getConstraintType() == ConstraintType.PRIMARY)));
		}

		// Handling physical objects (primary or unique key with physical
		// properties)
		if (result.getType() == IElementType.getInstance(UniqueKeyConstraint.TYPE_ID)) {
			if (source instanceof IPhysicalObject && target instanceof IPhysicalObject) {
				// Adding physicals
				IPhysicalObject src = (IPhysicalObject) source;
				IPhysicalObject tgt = (IPhysicalObject) target;

				IPhysicalProperties srcPty = (src == null ? null : src.getPhysicalProperties());
				IPhysicalProperties tgtPty = (tgt == null ? null : tgt.getPhysicalProperties());
				if (srcPty != null || tgtPty != null) {
					IMerger m = MergerFactory.getMerger(
							IElementType.getInstance(IIndexPhysicalProperties.TYPE_ID),
							getMergeStrategy().getComparisonScope());
					if (m != null) {
						result.addSubItem(ATTR_PHYSICAL, m.compare(srcPty, tgtPty));
					}
				}
			}
		}
		return result;
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
	 * @see com.nextep.datadesigner.vcs.impl.Merger#createTargetObject(com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object createTargetObject(IComparisonItem result, IActivity mergeActivity) {
		IKeyConstraint c = null;
		if (result.getType() == IElementType.getInstance("UNIQUE_KEY")) { //$NON-NLS-1$
			c = new UniqueKeyConstraint();
		} else {
			c = new ForeignKeyConstraint();
		}

		return c;
	}

	/**
	 * @see com.nextep.datadesigner.vcs.impl.Merger#fillObject(java.lang.Object,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IActivity)
	 */
	@Override
	protected Object fillObject(Object target, IComparisonItem result, IActivity activity) {
		IKeyConstraint c = (IKeyConstraint) target;
		fillName(result, c);
		if (c.getName() == null || "".equals(c.getName())) { //$NON-NLS-1$
			return null;
		}
		List<?> cols = getMergedList(CATEGORY_COLUMNS, result, activity);
		for (Object o : cols) {
			Reference ref = (Reference) o;
			if (ref != null) {
				c.addConstrainedReference(ref);
			}
		}
		if (result.getType() == IElementType.getInstance("FOREIGN_KEY")) { //$NON-NLS-1$
			IMerger m = MergerFactory.getMerger(IElementType.getInstance("REFERENCE"), //$NON-NLS-1$
					getMergeStrategy().getComparisonScope());
			Reference ref = (Reference) m.buildMergedObject(result.getSubItems(ATTR_UKREF)
					.iterator().next(), activity);
			final ForeignKeyConstraint fk = (ForeignKeyConstraint) c;
			fk.setRemoteConstraintRef(ref);
			fk.setConstraintType(ConstraintType.FOREIGN);
			final String onUpdate = getStringProposal(ATTR_ONUPDATE, result);
			if (onUpdate != null) {
				fk.setOnUpdateAction(ForeignKeyAction.valueOf(onUpdate));
			}
			final String onDelete = getStringProposal(ATTR_ONDELETE, result);
			if (onDelete != null) {
				fk.setOnDeleteAction(ForeignKeyAction.valueOf(onDelete));
			}
		} else {
			boolean pkFlag = Boolean.valueOf(getStringProposal(ATTR_PRIMARY, result));
			c.setConstraintType(pkFlag ? ConstraintType.PRIMARY : ConstraintType.UNIQUE);
		}
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
