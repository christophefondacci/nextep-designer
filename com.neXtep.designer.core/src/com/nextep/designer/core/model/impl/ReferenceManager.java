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
package com.nextep.designer.core.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ReferenceNotFoundException;
import com.nextep.datadesigner.exception.TooManyReferencesException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IReferencer;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.ReferenceContext;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.DependenciesMap;
import com.nextep.designer.core.model.IInternalReferenceManager;
import com.nextep.designer.core.model.IReferenceManager;

/**
 * Default {@link IReferenceManager} implementation. Any call to the {@link IReferenceManager}
 * methods will recompute all the dependencies from the currently registered workspace contents.
 * 
 * @author Christophe Fondacci
 */
public class ReferenceManager implements IInternalReferenceManager {

	private static final Log log = LogFactory.getLog(ReferenceManager.class);
	private static IReferenceManager instance = null;
	/** A map of all IReferenceable instances hashed by their reference */
	private DependenciesMap refMap;
	/** Same as refMap except that these references have a short lifetime */
	private Map<ReferenceContext, DependenciesMap> volatileRefMap;
	private Collection<ContextPair> pendingContexts;
	/** Date of the last session flush for reference contexts management */
	private Date lastFlushTime = new Date();
	private Set<IReferencer> explicitReferencers;
	private boolean isWorkspaceLoading = false;

	/**
	 * Reverse dependency map: given a Reference it returns all IReferenceable objects which depend
	 * on it
	 */
	// private DependenciesMap dependenciesMap;
	public ReferenceManager() {
		refMap = new DependenciesMap();
		volatileRefMap = new HashMap<ReferenceContext, DependenciesMap>();
		pendingContexts = new ArrayList<ContextPair>();
		explicitReferencers = new HashSet<IReferencer>();
	}

	/**
	 * This class allow to store {@link ReferenceContext} / {@link IReferenceable} pairs. Those
	 * pairs are stacked in a buffer when the {@link IReference} of the instance has not been yet
	 * initialized.<br>
	 * Pairs are reprocess during revalidation of the references.
	 */
	private static class ContextPair {

		private ReferenceContext context;
		private IReferenceable instance;

		public ContextPair(ReferenceContext context, IReferenceable instance) {
			this.context = context;
			this.instance = instance;
		}

		public ReferenceContext getContext() {
			return context;
		}

		public IReferenceable getReferenceable() {
			return instance;
		}
	}

	/**
	 * Retrieves all {@link IReferenceable} instances which points to this reference. This method
	 * handles volatile and persistent references.
	 * 
	 * @param ref reference used by the searched instances
	 * @return a collection of {@link IReferenceable} instances which return the specified reference
	 *         when calling the {@link IReferenceable#getReference()} method.
	 */
	@Override
	public List<IReferenceable> getReferencedItems(IReference ref) {
		if (ref == null) {
			throw new UnresolvedItemException("Cannot resolve a null reference");
		}
		// Reference lookup in the appropriate reference map
		return lookupReference(ref.isVolatile() ? getVolatileMap(ref.getReferenceContext())
				: refMap, ref);
	}

	/**
	 * Performs a reference lookup in the specified reference map.
	 * 
	 * @param referenceMap reference map to use for this lookup
	 * @param ref reference to look for
	 * @return the IReferenceable instances which point to this reference
	 */
	@SuppressWarnings("unchecked")
	private List<IReferenceable> lookupReference(DependenciesMap referenceMap, IReference ref) {
		revalidate(referenceMap);
		// Retrieving current view
		List<IReferenceable> refs = (List<IReferenceable>) referenceMap.get(ref);
		// Handling null or 0-sized reference list
		if (refs == null || refs.size() == 0) {
			throw new UnresolvedItemException("Item reference not found: id="
					+ ref.getReferenceId() + " type=<"
					+ (ref.getType() == null ? "null" : ref.getType().getName())
					+ "> last known name is '" + ref.getArbitraryName() + "'");
		} else {
			return refs;
		}
	}

