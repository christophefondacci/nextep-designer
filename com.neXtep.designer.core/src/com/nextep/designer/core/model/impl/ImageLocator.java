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

import com.nextep.designer.core.model.IResourceLocator;

/**
 * Default image locator implementation
 * 
 * @author Christophe Fondacci
 */
public class ImageLocator implements IResourceLocator {

	private String pluginId;
	private String imageFile;

	public ImageLocator(String pluginId, String imageFile) {
		this.pluginId = pluginId;
		this.imageFile = imageFile;
	}

	@Override
	public String getPluginId() {
		return pluginId;
	}

	@Override
	public String getFile() {
		return imageFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((imageFile == null) ? 0 : imageFile.hashCode());
		result = prime * result + ((pluginId == null) ? 0 : pluginId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageLocator other = (ImageLocator) obj;
		if (imageFile == null) {
			if (other.imageFile != null)
				return false;
		} else if (!imageFile.equals(other.imageFile))
			return false;
		if (pluginId == null) {
			if (other.pluginId != null)
				return false;
		} else if (!pluginId.equals(other.pluginId))
			return false;
		return true;
	}

}
