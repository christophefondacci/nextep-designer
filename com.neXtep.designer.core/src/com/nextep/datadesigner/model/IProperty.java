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
package com.nextep.datadesigner.model;

import java.util.List;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Represents a property. A property is represented
 * by the name/value pair and can contain children
 * properties. <br>
 * It is used to display the properties of an object
 * selected in the workspace.<br>
 * Any object can publish its properties by implementing
 * the {@link IAdaptable} interface and providing a
 * {@link IPropertyProvider} adapter.
 *
 * @author Christophe Fondacci
 *
 */
public interface IProperty extends ITypedObject, IObservable {

	/**
	 * @return the name of the property
	 */
	public String getName();
	/**
	 * @return the value of this property
	 */
	public String getValue();
	/**
	 * @return the chidren property of this property
	 */
	public List<IProperty> getChildren();

}
