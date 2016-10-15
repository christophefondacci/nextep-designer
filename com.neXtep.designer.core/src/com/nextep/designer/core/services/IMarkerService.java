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
package com.nextep.designer.core.services;

import java.util.Collection;
import com.nextep.datadesigner.model.IReferenceContainer;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerListener;

/**
 * The marker service provides methods to interact with markers computation and can notify listeners
 * about marker changes of any object in the workbench.<br>
 * The marker service needs an input container, passed as a {@link IReferenceContainer}, which it
 * will use to determine which element need to be processed when computing markers.<br>
 * Since marker computation could be time-consuming on large workspaces, most methods are
 * asynchronous and the only way to be informed about markers state is to register listeners.
 * 
 * @author Christophe Fondacci
 */
public interface IMarkerService {

	/**
	 * Requests the service to compute (or re-compute) all markers. The marker computation will be
	 * performed asynchronously so implementations which need to be informed about markers should be
	 * registered as {@link IMarkerListener} on this service to be notified about markers when they
	 * become available.
	 */
	void computeAllMarkers();

	/**
	 * Retrieves all markers currently defined for the specified object. Note that this method may
	 * return an empty collections on the following states :<br>
	 * - When markers have not yet been computed<br>
	 * - During a marker computation<br>
	 * 
	 * @param o object to retrieve markers for
	 * @return a collection of {@link IMarker} defined on the specified object
	 */
	Collection<IMarker> getMarkersFor(Object o);

	/**
	 * Defines the container from which the service can browse information to compile markers. All
	 * objects provided by the container will be processed.
	 * 
	 * @param container a {@link IReferenceContainer} to use as the input for objects to be
	 *        processed for markers
	 */
	void setInputContainer(IReferenceContainer container);

	/**
	 * Adds a listener which will be notified of marker events. Because listeners are the only way
	 * to get the full set of workbench markers, calling this method may invoke the listener method
	 * {@link IMarkerListener#markersReset(Collection)} when markers are already available
	 * 
	 * @param listener a {@link IMarkerListener} to register
	 */
	void addMarkerListener(IMarkerListener listener);

	/**
	 * Removes the specified listener from marker notifications
	 * 
	 * @param listener the {@link IMarkerListener} to unregister
	 */
	void removeMarkerListener(IMarkerListener listener);

	/**
	 * <p>
	 * This method asks the service to pause any further marker computation request. It is meant to
	 * be used before starting actions that we know will cause too many marker events and when it is
	 * preferrable to recompute all markers at the end of the process.
	 * </p>
	 * <p>
	 * <b>Warning :</b> This method should always be called in a
	 * <code>try { pauseMarkerComputation(); ... } finally { resumeMarkerComputation(); }
	 * </code> block so you make sure you don't deactivate marker computation.
	 * </p>
	 */
	void pauseMarkerComputation();

	/**
	 * <p>
	 * This method asks the service to resume marker computation. A full marker computation will
	 * always start when calling this method. It is meant to be used after actions that we know will
	 * cause too many marker events and when it is preferrable to recompute all markers at the end
	 * of the process.
	 * </p>
	 * <p>
	 * <b>Warning :</b> This method should always be called in a
	 * <code>try { pauseMarkerComputation(); ... } finally { resumeMarkerComputation(); }
	 * </code> block so you make sure you don't deactivate marker computation.
	 * </p>
	 */
	void resumeMarkerComputation();

	/**
	 * Fetches the markers for the specified object from registered marker providers. This method
	 * differs from the {@link IMarkerService#getMarkersFor(Object)} method because it will always
	 * recompute the markers from the providers while the "get" method may use cached markers. This
	 * method should only be used when immediate marker information is needed, because marker
	 * computation process is asynchronous.
	 * 
	 * @param o the object for which we want markers
	 * @return the collection of {@link IMarker} for this object
	 */
	Collection<IMarker> fetchMarkersFor(Object o);

	/**
	 * This helper method returns a collection of all markers as they were retrieved during the last
	 * marker collection. The provided collection might not be up to date and invoking this method
	 * does not trigger any kind of recomputation.
	 * 
	 * @return the collection of all known {@link IMarker} at the time of the call. Note that there
	 *         might be more markers which we don't yet know.
	 */
	Collection<IMarker> getCachedMarkers();
}
