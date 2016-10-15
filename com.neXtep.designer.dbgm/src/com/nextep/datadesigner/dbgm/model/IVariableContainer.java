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

/**
 * An interface for objects containing variables. Typically used for {@link IPackage},
 * {@link IProcedure} and IFunction.
 * 
 * @author Christophe Fondacci
 */
public interface IVariableContainer {

	/**
	 * Retrieves the list of all variables of this container.<br>
	 * <b>WARNING</b> : This method should not be called by parser as this method may need to parse
	 * the source to initialize the list of variables. This would cause a StackOverflow caused by an
	 * infinite recursive loop.
	 * 
	 * @return a list of this element's declared variables
	 */
	Collection<IVariable> getVariables();

	/**
	 * Adds a package variable
	 * 
	 * @param var variable
	 */
	void addVariable(IVariable var);

	/**
	 * Clears any defined variable from this container
	 */
	void clearVariables();

}
