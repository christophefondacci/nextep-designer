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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.sqlgen.model.ISynchronizationFilter;
import com.nextep.datadesigner.vcs.impl.MergeStrategy;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.services.IDataService;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.base.AbstractDatabaseConnector;
import com.nextep.designer.sqlgen.services.ICaptureService;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.synch.SynchMessages;
import com.nextep.designer.synch.dao.SynchronizationDAO;
import com.nextep.designer.synch.model.ISynchronizationListener;
import com.nextep.designer.synch.model.ISynchronizationResult;
import com.nextep.designer.synch.model.impl.SynchronizationResult;
import com.nextep.designer.synch.services.IDataCaptureService;
import com.nextep.designer.synch.services.ISynchronizationService;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IComparisonManager;
import com.nextep.designer.vcs.model.IMergeStrategy;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.services.IWorkspaceService;

/**
 * Synchronization service implementation. This class is in charge of performing
 * the various kind of synchronization.
 * 
 * @author Christophe Fondacci
 */
public class SynchronizationService implements ISynchronizationService {

	private final Collection<ISynchronizationListener> listeners;
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyMMddHHmmss"); //$NON-NLS-1$
	private IDataCaptureService dataCaptureService;
	private IGenerationService generationService;
	private IDataService dataService;
	private ICaptureService captureService;
	private IComparisonManager comparisonManager;
	private IWorkspaceService workspaceService;
	private IReferenceManager referenceManager;

	public SynchronizationService() {
		listeners = new HashSet<ISynchronizationListener>();
	}

