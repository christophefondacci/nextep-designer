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
/**
 *
 */
package com.nextep.datadesigner.impl;

import com.nextep.datadesigner.model.IAttribute;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;

/**
 * @author Christophe Fondacci
 *
 */
public class StringAttribute implements IAttribute,INamedObject {
	public final static String TYPE_ID = "ATTRIBUTE";
	private String name;
	private String value;
	private IReference ref;

	public StringAttribute(String name, String value) {
		setName(name);
		setValue(value);
		this.ref = new StringReference(name); //Reference(this.getType(),name,name);
	}
	/**
	 * @see com.nextep.datadesigner.model.IAttribute#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see com.nextep.datadesigner.model.IAttribute#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * @see com.nextep.datadesigner.model.IAttribute#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name=name;
	}

	/**
	 * @see com.nextep.datadesigner.model.IAttribute#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Object value) {
		this.value=(String)value;
	}

	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}
	/**
	 * @see com.nextep.datadesigner.model.IReferenceable#getReference()
	 */
	@Override
	public IReference getReference() {
		return ref;
	}
	/**
	 * @see com.nextep.datadesigner.model.IReferenceable#setReference(com.nextep.datadesigner.impl.Reference)
	 */
	@Override
	public void setReference(IReference ref) {
		// Nonsense
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof StringAttribute) {
			StringAttribute str = (StringAttribute)obj;
			boolean nameMatch = false;
			boolean valueMatch = false;
			if(this.getName() != null) {
				nameMatch = this.getName().equals(str.getName());
			} else {
				nameMatch = (str.getName() == null);
			}
			if(this.getValue()!=null) {
				valueMatch = this.getValue().equals(str.getValue());
			} else {
				valueMatch = (str.getValue() == null);
			}
			return nameMatch && valueMatch;
		}
		// TODO Auto-generated method stub
		return super.equals(obj);
	}
	public String toString() {
		return name + ":" + value;
	}
	/**
	 * @see com.nextep.datadesigner.model.INamedObject#getDescription()
	 */
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @see com.nextep.datadesigner.model.INamedObject#setDescription(java.lang.String)
	 */
	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub
		
	}

}
