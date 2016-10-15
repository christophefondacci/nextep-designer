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

import java.util.List;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * Interface which should be implemented for any object which has columns. This allows proper
 * abstraction of column-based elements.
 * 
 * @author Christophe Fondacci
 */
public interface IColumnable extends INamedObject, IdentifiedObject, ITypedObject {

	/**
	 * @return the list of columns defined by the element.
	 */
	List<IBasicColumn> getColumns();

	/**
	 * Adds a column to this columned element. This behaviour may not be implemented by all child
	 * classes, in which case a call to this method may do nothing.
	 * 
	 * @param column column to add
	 */
	void addColumn(IBasicColumn column);

	/**
	 * Removes a column from this columned element. This behaviour may not be implemented by all
	 * child classes, in which case a call to this method may do nothing.
	 * 
	 * @param column column to remove
	 */
	void removeColumn(IBasicColumn column);
}
