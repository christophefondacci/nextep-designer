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
package com.nextep.designer.synch.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.impl.VersionableSorter;
import com.nextep.datadesigner.vcs.services.MergeUtils;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerHint;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.core.model.ResourceConstants;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.dbgm.mergers.DataSetComparisonItem;
import com.nextep.designer.dbgm.model.IDataDelta;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.synch.SynchMessages;
import com.nextep.designer.synch.helper.SynchronizationHelper;
import com.nextep.designer.synch.model.IReverseSynchronizationContext;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.model.impl.ReverseSynchronizationContext;
import com.nextep.designer.synch.services.IReverseSynchronizationService;
import com.nextep.designer.util.Assert;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.exception.ImportFailedException;
import com.nextep.designer.vcs.exception.UnresolvedCheckFailedException;
import com.nextep.designer.vcs.marker.impl.CheckOutHint;
import com.nextep.designer.vcs.marker.impl.CommitHint;
import com.nextep.designer.vcs.model.ComparedElement;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionStatus;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.IWorkspace;
import com.nextep.designer.vcs.policies.CheckOutInExistingObjectVersionPolicy;
import com.nextep.designer.vcs.policies.DefaultVersionPolicy;
import com.nextep.designer.vcs.services.IDependencyService;
import com.nextep.designer.vcs.services.IVersioningService;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class ReverseSynchronizationService implements IReverseSynchronizationService {

	private final static Log LOGGER = LogFactory.getLog(ReverseSynchronizationService.class);

	private IDependencyService dependencyService;
	private IVersioningService versioningService;
	private IDataService dataService;
	private ICoreFactory coreFactory;
	private ICoreService coreService;
	private IVersionContainer newElementsTargetModule;

	@Override
	public void reverseSynchronize(ISynchronizationResult result, IProgressMonitor monitor) {
		// Creating new context
		IReverseSynchronizationContext context = createContext(result);
		reverseSynchronize(context, monitor);
	}

	@Override
	public void reverseSynchronize(IReverseSynchronizationContext context, IProgressMonitor monitor) {
		monitor.beginTask(
				SynchMessages.getString("synch.reverse.mainTask"), 5 + context.getItemsToSynchronize().size()); //$NON-NLS-1$
		progress(monitor, SynchMessages.getString("synch.reverse.computeDependencies"), 1); //$NON-NLS-1$

		// Sorting the elements to import by their dependencies
		final List<IVersionable<?>> sortedVersionables = VersionableSorter.sort(context
				.getVersionablesToImport());
		progress(monitor, SynchMessages.getString("synch.reverse.computeRemovals"), 1); //$NON-NLS-1$
		// Adding reverse sorted deletions
		List<IVersionable<?>> versionablesToRemove = VersionableSorter.sort(context
				.getVersionablesToRemove());
		progress(monitor, SynchMessages.getString("synch.reverse.computeRemovalDependencies"), 1); //$NON-NLS-1$
		Collections.reverse(versionablesToRemove);
		sortedVersionables.addAll(versionablesToRemove);
		progress(monitor, SynchMessages.getString("synch.reverse.processing"), 1); //$NON-NLS-1$

		// Iterating over elements to import and delegate to the appropriate method
		final Map<IVersionable<?>, IComparisonItem> versionableItemsMap = context
				.getVersionableItemsMap();
		final IWorkspace currentView = VCSPlugin.getViewService().getCurrentWorkspace();
		for (final IVersionable<?> v : sortedVersionables) {
			final IComparisonItem item = versionableItemsMap.get(v);
			if (monitor.isCanceled()) {
				return;
			}

			/*
			 * Each call to a method that modifies the current view or one of its objects has been
			 * encapsulated with a try/catch block so that no unexpected runtime exception
			 * interrupts the reverse synchronization process.
			 */
			switch (item.getDifferenceType()) {
			case MISSING_TARGET:
				monitor.subTask(MessageFormat.format(
						SynchMessages.getString("synch.reverse.addElement"), v.getType() //$NON-NLS-1$
								.getName().toLowerCase(), v.getName(), currentView.getName()));
				try {
					addToView(currentView, getNewElementsTargetModule(), v, context);
				} catch (RuntimeException rte) {
					LOGGER.error(
							MessageFormat.format(
									SynchMessages.getString("synch.reverse.addElementError"), v //$NON-NLS-1$
											.getType().getName().toLowerCase(), v.getName(),
									currentView.getName()) + rte.getMessage(), rte);
				}
				break;
			case DIFFER:
				monitor.subTask(MessageFormat.format(
						SynchMessages.getString("synch.reverse.updateElement"), v.getType() //$NON-NLS-1$
								.getName().toLowerCase(), v.getName(), currentView.getName()));
				try {
					updateIntoView(currentView, item, context);
				} catch (RuntimeException rte) {
					LOGGER.error(
							MessageFormat.format(
									SynchMessages.getString("synch.reverse.updateElementError"), v //$NON-NLS-1$
											.getType().getName().toLowerCase(), v.getName(),
									currentView.getName()) + rte.getMessage(), rte);
				}
				break;
			case MISSING_SOURCE:
				monitor.subTask(MessageFormat.format(
						SynchMessages.getString("synch.reverse.removeElement"), v //$NON-NLS-1$
								.getType().getName().toLowerCase(), v.getName(),
						currentView.getName()));
				try {
					removeFromView(v, context);
				} catch (RuntimeException rte) {
					LOGGER.error(
							MessageFormat.format(
									SynchMessages.getString("synch.reverse.removeElementError"), v //$NON-NLS-1$
											.getType().getName().toLowerCase(), v.getName(),
									currentView.getName()) + rte.getMessage(), rte);
				}
				break;
			}
			monitor.worked(1);
		}
		context.getImportPolicy().finalizeImport();
		// Explicitly saving import container as some use case may not save it
		if (getNewElementsTargetModule() != null) {
			CorePlugin.getIdentifiableDao().save(getNewElementsTargetModule());
			// Linking everything
			VersionHelper.relink(getNewElementsTargetModule());
		}

		// We explicitly require a marker full recomputation (DES-904)
		CorePlugin.getService(IMarkerService.class).computeAllMarkers();

		monitor.done();
	}

	private void progress(IProgressMonitor monitor, String message, int work) {
		if (monitor.isCanceled()) {
			throw new CancelException(SynchMessages.getString("synch.reverse.cancelMsg")); //$NON-NLS-1$
		}
		monitor.worked(1);
		monitor.subTask(message);
	}

	@Override
	public IReverseSynchronizationContext createContext(ISynchronizationResult result) {
		return new ReverseSynchronizationContext(result);
	}

	@Override
	public void addToView(IWorkspace view, IVersionContainer container, IVersionable<?> imported,
			IReverseSynchronizationContext context) {
		if (view == null) {
			view = VCSPlugin.getViewService().getCurrentWorkspace();
		}
		if (container == null) {
			container = view;
		}
		Assert.notNull(imported, SynchMessages.getString("synch.reverse.nullElementError")); //$NON-NLS-1$
		Assert.notNull(context, SynchMessages.getString("synch.reverse.noContextError")); //$NON-NLS-1$
		final Map<IReference, IReference> extRefMap = context.getSourceReferenceMapping();
		// Replacing db => view dependency when it maps
		SynchronizationHelper.replaceDependency(imported, extRefMap);

		// Replacing any dependency and removes any existing object
		SynchronizationHelper.replaceDependency(imported, extRefMap);
		Collection<IReference> importedRefs = new ArrayList<IReference>();
		// Adding current references as imported references
		importedRefs.add(imported.getReference());
		importedRefs.addAll(imported.getReferenceMap().keySet());
		// Checking all dependencies have already been imported
		if (context.shouldCheckForExternals()) {
			// Current view contents
			final Map<IReference, IReferenceable> viewContents = view.getReferenceMap();
			final Collection<IReference> dependencies = imported.getReferenceDependencies();
			for (IReference r : dependencies) {
				if (!importedRefs.contains(r) && !viewContents.containsKey(r)) {
					String objName = SynchMessages.getString("synch.reverse.unknownName"); //$NON-NLS-1$
					if (r.getInstance() != null && r.getInstance() instanceof INamedObject) {
						objName = ((INamedObject) r.getInstance()).getName();
					} else if (r.getArbitraryName() != null) {
						objName = r.getArbitraryName();
					}
					throw new UnresolvedCheckFailedException(
							MessageFormat.format(
									SynchMessages.getString("unresolvedWouldAppear"), imported.getType().getName() //$NON-NLS-1$
											.toLowerCase(), imported.getName(), r.getType()
											.getName().toLowerCase(), objName));
				}
			}
		}
		// Importing
		if (!context.getImportPolicy().importVersionable(imported, container,
				getVersioningService().getCurrentActivity())) {
			throw new ImportFailedException(imported);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void removeFromView(IVersionable<?> toRemove, IReverseSynchronizationContext context) {
		MultiValueMap invRefMap = context.getReverseDependenciesMap();
		Collection<IReferencer> referencers = invRefMap.getCollection(toRemove.getReference());
		if (referencers == null) {
			referencers = Collections.emptyList();
		}
		dependencyService
				.checkDeleteAllowed(toRemove, referencers, context.getDeletedReferencers());
		ControllerFactory.getController(toRemove.getType()).modelDeleted(toRemove);
	}

	@Override
	public void updateIntoView(IWorkspace view, IComparisonItem updatedItem,
			IReverseSynchronizationContext context) {
		Assert.notNull(context, SynchMessages.getString("synch.reverse.noContextError")); //$NON-NLS-1$
		final Map<IReference, IReference> extRefMap = context.getSourceReferenceMapping();
		final IVersionable<?> viewTarget = (IVersionable<?>) updatedItem.getTarget();
		final IVersionable<?> updated = (IVersionable<?>) updatedItem.getSource();
		final Collection<IVersionable<?>> toImport = context.getVersionablesToImport();
		final Collection<IComparisonItem> updatedItems = context.getVersionableItemsMap().values();

		// Replacing dependencies to link to current view (should not be useful since we do this
		// later).
		replaceComparisonReference(updatedItem, extRefMap);

		// We cannot import elements which already have checkouts, so we skip them.
		if (viewTarget.getVersion().getStatus() != IVersionStatus.CHECKED_IN) {
			toImport.remove(updated);
			LOGGER.warn(MessageFormat.format(
					SynchMessages.getString("synch.reverse.ignoredCheckedOut"), //$NON-NLS-1$
					NameHelper.getQualifiedName(updated)));
			return;
		}
		// Building imported reference list
		final List<IReference> importedRefs = buildImportedRefs(updatedItems);

		for (IVersionable<?> v : toImport) {
			if (v instanceof IReferenceContainer) {
				final IReferenceContainer refContainer = (IReferenceContainer) updated;
				importedRefs.addAll(refContainer.getReferenceMap().keySet());
			}
			if (v instanceof IReferenceable) {
				importedRefs.add(((IReferenceable) updated).getReference());
			}
		}

		if (updated instanceof IReferencer) {
			// Building target view contained references list
			final Collection<IReference> viewRefs = view.getReferenceMap().keySet();
			// Checking that every dependency is either in the view or in the import set
			if (!isDependenciesGranted(updatedItem, importedRefs, viewRefs, context)) {
				return;
			}
			// for(IReference r : referencer.getReferenceDependencies()) {
			// if(!importedRefs.contains(r) && !viewRefs.contains(r)) {
			// final Object o = VersionHelper.getReferencedItem(r);
			// log.warn(Designer.getInstance().getQualifiedName(updated) +
			// " has not been imported because it depends on the non-imported object: " +
			// Designer.getInstance().getQualifiedName(o));
			// toImport.remove(updated);
			// return null;
			// }
			// }
		}
		// Checking external references which would be created by the removal of one item
		// Collection<IReferencer> deletedReferencers = new ArrayList<IReferencer>();
		// fillDeletedReferencersList(Arrays.asList( new IComparisonItem[] {updatedItem}),
		// deletedReferencers);
		checkRecursiveDeletions(context, updatedItem);
		performUpdate(updatedItem, context);
	}

	/**
	 * Recursively switch the comparison item external references with their corresponding view
	 * references counterpart.
	 * 
	 * @param item comparison item to process
	 */
	private void replaceComparisonReference(IComparisonItem item,
			Map<IReference, IReference> extRefMap) {
		if (item.getSource() instanceof IReference) {
			// We may not have a comparison (for example, when importing an FK which does not exist
			// in repository
			// The FK will point to a primary key reference but the target comparison item is null.
			// So we make a lookup into our external reference map to switch it to a repository
			// reference to
			// avoid externals
			IReference r = extRefMap.get(item.getMergeInfo().getMergeProposal());
			if (r != null) {
				item.getMergeInfo().setMergeProposal(r);
			}
		}
		for (IComparisonItem subItem : item.getSubItems()) {
			replaceComparisonReference(subItem, extRefMap);
		}
	}

	/**
	 * Recursively look for any deletion in the comparison item tree and checks whether we can or
	 * cannot remove this element.
	 * 
	 * @param items comparison items to check
	 * @param deletedReferencers referencers which are removed with this item
	 * @return a collection of all {@link IReferencer} which would still reference the elements if
	 *         the reverse synchronization is performed
	 */
	@SuppressWarnings("unchecked")
	private Collection<IReferencer> checkRecursiveDeletions(IReverseSynchronizationContext context,
			IComparisonItem... items) {
		final Collection<IReferencer> referencers = new HashSet<IReferencer>();
		if (items == null) {
			final Collection<IComparisonItem> itemsToSynchronize = context.getItemsToSynchronize();
			items = itemsToSynchronize.toArray(new IComparisonItem[itemsToSynchronize.size()]);
		}
		Collection<IReferencer> deletedReferencers = context.getDeletedReferencers();
		MultiValueMap invRefMap = context.getReverseDependenciesMap();

		for (IComparisonItem item : items) {
			if (item.getDifferenceType() == DifferenceType.MISSING_SOURCE
					&& item.getTarget() instanceof IReferenceable
					&& item.getMergeInfo().getMergeProposal() == null) {
				Collection<IReferencer> invDeps = invRefMap.getCollection(item.getTarget()
						.getReference());
				if (invDeps == null) {
					invDeps = Collections.emptyList();
				}
				referencers.addAll(dependencyService.getReferencersAfterDeletion(
						(IReferenceable) item.getTarget(), invDeps, deletedReferencers));
				continue;
			}
			final Collection<IComparisonItem> subItems = item.getSubItems();
			referencers.addAll(checkRecursiveDeletions(context,
					subItems.toArray(new IComparisonItem[subItems.size()])));
		}
		return referencers;
	}

	/**
	 * Builds the list of imported references based on the collection of {@link IComparisonItem}
	 * currently being imported
	 * 
	 * @param items imported items
	 * @return list of imported references
	 */
	private List<IReference> buildImportedRefs(Collection<IComparisonItem> items) {
		final List<IReference> refs = new ArrayList<IReference>();
		for (IComparisonItem item : items) {
			final Object o = item.getMergeInfo().getMergeProposal();
			// We want referenceables here, and absolutely not references!
			if (item.getType() != IElementType.getInstance(IReference.TYPE_ID)
					&& (o instanceof IReferenceable)) {
				refs.add(((IReferenceable) o).getReference());
			}
			refs.addAll(buildImportedRefs(item.getSubItems()));
		}
		return refs;
	}

	/**
	 * Checks whether the element <b>only</b> depends on references which are either being imported
	 * or already in the current repository view. If not, the dependencies are not granted and the
	 * item should be skipped, else we can import
	 * 
	 * @param i imported comparison item
	 * @param importedRefs all imported references of the whole import operation
	 * @param viewRefs all existing references in the current view
	 * @return a boolean indicating whether <b>this</b> process should continue or not
	 */
	private boolean isDependenciesGranted(IComparisonItem i, Collection<IReference> importedRefs,
			Collection<IReference> viewRefs, IReverseSynchronizationContext context) {
		for (IComparisonItem child : i.getSubItems()) {
			if (child.getType() == IElementType.getInstance(IReference.TYPE_ID)) {
				IReference r = (IReference) child.getMergeInfo().getMergeProposal();
				if (r != null && !importedRefs.contains(r) && !viewRefs.contains(r)) {
					final Object o = VersionHelper.getReferencedItem(r);
					LOGGER.warn(MessageFormat.format(
							SynchMessages.getString("synch.reverse.ignoredUnimportedDependency"), //$NON-NLS-1$
							NameHelper.getQualifiedName(i.getSource()),
							NameHelper.getQualifiedName(o)));
					// Removing from the import list for consistency on the next elements to update
					context.getVersionablesToImport().remove(i.getSource());
					// Returning false to indicate we should not go further
					return false;
				}
			}
			// Recursive check
			// Warning: Not equivalent to returning directly, we only return if false, and continue
			// cycling when true
			if (!isDependenciesGranted(child, importedRefs, viewRefs, context)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Performs the import of the external element as a checkout of the corresponding view element.
	 * The element is passed as a {@link IComparisonItem} instance.
	 * 
	 * @param item comparison between the external and the view element
	 * @param activity activity to use for checkoutting the view element
	 * @return a list of commands which performs the import
	 */
	private void performUpdate(final IComparisonItem item, IReverseSynchronizationContext context) {
		final IVersionable<?> reposObject = (IVersionable<?>) item.getTarget();
		IVersionable<?> dbObject = (IVersionable<?>) item.getSource();

		// Merging
		// Following condition is true if we should merge source and target
		// according to user selection
		if (!MergeUtils.isSelected(item, ComparedElement.SOURCE)) {
			IMerger m = MergerFactory.getMerger(dbObject.getType(),
					ComparisonScope.DB_TO_REPOSITORY);
			dbObject = (IVersionable<?>) m.buildMergedObject(item, getVersioningService()
					.getCurrentActivity());
			final Collection<IVersionable<?>> toImport = context.getVersionablesToImport();
			toImport.remove(item.getSource());
			toImport.add(dbObject);
		}
		// Final dependency replacement pass
		SynchronizationHelper.replaceDependency(dbObject, context.getSourceReferenceMapping());
		// Check outting object
		reposObject.setVersionPolicy(new CheckOutInExistingObjectVersionPolicy(dbObject));
		getVersioningService().checkOut(new NullProgressMonitor(), reposObject);
		reposObject.setVersionPolicy(DefaultVersionPolicy.getInstance());
		// Specific data processing
		final ISynchronizationResult result = context.getSynchronizationResult();
		if (result != null && result.isDataSynchronization()) {
			final IDataSet dbSet = (IDataSet) dbObject.getVersionnedObject().getModel();
			final IDataSet repoSet = (IDataSet) reposObject.getVersionnedObject().getModel();
			// We need to align current row id from repository to ensure new lines will be properly
			// appended with a brand new rowid
			dbSet.setCurrentRowId(repoSet.getCurrentRowId());
			// We tag the version
			final IVersionInfo version = dbObject.getVersion();
			version.setVersionTag(VersionHelper.computeVersion(version));
			if (item instanceof DataSetComparisonItem) {
				IDataDelta delta = ((DataSetComparisonItem) item).getDataDelta();
				dataService.saveDataDeltaToRepository(dbSet, delta, new NullProgressMonitor());
				// Since we import the dataset in the workspace, we need to make it a regular
				// repository
				// dataset, we do this by emptying the handle which will force neXtep to refetch it
				// from
				// the repository
				dbSet.setStorageHandle(null);
			}
		}
	}

	@Override
	public List<IMarker> getProblems(IReverseSynchronizationContext context) {
		Collection<IVersionable<?>> needsVersionUpdate = context.getVersionablesToUpdateOrRemove();
		List<IVersionContainer> lockedContainers = new ArrayList<IVersionContainer>();
		List<IMarker> markers = new ArrayList<IMarker>();
		for (IVersionable<?> v : needsVersionUpdate) {
			final IVersionContainer parent = v.getContainer();
			// If locked container
			if (!VersionHelper.ensureModifiable(parent, false)) {
				// If not already excluded, add hint marker and exclude it
				if (!lockedContainers.contains(parent)) {
					lockedContainers.add(parent);
					final IMarker marker = coreFactory.createMarker(parent, MarkerType.ERROR,
							MessageFormat.format(SynchMessages
									.getString("synch.reverse.problem.locked"), parent.getName()), //$NON-NLS-1$
							new CheckOutHint());
					marker.setIcon(coreService.getResource(ResourceConstants.ICON_LOCK));
					markers.add(marker);
				}
			} else {
				if (Designer.checkIsModifiable(v, false)) {
					final IMarkerHint hint = new CommitHint();
					final IMarker marker = coreFactory.createMarker(
							v,
							MarkerType.ERROR,
							MessageFormat.format(
									SynchMessages.getString("synch.reverse.problem.checkedOut"), v //$NON-NLS-1$
											.getName()), hint);
					marker.setIcon(coreService.getResource(ResourceConstants.ICON_LOCK));
					markers.add(marker);
				}
				// Checking induced recursive deletions
			}
		}
		for (IComparisonItem item : context.getItemsToSynchronize()) {
			switch (item.getDifferenceType()) {
			case MISSING_SOURCE:
				// MultiValueMap invRefMap = context.getReverseDependenciesMap();
				// Collection<IReferencer> referencers = invRefMap.getCollection(item.getTarget()
				// .getReference());
				// Collection<IReferencer> remainingReferencers = dependencyService
				// .getReferencersAfterDeletion(item.getTarget(),
				// referencers == null ? Collections.EMPTY_LIST : referencers, context
				// .getDeletedReferencers());
				// if (!remainingReferencers.isEmpty()) {
				// final ITypedObject elt = (ITypedObject) item.getTarget();
				// final String refList = buildReferencersList(remainingReferencers);
				// final IMarker marker = new Marker(
				// elt,
				// MarkerType.ERROR,
				// elt.getType().getName()
				// + " "
				// + ((INamedObject) item.getTarget()).getName()
				// + " cannot be removed because it would generate unresolved references on : "
				// + refList);
				// }
				// break;
			case DIFFER:
				Collection<IReferencer> refs = checkRecursiveDeletions(context, item);
				if (!refs.isEmpty()) {
					final String depList = buildReferencersList(refs);
					final ITypedObject elt = (ITypedObject) item.getTarget();
					final IMarker marker = coreFactory.createMarker(elt, MarkerType.ERROR,
							MessageFormat.format(SynchMessages
									.getString("synch.reverse.problem.unresolveDependencies"), //$NON-NLS-1$
									elt.getType().getName(), ((INamedObject) item.getTarget())
											.getName(), depList));
					markers.add(marker);
				}

			}
		}
		return markers;
	}

	private String buildReferencersList(Collection<IReferencer> referencers) {
		StringBuilder b = new StringBuilder();
		String separator = ""; //$NON-NLS-1$
		for (IReferencer r : referencers) {
			b.append(separator);
			b.append(NameHelper.getQualifiedName(r));
			separator = ", "; //$NON-NLS-1$
		}
		return b.toString();
	}

	/**
	 * Service injection method
	 * 
	 * @param dependencyService {@link IDependencyService} implementation
	 */
	public void setDependencyService(IDependencyService dependencyService) {
		this.dependencyService = dependencyService;
	}

	/**
	 * Service injection method
	 * 
	 * @param dependencyService the {@link IDependencyService} to unset
	 */
	public void unsetDependencyService(IDependencyService dependencyService) {
		if (this.dependencyService == dependencyService) {
			this.dependencyService = null;
		}
	}

	public void setVersioningService(IVersioningService service) {
		this.versioningService = service;
	}

	protected IVersioningService getVersioningService() {
		return versioningService;
	}

	@Override
	public IVersionContainer getNewElementsTargetModule() {
		if (newElementsTargetModule == null) {
			return VCSPlugin.getViewService().getCurrentWorkspace();
		} else {
			return newElementsTargetModule;
		}
	}

	@Override
	public void setNewElementsTargetModule(IVersionContainer module) {
		this.newElementsTargetModule = module;
	}

	public void setDataService(IDataService dataService) {
		this.dataService = dataService;
	}

	/**
	 * @param coreFactory the coreFactory to set
	 */
	public void setCoreFactory(ICoreFactory coreFactory) {
		this.coreFactory = coreFactory;
	}

	/**
	 * @param coreService the coreService to set
	 */
	public void setCoreService(ICoreService coreService) {
		this.coreService = coreService;
	}

}
