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
package com.nextep.designer.synch.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.vcs.impl.ImportPolicyAddOnly;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.dbgm.mergers.DataSetComparisonItem;
import com.nextep.designer.synch.helper.SynchronizationHelper;
import com.nextep.designer.synch.model.IReverseSynchronizationContext;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.policies.ImportPolicyAddDataSet;
import com.nextep.designer.synch.policies.ImportPolicyEmptyView;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IImportPolicy;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.services.IWorkspaceService;

public class ReverseSynchronizationContext implements IReverseSynchronizationContext {

	private MultiValueMap revDependencyMap;
	private Collection<IVersionable<?>> versionsToImport;
	private Collection<IVersionable<?>> versionsToRemove;
	private Collection<IVersionable<?>> versionsToUpdateOrDelete;
	private Map<IReference, IReference> sourceRefMap;
	private Map<IVersionable<?>, IComparisonItem> versionableItemsMap;
	private Collection<IComparisonItem> itemsToSynchronize;
	private Collection<IReferencer> deletedReferencers;
	private boolean externalCheck = true;
	private ISynchronizationResult synchResult;
	private IImportPolicy importPolicy = ImportPolicyAddOnly.getInstance();
	private boolean initialImport = false;

	/**
	 * Builds a new {@link ReverseSynchronizationContext} from the current state of a
	 * {@link ISynchronizationResult}.
	 * 
	 * @param synchronizationResult
	 */
	@SuppressWarnings("unchecked")
	public ReverseSynchronizationContext(ISynchronizationResult synchronizationResult) {
		this.synchResult = synchronizationResult;
		final Collection<IComparisonItem> items = synchronizationResult.getComparedItems();
		// Building external references map
		sourceRefMap = SynchronizationHelper.buildSourceReferenceMapping(items
				.toArray(new IComparisonItem[items.size()]));
		// Building the list of versionable to import / remove, hashing them
		versionsToImport = new ArrayList<IVersionable<?>>();
		versionsToRemove = new ArrayList<IVersionable<?>>();
		versionsToUpdateOrDelete = new ArrayList<IVersionable<?>>();
		versionableItemsMap = new HashMap<IVersionable<?>, IComparisonItem>();
		for (final IComparisonItem item : items) {
			if (item.getDifferenceType() != DifferenceType.EQUALS) {
				if (shouldImport(item)) {
					if (item.getDifferenceType() != DifferenceType.MISSING_SOURCE) {
						final IVersionable<?> v = (IVersionable<?>) item.getSource();
						versionsToImport.add(v);
						versionableItemsMap.put(v, item);
						if (item.getDifferenceType() == DifferenceType.DIFFER) {
							versionsToUpdateOrDelete.add((IVersionable<?>) item.getTarget());
						}
					} else {
						final IVersionable<?> v = (IVersionable<?>) item.getTarget();
						versionsToRemove.add(v);
						versionableItemsMap.put(v, item);
						// Deleted item we need to monitor
						versionsToUpdateOrDelete.add(v);
					}
				}
			}
		}
		itemsToSynchronize = new ArrayList<IComparisonItem>(versionableItemsMap.values());
		// Checking external references which would be created by the removal of one item
		deletedReferencers = new ArrayList<IReferencer>();
		fillDeletedReferencersList(itemsToSynchronize, deletedReferencers);

		// Retrieving reverse references map
		revDependencyMap = CorePlugin.getService(IReferenceManager.class).getReverseDependenciesMap();
		// Hashing items by their target value
		Map<Object, IComparisonItem> itemsTargetMap = hashItemsByTarget(
				new HashMap<Object, IComparisonItem>(), itemsToSynchronize);
		// Processing reverse dependencies
		for (Object o : new ArrayList<IReferencer>(revDependencyMap.keySet())) {
			@SuppressWarnings("rawtypes")
			final Collection invRefs = revDependencyMap.getCollection(o);
			for (IReferencer referencer : new ArrayList<IReferencer>(invRefs)) {
				// If the referencer is not a database object we don't want to consider it (=>
				// Diagram ?)
				if (!(referencer instanceof IDatabaseObject<?>)) {
					revDependencyMap.remove(o, referencer);
				} else {
					// Locating our referencer as a comparison item
					IComparisonItem correspondingItem = itemsTargetMap.get(referencer);
					if (correspondingItem != null) {
						// Retrieving corresponding proposal
						final Object proposal = correspondingItem.getMergeInfo().getMergeProposal();
						if (correspondingItem.getDifferenceType() != DifferenceType.DIFFER
								&& proposal == null) {
							// The referencer will be removed, we remove dependency
							revDependencyMap.remove(o, referencer);
						} else if (proposal != referencer) {
							// The referencer is not the proposal, so the dependency is valid only
							// if the reference (o) is a merge proposal, if not we remove dependency
							if (!isMergeProposal(correspondingItem, o)) {
								revDependencyMap.remove(o, referencer);
							}
						}

					}
				}
			}
		}
		// Checking if we are importing in an empty view
		final IWorkspace workspace = VCSPlugin.getService(IWorkspaceService.class)
				.getCurrentWorkspace();
		final Collection<IReferenceable> contents = workspace.getReferenceMap().values();
		if (!synchronizationResult.isDataSynchronization()) {
			// We do not check external references when importing into an empty workspace or into a
			// workspace with a unique database module
			if (contents.isEmpty()
					|| (contents.size() == 1 && contents.iterator().next() instanceof IVersionContainer)) {
				externalCheck = false;
				importPolicy = new ImportPolicyEmptyView();
				initialImport = true;
			} else {
				externalCheck = true;
				importPolicy = ImportPolicyAddOnly.getInstance();
			}
		} else {
			// A specific policy to import datasets
			externalCheck = false;
			importPolicy = new ImportPolicyAddDataSet();
		}
	}

