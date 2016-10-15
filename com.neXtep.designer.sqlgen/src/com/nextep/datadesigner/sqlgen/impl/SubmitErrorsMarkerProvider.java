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
package com.nextep.datadesigner.sqlgen.impl;

import java.util.Collection;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.MarkerScope;
import com.nextep.designer.core.model.MarkerType;

public class SubmitErrorsMarkerProvider extends Observable implements IMarkerProvider {

	private static SubmitErrorsMarkerProvider instance;
	private static final Log log = LogFactory.getLog(SubmitErrorsMarkerProvider.class);
	private final MultiValueMap markersMap;

	public SubmitErrorsMarkerProvider() {
		instance = this;
		markersMap = new MultiValueMap();
	}

	public static SubmitErrorsMarkerProvider getInstance() {
		return instance;
	}

	// @SuppressWarnings("unchecked")
	// @Override
	// public Collection<IMarker> getMarkers() {
	// return markersMap.values();
	// }

	@SuppressWarnings("unchecked")
	@Override
	public Collection<IMarker> getMarkersFor(ITypedObject o) {
		// if (o instanceof ISQLScript) {
		// final ISQLScript script = (ISQLScript) o;
		// return markersMap.getCollection(script.getName());
		// }
		// return Collections.emptyList();
		return markersMap.getCollection(o);
	}

	@Override
	public void invalidate() {
	}

	@Override
	public void invalidate(Object o) {
	}

	public void addErrorMarker(ITypedObject object, String errorLine, int lineNumber) {
		final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
		// Building marker
		final IMarker marker = coreFactory.createMarker(object, MarkerType.ERROR, errorLine);

		marker.setAttribute(IMarker.ATTR_LINE, lineNumber);
		markersMap.put(object, marker);
	}

	@Override
	public MarkerScope getProvidedMarkersScope() {
		return MarkerScope.DEFAULT;
	}

}
