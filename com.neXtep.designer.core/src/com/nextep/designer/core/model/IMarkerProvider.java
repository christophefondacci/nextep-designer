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
import com.nextep.datadesigner.model.ITypedObject;

/**
 * This interface defines a marker provider. A marker provider is capable of generating markers for
 * objects of the workspace.
 * 
 * @author Christophe
 */
public interface IMarkerProvider {

	/**
	 * Retrieves markers defined for the specified object.
	 * 
	 * @param o object to retrieve markers for
	 * @return markers for a specific object
	 */
	Collection<IMarker> getMarkersFor(ITypedObject o);

	/**
	 * Informs this provider that markers information may have been invalidated by some user actions
	 */
	void invalidate();

	/**
	 * Invalidates markers for a specific object
	 * 
	 * @param o specific object to invalidate
	 */
	void invalidate(Object o);

	/**
	 * Informs about the scope for which markers are provided. This method may return null in which
	 * case it will be assumed to be {@link MarkerScope#DEFAULT}.
	 * 
	 * @return the {@link MarkerScope} of markers supplied by this provider
	 */
	MarkerScope getProvidedMarkersScope();

}
