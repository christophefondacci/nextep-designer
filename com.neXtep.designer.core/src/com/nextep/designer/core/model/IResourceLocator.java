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
package com.nextep.designer.core.model;

/**
 * A resource locator allows, for example, a non-UI plugin to locate a UI resource without
 * expressing any dependency to the UI layer. The problem with image descriptor provided by eclipse
 * is that it implies a dependency to jface, implying swt, implying ui...<br>
 * Such locator is typically used by beans defined through extensions (such as IElementType or
 * IMarker), which are intimately tight to a image resource.<br>
 * As an anticipation of any further similar need with other resources, this interface has been
 * generalized as a "resource" locator rather than an "image" locator
 * 
 * @author Christophe Fondacci
 * @since 1.0.6
 */
public interface IResourceLocator {

	/**
	 * Retrieves the plugin identifier where the file resource resides.
	 * 
	 * @return the plugin identifier where the image resource is located
	 */
	String getPluginId();

	/**
	 * Retrieves the file location in the defining plugin
	 * 
	 * @return the file location
	 */
	String getFile();
}
