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
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.model.IReference;

/**
 * A MySQL-specific extension of an index providing Mysql-specific support for indexes, such as
 * partial length indexed columns.
 * 
 * @author Christophe Fondacci
 */
public interface IMySQLIndex extends IIndex {

	/**
	 * Retrieves the prefix length for the specified index column. This method may return
	 * <code>null</code> to indicate that no explicit prefix length has been defined for this
	 * column.
	 * 
	 * @param indexColumn the {@link IBasicColumn} of the column to get prefix length for
	 * @return the prefix length or <code>null</code> if no explicit value defined
	 */
	Integer getColumnPrefixLength(IReference indexColumnRef);

	/**
	 * Defines the prefix length for the given column. This method will accept <code>null</code> as
	 * a prefix length value to indicate that no explicit prefix length is wanted for this indexed
	 * column.
	 * 
	 * @param indexColumn the {@link IBasicColumn} of the column to set prefix length for
	 * @param prefixLength the prefix length to set for this column or <code>null</code> to unset
	 *        any previously defined prefix length
	 */
	void setColumnPrefixLength(IReference indexColumnRef, Integer prefixLength);
}
