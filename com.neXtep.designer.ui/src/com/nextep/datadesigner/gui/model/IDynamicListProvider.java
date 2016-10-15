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
package com.nextep.datadesigner.gui.model;



import java.util.List;
import com.nextep.datadesigner.gui.impl.DynamicListConnector;
import com.nextep.datadesigner.model.IObservable;

/**
 * This interface defines methods to list
 * contents for the {@link DynamicListConnector}.
 * 
 * @author Christophe
 */
public interface IDynamicListProvider {

	/**
	 * @return the list to display in the dynamic list editor. 
	 * Elements should always be retrieved by the specified 
	 * instance.
	 */
	public List<? extends IObservable> getList(Object fromObject);
	/**
	 * Adds a new element to the specified object
	 * @param fromObject
	 */
	public void add(Object fromObject);
	/**
	 * Removes an element from the object.
	 * 
	 * @param fromObject main object to remove child from
	 * @param toRemove element to remove
	 */
	public void remove(Object fromObject, Object toRemove);
}
