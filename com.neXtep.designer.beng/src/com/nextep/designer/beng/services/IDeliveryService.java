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
package com.nextep.designer.beng.services;

import java.util.Collection;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.beng.exception.InvalidStrategyException;
import com.nextep.designer.beng.exception.UndeliverableIncrementException;
import com.nextep.designer.beng.model.DeliveryType;
import com.nextep.designer.beng.model.IArtefact;
import com.nextep.designer.beng.model.IDeliveryIncrement;
import com.nextep.designer.beng.model.IDeliveryInfo;
import com.nextep.designer.beng.model.IDeliveryItem;
import com.nextep.designer.beng.model.IDeliveryModule;
import com.nextep.designer.beng.model.IMatchingStrategy;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionInfo;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This service provides methods to generate and manipulate a delivery module.
 * 
 * @author Christophe Fondacci
 */
public interface IDeliveryService {

	String DEFAULT_MATCHING_STRATEGY_ID = "DEFAULT"; //$NON-NLS-1$

	/**
	 * Builds the specified delivery module by generating the SQL scripts embedded in a
	 * {@link IGenerationResult} list.
	 * 
	 * @param module the {@link IDeliveryModule} to build
	 * @return a collection of resulting generation represented as {@link IGenerationResult} list
	 */
	List<IGenerationResult> build(IDeliveryModule module, IProgressMonitor monitor);

	/**
	 * Adds a set of SQL generations to the specified delivery module. This method will raise an
	 * exception whenever any generation pre-exists in the delivery module.
	 * 
	 * @param module the {@link IDeliveryModule} to inject the SQL generations into
	 * @param generations the {@link IGenerationResult} to inject into the delivery module
	 */
	void addDeliveryGenerations(IDeliveryModule module, Collection<IGenerationResult> generations,
			IProgressMonitor monitor);

	/**
	 * Builds the list of {@link IComparisonItem} representing all the version deltas to generate
	 * for the specified module.
	 * 
	 * @param module module for which we want to build the deltas to generate
	 * @param monitor a {@link IProgressMonitor} to report progress, or null if none available
	 * @return a list of {@link IComparisonItem} representing all deltas to build for this module
	 */
	List<IComparisonItem> buildDeliveryModuleItems(IDeliveryModule module, IProgressMonitor monitor);

	/**
	 * Loads the delivery module for the specified version.
	 * 
	 * @param release the {@link IVersionInfo} to load delivery for
	 * @return the corresponding {@link IDeliveryModule}, or <code>null</code> if no delivery could
	 *         be found for this version.
	 */
	IDeliveryModule loadDelivery(IVersionInfo release);

	/**
	 * Loads the delivery of the specified container in a given version.
	 * 
	 * @param v container version
	 * @return the corresponding delivery module or null if none
	 */
	IDeliveryModule loadDelivery(IVersionable<IVersionContainer> v);

	/**
	 * Computes the delivery increment which needs to be delivered for a dependency upgrade. This
	 * method will look for the initial release of this dependency in the previous module's delivery
	 * and generate the correct delivery increment. If the dependency didn't exist in the previous
	 * module release, the increment will start from scratch.
	 * 
	 * @param module delivered module
	 * @param dependencyTargetRelease target release of the dependency to generate
	 * @return the delivery increment needed to generate the dependency upgrade
	 */
	IDeliveryIncrement computeIncrement(IDeliveryModule module, IVersionInfo dependencyTargetRelease);

	/**
	 * Retrieves the deliveries which can upgrade the specified delivery increment. This method will
	 * throw a checked exception if there is no delivery combination which can upgrade the specified
	 * delivery increment.
	 * 
	 * @param dlvIncrement source / target module release to find deliveries for
	 * @return a list of deliveries which can upgrade the specified increment
	 * @throws UndeliverableIncrementException if there is no delivery which can upgrade the
	 *         increment
	 */
	List<IDeliveryInfo> getDeliveries(IDeliveryIncrement dlvIncrement)
			throws UndeliverableIncrementException;

	List<IDeliveryInfo> getDeliveries(IVersionInfo fromVersion, IVersionInfo toVersion)
			throws UndeliverableIncrementException;

	/**
	 * Resolves the delivery chain for the specified increment among the full collection of
	 * available deliveries.<br>
	 * Note that this method is exposed in the unique purpose to be unitary tested, and should
	 * generally not be called externally.
	 * 
	 * @param dlvIncrement the {@link IDeliveryIncrement} to resolve a chain for
	 * @param allDeliveries all available deliveries as a collection of {@link IDeliveryInfo}
	 * @return the delivery chain, in the appropriate sequence, able to deploy the specified
	 *         increment
	 * @throws UndeliverableIncrementException whenever a delivery chain cannot be resolved for the
	 *         specified {@link IDeliveryIncrement}
	 */
	List<IDeliveryInfo> getDeliveryChain(IDeliveryIncrement dlvIncrement,
			Collection<IDeliveryInfo> allDeliveries, boolean strictVersionMatch)
			throws UndeliverableIncrementException;

	/**
	 * Builds all dependencies of a delivery module. This method will go recusrively in every
	 * dependent module to build a flat list of all dependencies.
	 * 
	 * @param processed processed containers (for recursivity, pass an empty list first)
	 * @param module module to compute dependencies of
	 * @return a flat list of all dependencies
	 */
	List<IVersionInfo> buildDependencies(List<IVersionInfo> processed, IDeliveryModule module);

	/**
	 * Retrieves the delivery information
	 * 
	 * @param moduleName name of the module to look for
	 * @param versionPattern version pattern as a string like : major[.minor[.iteration[.patch]]]
	 * @param matchingStrategy the strategy to match found modules releases against the pattern
	 * @return the list of found {@link IDeliveryInfo} representing existing deliveries for the
	 *         requested module and version pattern
	 */
	List<IDeliveryInfo> getDeliveries(String moduleName, String versionPattern,
			IMatchingStrategy matchingStrategy);

	/**
	 * Retrieves the delivery information. Same as
	 * {@link IDeliveryService#getDeliveries(String, String, IMatchingStrategy)} except that the
	 * module is uniquely expressed through its reference.
	 * 
	 * @param moduleRef the module reference ID, expressed as a string, the actual
	 *        {@link IReference} object will be resolved by the method
	 * @param versionPattern version pattern as a string like : major[.minor[.iteration[.patch]]]
	 * @param matchingStrategy the strategy to match found modules releases against the pattern
	 * @return the list of found {@link IDeliveryInfo} representing existing deliveries for the
	 *         requested module and version pattern
	 * @throws ReferenceNotFoundException
	 */
	List<IDeliveryInfo> getDeliveriesWithReference(String moduleRef, String versionPattern,
			IMatchingStrategy matchingStrategy) throws ReferenceNotFoundException;

	/**
	 * Creates a new delivery script from the SQL script provided.
	 * 
	 * @param script the {@link ISQLScript} to wrap into a delivery script
	 * @return the {@link IDeliveryItem} holding the speicified script
	 */
	IDeliveryItem<ISQLScript> createDeliveryScript(DeliveryType type, ISQLScript script);

	/**
	 * Creates a new delivery artefact instance
	 * 
	 * @return a {@link IArtefact} instance
	 */
	IArtefact createArtefact();

	/**
	 * Retrieves the matching strategy corresponding to the given code.
	 * 
	 * @param strategyCode code of the matching strategy to get
	 * @return the corresponding {@link IMatchingStrategy}
	 */
	IMatchingStrategy getMatchingStrategy(String strategyCode) throws InvalidStrategyException;
}
