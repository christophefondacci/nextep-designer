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
package com.nextep.designer.beng.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.set.ListOrderedSet;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

import com.neXtep.shared.model.ArtefactType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ExternalReferenceException;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.sqlgen.model.IDatafileGeneration;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.vcs.impl.ComparisonResult;
import com.nextep.datadesigner.vcs.impl.ContainerInfo;
import com.nextep.datadesigner.vcs.impl.ContainerMerger;
import com.nextep.datadesigner.vcs.impl.MergerFactory;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.beng.BengMessages;
import com.nextep.designer.beng.dao.IDeliveryDao;
import com.nextep.designer.beng.exception.InvalidStrategyException;
import com.nextep.designer.beng.exception.UndeliverableIncrementException;
import com.nextep.designer.beng.exception.UnresolvableDeliveryException;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IArtefact;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.model.IMatchingStrategy;
import com.nextep.designer.beng.model.impl.Artefact;
import com.nextep.designer.beng.model.impl.DefaultMatchingStrategy;
import com.nextep.designer.beng.model.impl.DeliveryFile;
import com.nextep.designer.beng.model.impl.DeliverySQLScript;
import com.nextep.designer.beng.model.impl.ModuleDeliveryIncrement;
import com.nextep.designer.beng.model.impl.StrictMatchingStrategy;
import com.nextep.designer.beng.services.IDeliveryService;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.dao.IIdentifiableDAO;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.vcs.model.ComparisonScope;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IMerger;
import com.nextep.designer.vcs.model.IRepositoryFile;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.model.impl.VersionInfo;

public class DeliveryService implements IDeliveryService {

	private IDeliveryDao deliveryDao;
	private IGenerationService generationService;

	/**
	 * A utility class which attaches a {@link ISQLScript} to a {@link DBVendor}
	 * .
	 */
	private static class VendorScript {

		private final ISQLScript script;
		private final DBVendor vendor;

		public VendorScript(ISQLScript s, DBVendor vendor) {
			this.script = s;
			this.vendor = vendor;
		}

		public ISQLScript getScript() {
			return script;
		}

		public DBVendor getVendor() {
			return vendor;
		}
	}

	@Override
	public List<IGenerationResult> build(IDeliveryModule module, IProgressMonitor m) {
		SubMonitor monitor = SubMonitor.convert(m);
		monitor.beginTask(BengMessages.getString("service.delivery.buildingModule"), 100); //$NON-NLS-1$
		// Retrieving module to build
		final IVersionContainer c = (IVersionContainer) VersionHelper.getReferencedItem(module
				.getModuleRef());
		final List<IComparisonItem> itemsToGenerate = buildDeliveryModuleItems(module,
				monitor.newChild(60));
		// Initializing script name
		final String scriptName = getGenerationName(c, module.getTargetRelease());

		// Computing vendors to generate
		List<IGenerationResult> results = new ArrayList<IGenerationResult>();
		Collection<DBVendor> vendorsToGenerate = getVendorsToGenerate(c);
		for (DBVendor vendor : vendorsToGenerate) {
			monitor.subTask(MessageFormat.format(BengMessages
					.getString("service.delivery.generatingVendorScript"), vendor.toString())); //$NON-NLS-1$
			IGenerationResult r = generationService.batchGenerate(monitor.newChild(5), vendor,
					scriptName, MessageFormat.format(BengMessages
							.getString("service.delivery.generationDesc"), //$NON-NLS-1$
							(module.getFromRelease() != null ? module.getFromRelease().getLabel()
									: "scratch"), module.getTargetRelease().getLabel()), //$NON-NLS-1$
					itemsToGenerate);
			r.setVendor(vendor);
			results.add(r);
		}
		return results;
	}

