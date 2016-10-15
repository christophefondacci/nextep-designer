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
package com.nextep.designer.core.dao;

import java.util.List;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.UID;

/**
 * @author Christophe Fondacci
 */
public interface ITypedObjectDAO<T extends ITypedObject> extends ITypedObject {

	/**
	 * Loads all objects of this type
	 * 
	 * @return all elements
	 */
	List<T> loadAll();

	/**
	 * Returns the typed element corresponding to the given id.
	 * 
	 * @param id the {@link UID} of the element to get
	 * @return the element, or null if not found
	 */
	T get(UID id);

	/**
	 * Saves the specified object to the repository.
	 * 
	 * @param object the {@link ITypedObject} to save
	 */
	void save(T object);
}
