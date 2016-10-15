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
package com.nextep.designer.dbgm.oracle.model;

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.IPhysicalObject;

/**
 * @author Christophe Fondacci
 */
public interface IMaterializedViewLog extends IDatabaseObject<IMaterializedViewLog>,
		IPhysicalObject {

	static final String TYPE_ID = "MVIEW_LOG"; //$NON-NLS-1$

	/**
	 * @return the reference of the table for which this log is defined
	 */
	IReference getTableReference();

	/**
	 * A convenience method for retrieving the table resolved from its reference. If the table
	 * cannot be found (unresolved or too many references), <code>null</code> will be returned.
	 * 
	 * @return the underlying table
	 */
	IBasicTable getTable();

	/**
	 * Defines the reference of the table to define the materialized log on.
	 * 
	 * @param tableRef table reference
	 */
	void setTableReference(IReference tableRef);

	/**
	 * @return whether this log supports row ids
	 */
	boolean isRowId();

	/**
	 * Defines whether this log supports row ids.
	 * 
	 * @param rowId
	 */
	void setRowId(boolean rowId);

	/**
	 * @return whether this log supports primary keys
	 */
	boolean isPrimaryKey();

	/**
	 * Defines whether this log supports primary keys.
	 * 
	 * @param primaryKey
	 */
	void setPrimaryKey(boolean primaryKey);

	/**
	 * @return whether this log supports sequence
	 */
	boolean isSequence();

	/**
	 * Defines whether this log supports sequence.
	 * 
	 * @param sequence
	 */
	void setSequence(boolean sequence);

	/**
	 * @return whether this log includes or excludes new values
	 */
	boolean isIncludingNewValues();

	/**
	 * Defines whether this log includes or excludes new values.
	 * 
	 * @param include
	 */
	void setIncludingNewValues(boolean include);

}