	@Override
	public List<IComparisonItem> buildDeliveryModuleItems(IDeliveryModule module, IProgressMonitor m) {
		final List<IComparisonItem> itemsToGenerate = new ArrayList<IComparisonItem>();
		final IProgressMonitor monitor = SubMonitor.convert(m);
		monitor.beginTask(BengMessages.getString("service.delivery.computingDeltas"), 100); //$NON-NLS-1$
		// Retrieving module to build
		final IVersionContainer c = (IVersionContainer) VersionHelper.getReferencedItem(module
				.getModuleRef());

		final IMerger moduleMerger = MergerFactory.getMerger(c.getType(),
				ComparisonScope.REPOSITORY);
		// Comparing initial and target module releases
		monitor.subTask(BengMessages.getString("service.delivery.compareInitialWithTarget")); //$NON-NLS-1$
		IComparisonItem result = moduleMerger.compare(module.getModuleRef(),
				module.getTargetRelease(), module.getFromRelease(), true);
		monitor.worked(70);
		monitor.subTask(BengMessages.getString("service.delivery.merging")); //$NON-NLS-1$
		moduleMerger.merge(result, null, null);
		monitor.worked(10);

		final Map<IReference, IArtefact> artefactRefMap = hashArtefactReferences(module
				.getArtefacts());
		monitor.worked(10);

		monitor.subTask(BengMessages.getString("service.delivery.filtering")); //$NON-NLS-1$
		// Extracting the subset of elements to generate
		List<IComparisonItem> contents = result.getSubItems(ContainerMerger.CATEGORY_CONTENTS);
		if (contents != null) {
			fillItemsToGenerate(itemsToGenerate, contents, artefactRefMap);
		} else {
			// Specific use-case for missing target we generate all artefacts
			if (result.getDifferenceType() == DifferenceType.MISSING_TARGET) {
				fillItemsToGenerate(itemsToGenerate, c, artefactRefMap);
			}
		}
		monitor.worked(10);
		monitor.done();
		return itemsToGenerate;
	}

	/**
	 * Recusrively fills {@link IComparisonItem} collection that needs to be
	 * generated by filtering specified collection against the {@link IArtefact}
	 * map specified. This method will recursively process any inner sub module.
	 * 
	 * @param itemsToGenerate
	 *            collection of {@link IComparisonItem} filled by this method
	 * @param contents
	 *            initial content list
	 * @param artefactRefMap
	 *            list of artefacts needing to be generated
	 */
	private void fillItemsToGenerate(Collection<IComparisonItem> itemsToGenerate,
			Collection<IComparisonItem> contents, Map<IReference, IArtefact> artefactRefMap) {
		for (IComparisonItem contentItem : contents) {
			final IArtefact artefact = artefactRefMap.get(contentItem.getReference());
			if (artefact != null) {
				itemsToGenerate.add(contentItem);
			} else if (contentItem.getType() == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
				final List<IComparisonItem> subContents = contentItem
						.getSubItems(ContainerMerger.CATEGORY_CONTENTS);
				if (subContents != null) {
					fillItemsToGenerate(itemsToGenerate, subContents, artefactRefMap);
				} else if (contentItem.getDifferenceType() == DifferenceType.MISSING_TARGET
						|| contentItem.getDifferenceType() == DifferenceType.MISSING_SOURCE) {
					// The whole container has been added, we must generate
					itemsToGenerate.add(contentItem);
				}
			}
		}
	}

	/**
	 * Recursively fills items to generate from a container. This alternate
	 * method is called when no comparison could be found from module comparison
	 * because the TARGET element is null. In this case, merger optimization
	 * will not process the container and will not compare every child because
	 * it knows it will be MISSING_TARGET as well.<br>
	 * This alternate method rebuilds a comparison item set with the
	 * MISSING_TARGET information to be able to generate elements.
	 * 
	 * @param itemsToGenerate
	 *            collection of {@link IComparisonItem} to fill
	 * @param module
	 *            module to generate, this method will inject all matching
	 *            content as items to generate with a {@link DifferenceType} of
	 *            MISSING_TARGET (thus causing full SQL generation) and will
	 *            process nested {@link IVersionContainer} recursively
	 * @param artefactRefMap
	 *            map of {@link IArtefact} that need to be generated
	 */
	private void fillItemsToGenerate(Collection<IComparisonItem> itemsToGenerate,
			IVersionContainer module, Map<IReference, IArtefact> artefactRefMap) {
		for (IVersionable<?> v : module.getContents()) {
			final IArtefact artefact = artefactRefMap.get(v.getReference());
			if (artefact != null) {
				final IComparisonItem result = new ComparisonResult(v, null,
						ComparisonScope.REPOSITORY);
				result.getMergeInfo().setMergeProposal(result.getSource());
				itemsToGenerate.add(result);
			} else if (v.getType() == IElementType.getInstance(IVersionContainer.TYPE_ID)) {
				fillItemsToGenerate(itemsToGenerate, (IVersionContainer) v, artefactRefMap);
			}
		}
	}

