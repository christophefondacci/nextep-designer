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

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferencer;

/**
 * Internal interface of the reference manager.
 * 
 * @author Christophe Fondacci
 */
public interface IInternalReferenceManager extends IReferenceManager {

	/**
	 * Fills the given dependency map with dependencies from the given object
	 * 
	 * @param dependenciesMap the {@link DependenciesMap} to fill with object dependencies
	 * @param o the object whose dependencies will fill the map
	 * @param typeRestriction an optional {@link IElementType} restriction indicating element types
	 *        of the dependencies to fill
	 */
	void fillDependenciesFromObject(DependenciesMap dependenciesMap, Object o,
			IElementType typeRestriction);

	/**
	 * Compiles the reverse dependencies map of the given reference map.
	 * 
	 * @param localRefMap reference mapping
	 * @param type type of referencer elements to filter or <code>null</code> for all
	 * @return a multi valued map whose key are {@link IReference} instances and the value is a
	 *         collection of {@link IReferencer} depending on it
	 */
	DependenciesMap getReverseDependenciesMap(DependenciesMap localRefMap, IElementType type);
}
