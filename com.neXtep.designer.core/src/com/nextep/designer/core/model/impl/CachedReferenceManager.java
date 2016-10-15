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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.core.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.hibernate.Session;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ReferenceContext;
import com.nextep.designer.core.model.DependenciesMap;
import com.nextep.designer.core.model.IInternalReferenceManager;

/**
 * This reference manager handles caching of the reference map to provide optimal performances.
 * 
 * @author Christophe Fondacci
 */
public class CachedReferenceManager implements IInternalReferenceManager {

	private IInternalReferenceManager referenceManager;
	private DependenciesMap cachedDependencies = null;
	private boolean isValid = false;

	public List<IReferenceable> getReferencedItems(IReference ref) {
		return referenceManager.getReferencedItems(ref);
	}

	public void reference(IReference ref, IReferenceable refInstance) {
		referenceManager.reference(ref, refInstance);
		isValid = false;
	}

	public void reference(IReference ref, IReferenceable refInstance, boolean updateIfExists) {
		referenceManager.reference(ref, refInstance, updateIfExists);
		isValid = false;
	}

	public void volatileReference(IReference ref, IReferenceable refInstance, Session session) {
		referenceManager.volatileReference(ref, refInstance, session);
	}

	public void flushVolatiles(Session session) {
		referenceManager.flushVolatiles(session);
	}

	public void flush() {
		isValid = false;
		referenceManager.flush();
	}

	public void dereference(IReferenceable instance) {
		referenceManager.dereference(instance);
		isValid = false;
	}

	public Collection<IReferencer> getReverseDependencies(IReferenceable ref) {
		return getReverseDependencies(ref, null);
	}

	public Collection<IReferencer> getReverseDependencies(IReferenceable ref, IElementType type) {
		if (ref.getReference().isVolatile() || type != null) {
			return referenceManager.getReverseDependencies(ref, type);
		} else {
			validate();
			Collection<IReferencer> refs = cachedDependencies.getCollection(ref.getReference());
			return refs == null ? Collections.EMPTY_LIST : refs;
		}

	}

	public DependenciesMap getReverseDependenciesMap() {
		validate();
		return cachedDependencies;
	}

	public DependenciesMap getReverseDependenciesMapFor(IReference r) {
		if (r.isVolatile()) {
			return referenceManager.getReverseDependenciesMapFor(r);
		} else {
			validate();
			Collection<IReferencer> referencers = cachedDependencies.getCollection(r);
			DependenciesMap map = new DependenciesMap();
			map.putAll(r, referencers);
			return map;
		}
	}

	public DependenciesMap getReverseDependenciesMap(IElementType type) {
		return referenceManager.getReverseDependenciesMap(type);
	}

	public IReferenceable findByTypeName(IElementType type, String name)
			throws ReferenceNotFoundException {
		return referenceManager.findByTypeName(type, name);
	}

	public IReferenceable findByTypeName(IElementType type, String name, boolean ignoreCase)
			throws ReferenceNotFoundException {
		return referenceManager.findByTypeName(type, name, ignoreCase);
	}

	public ReferenceContext getReferenceContext(Session s) {
		return referenceManager.getReferenceContext(s);
	}

	public void addReferencer(IReferencer referencer) {
		referenceManager.addReferencer(referencer);
	}

	public void removeReferencer(IReferencer referencer) {
		referenceManager.removeReferencer(referencer);
	}

	@Override
	public void fillDependenciesFromObject(DependenciesMap dependenciesMap, Object o,
			IElementType typeRestriction) {
		referenceManager.fillDependenciesFromObject(dependenciesMap, o, typeRestriction);
	}

	@Override
	public DependenciesMap getReverseDependenciesMap(DependenciesMap localRefMap, IElementType type) {
		return referenceManager.getReverseDependenciesMap(localRefMap, type);
	}

	private void validate() {
		if (!isValid || cachedDependencies == null) {
			cachedDependencies = referenceManager.getReverseDependenciesMap();
			isValid = true;
		}
	}

	/**
	 * @param referenceManager the referenceManager to set
	 */
	public void setReferenceManager(IInternalReferenceManager referenceManager) {
		this.referenceManager = referenceManager;
	}

	@Override
	public void startWorkspaceLoad() {
		referenceManager.startWorkspaceLoad();
	}

	@Override
	public void endWorkspaceLoad() {
		referenceManager.endWorkspaceLoad();
	}

}
