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
 * An interface for matching prototypes.
 * Given the contextual entity of the current cursor,
 * prototypes can indicate their relevancy.
 *  
 * @author Christophe Fondacci
 *
 */
public interface IPrototypeMatcher {

	/**
	 * Retrieves the relevancy of the prototype
	 * given the contextual entity of the current
	 * cursor position.<br> 
	 * Prototypes will then be sorted by relevancy
	 * before they are added to proposals.<br> 
	 * A relevancy of 0 will remove the prototype 
	 * from completion proposals.
	 * 
	 * @param contextualEntity the contextual entity
	 * 		  in which is located the current caret.
	 * @return the relevancy of the proposal
	 */
	public int match(Object contextualEntity);
}
