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
package com.nextep.datadesigner.dbgm.impl;

import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IProcedureParameter;
import com.nextep.datadesigner.dbgm.model.ParameterType;
import com.nextep.datadesigner.impl.NamedObservable;
import com.nextep.datadesigner.model.IFormatter;

/**
 * @author Christophe Fondacci
 *
 */
public class ProcedureParameter extends NamedObservable implements
		IProcedureParameter {

	private IDatatype datatype;
	private ParameterType type;
	private String defaultExpr;
	public ProcedureParameter(String name, ParameterType type, IDatatype datatype) {
		nameHelper.setFormatter(IFormatter.LOWERCASE);
		setName(name);
		setDatatype(datatype);
		setParameterType(type==null ? ParameterType.IN : type);
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedureParameter#getDatatype()
	 */
	@Override
	public IDatatype getDatatype() {
		return datatype;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedureParameter#getParameterType()
	 */
	@Override
	public ParameterType getParameterType() {
		return type;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedureParameter#setDatatype(com.nextep.datadesigner.dbgm.model.IDatatype)
	 */
	@Override
	public void setDatatype(IDatatype d) {
		this.datatype = d;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedureParameter#setParameterType(com.nextep.datadesigner.dbgm.model.ParameterType)
	 */
	@Override
	public void setParameterType(ParameterType type) {
		this.type = type;
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedureParameter#getDefaultExpr()
	 */
	@Override
	public String getDefaultExpr() {
		return defaultExpr;
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.IProcedureParameter#setDefaultExpr(java.lang.String)
	 */
	@Override
	public void setDefaultExpr(String defaultExpr) {
		this.defaultExpr = defaultExpr;
	}

}
