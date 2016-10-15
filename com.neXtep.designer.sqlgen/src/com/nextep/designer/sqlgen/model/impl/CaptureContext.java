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
package com.nextep.designer.sqlgen.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.model.IMutableCaptureContext;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * Default capture context implementation.
 * 
 * @author Bruno Gautier
 * @author Christophe Fondacci
 */
public class CaptureContext implements IMutableCaptureContext {

	private final List<IVersionable<?>> dbObjects = new ArrayList<IVersionable<?>>();

	private final Map<IElementType, Map<String, Object>> capturedObjectsMap = new HashMap<IElementType, Map<String, Object>>();
	private final Map<IDatabaseObject<?>, Map<String, String>> attributes = new HashMap<IDatabaseObject<?>, Map<String, String>>();

	private Object connObj;
	private String schema;
	private String catalog;
	private IConnection connection;

	@Override
	public void setConnectionObject(Object conn) {
		this.connObj = conn;
	}

	@Override
	public Object getConnectionObject() {
		return connObj;
	}

	@Override
	public Collection<IVersionable<?>> getDbObjectsAsVersionables() {
		return Collections.unmodifiableCollection(dbObjects);
	}

	private Map<String, Object> getTypedMap(IElementType type) {
		Map<String, Object> typedMap = capturedObjectsMap.get(type);
		if (typedMap == null) {
			typedMap = new HashMap<String, Object>();
			capturedObjectsMap.put(type, typedMap);
		}
		return typedMap;
	}

	@Override
	public void addCapturedObject(IElementType type, String name, Object object) {
		final Map<String, Object> typedMap = getTypedMap(type);
		typedMap.put(name, object);
		if (object instanceof IVersionable<?>) {
			dbObjects.add((IVersionable<?>) object);
		}
	}

	@Override
	public Object getCapturedObject(IElementType type, String name) {
		final Map<String, Object> typedMap = getTypedMap(type);
		return typedMap.get(name);
	}

	@Override
	public Map<String, Object> getCapturedObjectMap(IElementType type) {
		return getTypedMap(type);
	}

	@Override
	public Collection<Object> getCapturedObjects(IElementType type) {
		final Map<String, Object> typedMap = getTypedMap(type);
		return typedMap.values();
	}

	@Override
	public IBasicTable getTable(String name) {
		return (IBasicTable) getCapturedObject(IElementType.getInstance(IBasicTable.TYPE_ID), name);
	}

	@Override
	public String getSchema() {
		return schema;
	}

	@Override
	public void setSchema(String schema) {
		this.schema = schema;
	}

	@Override
	public String getCatalog() {
		return catalog;
	}

	@Override
	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}

	@Override
	public IConnection getConnection() {
		return connection;
	}

	@Override
	public void setConnection(IConnection connection) {
		this.connection = connection;
	}

	@Override
	public void setDbObjectAttribute(IDatabaseObject<?> dbObj, String attName, String attValue) {
		Map<String, String> dbObjAttributes = attributes.get(dbObj);
		if (dbObjAttributes == null) {
			dbObjAttributes = new HashMap<String, String>();
			attributes.put(dbObj, dbObjAttributes);
		}
		dbObjAttributes.put(attName, attValue);
	}

	@Override
	public String getDbObjectAttributeValue(IDatabaseObject<?> dbObj, String attName) {
		Map<String, String> dbObjAttributes = attributes.get(dbObj);
		if (dbObjAttributes != null) {
			return dbObjAttributes.get(attName);
		}
		return null;
	}

	@Override
	public Map<IDatabaseObject<?>, String> getAttributeValues(String attName) {
		Map<IDatabaseObject<?>, String> attValues = new HashMap<IDatabaseObject<?>, String>();
		for (Map.Entry<IDatabaseObject<?>, Map<String, String>> entry : attributes.entrySet()) {
			IDatabaseObject<?> dbObj = entry.getKey();
			Map<String, String> dbObjAttributes = entry.getValue();
			if (dbObjAttributes.containsKey(attName)) {
				attValues.put(dbObj, dbObjAttributes.get(attName));
			}
		}
		return attValues;
	}

}
