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

/**
 * Defines a hint for a marker. A hint is a possible solution to a problem which could be executed
 * to resolve the problem.
 * 
 * @author Christophe Fondacci
 */
public interface IMarkerHint {

	/**
	 * Retrieves the description of this hint
	 * 
	 * @return the hint description
	 */
	String getDescription();

	/**
	 * Executes the hint on the specified element
	 * 
	 * @param element element against which this hint should be applied
	 */
	void execute(Object element);
}
