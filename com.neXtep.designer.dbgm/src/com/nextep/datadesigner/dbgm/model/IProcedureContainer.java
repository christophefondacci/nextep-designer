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

import java.util.Collection;

/**
 * A common interface for elements which can contain procedures.
 * 
 * @author Christophe
 *
 */
public interface IProcedureContainer {
	/**
	 * A convenience method which returns the procedure
	 * List from the parsing of the package source code.
	 *
	 * @return a Collection of all defined procedures.
	 */
	public abstract Collection<IProcedure> getProcedures();
	/**
	 * Adds a procedure to this container
	 * @param procedure procedure to add
	 */
	public abstract void addProcedure(IProcedure procedure);
	/**
	 * Clears all procedures
	 */
	public abstract void clearProcedures();
	/**
	 * Defines all procedures of this container
	 * @param procedures a collection of contained procedures
	 */
	public abstract void setProcedures(Collection<IProcedure> procedures);
}
