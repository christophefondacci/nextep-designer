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
package com.nextep.designer.vcs.ui.model;

import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.core.model.IReferenceManager;

/**
 * This class represents a search request for dependencies.
 * 
 * @author Christophe Fondacci
 */
public interface IDependencySearchRequest {

	/**
	 * The element to compute dependencies for
	 * 
	 * @return the {@link IReferenceable} element to compute dependencies for
	 */
	IReferenceable getElement();

	/**
	 * Retrieves the type of dependencies to look for
	 * 
	 * @return the {@link DependencyMode}
	 */
	DependencyMode getRequestType();

	/**
	 * The reverse dependency map to use as the reference table for dependency lookup. This map
	 * should come from a call to the {@link IReferenceManager#getReverseDependenciesMap()}
	 * 
	 * @return the pre-computed reverse dependencies map
	 */
	MultiValueMap getReverseDependenciesMap();
}
