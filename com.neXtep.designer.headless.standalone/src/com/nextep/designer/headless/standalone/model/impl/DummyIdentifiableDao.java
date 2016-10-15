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
package com.nextep.designer.headless.standalone.model.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.hibernate.classic.Session;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.datadesigner.model.UID;
import com.nextep.designer.core.dao.IIdentifiableDAO;

/**
 * @author Christophe Fondacci
 */
public class DummyIdentifiableDao implements IIdentifiableDAO {

	@Override
	public void save(IdentifiedObject object) {
	}

	@Override
	public IdentifiedObject load(Class<?> clazz, UID id) {
		return null;
	}

	@Override
	public void delete(IdentifiedObject object) {

	}

	@Override
	public <U> List<? extends U> loadAll(Class<U> clazz) {
		return Collections.emptyList();
	}

	@Override
	public Map<UID, IdentifiedObject> getIdMap(Class<?> clazz) {
		return Collections.emptyMap();
	}

	@Override
	public List<?> loadForeignKey(Class<?> clazz, UID id, String fkName) {
		return Collections.emptyList();
	}

	@Override
	public List<?> loadForeignKey(Class<?> clazz, UID id, String fkName, boolean sandBox) {
		return Collections.emptyList();
	}

	@Override
	public List<?> loadForeignKey(Class<?> clazz, UID id, String fkName, boolean sandBox,
			boolean clearSession) {
		return Collections.emptyList();
	}

	@Override
	public List<?> loadWhere(Class<?> clazz, String columnName, String columnValue) {
		return Collections.emptyList();
	}

	@Override
	public void save(IdentifiedObject object, boolean forceSave) {

	}

	@Override
	public boolean isPersisting() {
		return false;
	}

	@Override
	public void refresh(IdentifiedObject o) {

	}

	@Override
	public void clearException() {

	}

	@Override
	public <T> List<? extends T> loadAll(Class<T> clazz, Session session) {
		return Collections.emptyList();
	}

	@Override
	public void delete(IdentifiedObject object, Session session) {

	}

	@Override
	public IdentifiedObject load(Class<?> clazz, UID id, Session session, boolean clearSession) {
		return null;
	}

	@Override
	public void save(IdentifiedObject object, boolean forceSave, Session session,
			boolean clearSession) {
	}

}