	/**
	 * Revalidates the reference map since Hibernate triggers the reference registration before the
	 * object has completely been loaded.
	 */
	@SuppressWarnings("unchecked")
	private void revalidate(DependenciesMap map) {
		if (!pendingContexts.isEmpty()) {
			for (ContextPair p : new ArrayList<ContextPair>(pendingContexts)) {
				final IReferenceable r = p.getReferenceable();
				if (r.getReference() != null) {
					// Attaching context to reference
					r.getReference().setReferenceContext(p.getContext());
					// Referencing
					volatileReferenceWithContext(r.getReference(), r, p.getContext());
					pendingContexts.remove(p);
				}
			}
		}
		if (map.get(null) != null) {
			List<IReferenceable> refs = new ArrayList<IReferenceable>(
					(List<IReferenceable>) map.get(null));
			map.remove(null);
			for (IReferenceable r : refs) {
				if (r.getReference() != null) {
					// if(r.getReference().isVolatile()) {
					// r.getReference().setReferenceContext(contextMap.get(r));
					// volatileReferenceWithContext(r.getReference(), r, contextMap.get(r));
					// contextMap.remove(r);
					// } else {
					reference(r.getReference(), r);
					// }
				}
			}
		}
	}

	/**
	 * References a new instance.
	 * 
	 * @param ref the absolute reference
	 * @param refInstance instance to reference
	 */
	@Override
	public void reference(IReference ref, IReferenceable refInstance) {
		reference(ref, refInstance, false);
	}

	/**
	 * Retrieves the appropriate volatile references map for the specified reference. If no volatile
	 * map has already been defined, it instantiates an empty new map
	 * 
	 * @param ref volatile reference
	 * @return the appropriate volatile map
	 */
	private DependenciesMap getVolatileMap(IReference ref) {
		return getVolatileMap(ref.getReferenceContext());
	}

