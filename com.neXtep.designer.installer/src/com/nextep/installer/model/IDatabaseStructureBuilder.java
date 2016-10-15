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
package com.nextep.installer.model;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * This interface is a builder of database structure definition.
 * 
 * @author Christophe Fondacci
 */
public interface IDatabaseStructureBuilder {

	/**
	 * Builds a {@link IDatabaseStructure} bean from the given connection.
	 * 
	 * @param the schema to consider or null for all accessible schema
	 * @param conn the database {@link Connection} to extract the structure from
	 * @return a {@link IDatabaseStructure}
	 */
	IDatabaseStructure buildStructure(String schema, Connection conn) throws SQLException;
}
