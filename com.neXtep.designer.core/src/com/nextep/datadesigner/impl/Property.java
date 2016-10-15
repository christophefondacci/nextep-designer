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
package com.nextep.datadesigner.impl;

import java.util.ArrayList;
import java.util.List;
import com.nextep.datadesigner.model.IAttribute;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IProperty;

/**
 * Basic implementation of a property.
 *
 * @author Christophe Fondacci
 *
 */
public class Property extends Observable implements IProperty {

	private String name;
	private String value;
	private List<IProperty> childProperties;
	private IElementType type;
	public Property(String name, String value) {
		this(name,value,IElementType.getInstance(IAttribute.TYPE_ID));
	}
	public Property(String name, String value,IElementType type) {
		this.name = name;
		this.value= value;
		this.type = type;
		this.childProperties = new ArrayList<IProperty>();
	}
	public void addChild(IProperty property) {
		childProperties.add(property);
	}
	public void addChildren(List<IProperty> properties) {
		childProperties.addAll(properties);
	}
	/**
	 * @see com.nextep.datadesigner.model.IProperty#getChildren()
	 */
	@Override
	public List<IProperty> getChildren() {
		return childProperties;
	}

	/**
	 * @see com.nextep.datadesigner.model.IProperty#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @see com.nextep.datadesigner.model.IProperty#getValue()
	 */
	@Override
	public String getValue() {
		return value == null ? "" : value;
	}
	/**
	 * @see com.nextep.datadesigner.model.ITypedObject#getType()
	 */
	@Override
	public IElementType getType() {
		return type;
	}

}
