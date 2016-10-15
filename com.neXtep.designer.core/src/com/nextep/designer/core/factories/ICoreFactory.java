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
package com.nextep.designer.core.factories;

import com.nextep.designer.core.model.IMarker;
import com.nextep.designer.core.model.IMarkerHint;
import com.nextep.designer.core.model.IResourceLocator;
import com.nextep.designer.core.model.MarkerType;

/**
 * @author Christophe Fondacci
 */
public interface ICoreFactory {

	/**
	 * Creates a default marker with no hint.
	 * 
	 * @param relatedObj object being marked
	 * @param type a {@link MarkerType}
	 * @param message the message to display on this marker
	 * @return the {@link IMarker} implementation
	 */
	IMarker createMarker(Object relatedObj, MarkerType type, String message);

	/**
	 * Creates a default marker with a default predefined hint.
	 * 
	 * @param relatedObj object being marked
	 * @param type a {@link MarkerType}
	 * @param message the message to display on this marker
	 * @param defaultHint hint of this marker
	 * @return the {@link IMarker} implementation
	 */
	IMarker createMarker(Object relatedObj, MarkerType type, String message, IMarkerHint defaultHint);

	/**
	 * Creates an image locator.
	 * 
	 * @param pluginId identifier of the plugin where the image is defined
	 * @param fileLocation the location of the image file within the defining plugin
	 * @return an {@link IResourceLocator}
	 */
	IResourceLocator createImageLocator(String pluginId, String fileLocation);
}
