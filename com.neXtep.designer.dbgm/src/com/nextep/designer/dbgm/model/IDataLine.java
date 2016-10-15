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
/**
 *
 */
package com.nextep.designer.dbgm.model;

import java.util.Collection;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface represents a line of data. A line of data is a set of column values, each of them
 * belonging to the same table / view object.
 * 
 * @author Christophe Fondacci
 */
public interface IDataLine extends IdentifiedObject, IObservable, ITypedObject {

	public static final String TYPE_ID = "DATALINE"; //$NON-NLS-1$

	/**
	 * @return all the column values defined in this data line ordered by ascending position
	 */
	Collection<IColumnValue> getColumnValues();

	/**
	 * Retrieves the column value defined at the specified position within this line of data.
	 * 
	 * @param linePosition the position of this column value in the line of data
	 * @return the column value defined at the given position on this data line
	 */
	IColumnValue getColumnValue(int linePosition);

	/**
	 * Retrieves the column value defined for the specified column.
	 * 
	 * @param column the column for which you want the value
	 * @return the column value mapped to this column in this line of data
	 */
	IColumnValue getColumnValue(IReference column);

	/**
	 * Adds a column value to this line of data.
	 * 
	 * @param columnValue the column value to add to this line
	 */
	void addColumnValue(IColumnValue columnValue);

	/**
	 * Removes a column value from this line of data.
	 * 
	 * @param columnValue the column value to remove
	 * @return a flag indicating if the specified column value has effectively be removed from this
	 *         data line.<br>
	 *         - <code>true</code> if the column value has been removed<br>
	 *         - <code>false</code> if the column value has not been found in this data line.
	 */
	boolean removeColumnValue(IColumnValue columnValue);

	/**
	 * @return the set in which this line has been defined
	 */
	IDataSet getDataSet();

	/**
	 * Defines the parent dataset of this dataline
	 * 
	 * @param parent new parent dataset
	 */
	void setDataSet(IDataSet parent);

	/**
	 * Retrieves the unique identifier of this row within the parent dataset. This information is
	 * used to trace changes of a single same line between different versions of the dataset
	 * 
	 * @return the row unique identifier for this dataset
	 */
	long getRowId();

	/**
	 * Defines the unique identifier of this row within the parent dataset. This information is used
	 * to trace changes of a single same line between different versions of the dataset
	 * 
	 * @param rowId the row unique identifier for this dataset
	 */
	void setRowId(long rowId);

}
