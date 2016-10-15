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
package com.nextep.designer.dbgm.services;

import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.model.IElementType;

/**
 * This service class provides methods to manipulate SQL-based elements which are parseable like
 * procedures, functions, packages, views, triggers, etc.<br>
 * 
 * @author Christophe Fondacci
 */
public interface IParsingService {

	/**
	 * Parses the specified parseable element. All parsers attached to this element through the
	 * sqlTypedParser extension point will be invoked.
	 * 
	 * @param p parseable element to parse.
	 */
	void parse(IParseable p);

	/**
	 * Parses a specific source into the supplied {@link IParseable}.
	 * 
	 * @param p the {@link IParseable} in which parse info should be stored
	 * @param parseSource the source code to parse
	 */
	void parse(IParseable p, String parseSource);

	/**
	 * Extracts the name from the parsing of the provided element.
	 * 
	 * @param parseable the element to parse in order to extract its name
	 * @return the parsed name
	 */
	String parseName(IParseable parseable);

	/**
	 * Extracts the name from the parsing the SQL string with the parser for the given
	 * {@link IElementType}.
	 * 
	 * @param type type of SQL source (used to delegate to the appropriate parser)
	 * @param sql SQL source to parse name from
	 * @return the parsed name
	 */
	String parseName(IElementType type, String sql);

	/**
	 * Renames the provided {@link IParseable} element by the new supplied name.
	 * 
	 * @param parseableToRename element to rename
	 * @param newName new name
	 */
	void rename(IParseable parseableToRename, String newName);

	/**
	 * Renames the provided raw SQL source of the given type with the supplied new name.
	 * 
	 * @param sqlType type of the raw SQL source to rename (used to delegate to the appropriate
	 *        parser)
	 * @param sqlToRename raw SQL source to rename
	 * @param newName new name to set in SQL
	 * @return the renamed SQL string
	 */
	String getRenamedSql(IElementType sqlType, String sqlToRename, String newName);
}
