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
package com.nextep.designer.core.model;

import java.util.Collection;
import com.nextep.designer.core.services.IMarkerService;

/**
 * This interface defines listener which could register on the {@link IMarkerService} to be notified
 * about marker events.
 * 
 * @author Christophe Fondacci
 */
public interface IMarkerListener {

	/**
	 * Method called when markers changed for the specified element.
	 * 
	 * @param o marked element
	 * @param oldMarkers old set of {@link IMarker} on this element
	 * @param newMarkers new set of {@link IMarker} on this element
	 */
	void markersChanged(Object o, Collection<IMarker> oldMarkers, Collection<IMarker> newMarkers);

	/**
	 * All markers have been reset and a brand new collection of markers is now available.
	 * 
	 * @param allMarkers new collection of {@link IMarker}
	 */
	void markersReset(Collection<IMarker> allMarkers);
}
