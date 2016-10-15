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
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.impl.Reference;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;

/**
 * @author Christophe Fondacci
 *
 */
public class TypeColumn extends IDNamedObservable implements ITypeColumn {

	private IDatatype datatype;
	private IUserType parentType;
	
	public TypeColumn() {
		nameHelper.setFormatter(IFormatter.UPPERCASE);
		setReference(new Reference(getType(),getName(),this));
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITypeColumn#getDatatype()
	 */
	@Override
	public IDatatype getDatatype() {
		return datatype;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITypeColumn#setDatatype(com.nextep.datadesigner.dbgm.model.IDatatype)
	 */
	@Override
	public void setDatatype(IDatatype datatype) {
		if(datatype != this.datatype) {
			this.datatype = datatype;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITypeColumn#getParent()
	 */
	@Override
	public IUserType getParent() {
		return parentType;
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITypeColumn#setParent(com.nextep.datadesigner.dbgm.model.IUserType)
	 */
	@Override
	public void setParent(IUserType type) {
		this.parentType = type;
	}
	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}
	/**
	 * @see com.nextep.datadesigner.dbgm.model.ITypeColumn#getPosition()
	 */
	@Override
	public int getPosition() {
		return getParent().getColumns().indexOf(this);
	}
	protected void setPosition(int position) {
		//Doing nothing
	}
}
