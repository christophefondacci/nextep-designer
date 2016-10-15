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
package com.nextep.datadesigner.dbgm.model;

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface represents a simple column of a
 * user type.<br>
 * A type column is a name and a datatype.
 * 
 * @author Christophe Fondacci
 *
 */
public interface ITypeColumn extends INamedObject, IdentifiedObject, IObservable, IReferenceable, ITypedObject {

	public static final String TYPE_ID = "TYPE_COLUMN";
	/**
	 * @return the datatype of this usertype column
	 */
	public IDatatype getDatatype();
	/**
	 * Defines the data type of this type column.
	 * @param datatype column's new datatype
	 */
	public void setDatatype(IDatatype datatype);
	/**
	 * Defines the owning user type
	 * @param type user type defining this column
	 */
	public void setParent(IUserType type);
	/**
	 * @return the user type defining this column
	 */
	public IUserType getParent();
	/**
	 * Position of this index in its parent type
	 * @return the position of this index in its parent type
	 */
	public int getPosition();

}
