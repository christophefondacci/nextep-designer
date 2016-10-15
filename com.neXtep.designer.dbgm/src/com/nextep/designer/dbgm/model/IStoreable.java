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
package com.nextep.designer.dbgm.model;

/**
 * This interface defines elements which are storeable locally. Storeable objects have connector
 * methods to get / set a storage handle which can define the underlying storage structure of the
 * element.
 * 
 * @author Christophe Fondacci
 */
public interface IStoreable {

	/**
	 * Retrieves the storage handle of this element
	 * 
	 * @return the {@link IStorageHandle} of this element or <code>null</code> if this element has
	 *         no structure initialized in the local storage
	 */
	IStorageHandle getStorageHandle();

	/**
	 * Defines the storage handle for this element
	 * 
	 * @param handle the {@link IStorageHandle} of this element
	 */
	void setStorageHandle(IStorageHandle handle);
}
