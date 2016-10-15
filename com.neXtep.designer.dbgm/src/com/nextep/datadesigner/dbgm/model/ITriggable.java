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

import java.util.Collection;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReferenceable;

/**
 * This interface describes elements on which triggers
 * could be defined.
 * 
 *  Typically, this will be tables and views (so far).
 *  
 * @author Christophe Fondacci
 *
 */
public interface ITriggable extends IReferenceable, INamedObject {
	/**
	 * @return the collection of all triggers defined on this table
	 */
	public Collection<ITrigger> getTriggers();
	/**
	 * Adds a trigger to this table
	 * @param trigger trigger to plug to this table
	 */
	public void addTrigger(ITrigger trigger);
	/**
	 * Removes a trigger from this table
	 * @param trigger trigger to remove from this table
	 */
	public void removeTrigger(ITrigger trigger);
}
