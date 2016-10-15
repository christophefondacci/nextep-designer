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
package com.nextep.designer.synch.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.model.DifferenceType;
import com.nextep.designer.vcs.model.IComparisonItem;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This helper provides some convenience methods to deal with synchronization references and the
 * match between database references and workspace references.<br>
 * This helper is mainly used by the reverse synchronization, but could have a larger scope.
 * 
 * @author Christophe Fondacci
 */
public class SynchronizationHelper {

	/**
	 * Computes the mapping between source and target references (generally coming from database
	 * capture). Each comparison item which has a target equivalent (either EQUALS or DIFFER) will
	 * be mapped. This method is intended to be used while replacing external dependecies from a
	 * database capture to repository dependencies if a corresponding item exists in the repository.
	 * 
	 * @param items item to hash
	 * @return a map containing reference indexing. A call to map.get(sourceReference) will return
	 *         the corresponding target reference.
	 */
	public static Map<IReference, IReference> buildSourceReferenceMapping(IComparisonItem... items) {
		Map<IReference, IReference> refMap = new HashMap<IReference, IReference>();
		for (IComparisonItem item : items) {
			fillReferenceMapping(refMap, item, true);
		}
		return refMap;
	}

	/**
	 * Computes the mapping between source and target references (generally coming from database
	 * capture). Each comparison item which has a source equivalent (either EQUALS or DIFFER) will
	 * be mapped. This method is intended to be used while replacing external dependecies from a
	 * database capture to repository dependencies if a corresponding item exists in the repository.
	 * 
	 * @param items item to hash
	 * @return a map containing reference indexing. A call to map.get(targetReference) will return
	 *         the corresponding source reference.
	 */
	public static Map<IReference, IReference> buildTargetReferenceMapping(IComparisonItem... items) {
		Map<IReference, IReference> refMap = new HashMap<IReference, IReference>();
		for (IComparisonItem item : items) {
			fillReferenceMapping(refMap, item, false);
		}
		return refMap;
	}

	/**
	 * Internal method used to recursively build reference matching from IComparisonItem.
	 * 
	 * @param extRefMap reference map to fill through recursive calls
	 * @param item item to process
	 * @param hashBySource hash elements by their source reference, or by their target reference
	 *        when set to <code>false</code>
	 */
	private static void fillReferenceMapping(Map<IReference, IReference> extRefMap,
			IComparisonItem item, boolean hashBySource) {
		if (item.getDifferenceType() != DifferenceType.MISSING_SOURCE
				&& item.getDifferenceType() != DifferenceType.MISSING_TARGET
				&& item.getType() != IElementType.getInstance(IReference.TYPE_ID)) {
			if (hashBySource) {
				if (item.getSource() != null) {
					extRefMap.put(item.getSource().getReference(), item.getTarget().getReference());
				}
			} else {
				if (item.getTarget() != null) {
					extRefMap.put(item.getTarget().getReference(), item.getSource().getReference());
				}
			}
			for (IComparisonItem subItem : item.getSubItems()) {
				fillReferenceMapping(extRefMap, subItem, hashBySource);
			}
		}
	}

	/**
	 * Replaces dependencies of all imported objects via the reference map. Any reference dependency
	 * will be replaced by the <code>extRefMap.get(ref)</code> equivalent, if (and only if) it
	 * exists.<br>
	 * This method should be used when integrating partial database imports into a non-empty
	 * container.<br>
	 * It is <b>very important</b> that the <code>extRefMap</code> map contains a full view
	 * comparison, otherwise, external references would be created.
	 * 
	 * @param imports elements to import
	 * @param extRefMap a map of repository references hashed by their corresponding database
	 *        reference (use the
	 *        {@link SynchronizationHelper#buildSourceReferenceMapping(IComparisonItem...)} to
	 *        initialize this map).
	 * @param removeExistingImports a flag indicating if objects from the <code>imports</code> list
	 *        should be removed when they already exists in the repository (a extRefMap entry
	 *        exists), and should therefore not be imported.
	 */
	public static void replaceDependencies(List<IVersionable<?>> imports,
			Map<IReference, IReference> extRefMap, boolean removeExistingImports) {
		for (IVersionable<?> imported : new ArrayList<IVersionable<?>>(imports)) {
			// If we want to remove imported objects when they already
			// exists in the repository, we check if we have an entry in the map
			if (removeExistingImports) {
				if (extRefMap.get(imported.getReference()) != null) {
					// If so, we remove it and skip to next imported object
					imports.remove(imported);
					continue;
				}
			}
			replaceDependency(imported, extRefMap);
		}
	}

	public static void replaceDependency(IVersionable<?> imported,
			Map<IReference, IReference> extRefMap) {
		final Map<IReference, IReferenceable> intRefMap = imported.getReferenceMap();
		for (IReference r : intRefMap.keySet()) {
			// Matching our external reference with a repository reference
			final IReference repositoryRef = extRefMap.get(r);
			// Switching external => repository reference
			if (repositoryRef != null) {
				final IReferenceable instance = intRefMap.get(r);
				instance.setReference(repositoryRef);
			}
		}
		// Reference management: since we have a partial import, imported objects
		// may reference external objects which we must map in our current view
		for (IReference r : imported.getReferenceDependencies()) {
			// Matching our external reference with a repository reference
			final IReference repositoryRef = extRefMap.get(r);
			// Switching external => repository reference
			if (repositoryRef != null) {
				imported.updateReferenceDependencies(r, repositoryRef);
			}
		}

	}
}
