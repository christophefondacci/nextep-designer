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

import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.model.IParentable;

/**
 * This interface represents a database column
 * 
 * @author Christophe Fondacci
 */
public interface IBasicColumn extends IDatabaseObject<IBasicColumn>, IdentifiedObject,
		Comparable<Object>, IReferenceable, IParentable<IColumnable> {

	String TYPE_ID = "COLUMN"; //$NON-NLS-1$

	/**
	 * @return the datatype of this column
	 */
	IDatatype getDatatype();

	/**
	 * Defines the datatype of this column
	 * 
	 * @param datatype
	 *            new column datatype
	 */
	void setDatatype(IDatatype datatype);

	/**
	 * @return the column rank in table for column ordering
	 */
	int getRank();

	/**
	 * Defines the rank of this column in table
	 * 
	 * @param rank
	 *            new rank in table
	 */
	void setRank(int rank);

	/**
	 * @return the "NOT NULL" flag of this column
	 */
	boolean isNotNull();

	/**
	 * Sets the "NOT NULL" flag of this column.
	 * 
	 * @param notNull
	 *            <code>true</code> if column is not null, else
	 *            <code>false</code>
	 */
	void setNotNull(boolean notNull);

	/**
	 * @return the default expression of this column
	 */
	String getDefaultExpr();

	/**
	 * Sets the default expression for this column
	 * 
	 * @param expr
	 *            default expression
	 */
	void setDefaultExpr(String expr);

	/**
	 * Is this column a virtual column?
	 * 
	 * @return <code>true</code> if column is virtual or <code>false</code> if
	 *         not or unsupported
	 */
	boolean isVirtual();

	/**
	 * Changes the virtual status of this column
	 * 
	 * @param virtual
	 *            the new virtual status (it may have no effect if db engine
	 *            does not support this)
	 */
	void setVirtual(boolean virtual);

	/**
	 * Copies the current column to a brand new instance. Since columns are used
	 * in many places, and there could be several implementation, we have
	 * decided to let the column implement a <code>copy</code> feature itself.
	 * 
	 * @return a new column instance identical to this one
	 */
	IBasicColumn copy();

}
