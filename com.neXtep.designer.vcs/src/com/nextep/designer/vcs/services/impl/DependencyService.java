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
package com.nextep.designer.vcs.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.vcs.VCSMessages;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.services.IDependencyService;

public class DependencyService implements IDependencyService {

	private final static Log log = LogFactory.getLog(DependencyService.class);

	@Override
	public boolean checkDeleteAllowed(IReferenceable ref, Collection<IReferencer> deps,
			Collection<IReferencer> deletedReferencers) {
		// if (!Designer.getPreferenceStore().getBoolean(DesignerCoreConstants.FORCE_DELETE)) {
		final Collection<IReferencer> dependencies = getReferencersAfterDeletion(ref, deps,
				deletedReferencers);
		if (!dependencies.isEmpty()) {
			String dependencyNamesList = ""; //$NON-NLS-1$
			for (IReferencer r : dependencies) {
				dependencyNamesList += "- " + NameHelper.getQualifiedName(r) + "\r\n"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			throw new ErrorException(MessageFormat.format(VCSMessages
					.getString("service.dependency.cannotDelete"), //$NON-NLS-1$
					NameHelper.getQualifiedName(ref), dependencyNamesList));
		}
		// } else {
		// log.warn("Removing " + Designer.getInstance().getQualifiedName(ref)
		// + " using FORCE DELETE.");
		// }
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean checkDeleteAllowed(IReferenceable ref) {
		// New compilation bug, cannot use Collections.emptyList() here
		return checkDeleteAllowed(ref, Collections.EMPTY_LIST);
	}

	@Override
	public boolean checkDeleteAllowed(IReferenceable ref, Collection<IReferencer> deletedReferencers) {
		Collection<IReferencer> dependencies = CorePlugin.getService(IReferenceManager.class)
				.getReverseDependencies(ref);
		return checkDeleteAllowed(ref, dependencies, deletedReferencers);
	}

	@Override
	public Collection<IReferencer> getReferencersAfterDeletion(IReferenceable ref,
			Collection<IReferencer> deps, Collection<IReferencer> deletedReferencers) {
		// Copying collection because we will alter it
		Collection<IReferencer> dependencies = new ArrayList<IReferencer>(deps);
		if (dependencies != null && dependencies.size() > 0) {
			// If the removed object is a reference container, we remove dependencies
			// which are declared by the removed object. Only to allow removal of objects
			// which have only dependencies to themselves.
			if (ref instanceof IReferenceContainer) {
				Collection<IReference> declaredRefs = new ArrayList<IReference>();
				declaredRefs.addAll(((IReferenceContainer) ref).getReferenceMap().keySet());
				declaredRefs.add(ref.getReference());
				for (IReferencer depRef : new ArrayList<IReferencer>(dependencies)) {
					if (depRef instanceof IReferenceable
							&& declaredRefs.contains(((IReferenceable) depRef).getReference())) {
						dependencies.remove(depRef);
					}
					// Also removing residual container references
					if (depRef instanceof ITypedObject
							&& ((ITypedObject) depRef).getType() == IElementType
									.getInstance(IVersionContainer.TYPE_ID)) {
						dependencies.remove(depRef);
					}
				}
			}
			dependencies.removeAll(deletedReferencers);
			// Removing encapsulating dependencies:
			// A table containing a FK will generate 2 dependencies, one from the table, the
			// other from the FK
			// For all remaining dependencies
			for (IReferencer r : new ArrayList<IReferencer>(dependencies)) {
				// If one is a container of referenceable instances
				if (r instanceof IReferenceContainer) {
					// Then we check if this instance is one of the deleted referencers,
					// in which case we "assume"
					// that the upper dependency is no longer valid
					final Collection<IReferenceable> containedItems = ((IReferenceContainer) r)
							.getReferenceMap().values();
					for (IReferenceable item : containedItems) {
						if (deletedReferencers.contains(item)) {
							dependencies.remove(r);
						}
						if (dependencies.contains(item)) {
							dependencies.remove(r);
						}
					}
				}
			}
		}
		return dependencies;
	}

	@Override
	public List<IReferencer> getDirectlyDependentObjects(IReferenceable referenceable) {
		// Building the reverse dependencies map
		final MultiValueMap invRefMap = CorePlugin.getService(IReferenceManager.class).getReverseDependenciesMap();
		return getDirectlyDependentObjects(invRefMap, referenceable);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<IReferencer> getDirectlyDependentObjects(MultiValueMap invRefMap,
			IReferenceable referenceable) {
		// Building inner referencers list as this method will not return internal dependencies
		List<IReferencer> innerReferencers = new ArrayList<IReferencer>();
		if (referenceable instanceof IReferencer) {
			innerReferencers.add((IReferencer) referenceable);
		}
		if (referenceable instanceof IReferenceContainer) {
			final Map<IReference, IReferenceable> referenceMap = ((IReferenceContainer) referenceable)
					.getReferenceMap();
			for (IReferenceable child : referenceMap.values()) {
				if (child instanceof IReferencer) {
					innerReferencers.add((IReferencer) child);
				}
			}
		}
		// Checking items removal
		// Retrieving this object's reverse dependencies
		Collection<IReferencer> revDeps = (Collection<IReferencer>) invRefMap
				.getCollection(referenceable.getReference());
		if (revDeps == null) {
			revDeps = Collections.emptySet();
		}

		// Retrieving referencers
		Collection<IReferencer> dependencies = getReferencersAfterDeletion(referenceable, revDeps,
				innerReferencers);
		List<IReferencer> directDependencies = new ArrayList<IReferencer>(dependencies);
		// Removing encapsulating dependencies : among all found dependencies, we remove those that
		// contains the others so that we only extract the 'direct' dependencies without
		// transitivity.
		// TODO: Check whether this processing should be done in getReferencersAfterDeletion or not
		// (regression check)
		for (IReferencer dep : dependencies) {
			if (dep instanceof IReferenceContainer) {
				// Getting all contained refs
				final Map<IReference, IReferenceable> containedRefs = ((IReferenceContainer) dep)
						.getReferenceMap();
				// We check wether any of this contained element is a depedency
				for (IReferencer r : dependencies) {
					if (containedRefs.values().contains(r)) {
						// If so, the current IReferencer is not a "direct" dependency so we remove
						// it
						directDependencies.remove(dep);
					}
				}
			}
		}
		return directDependencies;
	}
}
