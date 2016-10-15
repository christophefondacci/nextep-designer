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
package com.nextep.designer.core.services.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CoreMessages;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.impl.CoreFactory;
import com.nextep.designer.core.helpers.NameHelper;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerListener;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.IParentable;
import com.nextep.designer.core.model.IReferenceManager;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.core.services.IMarkerService;

public class MarkerService implements IMarkerService {

	private static final Log log = LogFactory.getLog(MarkerService.class);
	private static final String JOB_FAMILY_MARKERS = "markerJob"; //$NON-NLS-1$
	private static final int JOB_RESCHEDULE_DELAY = 1000;
	private static final int MAX_OBJECTS_FOR_INCREMENTAL_REFRESH = 10;

	private MultiValueMap markersMap;
	private Map<IMarkerProvider, ProviderEnablement> providersMap;
	private CoreFactory coreFactory;
	private IReferenceContainer inputContainer;
	private final Collection<IMarkerListener> listeners;
	private final ComputeAllMarkersJob computeAllMarkersJob = new ComputeAllMarkersJob();
	private final RefreshMarkersJob refreshMarkersJob = new RefreshMarkersJob();
	private volatile boolean markerPaused = false;
	private final List<RefreshKey> objectsToRefresh = new ArrayList<RefreshKey>();

	/**
	 * This inner class stores marker providers enablement attributes
	 */
	private static class ProviderEnablement {

		protected String context;
		protected String typeId;

		public ProviderEnablement(String context, String typeId) {
			this.context = context;
			this.typeId = typeId;
		}
	}

	/**
	 * This key allows to store an objects which needs to be refreshed by the
	 * marker refresher job. Each object is stored with a flag indicating
	 * whether the object is new or has already been added to the marker scope.
	 */
	private class RefreshKey extends MultiKey {

		private static final long serialVersionUID = -3952154232470863504L;

		public RefreshKey(Object o, boolean isAdded) {
			super(o, isAdded);
		}

		public Object getObject() {
			return getKeys()[0];
		}

		public boolean isAdded() {
			return ((Boolean) getKeys()[1]).booleanValue();
		}
	}

	/**
	 * This job handles the recomputation of all markers of the current
	 * workspace.
	 */
	private class ComputeAllMarkersJob extends Job {

		private boolean reschedule = false;

		public ComputeAllMarkersJob() {
			super(CoreMessages.getString("service.marker.computingMarkersTask")); //$NON-NLS-1$
			setUser(false);
			setPriority(Job.DECORATE);
		}

		public void invalidate() {
			this.reschedule = true;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (reschedule) {
				schedule(JOB_RESCHEDULE_DELAY);
				reschedule = false;
				return Status.OK_STATUS;
			} else {
				computeAllMarkers(monitor);
				return Status.OK_STATUS;
			}
		}

		@Override
		public boolean belongsTo(Object family) {
			return JOB_FAMILY_MARKERS.equals(family);
		}
	}

	private void computeAllMarkers(IProgressMonitor monitor) {
		SubMonitor m = SubMonitor.convert(monitor, 100);
		invalidate();
		m.worked(10);
		providersMap = new HashMap<IMarkerProvider, MarkerService.ProviderEnablement>();
		final Collection<IMarker> markers = getAllMarkers(m.newChild(80));
		m.setWorkRemaining(10);
		m.setTaskName(CoreMessages.getString("service.marker.notifyingWrokbenchTask")); //$NON-NLS-1$
		markersReset(markers);
		m.worked(10);
		m.done();
	}

	/**
	 * This Job handles the refresh of markers for objects stored in the
	 * <code>objectsToRefresh</code> buffer.
	 */
	private class RefreshMarkersJob extends Job {

		private boolean reschedule = false;

		public RefreshMarkersJob() {
			super(CoreMessages.getString("service.marker.updateMarkersJob")); //$NON-NLS-1$
			setUser(false);
		}

		private void fillMarkedObjects(List<RefreshKey> toFill, boolean isAdded, Object... toAdd) {
			for (Object o : toAdd) {
				if (o instanceof IParentable<?>) {
					fillMarkedObjects(toFill, isAdded, ((IParentable<?>) o).getParent());
				}
				toFill.add(new RefreshKey(o, isAdded));
			}
		}

