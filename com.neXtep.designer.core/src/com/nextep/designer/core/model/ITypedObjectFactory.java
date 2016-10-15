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

import com.nextep.datadesigner.model.ITypedObject;

/**
 * A typed factory is the creator of any {@link ITypedObject}. All factories should be registered
 * through extension points declarations.<br>
 * 
 * @author Christophe Fondacci
 * @param <T> type of objects provided by the factory
 */
public interface ITypedObjectFactory {

	/**
	 * Creates a new {@link ITypedObject} of the specified type. The specified type is specified for
	 * proper delegate implementation.
	 * 
	 * @param typeToCreate the type of element to create.
	 * @return an object of the required type. The returned object will validate the following
	 *         condition :<br>
	 *         <code>this.create(IElementType.getInstance("MY_TYPE")).getType() == IElementType.getInstance("MY_TYPE");</code>
	 */
	<T extends ITypedObject> T create(Class<T> classToCreate);

}
