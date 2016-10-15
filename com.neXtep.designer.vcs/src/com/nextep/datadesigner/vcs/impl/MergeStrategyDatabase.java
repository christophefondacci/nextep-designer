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
package com.nextep.datadesigner.vcs.impl;

import java.util.Collection;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.MultiKeyMap;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMergeStrategy;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.MergeInfo;
import com.nextep.designer.vcs.model.MergeStatus;

/**
 * A merge strategy for merging objects outside the repository. In this strategy, object types and
 * names are the main matching criteria.<br>
 * This strategy should be used when generating database objects from the repository to a target
 * database.<br>
 * This strategy is not thread-safe and should always be used within a single thread.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class MergeStrategyDatabase extends MergeStrategy implements IMergeStrategy {

	// private static final Log log = LogFactory.getLog(MergeStrategyDatabase.class);

	private Map<MultiKey, IReferenceable> sourcesNameMap;
	private Map<MultiKey, IReferenceable> targetsNameMap;

	@Override
	public MergeInfo merge(IMerger merger, IComparisonItem result, IComparisonItem sourceRootDiff,
			IComparisonItem targetRootDiff) {
		// Initializing merge information
		MergeInfo mergeInfo = result.getMergeInfo();

		// Merging
		switch (result.getDifferenceType()) {
		case EQUALS:
			// When both items are equal we choose the target
			mergeInfo.setMergeProposal(result.getTarget());
			mergeSubItems(result, null, null);
			break;
		case MISSING_SOURCE:
			// We rely on the drop strategy
			if (shouldDrop(result.getTarget())) {
				mergeInfo.setMergeProposal(result.getSource());
			} else {
				mergeInfo.setMergeProposal(result.getTarget());
			}
			mergeSubItems(result, null, null);
			break;
		case MISSING_TARGET:
			mergeInfo.setMergeProposal(result.getSource());
			mergeSubItems(result, null, null);
			break;
		case DIFFER:
			MergeStatus status = mergeSubItems(result, null, null);
			if (status == MergeStatus.MERGE_UNRESOLVED) {
				mergeInfo.setMergeProposal(result.getSource());
			} else {
				mergeInfo.setStatus(status);
			}
			break;
		}
		return mergeInfo;
	}

	@Override
	public boolean existsInSources(IReferenceable target) {
		return sourcesNameMap.containsKey(getReferenceableKey(target));
	}

	@Override
	public boolean existsInTargets(IReferenceable source) {
		return targetsNameMap.containsKey(getReferenceableKey(source));
	}

	@Override
	public IReferenceable getMatchingSource(IReferenceable target) {
		return sourcesNameMap.get(getReferenceableKey(target));
	}

	@Override
	public IReferenceable getMatchingTarget(IReferenceable source) {
		return targetsNameMap.get(getReferenceableKey(source));
	}

	@Override
	public <V extends IReferenceable> void initializeCollectionComparison(Collection<V> sources,
			Collection<V> targets) {
		sourcesNameMap = hashByNameAndType(sources);
		targetsNameMap = hashByNameAndType(targets);
	}

	@Override
	public boolean match(IReferenceable source, IReferenceable target) {
		// We compare names of source and target items only if their types are the same
		if (source != null && target != null
				&& getElementType(source).equals(getElementType(target))) {
			final String sourceName = getNamedObjectName(source);
			final String targetName = getNamedObjectName(target);

			return sourceName.equals(targetName);
		} else if (source == null && target == null) {
			return true;
		}
		return false;
	}

	public static INamedObject getNamedObject(IReferenceable item) {
		INamedObject namedItem = null;
		if (item instanceof IReference) {
			// Retrieving referenced instance
			IReferenceable ref = VersionHelper.getReferencedItem((IReference) item);
			// This instance should be named
			if (ref instanceof INamedObject) {
				namedItem = (INamedObject) ref;
			} else {
				throw new ErrorException("Unnamed object encountered during database target merge."); //$NON-NLS-1$
			}
		} else {
			// The specified item should be named
			if (item instanceof INamedObject) {
				namedItem = (INamedObject) item;
			} else {
				throw new ErrorException("Unnamed object encountered during database target merge."); //$NON-NLS-1$
			}
		}
		return namedItem;
	}

	/**
	 * Convenience method to retrieve the name of the specified {@link IReferenceable} object. This
	 * method handles the case sensitivity of the merge strategy by converting the name to upper
	 * case when it is not case sensitive.
	 * 
	 * @param item the {@link IReferenceable} object from which we must retrieve the name
	 * @return the name (converted to upper case if the merge strategy is not case sensitive) of the
	 *         {@link IReferenceable} object if the referenced object implements
	 *         {@link INamedObject}, null otherwise
	 */
	public static String getNamedObjectName(IReferenceable item) {
		String name = null;
		/*
		 * [BGA] FIXME: Maybe the #getNamedObject(IReferenceable) method should explicitly declare
		 * that it might throw an ErrorException and the #getNamedObjectName(IReferenceable) method
		 * should catch this error and replace the name by null or an empty string. But I cannot
		 * think of a use case where the error should be caught, so I prefer to leave it as is.
		 */
		final INamedObject namedItem = getNamedObject(item);
		if (namedItem != null) {
			name = namedItem.getName();
			if (!MergeStrategy.isCaseSensitive()) {
				return name.toUpperCase();
			}
		}
		return name;
	}

	/**
	 * Hashes a collection of <code>IReferenceable</code> objects by their reference
	 * 
	 * @param <V> type of the collection which must be a subclass of <code>IReferenceable</code>
	 * @param refList a collection of <code>IReferenceable</code>
	 * @return a Map of the <code>IReferenceable</code> hashed by reference
	 */
	@SuppressWarnings("unchecked")
	protected <V extends IReferenceable> Map<MultiKey, IReferenceable> hashByNameAndType(
			Collection<V> refList) {
		int size = refList.size();
		Map<MultiKey, IReferenceable> m = MultiKeyMap
				.decorate(new HashedMap((0 == size ? 1 : size)));

		for (V r : refList) {
			INamedObject o = getNamedObject(r);
			if (isCaseSensitive()) {
				m.put(new MultiKey(o.getName(), getElementType(r)), r);
			} else {
				m.put(new MultiKey(o.getName().toUpperCase(), getElementType(r)), r);
			}
		}
		return m;
	}

	private IElementType getElementType(IReferenceable ref) {
		return ((ref instanceof IReference) ? IElementType.getInstance(IReference.TYPE_ID) : ref
				.getReference().getType());
	}

	private MultiKey getReferenceableKey(IReferenceable ref) {
		return new MultiKey(getNamedObjectName(ref), getElementType(ref));
	}

	/**
	 * This strategy has a DATABASE comparison scope because it is dedicated to comparison / merge
	 * of database objects with repository objects.
	 * 
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#getComparisonScope()
	 */
	@Override
	public ComparisonScope getComparisonScope() {
		return ComparisonScope.DATABASE;
	}

}
