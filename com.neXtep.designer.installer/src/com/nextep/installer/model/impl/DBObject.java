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
package com.nextep.installer.model.impl;

import com.nextep.installer.model.IDBObject;

/**
 * Default {@link IDBObject} implementation
 * 
 * @author Christophe Fondacci
 */
public class DBObject implements IDBObject {

	public static final String TYPE_TABLE = "TABLE"; //$NON-NLS-1$
	public static final String TYPE_VIEW = "VIEW"; //$NON-NLS-1$
	public static final String TYPE_INDEX = "INDEX"; //$NON-NLS-1$

	private String type;
	private String name;

	public DBObject(String type, String name) {
		this.type = type;
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public boolean equals(Object obj) {
		if (obj instanceof DBObject) {
			return type.equals(((DBObject) obj).getType())
					&& name.equalsIgnoreCase(((DBObject) obj).getName());
		}
		return false;
	}

	public int hashCode() {
		return (type + "." + name).toUpperCase().hashCode(); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return type + " " + name; //$NON-NLS-1$
	}

}