	/**
	 * Hashes the list of comparison items by their target value
	 * 
	 * @param itemsMap map of items hashed by value to fill
	 * @param items list of items to process recursively
	 * @return a map of {@link IComparisonItem} hashed by their target value
	 */
	private Map<Object, IComparisonItem> hashItemsByTarget(Map<Object, IComparisonItem> itemsMap,
			Collection<IComparisonItem> items) {
		for (IComparisonItem item : items) {
			itemsMap.put(item.getTarget(), item);
			hashItemsByTarget(itemsMap, item.getSubItems());
		}
		return itemsMap;
	}

	/**
	 * Indicates whether the specified object is a merge proposal
	 * 
	 * @param i comparison item tree entry point
	 * @param o element value to check
	 * @return <code>true</code> if at least one child comparison item has the specified element
	 *         value as its merge proposal, else <code>false</code>
	 */
	private boolean isMergeProposal(IComparisonItem i, Object o) {
		for (IComparisonItem subItem : i.getSubItems()) {
			// If the reference is still selected as a merge proposal, the dependency is still valid
			if (subItem.getMergeInfo().getMergeProposal() == o) {
				return true;
			}
			// Recursive check
			if (isMergeProposal(subItem, o)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public MultiValueMap getReverseDependenciesMap() {
		return revDependencyMap;
	}

	@Override
	public Map<IReference, IReference> getSourceReferenceMapping() {
		return sourceRefMap;
	}

	@Override
	public Collection<IVersionable<?>> getVersionablesToImport() {
		return versionsToImport;
	}

	@Override
	public Collection<IVersionable<?>> getVersionablesToRemove() {
		return versionsToRemove;
	}

	@Override
	public Map<IVersionable<?>, IComparisonItem> getVersionableItemsMap() {
		return versionableItemsMap;
	}

	/**
	 * Fills the specified list with all referencers about to be removed
	 * 
	 * @param items collection of {@link IComparisonItem} which are selected to be resynch with the
	 *        repository
	 * @param toFill collection of deleted {@link IReferencer} to fill
	 */
	private void fillDeletedReferencersList(Collection<IComparisonItem> items,
			Collection<IReferencer> toFill) {
		for (IComparisonItem item : items) {
			if (item.getDifferenceType() == DifferenceType.MISSING_SOURCE
					&& item.getMergeInfo().getMergeProposal() == null
					&& item.getTarget() instanceof IReferencer) {
				toFill.add((IReferencer) item.getTarget());
			}
			fillDeletedReferencersList(item.getSubItems(), toFill);
		}
	}

	@Override
	public Collection<IReferencer> getDeletedReferencers() {
		return deletedReferencers;
	}

	/**
	 * Determines whether the specified item should be imported. The item will be imported if at
	 * least one of its sub item has a merge proposal equals to the source item.
	 * 
	 * @param item item eligible for import
	 * @return <code>true</code> if we need to import, else <code>false</code>
	 */
	private boolean shouldImport(IComparisonItem item) {
		final IReferenceable mergeProposal = item.getMergeInfo().getMergeProposal();
		if (mergeProposal == item.getSource() && mergeProposal != item.getTarget()) {
			return true;
		} else if (mergeProposal == item.getTarget() && mergeProposal != item.getSource()) {
			return false;
		} else {
			if (item instanceof DataSetComparisonItem) {
				return true;
			} else {
				for (IComparisonItem subItem : item.getSubItems()) {
					if (subItem.getScope().isCompatible(ComparisonScope.DB_TO_REPOSITORY)
							&& shouldImport(subItem)) {
						return true;
					}
				}

				return false;
			}
		}
	}

	@Override
	public Collection<IComparisonItem> getItemsToSynchronize() {
		return itemsToSynchronize;
	}

	@Override
	public Collection<IVersionable<?>> getVersionablesToUpdateOrRemove() {
		return versionsToUpdateOrDelete;
	}

	@Override
	public boolean shouldCheckForExternals() {
		return externalCheck;
	}

	@Override
	public IImportPolicy getImportPolicy() {
		return importPolicy;
	}

	@Override
	public ISynchronizationResult getSynchronizationResult() {
		return synchResult;
	}

	@Override
	public boolean isInitialImport() {
		return initialImport;
	}
}
