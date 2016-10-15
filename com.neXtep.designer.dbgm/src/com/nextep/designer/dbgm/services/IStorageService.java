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
package com.nextep.designer.dbgm.services;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.IDataSet;
import com.nextep.designer.dbgm.model.IStorageHandle;

public interface IStorageService {

	String ROWID_COLUMN_NAME = "nxtp_rowid"; //$NON-NLS-1$

	/**
	 * Gets a new connection from the local database. Callers are responsible for calling
	 * {@link Connection#close()} when they finished work with this connection
	 * 
	 * @return a jdbc {@link Connection}
	 * @throws SQLException whenever we experienced problems to create the connection
	 */
	Connection getLocalConnection() throws SQLException;

	/**
	 * Initializes a new storage unit for the specified data set. This specialized entry-point is
	 * internally used by the data service to create mirrored storage able to contain 2 versions of
	 * the table data in order to be able to dynamically compare previous and current values.
	 * 
	 * @param set data set to create storage for
	 * @param isMirrored indicates whether the storage should support mirror data.
	 * @return a {@link IStorageHandle} which locates the storage unit
	 */
	IStorageHandle createDataSetStorage(IDataSet set, boolean isMirrored);

	/**
	 * Initializes a new storage unit for the specified data set.
	 * 
	 * @param set data set to create storage for
	 * @return a {@link IStorageHandle} which locates the storage unit
	 */
	IStorageHandle createDataSetStorage(IDataSet set);

	/**
	 * Renames the underlying storage unit.
	 * 
	 * @param set the {@link IDataSet} whose storage needs to be renamed
	 * @param newName new name to assign to this storage
	 */
	void renameStorageHandle(IDataSet set, String newName);

	/**
	 * Retrieves the datatype of the underlying storage for the specified column of the dataset.
	 * 
	 * @param set the data set
	 * @param c the column to retrieve the corresponding derby datatype
	 * @return the storage-specific datatype to use for this column in the storage
	 */
	IDatatype getColumnDatatype(IDataSet set, IBasicColumn c);

	/**
	 * Retrieves the SQL type of the specified column.
	 * 
	 * @param set the dataset referencing this column.
	 * @param c the column to retrieve the SQL type of
	 * @return a SQL type from the {@link Types} constant definition to use with JDBC
	 */
	int getColumnSqlType(IDataSet set, IBasicColumn c);

	/**
	 * Decodes the column value as a properly casted object to use with this storage.
	 * 
	 * @param columnRef the IReference of the column for which the value needs to be decoded
	 * @param strValue the string value to decode
	 * @return a properly casted object that could be injected in the local storage
	 */
	Object decodeValue(IReference columnRef, String strValue);

	/**
	 * Escapes the specified identifier for the underlying storage implementation. The returned
	 * string may be exactly the same as the input if no escape is required.
	 * 
	 * @param identifier identifier to escape in a SQL statement sent to the storage service
	 * @return the escaped identifier
	 */
	String escape(String identifier);

	/**
	 * Builds the select statement which will retrieve data lines from local storage BEFORE the
	 * storage is actually created (i.e. we don't need the storage handle).
	 * 
	 * @param set the {@link IDataSet} to generate select statement for
	 * @return the select statement
	 */
	String getSelectStatement(IDataSet set);
}