	@Override
	public void addSynchronizationListener(ISynchronizationListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeSynchronizationListener(ISynchronizationListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies listeners that a new synchronization has just been performed
	 * 
	 * @param items
	 *            comparison result of items to synchronize
	 * @param vendor
	 *            vendor of the current synchronization, may be
	 *            <code>null</code>
	 */
	private void notifyNewSynchronization(ISynchronizationResult result) {
		for (ISynchronizationListener l : listeners) {
			l.newSynchronization(result);
		}
	}

	private void notifyScopeChanged(ComparisonScope scope) {
		for (ISynchronizationListener l : listeners) {
			l.scopeChanged(scope);
		}
	}

	@Override
	public void synchronize(IVersionContainer container, IConnection connection,
			IProgressMonitor mainMonitor) {
		SubMonitor subMonitor = SubMonitor.convert(mainMonitor, 100);
		referenceManager.flushVolatiles(null);
		final ComparisonScope scope = ComparisonScope.DATABASE;
		ISynchronizationResult result = buildSynchronizationResult(container, connection, scope,
				subMonitor.newChild(80));
		subMonitor.subTask(SynchMessages.getString(SynchMessages
				.getString("SynchronizationService.7"))); //$NON-NLS-1$
		buildScript(result, subMonitor.newChild(20));
		// Notifying
		notifyNewSynchronization(result);
		subMonitor.done();
	}

	/**
	 * Retrieves the contents from the specified database connection.
	 * 
	 * @param connection
	 *            database connection to extract information from
	 * @param monitor
	 *            progress monitor that the {@link AbstractDatabaseConnector}
	 *            may use while capturing
	 * @return a collection of {@link IVersionable} elements
	 */
	private Collection<IVersionable<?>> getContentsFromDatabase(IConnection connection,
			IProgressMonitor monitor) {
		return captureService.getContentsFromDatabase(connection, monitor);
	}

	/**
	 * Retrieves all elements implementing {@link IDatabaseObject} from the
	 * given container. They correspond to elements which may need to be
	 * synchronized.
	 * 
	 * @param container
	 *            repository container where to look for elements
	 * @return a collection of matching {@link IVersionable} objects
	 */
	private Collection<IVersionable<?>> getContentsFromContainer(IVersionContainer container) {
		List<IVersionable<?>> contents = new ArrayList<IVersionable<?>>();
		fillRepositoryObjects(contents, container);
		return contents;
	}

	/**
	 * Fills the given collection with the IDatabaseObject versionned elements
	 * of the specified container. Any sub container will recursively fill their
	 * database objects.
	 * 
	 * @param repObjects
	 *            initial list of repository database objects to fill
	 * @param container
	 *            container entry point of the lookup
	 */
	private static void fillRepositoryObjects(List<IVersionable<?>> repObjects,
			IVersionContainer container) {
		if (container == null)
			return;
		for (IVersionable<?> v : container.getContents()) {
			if (v.getVersionnedObject().getModel() instanceof IDatabaseObject<?>) {
				repObjects.add(v);
			} else if (v.getVersionnedObject().getModel() instanceof IVersionContainer) {
				fillRepositoryObjects(repObjects, (IVersionContainer) v.getVersionnedObject()
						.getModel());
			}
		}
	}

	/**
	 * Retrieves a collection of all defined synchronization filters for the
	 * given container. This method will recursively include filters defined by
	 * child containers of the specified one.
	 * 
	 * @param c
	 *            the {@link IVersionContainer} to retrieve the filters for
	 * @return a collection of {@link ISynchronizationFilter} representing the
	 *         defined filters
	 */
	private Collection<ISynchronizationFilter> getAllFilters(IVersionContainer c) {
		Collection<ISynchronizationFilter> filters = new ArrayList<ISynchronizationFilter>();
		filters.addAll(SynchronizationDAO.getFilters(c));
		// Recursively iterating into every sub container
		for (IVersionable<?> subContainer : VersionHelper.getAllVersionables(c,
				IElementType.getInstance(IVersionContainer.TYPE_ID))) {
			filters.addAll(getAllFilters((IVersionContainer) subContainer.getVersionnedObject()
					.getModel()));
		}
		return filters;
	}

	/**
	 * Filters the given collection of {@link IComparisonItem} according to the
	 * synchronization filters defined by the specified module.
	 * 
	 * @param items
	 *            inital collection of {@link IComparisonItem}
	 * @param container
	 *            a {@link IVersionContainer} to use for retrieving any
	 *            synchronization filter
	 * @param basedOnTarget
	 *            should we base the filtering on the target item name or on the
	 *            source ?
	 * @return the filtered collection of {@link IComparisonItem}, with any item
	 *         matching any synchronization filter removed
	 */
	private Collection<IComparisonItem> filter(Collection<IComparisonItem> items,
			IVersionContainer container, boolean basedOnTarget) {
		Collection<ISynchronizationFilter> filters = getAllFilters(container);
		if (filters != null && !filters.isEmpty()) {
			final List<IComparisonItem> filteredItems = new ArrayList<IComparisonItem>();
			for (IComparisonItem item : items) {
				if (!filteredItems.contains(item)) {
					boolean itemFiltered = false;
					for (ISynchronizationFilter filter : filters) {
						if (filter.match(basedOnTarget ? (ITypedObject) item.getTarget()
								: (ITypedObject) item.getSource())) {
							itemFiltered = true;
						}
					}
					if (!itemFiltered) {
						filteredItems.add(item);
					}
				}
			}
			return filteredItems;
		} else {
			return items;
		}
	}

	/**
	 * Merges every {@link IComparisonItem} of the provided list. The merge will
	 * compute merge proposals for every item of the list.
	 * 
	 * @param items
	 *            a collection of {@link IComparisonItem} to merge
	 */
	private void merge(Collection<IComparisonItem> items, ComparisonScope scope) {
		// For each comparison item
		for (IComparisonItem item : items) {
			// Retrieving the item merger
			IMerger m = MergerFactory.getMerger(item.getType(), scope);
			if (m != null) {
				// If we have found a merger, merging items to fill merge
				// information
				m.merge(item, null, null);
			}
		}
	}

	@Override
	public void buildScript(ISynchronizationResult result, IProgressMonitor monitor) {
		// Preparing name
		final String defaultName = "synchBuild_" + formatter.format(new Date()); //$NON-NLS-1$
		// Generating everything
		final IGenerationResult generation = generationService.batchGenerate(monitor, result
				.getConnection().getDBVendor(), defaultName, "", result.getComparedItems()); //$NON-NLS-1$
		// Building scripts
		result.setGenerationResult(generation);
		result.setDirty(false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void changeSynchronizationScope(ComparisonScope scope, ISynchronizationResult result,
			IProgressMonitor monitor) {
		if (scope == result.getComparisonScope()) {
			return;
		}
		monitor.beginTask(SynchMessages.getString("synch.job.changeComparisonScope"), 5); //$NON-NLS-1$
		// Comparing the two sets
		monitor.subTask(SynchMessages.getString("synch.job.initializing")); //$NON-NLS-1$
		final Collection<IReferenceable> originalSources = (Collection<IReferenceable>) result
				.getSourceElements();
		final Collection<IReferenceable> originalTargets = (Collection<IReferenceable>) result
				.getTargetElements();
		final IMergeStrategy strategy = MergeStrategy.create(scope);
		monitor.worked(1);
		monitor.subTask(SynchMessages.getString("synch.comparing")); //$NON-NLS-1$
		Collection<IComparisonItem> comparisons = VCSPlugin.getComparisonManager().compare(
				originalTargets, originalSources, strategy, false);
		monitor.worked(1);

		// Filtering results
		monitor.subTask(SynchMessages.getString("synch.filtering")); //$NON-NLS-1$
		comparisons = filter(comparisons, VCSPlugin.getService(IWorkspaceService.class)
				.getCurrentWorkspace(), scope == ComparisonScope.DATABASE);
		monitor.worked(1);

		// Merging results
		monitor.subTask(SynchMessages.getString("synch.merging")); //$NON-NLS-1$
		merge(comparisons, scope);
		monitor.worked(1);
		final ISynchronizationResult newResult = new SynchronizationResult(originalTargets,
				originalSources, comparisons, result.getConnection(), scope);
		// Preserving data synch flag
		newResult.setDataSynchronization(result.isDataSynchronization());
		// Notifying
		notifyNewSynchronization(newResult);
		notifyScopeChanged(scope);
		monitor.done();

	}

	@Override
	public void clearSynchronization() {
		final Collection<? extends IReferenceable> emptyRef = Collections.emptyList();
		final Collection<IComparisonItem> emptyItems = Collections.emptyList();
		ISynchronizationResult emptyResult = new SynchronizationResult(emptyRef, emptyRef,
				emptyItems, null, ComparisonScope.DATABASE);
		notifyNewSynchronization(emptyResult);
		notifyScopeChanged(ComparisonScope.DATABASE);
	}

	@Override
	public ISynchronizationResult buildSynchronizationResult(IVersionContainer container,
			IConnection connection, ComparisonScope scope, IProgressMonitor mainMonitor) {
		// Starting monitor
		SubMonitor monitor = SubMonitor.convert(mainMonitor,
				MessageFormat.format(SynchMessages.getString("synch.mainTask"), container //$NON-NLS-1$
						.getName(), connection.getName()), 10000);

		ISynchronizationResult result = null;
		try {
			Designer.getInstance().setContext(connection.getDBVendor().name());
			// Retrieving database content
			monitor.subTask(MessageFormat.format(
					SynchMessages.getString("synch.fetchDb"), connection //$NON-NLS-1$
							.getName()));
			final Collection<IVersionable<?>> dbContent = getContentsFromDatabase(connection,
					monitor.newChild(9600));
			monitor.setWorkRemaining(400);

			// Retrieving repository content
			monitor.subTask(MessageFormat.format(SynchMessages.getString("synch.fetchContainer"), //$NON-NLS-1$
					container.getName()));
			final Collection<IVersionable<?>> contContent = getContentsFromContainer(container);
			monitor.worked(50);

			// Comparing the two sets
			monitor.subTask(SynchMessages.getString("synch.comparing")); //$NON-NLS-1$
			// Handling scope
			final Collection<IVersionable<?>> sources = scope == ComparisonScope.DB_TO_REPOSITORY ? dbContent
					: contContent;
			final Collection<IVersionable<?>> targets = scope == ComparisonScope.DB_TO_REPOSITORY ? contContent
					: dbContent;
			Collection<IComparisonItem> comparisons = comparisonManager.compare(sources, targets,
					MergeStrategy.create(scope), false);
			monitor.worked(250);

			// Filtering results
			monitor.subTask(SynchMessages.getString("synch.filtering")); //$NON-NLS-1$
			comparisons = filter(comparisons, container, scope == ComparisonScope.DATABASE);
			monitor.worked(10);

			// Merging results
			monitor.subTask(SynchMessages.getString(SynchMessages
					.getString("SynchronizationService.6"))); //$NON-NLS-1$
			merge(comparisons, scope);
			monitor.worked(90);
			monitor.done();
			result = new SynchronizationResult(sources, targets, comparisons, connection, scope);

			return result;
		} finally {
			final DBVendor currentVendor = workspaceService.getCurrentWorkspace().getDBVendor();
			Designer.getInstance().setContext(currentVendor.name());
		}
	}

	@Override
	public void synchronizeData(IConnection conn, IProgressMonitor mon, IVersionable<?>... vTables) {
		final SubMonitor monitor = SubMonitor.convert(mon,
				SynchMessages.getString("service.synchronization.synchData"), 10000); //$NON-NLS-1$
		final Collection<IBasicTable> tables = new ArrayList<IBasicTable>(vTables.length);

		final IProgressMonitor tableMonitor = monitor.newChild(3000);
		// final Map<IBasicTable, IDataSet> repositoryTableDataMap = new
		// HashMap<IBasicTable,
		// IDataSet>();
		final List<IVersionable<IDataSet>> repoSets = new ArrayList<IVersionable<IDataSet>>(
				vTables.length);
		int work = 0;
		tableMonitor.beginTask(
				SynchMessages.getString("service.synchronization.fetchData"), vTables.length); //$NON-NLS-1$
		for (IVersionable<?> t : vTables) {
			final IBasicTable table = (IBasicTable) t.getVersionnedObject().getModel();
			work++;
			tables.add(table);
			if (!table.getDataSets().isEmpty()) {
				tableMonitor.subTask(MessageFormat.format(SynchMessages
						.getString("service.synchronization.fetchDataLines"), table.getName())); //$NON-NLS-1$
				tableMonitor.worked(work);
				work = 0;
				if (tableMonitor.isCanceled()) {
					return;
				}
				// Assuming 1 and only 1 set here
				// TODO: plug data scopes here
				final IDataSet dataSet = table.getDataSets().iterator().next();
				// Loading our set in local storage
				dataService.loadDataLinesFromRepository(dataSet, tableMonitor);
				// repositoryTableDataMap.put(table, dataSet);
				repoSets.add(VersionHelper.getVersionable(dataSet));
			}
		}
		tableMonitor.done();
		monitor.setWorkRemaining(7000);
		// Fetching target db data
		final Collection<IVersionable<IDataSet>> dbSets = dataCaptureService.captureTablesData(
				conn, tables, monitor.newChild(3000));
		// Comparing with workspace sets
		final IProgressMonitor compareMonitor = monitor.newChild(4000);
		compareMonitor.beginTask(
				SynchMessages.getString("service.synchronization.comparingData"), dbSets.size()); //$NON-NLS-1$
		// Comparing
		compareMonitor.subTask(SynchMessages
				.getString("service.synchronization.comparingDataSubTask")); //$NON-NLS-1$
		final List<IComparisonItem> comparisonItems = comparisonManager.compare(repoSets, dbSets,
				MergeStrategy.create(ComparisonScope.DATABASE), false);
		merge(comparisonItems, ComparisonScope.DATABASE);
		monitor.subTask(SynchMessages.getString("service.synchronization.notifying")); //$NON-NLS-1$
		ISynchronizationResult result = new SynchronizationResult(repoSets, dbSets,
				comparisonItems, conn, ComparisonScope.DATABASE);
		result.setDataSynchronization(true);
		notifyNewSynchronization(result);
		monitor.done();
	}

	public void setDataCaptureService(IDataCaptureService dataCaptureService) {
		this.dataCaptureService = dataCaptureService;
	}

	public void setDataService(IDataService dataService) {
		this.dataService = dataService;
	}

	public void setCaptureService(ICaptureService captureService) {
		this.captureService = captureService;
	}

	public void setComparisonManager(IComparisonManager comparisonManager) {
		this.comparisonManager = comparisonManager;
	}

	/**
	 * @param generationService
	 *            the generationService to set
	 */
	public void setGenerationService(IGenerationService generationService) {
		this.generationService = generationService;
	}

	/**
	 * @param workspaceService
	 *            the workspaceService to set
	 */
	public void setWorkspaceService(IWorkspaceService workspaceService) {
		this.workspaceService = workspaceService;
	}

	/**
	 * @param referenceManager
	 *            the referenceManager to set
	 */
	public void setReferenceManager(IReferenceManager referenceManager) {
		this.referenceManager = referenceManager;
	}
}
