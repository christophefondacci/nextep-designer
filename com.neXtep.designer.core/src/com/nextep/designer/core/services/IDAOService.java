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
package com.nextep.designer.core.services;

import java.util.List;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.dao.ITypedObjectDAO;

/**
 * A global Data Access Object service that can manage {@link ITypedObject}. It uses contributions
 * of {@link ITypedObjectDAO} to delegate the processing.
 * 
 * @author Christophe Fondacci
 */
public interface IDAOService {

	/**
	 * Loads all element of the given type
	 * 
	 * @param type type of elements to load
	 * @return all repository element of this type
	 */
	List<ITypedObject> loadAll(IElementType type);

	/**
	 * Retrieves a unique typed element
	 * 
	 * @param type the {@link IElementType} of the element to get
	 * @param id the unique {@link UID} of the element to look for
	 * @return the element or null if no match
	 */
	ITypedObject get(IElementType type, UID id);

	/**
	 * Saves the given element to the repository.
	 * 
	 * @param objectToSave the {@link ITypedObject} to save to the repository.
	 */
	void save(ITypedObject objectToSave);
}
