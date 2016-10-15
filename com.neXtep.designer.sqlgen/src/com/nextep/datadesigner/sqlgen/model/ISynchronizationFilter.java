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
package com.nextep.datadesigner.sqlgen.model;

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface is a filter for the synchronization service.
 * It allows to define regexp which will filter the items to synchronize.
 * Items present in the target database which are of the specified type
 * and which match the corresponding regexp (which is the name of the filter)
 * will never appear in the synchronization window.
 * 
 * @author Christophe
 *
 */
public interface ISynchronizationFilter extends INamedObject,IObservable,ITypedObject, IdentifiedObject {
	

	/**
	 * Defines the type of elements to filter. A <code>null</code> value
	 * will allow all types and will only filter on names.
	 * 
	 * @param type type of item this instance will filter
	 */
	public abstract void setType(IElementType type);
	
	/**
	 * Defines the reference of the container which defines this 
	 * filter.
	 * 
	 * @param ref reference of the container.
	 */
	public abstract void setContainerRef(IReference ref);
	/**
	 * @return the reference of the container which defines this filter
	 */
	public abstract IReference getContainerRef();
	/**
	 * Indicates if this filter matches the specified object
	 * @param object eligible object  
	 * @return <code>true</code> if the object should be filtered by 
	 * 		   the current filter.
	 */
	public abstract boolean match(ITypedObject object);
}
