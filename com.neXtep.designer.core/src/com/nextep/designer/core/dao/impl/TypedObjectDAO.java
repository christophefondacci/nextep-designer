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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.core.dao.impl;

import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.dao.base.AbstractTypedObjectDAO;

/**
 * @author Christophe Fondacci
 */
public class TypedObjectDAO extends AbstractTypedObjectDAO<ITypedObject> {

	private IElementType type;
	private Class<? extends IdentifiedObject> persistedClass;

	public TypedObjectDAO(IElementType type, Class<? extends IdentifiedObject> persistedClass) {
		this.type = type;
		this.persistedClass = persistedClass;
	}

	@Override
	public IElementType getType() {
		return type;
	}

	@Override
	protected Class<? extends IdentifiedObject> getPersistedClass() {
		return persistedClass;
	}
}
