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

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;

/**
 * Interface representing a database datatype.
 * 
 * @author Christophe Fondacci
 */
public interface IDatatype extends INamedObject, IObservable {

	/**
	 * @return the datatype length or <code>null</code> if not set
	 */
	Integer getLength();

	/**
	 * Defines the length of this datatype.
	 * 
	 * @param length
	 *            length of the datatype
	 */
	void setLength(Integer length);

	/**
	 * @return the precision of this datatype or null if not set
	 */
	Integer getPrecision();

	/**
	 * Sets the precision of this datatype.
	 * 
	 * @param precision
	 *            precision of this datatype
	 */
	void setPrecision(Integer precision);

	/**
	 * @return whether this datatype is unsigned or not
	 */
	boolean isUnsigned();

	/**
	 * Defines the unsigned flag of this datatype
	 * 
	 * @param unsigned
	 *            true for unsigned datatypes
	 */
	void setUnsigned(boolean unsigned);

	/**
	 * Returns the type of the datatype length. Used for Oracle char types,
	 * might be useful for other vendors supporting both BYTES / CHAR modes
	 * 
	 * @return the {@link LengthType} of this datatype's lenght
	 */
	LengthType getLengthType();

	/**
	 * Sets the length type of this datatype
	 * 
	 * @param lengthType
	 *            the {@link LengthType} of this datatype's length
	 */
	void setLengthType(LengthType lengthType);

	/**
	 * Defines the length in char of this column. This information is used to
	 * store both representation of a column length during synchronization (BYTE
	 * and CHAR). Since we don't yet know which version we will need to compare.
	 * This information is not used when it comes from the repository where a
	 * single version of the length is stored in the <code>length</code> field
	 * with the <code>lengthInChar</code> flag.
	 * 
	 * @param charLength
	 *            the length of the column in CHAR.
	 */
	void setAlternateLength(Integer charLength);

	/**
	 * Provides the length in CHAR of this column. Only available when data
	 * comes from a synchronization. Callers should always consider that this
	 * information may be <code>null</code>. This information should typically
	 * only be used by comparators.
	 * 
	 * @return the length of this column, expressed in CHARs, if available
	 *         otherwise it is <code>null</code>
	 */
	Integer getAlternateLength();
}
