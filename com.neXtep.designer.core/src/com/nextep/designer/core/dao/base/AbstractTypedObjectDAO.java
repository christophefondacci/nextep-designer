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
package com.nextep.designer.core.dao.base;

import java.util.List;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.dao.IIdentifiableDAO;
import com.nextep.designer.core.dao.ITypedObjectDAO;

/**
 * @author Christophe Fondacci
 */
public abstract class AbstractTypedObjectDAO<T extends ITypedObject> implements ITypedObjectDAO<T> {

	protected abstract Class<? extends IdentifiedObject> getPersistedClass();

	private IIdentifiableDAO identifiableDao;

	public AbstractTypedObjectDAO() {
		identifiableDao = CorePlugin.getIdentifiableDao();
	}

	@Override
	public List<T> loadAll() {
		return (List<T>) identifiableDao.loadAll(getPersistedClass());
	}

	@Override
	public T get(UID id) {
		return (T) identifiableDao.load(getPersistedClass(), id);
	}

	@Override
	public void save(T object) {
		identifiableDao.save((IdentifiedObject) object);
	}

}
