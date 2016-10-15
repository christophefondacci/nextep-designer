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

import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IDatatype;

/**
 * @author Christophe Fondacci
 */
public interface IUserCollection extends IDatabaseObject<IUserCollection> {

	public static final String TYPE_ID = "USER_COLLECTION"; //$NON-NLS-1$

	/**
	 * @return the datatype of this collection
	 */
	public IDatatype getDatatype();

	/**
	 * Defines the datatype of this collection
	 * 
	 * @param datatype datatype of the elements of this collection
	 */
	public void setDatatype(IDatatype datatype);

	/**
	 * @return the type of collection
	 * @see CollectionType
	 */
	public CollectionType getCollectionType();

	/**
	 * Defines the type of collection
	 * 
	 * @param collectionType type of collection
	 * @see CollectionType
	 */
	public void setCollectionType(CollectionType collectionType);

	/**
	 * @return the sizze of the table (for varrays)
	 */
	public int getSize();

	/**
	 * Defines the size of the table (for varrays)
	 * 
	 * @param size new table size
	 */
	public void setSize(int size);

}
