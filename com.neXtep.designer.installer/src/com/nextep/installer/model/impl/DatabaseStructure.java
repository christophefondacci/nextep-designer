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

import java.util.ArrayList;
import java.util.Collection;
import com.nextep.installer.model.IDBObject;
import com.nextep.installer.model.IDatabaseStructure;

/**
 * Default basic database structure implementation
 * 
 * @author Christophe Fondacci
 */
public class DatabaseStructure implements IDatabaseStructure {

	private Collection<IDBObject> dbObjects;

	public DatabaseStructure() {
		dbObjects = new ArrayList<IDBObject>();
	}

	public DatabaseStructure(Collection<IDBObject> objects) {
		this.dbObjects = objects;
	}

	public void addObject(IDBObject structuralObject) {
		dbObjects.add(structuralObject);
	}

	public Collection<IDBObject> getObjects() {
		return dbObjects;
	}

	public void removeObject(IDBObject structuralObject) {
		dbObjects.remove(structuralObject);
	}

}
