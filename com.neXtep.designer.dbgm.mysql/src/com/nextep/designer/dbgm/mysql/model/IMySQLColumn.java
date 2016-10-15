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
package com.nextep.designer.dbgm.mysql.model;

import com.nextep.datadesigner.dbgm.model.IBasicColumn;

/**
 * This class adds MySQL specific features to columns such as auto increment support, unsigned
 * datatypes.
 * 
 * @author Christophe Fondacci
 */
public interface IMySQLColumn extends IBasicColumn {

	/**
	 * @return a boolean indicating if this column is auto incremented
	 */
	boolean isAutoIncremented();

	/**
	 * Defines if this column is auto-incremented.
	 * 
	 * @param autoIncremented the auto increment flag
	 */
	void setAutoIncremented(boolean autoIncremented);

	/**
	 * Defines the character set of this table
	 * 
	 * @param charset character set name
	 */
	void setCharacterSet(String charset);

	/**
	 * @return the character set of this table
	 */
	String getCharacterSet();

	/**
	 * Defines the collation name of this table
	 * 
	 * @param collation the MySql collation name
	 */
	void setCollation(String collation);

	/**
	 * Retrieves the MySql collation name
	 * 
	 * @return the table's collation name
	 */
	String getCollation();
}
