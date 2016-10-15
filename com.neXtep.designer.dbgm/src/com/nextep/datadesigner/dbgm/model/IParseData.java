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
import com.nextep.designer.dbgm.sql.TextPosition;

/**
 * Contains information about the result of a parsing
 * of an element.
 *
 * @author Christophe Fondacci
 *
 */
public interface IParseData {

	/**
	 * Retrieves the position of an entity
	 * in its parent text.
	 *
	 * @param entity parsed element for which we need to
	 * 		  retrieve the position
	 * @return the position of this element in the parent text.
	 */
	public TextPosition getPosition(Object entity);
	/**
	 * Defines the position of an entity in
	 * its parent defining text.
	 *
	 * @param entity entity for which you want to define a position
	 * @param position position of this entity in the parent text
	 */
	public void setPosition(Object entity, TextPosition position);

	/**
	 * @return the positions which has been defined during parse
	 */
	public Collection<TextPosition> getPositions();
	/**
	 * @param offset text offset 
	 * @return the entity defined at the specified offset or <code>null</code>
	 * 		   if no entity is defined for this offset.
	 */
	public Object getEntity(int offset);
}
