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
package com.nextep.designer.vcs.ui.impl;

import org.apache.commons.collections.map.MultiValueMap;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.designer.vcs.ui.model.DependencyMode;
import com.nextep.designer.vcs.ui.model.IDependencySearchRequest;

/**
 * Default implementation of a {@link IDependencySearchRequest}
 * 
 * @author Christophe Fondacci
 */
public class DependencySearchRequest implements IDependencySearchRequest {

	private IReferenceable element;
	private DependencyMode type;
	private MultiValueMap invRefMap;

	/**
	 * Instantiates a new dependency search request
	 * 
	 * @param element element to compute dependencies for
	 * @param type type of dependencies to compute
	 * @param invRefMap precomputed reverse dependencies map
	 */
	public DependencySearchRequest(IReferenceable element, DependencyMode type,
			MultiValueMap invRefMap) {
		this.element = element;
		this.type = type;
		this.invRefMap = invRefMap;
	}

	@Override
	public IReferenceable getElement() {
		return element;
	}

	@Override
	public DependencyMode getRequestType() {
		return type;
	}

	@Override
	public MultiValueMap getReverseDependenciesMap() {
		return invRefMap;
	}

}
