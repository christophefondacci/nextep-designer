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

import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * This interface represents the value of a given column. A column value is always part of a data
 * line within a data set.
 * 
 * @author Christophe Fondacci
 */
public interface IColumnValue extends IdentifiedObject, ITypedObject, IObservable {

	public static final String TYPE_ID = "COLUMN_VALUE"; //$NON-NLS-1$

	/**
	 * @return the column for which this value is defined
	 */
	IBasicColumn getColumn();

	/**
	 * Defines the column for which this value is defined. Setting a column when it has already been
	 * assigned may not be authorized.
	 * 
	 * @param column the {@link IBasicColumn} for which this value is defined
	 */
	void setColumn(IBasicColumn column);

	/**
	 * @return the column reference for which this value is defined
	 */
	IReference getColumnRef();

	/**
	 * Defines the column as a reference for which this value is defined. Setting a column when it
	 * has already been assigned may not be authorized.
	 * 
	 * @param column the {@link IReference} of the column for which this value is defined
	 */
	void setColumnRef(IReference r);

	/**
	 * @return the string representation of the defined value. This method should never return
	 *         <code>null</code> and will return "" for <code>null</code> or undefined values.
	 */
	String getStringValue();

	/**
	 * @return the value defined for this column in the associated data line.
	 */
	Object getValue();

	/**
	 * Sets the value of this column value
	 * 
	 * @param value new value for this column value
	 */
	void setValue(Object value);

	/**
	 * Defines the dataline for which this column value is defined
	 * 
	 * @param line the {@link IDataLine} defining this column value
	 */
	void setDataLine(IDataLine line);

	/**
	 * Retrieves the data line which defines this column value.
	 * 
	 * @return the {@link IDataLine} defining this column
	 */
	IDataLine getDataLine();
	/**
	 * @return the position of this column value within the associated data line.
	 */
	// int getPosition();
}
