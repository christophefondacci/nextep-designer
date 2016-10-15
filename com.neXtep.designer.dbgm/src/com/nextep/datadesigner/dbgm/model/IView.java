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

import java.util.List;

/**
 * This interface represents a database view.
 * 
 * @author Christophe Fondacci
 *
 */
public interface IView extends IDatabaseObject<IView>, ITriggable, IColumnable, ISqlBased {

	public static final String TYPE_ID="SQL_VIEW";

	public List<String> getColumnAliases();
	/**
	 * Adds the specified alias to the alias definition of this 
	 * view. Note that alias should be in synch with the columns
	 * selection of the underlying SQL query.
	 *  
	 * @param alias to add
	 */
	public void addColumnAlias(String alias);
	/**
	 * Updates the alias of the given index with
	 * the one specified.
	 * @param i index of the alias to set
	 * @param alias alias text
	 */
	public void setColumnAlias(int i, String alias);
	/**
	 * Removes the specified alias to the alias definition of this
	 * view. 
	 * @param i index of the alias to remove (zero-relative)
	 */
	public void removeColumnAlias(int i);
	/**
	 * @return the SQL statement which defines this view
	 * @deprecated please use getSql() instead
	 */
	@Deprecated
	public String getSQLDefinition();
	/**
	 * Defines the underlying SQL definition of this view.
	 * @param sql the sql definition of this view
	 */
	public void setSQLDefinition(String sql);
}
