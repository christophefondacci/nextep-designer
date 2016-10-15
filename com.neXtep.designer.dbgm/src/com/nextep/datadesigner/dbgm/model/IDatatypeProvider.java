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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.nextep.datadesigner.exception.UnsupportedDatatypeException;

/**
 * A data type provider is the class which provides the list of supported
 * database types for a given database vendor. <br>
 * Implementations should register to the <code>datatypeProvider</code>
 * extension point.<br>
 * There could be 1 and only 1 provider registered for a given vendor.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public interface IDatatypeProvider {

	/**
	 * Lists all data types names supported by the database vendor.
	 * 
	 * @return a list of all supported data types names
	 */
	List<String> listSupportedDatatypes();

	/**
	 * @return the default data type for this provider
	 */
	IDatatype getDefaultDatatype();

	/**
	 * Lists all data types for which values should be encapsulated within the
	 * string delimiter.
	 * 
	 * @return an exhaustive list of all string data types
	 */
	List<String> listStringDatatypes();

	/**
	 * Retrieves the list of numeric datatypes.
	 * 
	 * @return a list of vendor-specific datatypes handling numeric values
	 */
	List<String> getNumericDatatypes();

	/**
	 * Informs whether the specified {@link IDatatype} can store a decimal value
	 * or not.
	 * 
	 * @param type
	 *            the {@link IDatatype} to check
	 * @return <code>true</code> if the specified datatype can accept decimal
	 *         numbers, else <code>false</code>
	 */
	boolean isDecimalDatatype(IDatatype type);

	/**
	 * Retrieves the list of vendor-specific datatypes which defines date, or
	 * date+time, or timestamps
	 * 
	 * @return a list of vendor-specific datatypes handling dated information
	 */
	List<String> getDateDatatypes();

	/**
	 * @return a map of data types names which could be considered equivalent
	 *         and will not return as differences for the synchronizer.
	 */
	Map<String, String> getEquivalentDatatypesMap();

	/**
	 * Returns a list of data types for which the length and precision
	 * attributes are not relevant for the database vendor.
	 * 
	 * @return a {@link List} of data types
	 */
	List<String> getUnsizableDatatypes();

	/**
	 * Returns the maximum size value supported by the database vendor for the
	 * specified data type.
	 * 
	 * @param type
	 *            a <code>String</code> representing a data type, must match one
	 *            the supported data types returned by the
	 *            {@link IDatatypeProvider#listSupportedDatatypes()}
	 * @return a <code>BigDecimal</code> representing the maximum size of the
	 *         specified data type
	 * @throws UnsupportedDatatypeException
	 *             if the specified data type is <code>null</code>, or is not
	 *             supported by the database vendor, or if the maximum size
	 *             cannot be determined by this <code>DatatypeProvider</code>
	 */
	BigDecimal getDatatypeMaxSize(String type) throws UnsupportedDatatypeException;

	/**
	 * Informs whether the provider supports typed length. For example, Oracle
	 * supports typed length for CHAR related datatypes: COL VARCHAR2(20 CHAR)
	 * differs from COL VARCHAR2(20 BYTE)
	 * 
	 * @param datatype
	 *            the datatype being processed
	 * @return <code>true</code> if datatype length could by typed, else
	 *         <code>false</code>
	 */
	boolean isTypedLengthSupportedFor(String datatype);

}
