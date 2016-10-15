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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class ComparisonResult extends AbstractComparisonItem implements IComparisonItem {

	private static final Log LOGGER = LogFactory.getLog(ComparisonResult.class);

	private transient Map<String, IComparisonItem> outOfScopeAttrs = new HashMap<String, IComparisonItem>();
	private DifferenceType type;

	public ComparisonResult(IReferenceable source, IReferenceable target,
			ComparisonScope currentScope) {
		super(source, target, null);
		setScope(currentScope);

		if (source == null) {
			setDifferenceType(DifferenceType.MISSING_SOURCE);
		} else if (target == null) {
			setDifferenceType(DifferenceType.MISSING_TARGET);
		} else {
			switch (currentScope) {
			case DATABASE:
			case DB_TO_REPOSITORY:
				// If we are in a database scope, we have some volatile
				// reference so we compare
				// names
				performNameComparison(source, target);
				break;
			case REPOSITORY:
			default:
				/*
				 * If we are in a repository scope, we try to compare the unique
				 * IDs of the source and target if available, and fallback on
				 * name comparison otherwise.
				 */
				UID sourceUID = source.getReference().getUID();
				UID targetUID = target.getReference().getUID();
				if (sourceUID == null || targetUID == null) {
					LOGGER.debug("WARNING: performing name comparison on a REPOSITORY scope, " //$NON-NLS-1$
							+ "might be OK when reverse synchronizing from database"); //$NON-NLS-1$
					performNameComparison(source, target);
				} else if (sourceUID.equals(targetUID)) {
					setDifferenceType(DifferenceType.EQUALS);
				} else {
					setDifferenceType(DifferenceType.DIFFER);
				}
				break;
			}
		}
	}

	/**
	 * Performs a name comparison between source and target.
	 * 
	 * @param source
	 *            source object to compare
	 * @param target
	 *            target object to compare
	 */
	public void performNameComparison(IReferenceable source, IReferenceable target) {
		// Different element types are different, whatever their names
		if (source instanceof ITypedObject && target instanceof ITypedObject) {
			if (((ITypedObject) source).getType() != ((ITypedObject) target).getType()) {
				setDifferenceType(DifferenceType.DIFFER);
				return;
			}
		}

		final String sourceName = MergeStrategyDatabase.getNamedObjectName(source);
		final String targetName = MergeStrategyDatabase.getNamedObjectName(target);

		/*
		 * We don't need to check for the case sensitivity of the merge strategy
		 * as the source and target names are already converted to upper case by
		 * the #getNamedObjectName(IReferenceable) method if the merge strategy
		 * is case insensitive.
		 */
		if ((sourceName == null && targetName == null)
				|| (sourceName != null && sourceName.equals(targetName))) {
			setDifferenceType(DifferenceType.EQUALS);
		} else {
			setDifferenceType(DifferenceType.DIFFER);
		}
	}

	@Override
	public DifferenceType getDifferenceType() {
		return type;
	}

	public void setDifferenceType(DifferenceType type) {
		this.type = type;
	}

	@Override
	public void addSubItem(IComparisonItem subItem) {
		// We only add sub items that are in the current comparison scope
		if (subItem != null) {
			if (getScope().isCompatible(subItem.getScope())
					|| subItem.getScope() == ComparisonScope.ALL) {
				super.addSubItem(subItem);
				if (type == DifferenceType.EQUALS
						&& subItem.getDifferenceType() != DifferenceType.EQUALS) {
					setDifferenceType(DifferenceType.DIFFER);
				}
			} else if (subItem instanceof ComparisonAttribute) {
				subItem.setParent(this);
				// If we have a scope difference we only register attributes for
				// proper merge
				final ComparisonAttribute attr = (ComparisonAttribute) subItem;
				outOfScopeAttrs.put(attr.getName(), attr);
			}
		}
	}

	public IComparisonItem getOutOfScopeAttribute(String name) {
		return outOfScopeAttrs.get(name);
	}

}
