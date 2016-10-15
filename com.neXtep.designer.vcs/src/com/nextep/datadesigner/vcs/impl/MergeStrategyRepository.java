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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMergeStrategy;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.MergeInfo;

/**
 * The merge strategy for merges happening inside the repository, that is to say version merges. <br>
 * The versioning information will be extensively used to provide accurate merge resolutions. This
 * strategy will retrieve the last common ancestor release of the 2 items and compare the source and
 * the target to this ancestor. For each compared / merged item which is different between source
 * and target, the strategy will check the evolution since the ancestor release for both source and
 * target to try to resolve conflicts.<br>
 * This is the default merge strategy.
 * 
 * @author Christophe Fondacci
 */
public class MergeStrategyRepository extends MergeStrategy implements IMergeStrategy {

	private static final Log LOGGER = LogFactory.getLog(MergeStrategyRepository.class);

	private Map<UID, IReferenceable> sourcesRefMap;
	private Map<UID, IReferenceable> targetsRefMap;

	/**
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#merge(com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IComparisonItem,
	 *      com.nextep.designer.vcs.model.IComparisonItem)
	 */
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
			mergeSubItems(result, sourceRootDiff, targetRootDiff);
			break;
		default:
			if (merger.isVersionable()) { // || (sourceRootDiff == null && targetRootDiff == null))
											// {
				if (result.getSource() == null) {
					// If the source is null, we merge depending on the drop strategy
					if (shouldDrop(result.getTarget())) {
						mergeInfo.setMergeProposal(result.getSource());
					} else {
						mergeInfo.setMergeProposal(result.getTarget());
					}
					mergeSubItems(result, sourceRootDiff, targetRootDiff);
					break;
				}
				if (result.getTarget() == null) {
					mergeInfo.setMergeProposal(result.getSource());
					mergeSubItems(result, sourceRootDiff, targetRootDiff);
					break;
				}
				if (sourceRootDiff == null && targetRootDiff == null) {
					IReferenceable commonAncestor = findCommonAncestor(
							VersionHelper.getVersionable(result.getSource()),
							VersionHelper.getVersionable(result.getTarget()));
					if (commonAncestor != null) {
						sourceRootDiff = merger.compare(commonAncestor, result.getSource());
						targetRootDiff = merger.compare(commonAncestor, result.getTarget());
					} else {
						sourceRootDiff = null;
						targetRootDiff = null;
					}
				}
			}
			// Handled by the OR clause (sourceAttr == null) check
			// in the following if.
			if (sourceRootDiff == null
					|| sourceRootDiff.getDifferenceType() == DifferenceType.EQUALS) {
				mergeInfo.setMergeProposal(result.getTarget());
				mergeSubItems(result, sourceRootDiff, targetRootDiff);
			} else {
				if (targetRootDiff == null
						|| targetRootDiff.getDifferenceType() == DifferenceType.EQUALS) {
					mergeInfo.setMergeProposal(result.getSource());
					mergeSubItems(result, sourceRootDiff, targetRootDiff);
				} else {
					mergeInfo.setStatus(mergeSubItems(result, sourceRootDiff, targetRootDiff));
				}
			}
			break;
		}
		return mergeInfo;
	}

	/**
	 * Finds the last common ancestor version of the 2 given versionable. This method will go
	 * through the version predecessors of the 2 elements and will return the first object which
	 * matches.
	 * 
	 * @param source source versionable element
	 * @param target target versionable element
	 * @return the last common ancestor of the 2 elements
	 */
	protected IReferenceable findCommonAncestor(IVersionable<?> source, IVersionable<?> target) {
		// Checking nullity
		if (source == null || target == null) {
			return null;
		}
		// Building target ancestors list
		List<IVersionInfo> targetAncestors = new ArrayList<IVersionInfo>();
		IVersionInfo version = target.getVersion();
		while (version != null) {
			targetAncestors.add(version);
			version = version.getPreviousVersion();
		}
		// Browsing source ancestors until a matching target ancestor is found
		version = source.getVersion();
		while (version != null) {
			// Have we got a match?
			if (targetAncestors.contains(version)) {
				// Then we load this ancestor
				IVersionInfo commonAncestorVersion = version;
				IVersionable<IReferenceable> commonAncestor = (IVersionable<IReferenceable>) CorePlugin
						.getIdentifiableDao().load(IVersionable.class,
								commonAncestorVersion.getUID(),
								HibernateUtil.getInstance().getSandBoxSession(), false);
				return commonAncestor.getVersionnedObject().getModel();
			}
			version = version.getPreviousVersion();
		}
		return null;
		// throw new ErrorException("Unable to find any common ancestor of " +
		// target.getType().getName().toLowerCase() + " <"+ target.getName() +
		// ">. Merge process failed!");
	}

	/**
	 * A matching algorithm based on the repository references.
	 * 
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#match(com.nextep.datadesigner.model.IReferenceable,
	 *      com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public boolean match(IReferenceable source, IReferenceable target) {
		if (source != null && target != null) {
			return source.getReference().getUID().equals(target.getReference().getUID());
		} else if (source == null && target == null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#existsInSources(com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public boolean existsInSources(IReferenceable target) {
		return sourcesRefMap.containsKey(target.getReference().getUID());
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#existsInTargets(com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public boolean existsInTargets(IReferenceable source) {
		return targetsRefMap.containsKey(source.getReference().getUID());
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#getMatchingSource(com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IReferenceable getMatchingSource(IReferenceable target) {
		return sourcesRefMap.get(target.getReference().getUID());
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#getMatchingTarget(com.nextep.datadesigner.model.IReferenceable)
	 */
	@Override
	public IReferenceable getMatchingTarget(IReferenceable source) {
		return targetsRefMap.get(source.getReference().getUID());
	}

	/**
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#initializeCollectionComparison(java.util.List,
	 *      java.util.List)
	 */
	@Override
	public <V extends IReferenceable> void initializeCollectionComparison(Collection<V> sources,
			Collection<V> targets) {
		sourcesRefMap = hashByRef(sources);
		targetsRefMap = hashByRef(targets);
	}

	/**
	 * Hashes a collection of <code>IReferenceable</code> objects by their reference
	 * 
	 * @param <V> type of the collection which must be a subclass of <code>IReferenceable</code>
	 * @param refList a collection of <code>IReferenceable</code>
	 * @return a Map of the <code>IReferenceable</code> hashed by reference
	 */
	protected <V extends IReferenceable> Map<UID, IReferenceable> hashByRef(Collection<V> refList) {
		Map<UID, IReferenceable> m = new HashMap<UID, IReferenceable>();
		for (V r : refList) {
			if (r != null) {
				m.put(r.getReference().getUID(), r);
			} else {
				LOGGER.warn(MessageFormat.format(
						VCSMessages.getString("mergeStrategyRepository.nullReferenceWhileHashing"), //$NON-NLS-1$
						refList));
			}
		}
		return m;
	}

	/**
	 * This strategy has a REPOSITORY scope because it is dedicated to compare 2 objects from the
	 * repository.
	 * 
	 * @see com.nextep.designer.vcs.model.IMergeStrategy#getComparisonScope()
	 */
	@Override
	public ComparisonScope getComparisonScope() {
		return ComparisonScope.REPOSITORY;
	}

}