		public void invalidate() {
			reschedule = true;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			final List<RefreshKey> objectsToProcess = new ArrayList<RefreshKey>();
			// Checking if objects have been added since schedule, in which case
			// we wait.
			if (reschedule) {
				schedule(JOB_RESCHEDULE_DELAY);
				reschedule = false;
				return Status.OK_STATUS;
			}
			synchronized (objectsToRefresh) {
				for (RefreshKey key : objectsToRefresh) {
					if (markerPaused) {
						break;
					}
					final Object o = key.getObject();
					// Unwrapping references to update markers of referenced
					// element
					if (o instanceof IReference) {
						try {
							final List<IReferenceable> referencedObjects = CorePlugin.getService(
									IReferenceManager.class).getReferencedItems((IReference) o);
							fillMarkedObjects(objectsToProcess, key.isAdded(),
									referencedObjects.toArray());
						} catch (ErrorException e) {
							log.warn("Unable to resolve reference " + o //$NON-NLS-1$
									+ " while computing markers: " + e.getMessage(), e); //$NON-NLS-1$
						}
					} else {
						fillMarkedObjects(objectsToProcess, key.isAdded(), o);
					}
				}
				// Clearing collection
				objectsToRefresh.clear();
			}
			// If we have too many objects, we prefer full computation
			if (objectsToProcess.size() > MAX_OBJECTS_FOR_INCREMENTAL_REFRESH) {
				computeAllMarkers(monitor);
			} else {
				// Processing elements
				monitor.beginTask(
						CoreMessages.getString("service.marker.incrementalProcessing"), objectsToProcess.size()); //$NON-NLS-1$
				for (RefreshKey key : objectsToProcess) {
					final boolean isAdded = key.isAdded();
					final Object o = key.getObject();
					if (isAdded && (o instanceof IObservable)) {
						Designer.getListenerService().registerListener(MarkerService.this,
								(IObservable) o, new MarkedObjectListener(o, markersMap));
					}
					// Processing markers delta
					processMarkersDelta(o);
					// Recursively process contained elements
					if (o instanceof IReferenceContainer) {
						final Map<IReference, IReferenceable> refMap = ((IReferenceContainer) o)
								.getReferenceMap();
						for (IReferenceable r : refMap.values()) {
							if (isAdded && (r instanceof IObservable)) {
								Designer.getListenerService().registerListener(MarkerService.this,
										(IObservable) r, new MarkedObjectListener(r, markersMap));
							}
							// Contained delta
							processMarkersDelta(r);
						}
					}
					monitor.worked(1);
				}
			}
			monitor.done();
			return Status.OK_STATUS;
		}
	}

	/**
	 * A listener on every marked object
	 * 
	 * @author Christophe Fondacci
	 */
	private class MarkedObjectListener implements IEventListener, IModelOriented<Object> {

		private Object model;
		private final MultiValueMap markersMap;

		public MarkedObjectListener(Object markedObject, MultiValueMap markersMap) {
			this.markersMap = markersMap;
			this.model = markedObject;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setModel(Object model) {
			Collection<IMarker> markers = markersMap.getCollection(model);
			if (markers != null) {
				markersMap.remove(this.model);
				if (model != null) {
					markersMap.putAll(model, markers);
					for (IMarker m : markers) {
						m.setRelatedObject(model);
					}
				}
			}
			this.model = model;
		}

		@Override
		public Object getModel() {
			return model;
		}

		@Override
		public void handleEvent(ChangeEvent event, IObservable source, Object data) {
			switch (event) {
			case MODEL_CHANGED:
				// Processing update on source
				if (source instanceof IParentable<?>) {
					markedObjectChanged(false, source, ((IParentable<?>) source).getParent());
				} else {
					markedObjectChanged(false, source);
				}
				break;
			case VERSIONABLE_ADDED:
			case COLUMN_ADDED:
			case CONSTRAINT_ADDED:
			case DATASET_ADDED:
			case GENERIC_CHILD_ADDED:
				// PRocessing update on added element
				markedObjectChanged(true, data, source);
				break;
			case VERSIONABLE_REMOVED:
			case COLUMN_REMOVED:
			case CONSTRAINT_REMOVED:
			case DATASET_REMOVED:
			case GENERIC_CHILD_REMOVED:
				// Instead of computing dependencies recursively and refreshing
				// markers we recompute
				// everything
				computeAllMarkers();
				break;

			}
		}

	}

	public MarkerService() {
		markersMap = new MultiValueMap();
		providersMap = new HashMap<IMarkerProvider, ProviderEnablement>();
		listeners = new ArrayList<IMarkerListener>();
	}

	private void markedObjectChanged(final boolean isAdded, final Object... objects) {
		for (Object o : objects) {
			objectsToRefresh.add(new RefreshKey(o, isAdded));
		}
		refreshMarkersJob.schedule(1000);
		refreshMarkersJob.invalidate();
	}

