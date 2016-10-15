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

/**
 * An abstraction for all objects which are defined by a SQL code. It could be elements like views,
 * triggers, procedures, packages, etc.<br>
 * This interface provides common methods to access the SQL code whatever the concrete
 * implementation.
 * 
 * @author Christophe Fondacci
 */
public interface ISqlBased {

	/**
	 * @return the SQL code of this SQL-based element as a string
	 */
	String getSql();

	/**
	 * Defines the SQL code of this SQL-based element
	 * 
	 * @param sql new SQL code
	 */
	void setSql(String sql);
}
