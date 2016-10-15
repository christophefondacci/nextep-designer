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

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;

/**
 * This object represents a check constraint.
 * 
 * @author Christophe
 */
public interface ICheckConstraint extends IDatabaseObject<ICheckConstraint> {

	public static final String TYPE_ID = "CHECK"; //$NON-NLS-1$

	/**
	 * @return the condition validated by this constraint
	 */
	String getCondition();

	/**
	 * Defines the condition validated by this constraint
	 * 
	 * @param condition
	 */
	void setCondition(String condition);

	/**
	 * @return the table to which this constraint applies
	 */
	IBasicTable getConstrainedTable();

	/**
	 * Defines the table to which this constraint applies
	 * 
	 * @param t parent table
	 */
	void setConstrainedTable(IBasicTable t);
}