	/**
	 * Retrieves the appropriate volatile references map for the specified reference context. If no
	 * volatile map has yet been defined, it instantiates a new empty map.
	 * 
	 * @param context current reference context
	 * @return the volatile reference map
	 */
	private DependenciesMap getVolatileMap(ReferenceContext context) {
		DependenciesMap volRefMap = volatileRefMap.get(context);
		if (volRefMap == null) {
			volRefMap = new DependenciesMap();
			volatileRefMap.put(context, volRefMap);
		}
		return volRefMap;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void reference(IReference ref, IReferenceable refInstance, boolean updateIfExists) {
		// If type is not set, we might have a partial object
		// (because of Hibernate which link objects before they are loaded!)
		// So we register them for revalidation.
		if (ref != null && ref.getType() == null) {
			if (ref.isVolatile()) {
				DependenciesMap volRefMap = getVolatileMap(ref);
				volRefMap.put(null, refInstance);
			} else {
				refMap.put(null, refInstance);
			}
			return;
		}
		// We route volatiles to correct referencer method
		if (ref != null && ref.isVolatile()) {
			volatileReference(ref, refInstance, null);
		} else {
			// if(ref == null || !ref.isVolatile() || (ref != null && ref.getUID()==null)) {

			if (updateIfExists) {
				List<IReferenceable> refs = (List<IReferenceable>) refMap.get(ref);
				if (refs != null && !refs.isEmpty()) {
					refMap.remove(ref);
				}
			}
			// Ensuring unicity
			if (!isWorkspaceLoading) {
				if (!refMap.containsValue(ref, refInstance)) {
					if (ref != null) {
						ref.setInstance(refInstance);
					}
					refMap.put(ref, refInstance);
				}
			} else {
				if (ref != null) {
					ref.setInstance(refInstance);
				}
				refMap.put(ref, refInstance);
			}
		}
	}

	/**
	 * Registers a new volatile instance reference. A volatile reference instance will not interfere
	 * with currently registered references and may have a short life time.
	 * 
	 * @param ref the absolute reference
	 * @param refInstance instance to register
	 */
	@Override
	public void volatileReference(IReference ref, IReferenceable refInstance, Session session) {
		// References must be volatile, so we indicate it
		final ReferenceContext context = getReferenceContext(session); // ==null ?
		// HibernateUtil.getInstance().getSandBoxSession()
		// : session);
		if (ref != null) {
			ref.setVolatile(true);
			ref.setInstance(refInstance);
			ref.setReferenceContext(context);
		} else {
			pendingContexts.add(new ContextPair(context, refInstance));
			return;
		}
		// Referencing volatile for the specified context
		volatileReferenceWithContext(ref, refInstance, context);
	}

	/**
	 * References the specified volatile reference with the specified reference context
	 * 
	 * @param ref volatile reference
	 * @param refInstance corresponding object instance
	 * @param context reference context
	 */
	private void volatileReferenceWithContext(IReference ref, IReferenceable refInstance,
			ReferenceContext context) {
		DependenciesMap volRefMap = getVolatileMap(context);
		if (ref == null || !volRefMap.containsValue(ref, refInstance)) {
			volRefMap.put(ref, refInstance);
		}
	}

	/**
	 * Clears all registered volatile references
	 */
	@Override
	public void flushVolatiles(Session session) {
		log.debug(">>> FLUSHING VOLATILE REFENCES <<<"); //$NON-NLS-1$
		final ReferenceContext context = new ReferenceContext(session, lastFlushTime);
		DependenciesMap volatileMap = volatileRefMap.get(context);
		if (volatileMap != null) {
			revalidate(volatileMap);
		}
		// TODO Regression if removed while generating deliveries
		// volatileRefMap.remove(context);
		// New flush time
		lastFlushTime = new Date();
	}

	@Override
	public void flush() {
		// flushVolatiles();
		log.debug(">>> FLUSHING ALL REFERENCES <<<");
		refMap.clear();
		volatileRefMap.clear();
		explicitReferencers.clear();
	}

	/**
	 * Dereferences an object instance
	 * 
	 * @param instance instance to dereference
	 */
	@Override
	public void dereference(IReferenceable instance) {
		revalidate(refMap);
		DependenciesMap volRefMap = getVolatileMap(instance.getReference());
		revalidate(volRefMap);
		Object o = refMap.remove(instance.getReference(), instance);
		Object v = volRefMap.remove(instance.getReference(), instance);
		if (log.isDebugEnabled()) {
			if (o == null && v == null) {
				// Collection c = refMap.getCollection(instance.getReference());
				log.error("Unable to dereference ref <" + instance.getReference().toString()
						+ ">: " + instance.toString());
			} else {
				log.debug("Unreferenced ref <" + instance.getReference().toString() + ">: "
						+ instance.toString());
			}
		}
	}

	/**
	 * Computing reverse dependencies to the specified referenceable elements. All dependencies will
	 * be computed.
	 * 
	 * @see ReferenceManager#getReverseDependencies(IReferenceable, IElementType)
	 * @param ref reference for which the dependencies will be retrieved
	 * @return a collection of all items referencing the specified element
	 */
	@Override
	public Collection<IReferencer> getReverseDependencies(IReferenceable ref) {
		return getReverseDependencies(ref, null);
	}

	/**
	 * This method provide the reverse dependencies of a given IReferenceable object. Reverse
	 * dependencies are objects which have a reference to the specified {@link IReferenceable}
	 * object. Type of the returned reverse dependencies is {@link IReferencer} since they have a
	 * reference to the specified {@link IReferenceable} object. The following condition will be
	 * true for every item of the returned list:<br>
	 * obj.getReferenceDependencies().contains(ref)
	 * 
	 * @param ref reference for which we like to retrieve the reverse dependencies.
	 * @return a collection of {@link IReferencer} objects which have a reference to parameter ref.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Collection<IReferencer> getReverseDependencies(IReferenceable ref, IElementType type) {

		final DependenciesMap localRefMap = (ref.getReference().isVolatile() ? volatileRefMap
				.get(ref.getReference().getReferenceContext()) : refMap);
		final DependenciesMap dependenciesMap = getReverseDependenciesMap(localRefMap, type);
		Collection<IReferencer> result = dependenciesMap.getCollection(ref.getReference());
		if (result == null) {
			result = new HashSet<IReferencer>();
		} else {
			result = new HashSet<IReferencer>(result);
		}
		return result;
	}

	/**
	 * Compiles the reverse dependencies map of the current view.
	 * 
	 * @return a multi valued map whose key are {@link IReference} instances and the value is a
	 *         collection of {@link IReferencer} depending on it
	 */
	@Override
	public DependenciesMap getReverseDependenciesMap() {
		return getReverseDependenciesMap(refMap, null);
	}

	@Override
	public DependenciesMap getReverseDependenciesMapFor(IReference r) {

		DependenciesMap volatileMap = null;
		if (r.isVolatile()) {
			volatileMap = volatileRefMap.get(r.getReferenceContext());
			if (volatileMap != null) {
				revalidate(volatileMap);
				return getReverseDependenciesMap(volatileMap, null);
			} else {
				return new DependenciesMap();
			}
		} else {
			return getReverseDependenciesMap(refMap, null);
		}
	}

	/**
	 * Compiles the reverse dependencies map of the current view, containing only dependencies
	 * matching the specified {@link IElementType}.
	 * 
	 * @return a multi valued map whose key are {@link IReference} instances and the value is a
	 *         collection of {@link IReferencer} depending on it
	 */
	@Override
	public DependenciesMap getReverseDependenciesMap(IElementType type) {
		return getReverseDependenciesMap(refMap, type);
	}

	/**
	 * Compiles the reverse dependencies map of the given reference map.
	 * 
	 * @param localRefMap reference mapping
	 * @param type type of referencer elements to filter or <code>null</code> for all
	 * @return a multi valued map whose key are {@link IReference} instances and the value is a
	 *         collection of {@link IReferencer} depending on it
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DependenciesMap getReverseDependenciesMap(DependenciesMap localRefMap, IElementType type) {
		DependenciesMap dependenciesMap = new DependenciesMap();
		// Patch for MultiMap bug (NPE), processing manually
		if (localRefMap != null) {
			final Collection<IReference> refs = localRefMap.keySet();
			if (refs != null) {
				for (Object key : new ArrayList<Object>(refs)) {
					Collection col = localRefMap.getCollection(key);
					if (col != null && !col.isEmpty()) {
						for (Object o : col) {
							fillDependenciesFromObject(dependenciesMap, o, type);
						}
					}
				}
			}
		}
		// Now processing explicit referencers
		for (IReferencer r : explicitReferencers) {
			fillDependenciesFromObject(dependenciesMap, r, type);
		}
		return dependenciesMap;
	}

	@Override
	public void fillDependenciesFromObject(DependenciesMap dependenciesMap, Object o,
			IElementType typeRestriction) {
		// Referencing dependencies
		if (o instanceof IReferencer
				&& (typeRestriction == null || (o instanceof ITypedObject && ((ITypedObject) o)
						.getType() == typeRestriction))) {
			IReferencer referencer = (IReferencer) o;
			try {
				Collection<IReference> dependencyList = referencer.getReferenceDependencies();
				for (IReference dependencyRef : dependencyList) {
					if (!dependenciesMap.containsValue(dependencyRef, referencer)) {
						dependenciesMap.put(dependencyRef, referencer);
					}
				}
			} catch (ErrorException e) {
				log.debug("Exception while tryiong to fetch dependencies of "
						+ NameHelper.getQualifiedName(referencer));
			}
		}
	}

	/**
	 * Searches in all registered references the instances matching the specified object type and
	 * name. This method will throw an {@link ErrorException} when there is more than 1 match.<br>
	 * Use with care as this might be time consuming.
	 * 
	 * @param type type of the item to look for
	 * @param name name of the item to look for
	 * @return the referenceable object (actually a {@link ITypedObject} and {@link INamedObject}
	 *         implementation)
	 */
	@Override
	public IReferenceable findByTypeName(IElementType type, String name)
			throws ReferenceNotFoundException {
		return findByTypeName(type, name, false);
	}

	@Override
	public IReferenceable findByTypeName(IElementType type, String name, boolean ignoreCase)
			throws ReferenceNotFoundException {
		revalidate(refMap);
		IReferenceable ref = null;
		if (ignoreCase) {
			name = name.toUpperCase();
		}
		for (Object o : refMap.values()) {
			if (o instanceof ITypedObject && o instanceof INamedObject) {
				String objName = ((INamedObject) o).getName();
				if (ignoreCase) {
					objName = objName.toUpperCase();
				}
				if (((ITypedObject) o).getType() == type && name.equals(objName)) {
					// Handling too many matches
					if (ref != null) {
						throw new TooManyReferencesException(
								"Too many matching items for {"
										+ type.getName()
										+ ","
										+ name
										+ "}. You should have a global view uniqueness on names of objects with the same type. Check the \"Problems\" view and fix it.");
					} else {
						ref = (IReferenceable) o;
					}
				}
			}
		}
		// Handling not found items
		if (ref == null) {
			throw new ReferenceNotFoundException("No item has been found for {" + type.getName()
					+ "," + name + "}!");
		}
		return ref;
	}

	/**
	 * @param s session used to load references
	 * @return the reference context to use when loading objects from this session
	 */
	@Override
	public ReferenceContext getReferenceContext(Session s) {
		return new ReferenceContext(s, lastFlushTime);
	}

	@Override
	public void addReferencer(IReferencer referencer) {
		explicitReferencers.add(referencer);
	}

	@Override
	public void removeReferencer(IReferencer referencer) {
		explicitReferencers.add(referencer);
	}

	@Override
	public void startWorkspaceLoad() {
		this.isWorkspaceLoading = true;
	}

	@Override
	public void endWorkspaceLoad() {
		this.isWorkspaceLoading = false;
	}

}
