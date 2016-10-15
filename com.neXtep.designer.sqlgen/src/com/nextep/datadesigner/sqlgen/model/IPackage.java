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

import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.dbgm.model.IProcedureContainer;
import com.nextep.datadesigner.dbgm.model.ISqlBased;
import com.nextep.datadesigner.dbgm.model.IVariableContainer;

/**
 * This class represents a database package.
 *
 * @author Christophe Fondacci
 *
 */
public interface IPackage extends IDatabaseObject<IPackage>, IVariableContainer, IParseable, IProcedureContainer, ISqlBased {

	public static final String TYPE_ID = "PACKAGE";
	/**
	 * Retrieves the source code of this package body.
	 *
	 * @return a String representing this package source
	 * 		   code.
	 */
	public String getBodySourceCode();
	/**
	 * Sets the source code for this package.
	 *
	 * @param sourceCode the source code of this package
	 */
	public void setBodySourceCode(String sourceCode);
	/**
	 * Retrieves the source code of the specification
	 * of this package.
	 * 
	 * @return the source of the specification package
	 */
	public String getSpecSourceCode();
	/**
	 * Defines the source code of the specification 
	 * of this package.
	 * 
	 * @param spec the specification source code
	 */
	public void setSpecSourceCode(String spec);


}
