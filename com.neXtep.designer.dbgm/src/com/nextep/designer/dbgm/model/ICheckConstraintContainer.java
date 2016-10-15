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
package com.nextep.designer.dbgm.model;

import java.util.Set;

/**
 * @author Christophe Fondacci
 *
 */
public interface ICheckConstraintContainer {

	/**
	 * @return all check constraints of this table
	 */
	Set<ICheckConstraint> getCheckConstraints();

	/**
	 * Adds a check constraint to this table.
	 * 
	 * @param constraint check constraint to add
	 */
	void addCheckConstraint(ICheckConstraint constraint);

	/**
	 * Removes a check constraint from this table.
	 * 
	 * @param constraint check constraint to remove
	 */
	void removeCheckConstraint(ICheckConstraint constraint);

}
