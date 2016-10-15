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
package com.nextep.datadesigner.dbgm.model;

import java.util.List;
import com.nextep.datadesigner.model.IReference;

/**
 * An interface representing a database index.
 * 
 * @author Christophe Fondacci
 */
public interface IIndex extends IDatabaseObject<IIndex>, IColumnable {

	public static final String INDEX_TYPE = "INDEX"; //$NON-NLS-1$

	/**
	 * @return the reference of the indexed table
	 */
	IReference getIndexedTableRef();

	/**
	 * A convenience method which returns the indexed table (rather than the table reference).
	 * 
	 * @return the indexed table
	 */
	IBasicTable getIndexedTable();

	/**
	 * Defines the reference of the indexed table
	 * 
	 * @param tableRef reference of indexed table
	 */
	void setIndexedTableRef(IReference tableRef);

	/**
	 * @return the list of indexed columns
	 */
	List<IReference> getIndexedColumnsRef();

	/**
	 * @return the index type
	 */
	IndexType getIndexType();

	/**
	 * Defines the index type from the enumerated constants
	 * 
	 * @param type index type
	 */
	void setIndexType(IndexType type);

	/**
	 * Adds a column to the list of indexed columns
	 * 
	 * @param column column to add to the index
	 */
	void addColumnRef(IReference colRef);

	/**
	 * Removes a column to the list of indexed columns
	 * 
	 * @param column column to remove from indexed columns
	 */
	void removeColumnRef(IReference colRef);

	/**
	 * @return the index strict name. For multi-database support, index names may not be unique in a
	 *         single database. The {@link IIndex#getName()} method will return a fully qualified
	 *         name (prefixed by table name) while this methods will return the index solo name.
	 */
	String getIndexName();

	/**
	 * Defines the index strict name.
	 * 
	 * @see IIndex#getIndexName()
	 * @param name index name
	 */
	void setIndexName(String name);

	/**
	 * Retrieves the function associated to this column in this index.
	 * 
	 * @param r column reference
	 * @return the indexed function for this column or <code>null</code> when the column is not a
	 *         function-based index column
	 */
	String getFunction(IReference r);

	/**
	 * Defines a function on the given column reference.
	 * 
	 * @param r the column reference
	 * @param func the function on this column
	 */
	void setFunction(IReference r, String func);
}