	private Map<IReference, IArtefact> hashArtefactReferences(Collection<IArtefact> artefacts) {
		Map<IReference, IArtefact> refMap = new HashMap<IReference, IArtefact>();
		for (IArtefact a : artefacts) {
			refMap.put(a.getUnderlyingReference(), a);
		}
		return refMap;
	}

	private static String getGenerationName(IVersionContainer container, IVersionInfo release) {
		return container.getName().toLowerCase() + "_" + release.getLabel(); //$NON-NLS-1$
	}

	private Collection<DBVendor> getVendorsToGenerate(IVersionContainer container) {
		// Specific check to generate all vendors for JDBC-generic modules
		if (container.getDBVendor() == DBVendor.JDBC) {
			return Arrays.asList(DBVendor.values());
		} else {
			return Arrays.asList(container.getDBVendor());
		}
	}

	@Override
	public void addDeliveryGenerations(IDeliveryModule module,
			Collection<IGenerationResult> generations, IProgressMonitor m) {
		SubMonitor monitor = SubMonitor.convert(m);
		monitor.beginTask(BengMessages.getString("service.delivery.injectingArtefacts"), 100); //$NON-NLS-1$
		List<VendorScript> scripts = checkPrerequisites(module, generations, monitor.newChild(30));
		monitor.setWorkRemaining(70);
		addScriptsToModule(module, scripts, monitor.newChild(60));
		monitor.setWorkRemaining(10);
		addDatafileGenerations(module, generations, monitor.newChild(10));
		monitor.done();
	}

	private List<VendorScript> checkPrerequisites(IDeliveryModule module,
			Collection<IGenerationResult> generations, IProgressMonitor monitor) {
		monitor.beginTask(
				BengMessages.getString("service.delivery.prerequisiteCheck"), generations.size()); //$NON-NLS-1$
		List<VendorScript> scripts = new ArrayList<VendorScript>();
		for (IGenerationResult r : generations) {
			Collection<ISQLScript> resultScripts = r.buildScript();
			if (resultScripts == null) {
				resultScripts = Collections.emptyList();
			}
			for (ISQLScript s : resultScripts) {
				// This loop is a safety check because the UI service should
				// have cleaned everything
				for (IDeliveryItem<?> item : module.getDeliveryItems()) {
					if (item.getName().equals(s.getName())) {
						throw new ErrorException(
								BengMessages.getString("service.delivery.existingScriptConflict") //$NON-NLS-1$
										+ s.getName());
					}
				}
				// Here we attach a script to a vendor to be able
				// to retrieve this association later when generating
				// DeliverySQLScript
				// which needs the vendor
				scripts.add(new VendorScript(s, r.getVendor()));
			}
			monitor.worked(1);
		}
		return scripts;
	}

	private void addScriptsToModule(IDeliveryModule module, List<VendorScript> scripts,
			IProgressMonitor monitor) {
		monitor.beginTask(BengMessages.getString("service.delivery.addingScripts"), scripts.size()); //$NON-NLS-1$
		// Adding scripts to module
		for (VendorScript vendorScript : scripts) {
			final ISQLScript s = vendorScript.getScript();
			if (s != null) {
				// Saving script
				CorePlugin.getIdentifiableDao().save(s);
				// Adding to module (this is where we need the vendor from the
				// VendorScript)
				DeliveryType type = DeliveryType.DDL;
				switch (s.getScriptType()) {
				case DATA:
					type = DeliveryType.DATA;
					break;
				case DATADEL:
					type = DeliveryType.DATADEL;
					break;
				}
				module.addDeliveryItem(new DeliverySQLScript(s, type, vendorScript.getVendor()));
			}
			monitor.worked(1);
		}
	}

