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

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface describes the methods to access
 * the naming pattern string of a typed element
 * 
 * @author Christophe
 */
public interface INamingPattern extends IObservable, IdentifiedObject {

	/**
	 * @return the type handled by this pattern
	 */
	public IElementType getRelatedType();
	/**
	 * Defines the type handled by this pattern
	 * @param type type to handle
	 */
	public void setRelatedType(IElementType type);
	/**
	 * @return the container affected by this pattern, this method
	 * 			may return <code>null</code> for global patterns
	 */
	public IVersionContainer getRelatedContainer();
	/**
	 * Defines the container affected by this pattern
	 * @param container 
	 */
	public void setRelatedContainer(IVersionContainer container);
	/**
	 * @return the naming pattern
	 */
	public String getPattern();
	/**
	 * Defines the new string pattern to use when naming
	 * an object which matches type and container.
	 * @param pattern
	 */
	public void setPattern(String pattern);
}
