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

import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.model.IConnection;

/**
 * @author Bruno Gautier
 * @author Christophe Fondacci
 */
public interface IMutableCaptureContext extends ICaptureContext {

	/**
	 * Sets the connection Object to use to connect to the data source.
	 * 
	 * @param conn an <code>Object</code> that can be used to connect to a data source
	 */
	void setConnectionObject(Object conn);

	void addCapturedObject(IElementType type, String name, Object object);

	//
	// /**
	// * Adds a new table to this capturer context.
	// *
	// * @param column a {@link IBasicTable} to add to this context
	// */
	// void addTable(IBasicTable table);
	//
	// /**
	// * Adds a new column to this capturer context.
	// *
	// * @param column a {@link IBasicColumn} to add to this context
	// */
	// void addColumn(IBasicColumn column);
	//
	// /**
	// * Adds a new index to this capturer context.
	// *
	// * @param index a {@link IIndex} to add to this context
	// */
	// void addIndex(IIndex index);
	//
	// /**
	// * Adds a new view to this capturer context.
	// *
	// * @param view a {@link IView} to add to this context
	// */
	// void addView(IView view);
	//
	// /**
	// * Adds a new vendor specific database object to this capturer context.
	// *
	// * @param dbObj a {@link IDatabaseObject} to add to this context
	// */
	// void addVendorSpecificDbObject(IDatabaseObject<?> dbObj);

	/**
	 * Defines the schema name from which structure should be retrieved.
	 * 
	 * @param schema the schema name of the capture, or <code>null</code> if no explicit schema
	 *        should be used in this capture
	 */
	void setSchema(String schema);

	/**
	 * Defines the catalog name from which structure should be retrieved.
	 * 
	 * @param catalog the catalog name for the capture, <code>null</code> if the catalog name should
	 *        not be used to narrow the search
	 */
	void setCatalog(String catalog);

	/**
	 * Defines the connection used in the current capture.
	 * 
	 * @param connection the {@link IConnection} used by the capture represented by this context.
	 */
	void setConnection(IConnection connection);

}
