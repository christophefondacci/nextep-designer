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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.mysql.services;

import java.util.List;

/**
 * @author Christophe Fondacci
 */
public interface IMySqlModelService {

	/**
	 * Returns the list of available charset codes
	 * 
	 * @return the list of MySql supported charset
	 */
	List<String> getCharsetsList();

	/**
	 * Returns a user-oriented list of full qualified names of available MySql charsets
	 * 
	 * @return a user-oriented list of full qualified names of available MySql charsets
	 */
	List<String> getCharsetNamesList();

	/**
	 * Retrieves a charset code from its user-oriented name
	 * 
	 * @param charsetName the charset name to retrieve the code for
	 * @return the code of the corresponding charset or <code>null</code> if the specified name does
	 *         not correspond to any charset
	 */
	String getCharsetFromName(String charsetName);

	/**
	 * Retrieves a charset name from its code
	 * 
	 * @param charset the charset code to retrieve the name for
	 * @return the user-oriented full qualified name of the corresponding charset or
	 *         <code>null</code> if the specified code does not correspond to a valid charset
	 */
	String getCharsetName(String charset);

	/**
	 * Retrieves the list of available collations
	 * 
	 * @return the list of available collations
	 */
	List<String> getCollationsList();

	/**
	 * Retrieves the default collation for the specified charset.
	 * 
	 * @param charset the charset to retrieve the default collation for
	 * @return the default charset collation
	 */
	String getDefaultCollation(String charset);

	/**
	 * Retrieves a non-exhaustive list of most commonly used MySql engines. Engines are expressed as
	 * string as they are understood by the '<code>Engine=</code>' clause of a mysql statement
	 * 
	 * @return a list of the most commonly used MySql engines
	 */
	List<String> getEngineList();

	/**
	 * Retrieves the default mysql engine
	 * 
	 * @return the default mysql storage engine
	 */
	String getDefaultEngine();
}
