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
package com.nextep.designer.sqlgen.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.sqlgen.model.DatabaseReference;
import com.nextep.datadesigner.sqlgen.model.IDatafileGeneration;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.model.ISqlScriptBuilder;
import com.nextep.designer.sqlgen.services.IGenerationService;

/**
 * The {@link IGenerationResult} default implementation.
 * 
 * @author Christophe Fondacci
 */
public class GenerationResult implements IGenerationResult {

	private List<ISQLScript> additions;
	private List<ISQLScript> updates;
	private List<ISQLScript> drops;
	private Collection<DatabaseReference> preconditions;
	private Map<ISQLScript, IGenerationResult> integratedResults;
	private Map<DatabaseReference, ISQLScript> addedReferences;
	private Map<DatabaseReference, ISQLScript> updatedReferences;
	private Map<DatabaseReference, ISQLScript> droppedReferences;
	private Map<DatabaseReference, IDatafileGeneration> datafileGeneration;
	private String name;
	private String description;
	private DBVendor vendor;

	/**
	 * Creates a new {@link GenerationResult} bean
	 */
	public GenerationResult() {
		additions = new ArrayList<ISQLScript>();
		updates = new ArrayList<ISQLScript>();
		drops = new ArrayList<ISQLScript>();
		preconditions = new ArrayList<DatabaseReference>();
		integratedResults = new HashMap<ISQLScript, IGenerationResult>();
		addedReferences = new HashMap<DatabaseReference, ISQLScript>();
		droppedReferences = new HashMap<DatabaseReference, ISQLScript>();
		updatedReferences = new HashMap<DatabaseReference, ISQLScript>();
		datafileGeneration = new HashMap<DatabaseReference, IDatafileGeneration>();
	}

	public GenerationResult(String name) {
		this();
		this.name = name;
	}

	@Override
	public void addAdditionScript(DatabaseReference ref, ISQLScript script) {
		if (!addedReferences.containsKey(ref) && script != null) {
			additions.add(script);
			addedReferences.put(ref, script);
		}
	}

	@Override
	public void addDropScript(DatabaseReference ref, ISQLScript script) {
		if (!droppedReferences.containsKey(ref) && script != null) {
			drops.add(script);
			droppedReferences.put(ref, script);
		}
	}

	@Override
	public void addUpdateScript(DatabaseReference ref, ISQLScript script) {
		if (!updatedReferences.containsKey(ref) && script != null) {
			updates.add(script);
			updatedReferences.put(ref, script);
		}
	}

	@Override
	public List<ISQLScript> getAdditions() {
		// Sorting results
		additions = getSortedScripts(additions, getAddedReferences());
		return additions;
	}

	@Override
	public List<ISQLScript> getDrops() {
		// Sorting results
		drops = getSortedScripts(drops, getDroppedReferences());
		Collections.reverse(drops);
		return drops;
	}

	/**
	 * Builds the sorted list of scripts from the specified list. Scripts are sorted from their
	 * dependencies through generation results precondition information.<br>
	 * <b>Important note :</b> because dependency sort cannot base its algorithm on any kind of
	 * transitivity, this sort is not optimized, very time consuming and should be performed only
	 * when needed.
	 * 
	 * @param scripts the list of {@link ISQLScript} to sort
	 * @param refMap the corresponding script reference map (a map of scripts hashed by their
	 *        {@link DatabaseReference}
	 * @return the list with the same {@link ISQLScript}, sorted so that the less dependent script
	 *         is created first and the most dependent is last
	 */
	private List<ISQLScript> getSortedScripts(List<ISQLScript> scripts,
			Map<DatabaseReference, ISQLScript> refMap) {
		List<ISQLScript> sortedScripts = new ArrayList<ISQLScript>();
		List<ISQLScript> processedScripts = new ArrayList<ISQLScript>();
		for (ISQLScript s : scripts) {
			fillSortedScriptMap(sortedScripts, processedScripts, s, refMap);
		}
		return sortedScripts;
	}

	/**
	 * This method recursively fills the sorted scripts list according to the dependency hierarchy.
	 * 
	 * @param sortedScripts the {@link ISQLScript} sorted list to fill
	 * @param processed a list of {@link ISQLScript} corresponding to the scripts that have already
	 *        been processed by this method. This distinct list exists because scripts need to be
	 *        tagged <b>before</b> they are added to the sorted list in order to avoid infinite loop
	 *        on circular dependencies
	 * @param script the {@link ISQLScript} to process
	 * @param refMap a map of all {@link ISQLScript} that are being processed, hashed by their
	 *        {@link DatabaseReference}
	 */
	private void fillSortedScriptMap(List<ISQLScript> sortedScripts, List<ISQLScript> processed,
			ISQLScript script, Map<DatabaseReference, ISQLScript> refMap) {
		if (!processed.contains(script)) {
			// Specific list to handle processing (unsorted) to avoid infinite loops
			processed.add(script);
			// Getting the IGenerationResult corresponding to the script so that we can have access
			// to preconditions
			final IGenerationResult result = integratedResults.get(script);
			if (result != null) {
				// Processing preconditions
				for (DatabaseReference r : result.getPreconditions()) {
					final ISQLScript scriptDependency = refMap.get(r);
					if (scriptDependency != null) {
						fillSortedScriptMap(sortedScripts, processed, scriptDependency, refMap);
					}
				}
			}
			sortedScripts.add(script);
		}
	}

