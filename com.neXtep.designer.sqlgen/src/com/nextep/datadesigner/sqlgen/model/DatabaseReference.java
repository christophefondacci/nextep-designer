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

import org.apache.commons.collections.keyvalue.MultiKey;
import com.nextep.datadesigner.model.IElementType;

/**
 * This class is a database reference. The unicity of an object in the database is only defined by
 * its type and its name.
 * 
 * @author Christophe Fondacci
 */
public class DatabaseReference extends MultiKey {

	/** A generated serial id */
	private static final long serialVersionUID = 1755561257196374397L;

	private IElementType type;
	private String name;

	/**
	 *
	 */
	public DatabaseReference(IElementType type, String name) {
		super(type, name);
		this.type = type;
		this.name = name;
	}

	public DatabaseReference(IElementType type, String name, String parentName) {
		super(type, name, parentName);
		this.type = type;
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return (String) getKeys()[1];
	}

	/**
	 * @return the type
	 */
	public IElementType getType() {
		return (IElementType) getKeys()[0];
	}

	/**
	 * @return the parent database object of this element. Note that this method may return
	 *         <code>null</code> for many references
	 */
	public String getParent() {
		if (getKeys().length == 3) {
			return (String) getKeys()[2];
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return type.getName() + " " + name;
	}
}
