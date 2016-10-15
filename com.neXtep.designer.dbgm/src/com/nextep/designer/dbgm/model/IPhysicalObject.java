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

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.ITypedObject;

/**
 * This interface defines objects which can define physical properties.
 * 
 * @author Christophe Fondacci
 */
public interface IPhysicalObject extends INamedObject, ITypedObject {

	/**
	 * Defines the Oracle physical properties associated with this object.
	 * 
	 * @param properties physical properties of this Oracle table
	 */
	void setPhysicalProperties(IPhysicalProperties properties);

	/**
	 * Retrieves the physical properties of this Oracle object.
	 * 
	 * @return this object's physical properties
	 */
	IPhysicalProperties getPhysicalProperties();

	/**
	 * Indicates the type of {@link IPhysicalProperties} that should be used with this physical
	 * object
	 * 
	 * @return a {@link IElementType} corresponding to
	 *         <code>getPhysicalProperties().getType()</code>
	 */
	IElementType getPhysicalPropertiesType();

}
