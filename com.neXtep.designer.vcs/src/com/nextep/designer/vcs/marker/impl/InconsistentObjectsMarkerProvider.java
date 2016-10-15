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
package com.nextep.designer.vcs.marker.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.impl.Observable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.ICheckedObject;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerProvider;
import com.nextep.designer.core.model.MarkerScope;
import com.nextep.designer.core.model.MarkerType;

public class InconsistentObjectsMarkerProvider extends Observable implements IMarkerProvider {

	private Map<ITypedObject, IMarker> markersMap;
	private boolean validated = false;

	public InconsistentObjectsMarkerProvider() {
		markersMap = new HashMap<ITypedObject, IMarker>();
	}

	@Override
	public Collection<IMarker> getMarkersFor(ITypedObject o) {
		final ICoreFactory coreFactory = CorePlugin.getService(ICoreFactory.class);
		if (o instanceof ICheckedObject) {
			IMarker m = null;
			final ICheckedObject checkedObj = (ICheckedObject) o;
			try {
				checkedObj.checkConsistency();
			} catch (InconsistentObjectException e) {
				m = coreFactory.createMarker(o, MarkerType.ERROR, e.getMessage());
			} catch (RuntimeException e) {
				m = coreFactory.createMarker(o, MarkerType.ERROR, e.getMessage());
			}
			if (m != null) {
				return Arrays.asList(m);
			}
		}
		return Collections.emptyList();
	}

	// @Override
	// public Collection<IMarker> getMarkers() {
	// if (!validated) {
	// markersMap.clear();
	// IViewService viewService = VCSPlugin.getService(IViewService.class);
	// Collection<IReferenceable> contents = viewService.getCurrentView().getReferenceMap()
	// .values();
	// for (IReferenceable r : contents) {
	// if (r instanceof ICheckedObject) {
	// if (r instanceof ITypedObject) {
	// Collection<IMarker> markers = getMarkersFor((ITypedObject) r);
	// if (!markers.isEmpty()) {
	// markersMap.put((ITypedObject) r, markers.iterator().next());
	// }
	// }
	// }
	// }
	// }
	// return markersMap.values();
	// }

	@Override
	public void invalidate() {
		validated = false;
	}

	@Override
	public void invalidate(Object o) {
		if (o instanceof ITypedObject) {
			final ITypedObject typedObj = (ITypedObject) o;
			Collection<IMarker> markers = getMarkersFor(typedObj);
			if (!markers.isEmpty()) {
				// Simplified as we can only have one marker
				markersMap.put(typedObj, markers.iterator().next());
			}
		} else {
			invalidate();
		}

	}

	@Override
	public MarkerScope getProvidedMarkersScope() {
		return MarkerScope.CONSISTENCY;
	}
}