	@Override
	public List<ISQLScript> getUpdates() {
		// Sorting results
		updates = getSortedScripts(updates, getUpdatedReferences());
		return updates;
	}

	@Override
	public void addPrecondition(DatabaseReference ref) {
		preconditions.add(ref);
	}

	@Override
	public Collection<DatabaseReference> getPreconditions() {
		return preconditions;
	}

	@Override
	public Collection<DatabaseReference> getGeneratedReferences() {
		Collection<DatabaseReference> generatedRefs = new HashSet<DatabaseReference>();
		generatedRefs.addAll(addedReferences.keySet());
		generatedRefs.addAll(updatedReferences.keySet());
		return generatedRefs;
	}

	@Override
	public void integrate(IGenerationResult childResult) {
		if (childResult == null)
			return;

		Map<ISQLScript, DatabaseReference> revMap = reverseMap(childResult.getAddedReferences());
		for (ISQLScript s : childResult.getAdditions()) {
			this.addAdditionScript(revMap.get(s), s);
			integratedResults.put(s, childResult);
		}
		revMap = reverseMap(childResult.getUpdatedReferences());
		for (ISQLScript s : childResult.getUpdates()) {
			this.addUpdateScript(revMap.get(s), s);
			integratedResults.put(s, childResult);
		}
		revMap = reverseMap(childResult.getDroppedReferences());
		for (ISQLScript s : childResult.getDrops()) {
			this.addDropScript(revMap.get(s), s);
			integratedResults.put(s, childResult);
		}
		Map<IDatafileGeneration, DatabaseReference> revFileMap = reverseMap(childResult
				.getLoadedReferences());
		for (IDatafileGeneration g : childResult.getDatafilesGenerations()) {
			addDatafileGeneration(revFileMap.get(g), g);
		}

		getPreconditions().addAll(childResult.getPreconditions());
		getGeneratedReferences().addAll(childResult.getGeneratedReferences());
	}

	private <V> Map<V, DatabaseReference> reverseMap(Map<DatabaseReference, V> map) {
		Map<V, DatabaseReference> revMap = new HashMap<V, DatabaseReference>();
		for (DatabaseReference r : map.keySet()) {
			revMap.put(map.get(r), r);
		}
		return revMap;
	}

	/**
	 * A comparator to order the results by their dependencies. Non dependent object will be placed
	 * first, most dependent objects last. <br>
	 * Note: this class has a natural ordering that is inconsistent with equals.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(IGenerationResult o) {
		// Quick comparison result in case of no precondition object
		if (this.getPreconditions().isEmpty()) {
			return -1;
		} else if (o.getPreconditions().isEmpty()) {
			return 1;
		}
		// Checking if the generated references of this result
		// is a precondition to the compared result
		for (DatabaseReference r : this.getGeneratedReferences()) {
			if (o.getPreconditions().contains(r)) {
				return -1;
			}
		}
		// Checking if the compared result generated some of
		// this result preconditions
		for (DatabaseReference r : o.getGeneratedReferences()) {
			if (this.getPreconditions().contains(r)) {
				return 1;
			}
		}
		// Here the order has no importance, but we choose
		// to put the result having the less preconditions first
		if (o.getPreconditions().size() < this.getPreconditions().size()) {
			return 1;
		} else {
			return -1;
		}
	}

	@Override
	public Map<DatabaseReference, ISQLScript> getAddedReferences() {
		return addedReferences;
	}

	@Override
	public Map<DatabaseReference, ISQLScript> getUpdatedReferences() {
		return updatedReferences;
	}

	@Override
	public Map<DatabaseReference, ISQLScript> getDroppedReferences() {
		return droppedReferences;
	}

	@Override
	public List<ISQLScript> buildScript() {
		// Retrieving generation service
		final IGenerationService generationService = SQLGenPlugin
				.getService(IGenerationService.class);
		final ISqlScriptBuilder builder = generationService.getSqlScriptBuilder(DBGMHelper
				.getCurrentVendor());
		return builder.buildScript(this);
	}

	@Override
	public String getName() {
		return getVendor() == null ? name : (vendor.name().toLowerCase() + '.' + name);
	}

	@Override
	public void addDatafileGeneration(DatabaseReference ref, IDatafileGeneration fileGeneration) {
		datafileGeneration.put(ref, fileGeneration);
	}

	@Override
	public List<IDatafileGeneration> getDatafilesGenerations() {
		final List<IDatafileGeneration> datafiles = new ArrayList<IDatafileGeneration>(
				datafileGeneration.values());
		Collections.sort(datafiles);
		return datafiles;
	}

	@Override
	public Map<DatabaseReference, IDatafileGeneration> getLoadedReferences() {
		return datafileGeneration;
	}

	@Override
	public DBVendor getVendor() {
		return vendor;
	}

	@Override
	public void setVendor(DBVendor vendor) {
		this.vendor = vendor;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

}
