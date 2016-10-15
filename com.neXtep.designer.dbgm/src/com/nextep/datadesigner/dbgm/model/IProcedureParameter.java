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

import com.nextep.datadesigner.model.INamedObject;

/**
 * This class represents a parameter of a stored
 * procedure.
 *
 * @author Christophe Fondacci
 *
 */
public interface IProcedureParameter extends INamedObject {

	/**
	 * @return the datatype of this parameter
	 */
	public IDatatype getDatatype();
	/**
	 * Defines the datatype of this parameter.
	 * @param d parameter's datatype
	 */
	public void setDatatype(IDatatype d);
	/**
	 * @return the parameter's type (input, output etc.)
	 */
	public ParameterType getParameterType();
	/**
	 * Defines the parameter's type.
	 * @param type parameter's type.
	 */
	public void setParameterType(ParameterType type);
	public void setDefaultExpr(String defaultExpr);
	public String getDefaultExpr();
}
