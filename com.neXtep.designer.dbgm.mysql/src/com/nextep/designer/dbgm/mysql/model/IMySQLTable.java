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

import com.nextep.datadesigner.dbgm.model.IBasicTable;

/**
 * MySQL table representation. This interface extends the standard {@link IBasicTable} interface to
 * add / manage MySQL specific attributes to a table definition.
 * 
 * @author Christophe Fondacci
 */
public interface IMySQLTable extends IBasicTable {

	/**
	 * @return the engine to use for this table
	 */
	String getEngine();

	/**
	 * Defines the engine of this table.
	 * 
	 * @param engine table engine
	 */
	void setEngine(String engine);

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
