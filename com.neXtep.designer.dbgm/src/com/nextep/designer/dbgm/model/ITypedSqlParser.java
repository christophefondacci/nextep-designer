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
package com.nextep.designer.dbgm.model;

import com.nextep.datadesigner.dbgm.impl.ParseData;
import com.nextep.datadesigner.dbgm.model.IParseData;
import com.nextep.datadesigner.dbgm.model.IParseable;

/**
 * An interface for implementing typed SQL parsers. Typed parsers are used to fill some object
 * properties from a source (generally multi-line) property of the same element.<br>
 * 
 * @author Christophe
 */
public interface ITypedSqlParser {

	/**
	 * Parses the specified element and returns parse metadata as a {@link ParseData} bean.
	 * Implementors may directly inject parsed information into the {@link IParseable} as well.<br>
	 * Parse data is used by UI editors to properly locate user's edit location and to link
	 * correctly with outlines.
	 * 
	 * @param p the SQL based element to parse
	 * @param contentsToParse string to parse
	 */
	IParseData parse(IParseable p, String contentsToParse);

	/**
	 * Extracts the name from the parsing of the provided element.
	 * 
	 * @param parseable the element to parse in order to extract its name
	 * @return the parsed name
	 */
	String parseName(String sql);

	/**
	 * Renames the provided raw SQL string by the new supplied name.
	 * 
	 * @param sqlToRename the sql source to rename
	 * @param newName new name
	 * @return the resulting renamed SQL source
	 */
	String rename(String sqlToRename, String newName);

	/**
	 * Renames the SQL-source of this parseable by the new provided name. The rename will be
	 * performed <u>at a SQL-level</u>.
	 * 
	 * @param parseable the {@link IParseable} to rename
	 * @param newName the new name to define in the SQL-source
	 */
	void rename(IParseable parseable, String newName);
}
