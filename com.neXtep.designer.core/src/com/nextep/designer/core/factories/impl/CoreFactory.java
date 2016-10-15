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
package com.nextep.designer.core.factories.impl;

import com.nextep.designer.core.factories.ICoreFactory;
import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerHint;
import com.nextep.designer.core.model.IResourceLocator;
import com.nextep.designer.core.model.MarkerType;
import com.nextep.designer.core.model.impl.ImageLocator;
import com.nextep.designer.core.model.impl.Marker;

/**
 * @author Christophe Fondacci
 */
public final class CoreFactory implements ICoreFactory {

	@Override
	public IMarker createMarker(Object relatedObj, MarkerType type, String message) {
		final Marker marker = new Marker();
		marker.setRelatedObject(relatedObj);
		marker.setMarkerType(type);
		marker.setMessage(message);
		return marker;
	}

	@Override
	public IMarker createMarker(Object relatedObj, MarkerType type, String message,
			IMarkerHint defaultHint) {
		final IMarker marker = createMarker(relatedObj, type, message);
		marker.addAvailableHint(defaultHint);
		marker.setSelectedHint(defaultHint);
		return marker;
	}

	@Override
	public IResourceLocator createImageLocator(String pluginId, String fileLocation) {
		return new ImageLocator(pluginId, fileLocation);
	}
}