	private void processMarkersDelta(Object toRefresh) {
		final Collection<IMarker> oldMarkers = getMarkersFor(toRefresh);
		final Collection<IMarker> newMarkers = fetchMarkersFor(toRefresh, true);
		if (!oldMarkers.isEmpty() || !newMarkers.isEmpty()) {
			markersChanged(toRefresh, oldMarkers, newMarkers);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IMarker> getMarkersFor(Object o) {
		Collection<IMarker> markers = markersMap.getCollection(o);
		if (markers == null) {
			markers = Collections.emptyList();
		}
		return markers;
	}

	private Collection<IMarker> fetchMarkersFor(Object o, boolean invalidate) {
		// Low consuming request, not using cache to ensure up to date
		// information
		Collection<IMarker> markers = new ArrayList<IMarker>();
		if (o instanceof ITypedObject) {
			final ITypedObject typedObj = (ITypedObject) o;
			markersMap.remove(o);
			for (IMarkerProvider p : getMarkerProvidersFor(typedObj)) {
				try {
					if (invalidate) {
						p.invalidate(typedObj);
					}
					final Collection<IMarker> providedMarkers = p.getMarkersFor(typedObj);
					// Safety check
					if (providedMarkers != null && !providedMarkers.isEmpty()) {
						markers.addAll(providedMarkers);
						if (typedObj instanceof IParentable<?>) {
							MarkerType type = null;
							// Getting highest marker type
							for (IMarker m : providedMarkers) {
								final MarkerType mt = m.getMarkerType();
								if (type == null || mt == MarkerType.ERROR
										|| (mt == MarkerType.WARNING && type != MarkerType.ERROR)) {
									type = mt;
								}
							}
							// Building marker message
							final String name = NameHelper.getName(typedObj);
							final String objType = typedObj.getType().getName();
							final Object parent = ((IParentable<?>) typedObj).getParent();
							final IMarker parentMarker = coreFactory.createMarker(
									parent,
									type,
									MessageFormat.format(
											CoreMessages.getString("service.marker.childMarkers"), //$NON-NLS-1$
											objType, name));

							markersMap.putAll(parent, Arrays.asList(parentMarker));
						}
					}
				} catch (RuntimeException e) {
					log.warn(
							MessageFormat.format(
									CoreMessages.getString("service.marker.fetchFailed"), p.getProvidedMarkersScope().name(), //$NON-NLS-1$
									NameHelper.getQualifiedName(typedObj)), e);
				}
			}
			markersMap.putAll(o, markers);
		}
		return markers;
	}

	// private void appendMarkers(Object o, Collection<IMarker> markers) {
	// Collection<IMarker> objMarkers = markersMap.getCollection(o);
	// if (objMarkers == null || objMarkers.isEmpty()) {
	// objMarkers = new ArrayList<IMarker>();
	// markersMap.putAll(o, objMarkers);
	// }
	// objMarkers.addAll(markers);
	// }

	@Override
	public Collection<IMarker> fetchMarkersFor(Object o) {
		final Collection<IMarker> markers = fetchMarkersFor(o, true);
		if (markers == null) {
			return Collections.emptyList();
		} else {
			return markers;
		}
	}

	private Collection<IMarker> getAllMarkers(IProgressMonitor monitor) {
		final Collection<IMarker> allMarkers = new ArrayList<IMarker>();
		if (inputContainer != null) {
			// Retrieving elements to process
			Map<IReference, IReferenceable> refMap = inputContainer.getReferenceMap();
			final Collection<IReferenceable> referenceables = refMap.values();
			monitor.beginTask(
					CoreMessages.getString("service.marker.fetchingMarkersTask"), referenceables.size()); //$NON-NLS-1$
			// Unregistering listeners
			Designer.getListenerService().unregisterListeners(this);
			// Preparing our collections to cache markers
			markersMap = new MultiValueMap();
			// Listening to our root container
			if (inputContainer instanceof IObservable) {
				Designer.getListenerService().registerListener(this, (IObservable) inputContainer,
						new MarkedObjectListener(inputContainer, markersMap));
			}
			for (IReferenceable r : referenceables) {
				// Checking if pause is requested
				if (markerPaused) {
					break;
				}
				if (r instanceof ITypedObject) {
					final Collection<IMarker> objectMarkers = fetchMarkersFor(r, false);
					allMarkers.addAll(objectMarkers);
					if (r instanceof IObservable) {
						Designer.getListenerService().registerListener(this, (IObservable) r,
								new MarkedObjectListener(r, markersMap));
					}
				}
				monitor.worked(1);
			}
			monitor.done();
		}
		return allMarkers;
	}

	private void invalidate() {
		// Recursively invalidating declared marker providers
		final Map<IMarkerProvider, ProviderEnablement> providersMap = getMarkerProvidersMap();
		for (IMarkerProvider p : providersMap.keySet()) {
			p.invalidate();
		}
	}

	// Kept here in case we need this in the future but it's unlikely
	// private void invalidate(Object o) {
	// // Recursively invalidating declared marker providers
	// if (o instanceof ITypedObject) {
	// final ITypedObject typedObj = (ITypedObject) o;
	// final Collection<IMarkerProvider> providers =
	// getMarkerProvidersFor(typedObj);
	// for (IMarkerProvider p : providers) {
	// p.invalidate(o);
	// }
	// }
	// }

	/**
	 * Retrieves all marker providers enabled for the specified object
	 * 
	 * @param o
	 *            object to retrieve markers for
	 * @return every {@link IMarkerProvider} enabled for this object
	 */
	private Collection<IMarkerProvider> getMarkerProvidersFor(ITypedObject o) {
		final Map<IMarkerProvider, ProviderEnablement> providersMap = getMarkerProvidersMap();
		final List<IMarkerProvider> providers = new ArrayList<IMarkerProvider>();
		for (IMarkerProvider p : new ArrayList<IMarkerProvider>(providersMap.keySet())) {
			final ProviderEnablement enablement = providersMap.get(p);
			if (enablement.typeId == null || "".equals(enablement.typeId) //$NON-NLS-1$
					|| o.getType().getId().equals(enablement.typeId)) {
				providers.add(p);
			}
		}
		return providers;
	}

	private Map<IMarkerProvider, ProviderEnablement> getMarkerProvidersMap() {
		if (!providersMap.isEmpty()) {
			return providersMap;
		}
		Collection<IConfigurationElement> elts = Designer.getInstance().getExtensions(
				"com.neXtep.designer.core.markerProvider", "class", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (IConfigurationElement elt : elts) {
			final String context = elt.getAttribute("context"); //$NON-NLS-1$
			final String typeId = elt.getAttribute("typeId"); //$NON-NLS-1$
			final ProviderEnablement enablement = new ProviderEnablement(context, typeId);
			final String designerContext = Designer.getInstance().getContext();
			if (context == null
					|| "".equals(context) || (designerContext != null && designerContext.equals(context))) { //$NON-NLS-1$
				try {
					IMarkerProvider provider = (IMarkerProvider) elt
							.createExecutableExtension("class"); //$NON-NLS-1$
					providersMap.put(provider, enablement);
				} catch (CoreException e) {
					throw new ErrorException(e);
				}
			}
		}
		return providersMap;
	}

	@Override
	public void setInputContainer(IReferenceContainer container) {
		this.inputContainer = container;
		providersMap = new HashMap<IMarkerProvider, MarkerService.ProviderEnablement>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addMarkerListener(IMarkerListener listener) {
		listeners.add(listener);
		if (markersMap != null && !markersMap.isEmpty()) {
			markersReset(markersMap.values());
		}
	}

	@Override
	public void removeMarkerListener(IMarkerListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Notifies all registered listeners that listeners have changed for the
	 * specified object
	 * 
	 * @param obj
	 *            marked object
	 * @param oldMarkers
	 *            previously attached markers (will be empty when the object was
	 *            not marked)
	 * @param newMarkers
	 *            newly attached markers
	 */
	private void markersChanged(Object obj, Collection<IMarker> oldMarkers,
			Collection<IMarker> newMarkers) {
		for (IMarkerListener l : new ArrayList<IMarkerListener>(listeners)) {
			try {
				l.markersChanged(obj, oldMarkers, newMarkers);
			} catch (RuntimeException e) {
				log.error("A marker listener threw an exception : " + e.getMessage(), e);
			}
		}
	}

	private void markersReset(Collection<IMarker> allMarkers) {
		for (IMarkerListener l : new ArrayList<IMarkerListener>(listeners)) {
			try {
				l.markersReset(allMarkers);
			} catch (RuntimeException e) {
				log.error("A marker listener threw an exception : " + e.getMessage(), e);
			}
		}
	}

	@Override
	public void computeAllMarkers() {
		if (!markerPaused) {
			invalidate();
			computeAllMarkersJob.schedule(1000);
			computeAllMarkersJob.invalidate();
		}
	}

	@Override
	public void pauseMarkerComputation() {
		this.markerPaused = true;
	}

	@Override
	public void resumeMarkerComputation() {
		this.markerPaused = false;
		computeAllMarkers();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IMarker> getCachedMarkers() {
		return markersMap.values();
	}

	/**
	 * @param coreFactory
	 *            the coreFactory to set
	 */
	public void setCoreFactory(CoreFactory coreFactory) {
		this.coreFactory = coreFactory;
	}

	/**
	 * @return the coreFactory
	 */
	public CoreFactory getCoreFactory() {
		return coreFactory;
	}
}