	private void addDatafileGenerations(IDeliveryModule module,
			Collection<IGenerationResult> generations, IProgressMonitor monitor) {
		monitor.beginTask(
				BengMessages.getString("service.delivery.addingDatafiles"), generations.size()); //$NON-NLS-1$
		for (IGenerationResult r : generations) {
			for (IDatafileGeneration g : r.getDatafilesGenerations()) {
				for (IRepositoryFile f : g.getDataFiles()) {
					// For Oracle, datafiles are SQL*loader artefact, for Mysql
					// they are simply
					// resources
					ArtefactType type = (DBGMHelper.getCurrentVendor() == DBVendor.ORACLE ? ArtefactType.SQLLOAD
							: ArtefactType.MYSQLLOAD);
					DeliveryFile file = new DeliveryFile(f, DeliveryType.DATA, type,
							g.getControlFileHeader(), r.getVendor());
					module.addDeliveryItem(file);
				}
			}
			monitor.worked(1);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public IDeliveryModule loadDelivery(IVersionInfo release) {
		List<IDeliveryModule> deliveries = (List<IDeliveryModule>) CorePlugin.getIdentifiableDao()
				.loadForeignKey(IDeliveryModule.class, release.getUID(), "targetRelease", true); //$NON-NLS-1$
		if (deliveries != null && deliveries.size() > 0) {
			return deliveries.iterator().next();
		} else {
			return null;
		}
	}

	@Override
	public IDeliveryIncrement computeIncrement(IDeliveryModule module,
			IVersionInfo dependencyTargetRelease) {
		final IVersionInfo fromRel = module.getFromRelease();
		// Lot's of shit here to get a container instance, don't know why we
		// sometimes got a
		// volatile reference here
		// And don't have time to debug/patch.
		// Skip this piece if you only want to understand what this method do.
		//
		// Getting container target release (which should be in view)
		IVersionContainer c = null;
		boolean savedVolatileFlag = dependencyTargetRelease.getReference().isVolatile();
		try {
			dependencyTargetRelease.getReference().setVolatile(false);
			c = (IVersionContainer) VersionHelper.getReferencedItem(dependencyTargetRelease
					.getReference());
		} catch (ExternalReferenceException e) {
			// Trying to locate container from volatile
			// TODO: might be useless, we should load container from database
			// instead
			dependencyTargetRelease.getReference().setVolatile(true);
			try {
				c = (IVersionContainer) VersionHelper.getReferencedItem(dependencyTargetRelease
						.getReference());
			} catch (ExternalReferenceException ex) {
				// Trying to continue without container as it should only be
				// necessary to display
				// error messages
				c = null;
			}
		} catch (UnresolvedItemException e) {
			c = null;
		} finally {
			dependencyTargetRelease.getReference().setVolatile(savedVolatileFlag);
		}
		// Here comes the real part
		// Now processing dependencies increment
		if (fromRel != null) {
			final IDeliveryModule fromModule = loadDelivery(fromRel);
			if (fromModule != null) {
				for (IVersionInfo dependency : fromModule.getDependencies()) {
					if (dependency.getReference().getUID()
							.equals(dependencyTargetRelease.getReference().getUID())) {
						return new ModuleDeliveryIncrement(c, dependency, dependencyTargetRelease);
					}
				}
			}
		}
		return new ModuleDeliveryIncrement(c, null, dependencyTargetRelease);
	}

	@Override
	public List<IDeliveryInfo> getDeliveries(IVersionInfo fromVersion, IVersionInfo toVersion)
			throws UndeliverableIncrementException {
		return getDeliveries(new ModuleDeliveryIncrement(null, fromVersion, toVersion), false);
	}

	@Override
	public List<IDeliveryInfo> getDeliveries(IDeliveryIncrement dlvIncrement)
			throws UndeliverableIncrementException {
		return getDeliveries(dlvIncrement, true);
	}

	private List<IDeliveryInfo> getDeliveries(IDeliveryIncrement dlvIncrement,
			boolean matchVersionEquality) throws UndeliverableIncrementException {
		// If from and to releases are equal, the chain is empty
		if (VersionHelper.computeVersion(dlvIncrement.getFromRelease()) == VersionHelper
				.computeVersion(dlvIncrement.getToRelease())) {
			return Collections.emptyList();
		}
		List<IDeliveryInfo> allDeliveries = deliveryDao.getAvailableDeliveries(dlvIncrement
				.getToRelease().getReference());
		if (allDeliveries == null || allDeliveries.isEmpty()) {
			final List<IDeliveryInfo> emptyList = Collections.emptyList();
			throw new UndeliverableIncrementException(dlvIncrement, emptyList,
					dlvIncrement.getFromRelease());
		}
		// Resolving the delivery chain
		return getDeliveryChain(dlvIncrement, allDeliveries, matchVersionEquality);
	}

	@Override
	public List<IDeliveryInfo> getDeliveryChain(IDeliveryIncrement dlvIncrement,
			Collection<IDeliveryInfo> allDeliveries, boolean strictVersionMatch)
			throws UndeliverableIncrementException {
		final List<IDeliveryInfo> deliveries = new ArrayList<IDeliveryInfo>();
		// Filtering only deliveries which can upgrade delivery increment source
		// to target release
		for (IDeliveryInfo i : allDeliveries) {
			if (VersionHelper.computeVersion(i.getTargetRelease()) > VersionHelper
					.computeVersion(dlvIncrement.getFromRelease())
					&& VersionHelper.computeVersion(i.getTargetRelease()) <= VersionHelper
							.computeVersion(dlvIncrement.getToRelease())) {
				deliveries.add(i);
			}
		}
		// Ordering deliveries
		Collections.sort(deliveries);
		Collections.reverse(deliveries);
		// Checking that we have a full chain
		IVersionInfo lastRelease = dlvIncrement.getToRelease();
		for (IDeliveryInfo i : new ArrayList<IDeliveryInfo>(deliveries)) {
			if (lastRelease != null && lastRelease.equals(i.getTargetRelease())) {
				lastRelease = i.getSourceRelease();
			} else {
				deliveries.remove(i);
			}
		}
		Collections.reverse(deliveries);
		if (lastRelease != null) {
			if (lastRelease.equals(dlvIncrement.getFromRelease())) {
				return deliveries;
			} else if (!strictVersionMatch) {
				// If a full version equality is not required, we only match
				// version numbers
				// This is what happens during a headless build when branches
				// and status are
				// truncated
				if (VersionHelper.computeVersion(lastRelease) == VersionHelper
						.computeVersion(dlvIncrement.getFromRelease())) {
					return deliveries;
				}
			}
		} else if (dlvIncrement.getFromRelease() == null) {
			return deliveries;
		}
		throw new UndeliverableIncrementException(dlvIncrement, deliveries, lastRelease);
	}

	@Override
	public IDeliveryModule loadDelivery(IVersionable<IVersionContainer> v) {
		return loadDelivery(v.getVersion());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IVersionInfo> buildDependencies(List<IVersionInfo> processed, IDeliveryModule module) {
		IVersionInfo moduleRelease = module.getTargetRelease();
		if (processed.contains(moduleRelease)) {
			return Collections.EMPTY_LIST;
		} else {
			processed.add(moduleRelease);
		}
		// FIXME transform the collection to IVersionable<IVersionContainer>
		// collection
		ListOrderedSet containers = new ListOrderedSet();
		// containers.addAll(getContainerDependencies(moduleContainer));
		for (IVersionInfo vc : module.getDependencies()) {
			containers.add(vc);
		}
		for (IVersionInfo c : new ArrayList<IVersionInfo>(containers)) {
			final IDeliveryModule depModule = loadDelivery(c);
			if (depModule != null) {
				containers.addAll(0, buildDependencies(processed, depModule));
			} else {
				throw new ErrorException(BengMessages.getString("missingDependentDelivery")); //$NON-NLS-1$
			}
		}
		// containers.add(moduleRelease);
		return containers.asList();
	}

	@Override
	public IDeliveryItem<ISQLScript> createDeliveryScript(DeliveryType type, ISQLScript script) {
		final DeliverySQLScript dlvScript = new DeliverySQLScript(script, type, null);
		return dlvScript;
	}

	@Override
	public List<IDeliveryInfo> getDeliveries(String moduleName, String versionPattern,
			IMatchingStrategy matchingStrategy) {
		// Retrieving module reference
		final Collection<IReference> moduleRefs = deliveryDao.lookupModuleName(moduleName);

		// Iterating over every module : if only one module resolves, then it is
		// ok, otherwise
		// we keep track of matching modules so that we could throw an explicit
		// exception to the
		// user
		final Collection<IReference> matchingModules = new ArrayList<IReference>();
		List<IDeliveryInfo> deliveries = Collections.emptyList();
		for (IReference moduleRef : moduleRefs) {
			// Retrieving deliveries matching for this module
			final List<IDeliveryInfo> moduleDeliveries = getDeliveriesFor(moduleRef,
					versionPattern, matchingStrategy);
			// If found we register this module as matching
			if (!moduleDeliveries.isEmpty()) {
				matchingModules.add(moduleRef);
				deliveries = moduleDeliveries;
			}
		}
		// If we have more than one matching module, we throw an exception
		final IIdentifiableDAO identifiableDao = CorePlugin.getService(IIdentifiableDAO.class);
		if (matchingModules.size() > 1) {
			final List<ContainerInfo> containers = new ArrayList<ContainerInfo>();
			for (IReference r : matchingModules) {
				final List<IVersionInfo> versions = (List<IVersionInfo>) identifiableDao
						.loadForeignKey(VersionInfo.class, r.getUID(), "reference", true, true);
				// Reverse sort
				Collections.sort(versions, Collections.reverseOrder());
				// Taking last module version
				final UID containerLastId = versions.iterator().next().getUID();
				final ContainerInfo container = (ContainerInfo) identifiableDao.load(
						ContainerInfo.class, containerLastId);
				containers.add(container);
			}
			throw new UnresolvableDeliveryException(containers);
		} else if (matchingModules.isEmpty()) {
			throw new UnresolvableDeliveryException(null);
		} else {
			return deliveries;
		}

	}

	@Override
	public List<IDeliveryInfo> getDeliveriesWithReference(String moduleRef, String versionPattern,
			IMatchingStrategy matchingStrategy) throws ReferenceNotFoundException {
		final IIdentifiableDAO dao = CorePlugin.getIdentifiableDao();
		try {
			final IReference ref = (IReference) dao.load(Reference.class,
					new UID(Long.valueOf(moduleRef)));
			return getDeliveriesFor(ref, versionPattern, matchingStrategy);
		} catch (Exception e) {
			throw new ReferenceNotFoundException("Unable to find reference " + moduleRef + " : "
					+ e);
		}
	}

	/**
	 * Internal factorized method that gets deliveries from a module reference
	 * and version pattern
	 * 
	 * @param moduleRef
	 *            the module unique {@link IReference} to search deliveries for
	 * @param versionPattern
	 *            the string pattern corresponding to the version to match
	 * @param matchingStrategy
	 *            the {@link IMatchingStrategy} to use for matching versions
	 * @return the list of resolved {@link IDeliveryInfo}
	 */
	private List<IDeliveryInfo> getDeliveriesFor(IReference moduleRef, String versionPattern,
			IMatchingStrategy matchingStrategy) {
		// Retrieving deliveries information for this module
		final List<IDeliveryInfo> deliveries = deliveryDao.getAvailableDeliveries(moduleRef);
		// Fallbacking to default strategy
		if (matchingStrategy == null) {
			try {
				matchingStrategy = getMatchingStrategy(DEFAULT_MATCHING_STRATEGY_ID);
			} catch (InvalidStrategyException e) {
				// This should never happen
				throw new ErrorException("Cannot get default matching strategy", e);
			}
		}
		// Computing version upper bound
		final long versionUpperBound = matchingStrategy.computeVersionBound(versionPattern);

		List<IDeliveryInfo> filteredDeliveries = new ArrayList<IDeliveryInfo>();
		for (IDeliveryInfo dlv : deliveries) {
			final IVersionInfo targetRel = dlv.getTargetRelease();
			final long targetRelNumber = VersionHelper.computeVersion(targetRel);
			if (matchingStrategy.matches(targetRelNumber, versionUpperBound)) {
				filteredDeliveries.add(dlv);
			}
		}
		return filteredDeliveries;
	}

	@Override
	public IArtefact createArtefact() {
		return new Artefact();
	}

	/**
	 * @param deliveryDao
	 *            the deliveryDao to set
	 */
	public void setDeliveryDao(IDeliveryDao deliveryDao) {
		this.deliveryDao = deliveryDao;
	}

	@Override
	public IMatchingStrategy getMatchingStrategy(String strategyCode)
			throws InvalidStrategyException {
		// Basic simple implementation
		if ("STRICT".equalsIgnoreCase(strategyCode)) {
			return new StrictMatchingStrategy();
		} else if (DEFAULT_MATCHING_STRATEGY_ID.equalsIgnoreCase(strategyCode)
				|| strategyCode == null || "".equals(strategyCode)) {
			return new DefaultMatchingStrategy();
		}
		throw new InvalidStrategyException("Invalid matching strategy " + strategyCode);
	}

	/**
	 * @param generationService
	 *            the generationService to set
	 */
	public void setGenerationService(IGenerationService generationService) {
		this.generationService = generationService;
	}
}
