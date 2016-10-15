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
package com.nextep.designer.dbgm.sql;

/**
 * This interface defines a DDL utility that can be used by to generate a DDL statement that will
 * create, drop or alter a sequence in database.
 * 
 * @author Bruno Gautier
 */
public interface ISequenceDialect extends IDDLDialect {

	/**
	 * Returns the vendor specific keyword for the START WITH clause of the statements used to
	 * create or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the START WITH clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getStartWithClause();

	/**
	 * Returns the vendor specific keyword for the RESTART WITH clause of the statements used to
	 * create or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the RESTART WITH clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getRestartWithClause();

	/**
	 * Returns the vendor specific keyword for the INCREMENT BY clause of the statements used to
	 * create or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the INCREMENT BY clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getIncrementByClause();

	/**
	 * Returns the vendor specific keyword for the MINVALUE clause of the statements used to create
	 * or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the MINVALUE clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getMinValueClause();

	/**
	 * Returns the vendor specific keyword for the NO MINVALUE clause of the statements used to
	 * create or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the NO MINVALUE clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getNoMinValueClause();

	/**
	 * Returns the vendor specific keyword for the MAXVALUE clause of the statements used to create
	 * or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the MAXVALUE clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getMaxValueClause();

	/**
	 * Returns the vendor specific keyword for the NO MAXVALUE clause of the statements used to
	 * create or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the NO MAXVALUE clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getNoMaxValueClause();

	/**
	 * Returns the vendor specific keyword for the CYCLE clause of the statements used to create or
	 * modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the CYCLE clause, <code>null</code>
	 *         if this clause is not supported by the vendor
	 */
	String getCycleClause();

	/**
	 * Returns the vendor specific keyword for the NO CYCLE clause of the statements used to create
	 * or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the NO CYCLE clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getNoCycleClause();

	/**
	 * Returns the vendor specific keyword for the CACHE clause of the statements used to create or
	 * modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the CACHE clause, <code>null</code>
	 *         if this clause is not supported by the vendor
	 */
	String getCacheClause();

	/**
	 * Returns the vendor specific keyword for the NO CACHE clause of the statements used to create
	 * or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the NO CACHE clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getNoCacheClause();

	/**
	 * Returns the vendor specific keyword for the ORDER clause of the statements used to create or
	 * modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the ORDER clause, <code>null</code>
	 *         if this clause is not supported by the vendor
	 */
	String getOrderClause();

	/**
	 * Returns the vendor specific keyword for the NO ORDER clause of the statements used to create
	 * or modify a sequence.
	 * 
	 * @return a <code>String</code> representing the keyword of the NO ORDER clause,
	 *         <code>null</code> if this clause is not supported by the vendor
	 */
	String getNoOrderClause();

}
