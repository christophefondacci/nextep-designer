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
package com.nextep.designer.core.model;

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.factories.ControllerFactory;

public interface ITypedObjectController {

	/**
	 * Method which should be called to alert the controller that a significant change has occurred
	 * on the specified object. The controller may (or may not) cascade some actions by implementing
	 * this method.<br>
	 * <br>
	 * This method should not save the object.
	 * 
	 * @param content controlled object which has changed
	 */
	void modelChanged(Object content);

	void modelDeleted(Object content);

	/**
	 * Physically saves the object. No method other than this one and <code>newInstance</code>
	 * should save an object.
	 * 
	 * @param content controlled object to save
	 */
	void save(IdentifiedObject content);

	/**
	 * A method to load the object
	 * 
	 * @param className class name to load
	 * @param id id to load
	 * @return the loaded object
	 */
	Object load(String className, UID id);

	/**
	 * Defines the type for which the controller has been defined.<br>
	 * <b>Should never be called except by the {@link ControllerFactory}</b>
	 * 
	 * @param type the {@link IElementType} of this controller
	 */
	void setType(IElementType type);

	/**
	 * Retrieves the {@link IElementType} for which the controller has been defined
	 * 
	 * @return the {@link IElementType} of this controller
	 */
	IElementType getType();
}
