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
package com.nextep.designer.vcs.model;

import com.nextep.datadesigner.model.ITypedObject;

/**
 * This interface is a provider for variables
 * to use when naming an object.
 * 
 * @see INamingPattern
 * @author Christophe
 */
public interface INamingVariableProvider {

	/**
	 * @return the variable name to substitute in the
	 * 			naming pattern.
	 */
	public String getVariableName();
	/**
	 * @return the description of this variable
	 */
	public String getDescription();
	/**
	 * This method computes the value of the variable
	 * for the given object.
	 * 
	 * @param o object being named
	 * @return the variable value if computable, an empty string otherwise
	 */
	public String getVariableValue(ITypedObject o);
	/**
	 * Indicates whether this provider is active for 
	 * the given object
	 * @param o object being named
	 * @return <code>true</code> if this provider can handle
	 * 		   the specified object, else <code>false</code>
	 */
	public boolean isActiveFor(ITypedObject o);
}
