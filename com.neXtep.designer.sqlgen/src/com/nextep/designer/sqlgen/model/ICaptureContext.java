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
package com.nextep.designer.sqlgen.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.helpers.CaptureHelper;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * This interface represents the context of a capture. A capture context is the place where captured
 * objects are placed as soon as they have been captured. It allows {@link ICapturer} implementation
 * to have access to partially initialized objects, or access to the connection objects, etc.
 * 
 * @author Bruno Gautier
 * @author Christophe Fondacci
 */
public interface ICaptureContext {

	/**
	 * Returns the connection Object to use to connect to the data source.
	 * 
	 * @return an <code>Object</code> that can be used to connect to a data source.
	 */
	Object getConnectionObject();

	/**
	 * Returns an unmodifiable <code>Collection</code> of <code>IVersionable</code> objects
	 * representing the list of the <i>versionable</i> objects currently available in this context.
	 * 
	 * @return an unmodifiable {@link Collection} of {@link IVersionable} objects if some
	 *         <i>versionable</i> objects are available in this context, an empty {@link List}
	 *         otherwise
	 */
	Collection<IVersionable<?>> getDbObjectsAsVersionables();

	/**
	 * Returns the captured object corresponding to the specified typed object's unique name if
	 * available in this context. Callers may base their call on {@link CaptureHelper} methods to
	 * generate the unique name of a potentially non-unique object name.
	 * 
	 * @param type the type of the object we are looking for.
	 * @param name a formatted object name according to the database vendor formatting policy
	 * @return the captured object instance corresponding to the specified object's unique name if
	 *         available, <code>null</code> otherwise
	 * @see CaptureHelper
	 */
	Object getCapturedObject(IElementType type, String name);

	/**
	 * Returns a collection of objects instances captured for the specified type or an empty list.
	 * 
	 * @param type the {@link IElementType} to retrieve captured objects for
	 * @return an {@link Collection} of objects captured under the given {@link IElementType}
	 */
	Collection<Object> getCapturedObjects(IElementType type);

	/**
	 * Returns a <code>Map</code> of objects captured for the given type as a map of instances
	 * hashed by object's names
	 * 
	 * @return an {@link Map} of captured object instances hashed by their name. Empty when no
	 *         objects captured for the specified type
	 */
	Map<String, Object> getCapturedObjectMap(IElementType type);

	/**
	 * Returns the <code>IBasicTable</code> corresponding to the specified table's unique name if
	 * available in this context.
	 * <p>
	 * This is a convenience method equivalent to :
	 * <code>(IBasicTable)getCapturedObject(IElementType.getInstance(IBasicTable.TYPE_ID),name)</code>
	 * </p>
	 * 
	 * @param name the table name to retrieve
	 * @return a {@link IBasicTable} corresponding to the specified table's unique name if
	 *         available, <code>null</code> otherwise
	 */
	IBasicTable getTable(String name);

	/**
	 * Indicates the schema name to use for the capture.
	 * 
	 * @return the schema name to fetch structure for
	 */
	String getSchema();

	/**
	 * Returns the catalog name to use for the capture.
	 * 
	 * @return the catalog name.
	 */
	String getCatalog();

	/**
	 * Indicates the current connection of this capture.
	 * 
	 * @return the {@link IConnection} of the current capture
	 */
	IConnection getConnection();

	/**
	 * Defines a context attribute for a database object.<br>
	 * Context attributes are volatile information about database objects that can be stored by
	 * capturers for internal use as part of the capture process.
	 * 
	 * @param dbObj the {@link IDatabaseObject} for which we define an attribute
	 * @param attName the name of the attribute
	 * @param attValue the value of the attribute
	 */
	void setDbObjectAttribute(IDatabaseObject<?> dbObj, String attName, String attValue);

	/**
	 * Returns the value of a database object's attribute if available.
	 * 
	 * @param dbObj the {@link IDatabaseObject} for which we must return an attribute's value
	 * @param attName the name of the attribute
	 * @return a <code>String</code> representing the attribute's value, <code>null</code> if this
	 *         attribute does not exists for the specified database object
	 */
	String getDbObjectAttributeValue(IDatabaseObject<?> dbObj, String attName);

	/**
	 * Returns a <code>Map</code> of values stored in this context for the given attribute as a map
	 * of attribute values hashed by database objects.
	 * 
	 * @param attName the name of the attribute
	 * @return an {@link Map} of attribute values hashed by database objects. Empty if no value has
	 *         been stored in this context for the specified attribute
	 */
	Map<IDatabaseObject<?>, String> getAttributeValues(String attName);

}
