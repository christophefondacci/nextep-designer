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
package com.nextep.datadesigner.sqlgen.model;

/**
 * @author Christophe Fondacci
 *
 */
public interface IPrototype {

	/**
	 * @return the name of this prototype as it will be
	 * 			displayed to the user in the content assist
	 */
	public String getName();
	/**
	 * @return the description of this prototype as it will
	 * 		   be displayed to the user in the content assist
	 * 		   for a short description
	 */
	public String getDescription();
	/**
	 * @return the template which will be inserted in the user code
	 */
	public String getTemplate();
	/**
	 * @return the position (relative to the template string) at which
	 * 		   the cursor will be set after the template has been inserted
	 */
	public int getCursorPosition();
	/**
	 * @return the matcher for this prototype
	 */
	public IPrototypeMatcher getPrototypeMatcher();
}
