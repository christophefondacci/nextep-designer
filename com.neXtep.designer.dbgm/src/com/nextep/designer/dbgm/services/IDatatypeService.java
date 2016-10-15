/*******************************************************************************
 * Copyright (c) 2013 neXtep Software and contributors.
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
 * along with neXtep.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.services;

import java.util.Collection;

import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.designer.core.model.DBVendor;

/**
 * This service exposes features that can manage datatypes such as mapping a
 * datatype from one vendor to another, generating the SQL of a datatype, etc.
 * 
 * @author Christophe Fondacci
 * 
 */
public interface IDatatypeService {

	/**
	 * Lists all known domains (datatype vendor mappings)
	 * 
	 * @return a list of all known domains
	 */
	Collection<IDomain> getDomains();

	/**
	 * Adds a global datatype mapping
	 * 
	 * @param domain
	 *            the {@link IDomain} to add
	 */
	void addDomain(IDomain domain);

	/**
	 * Removes the global datatype mapping
	 * 
	 * @param domain
	 *            the {@link IDomain} to remove
	 */
	void removeDomain(IDomain domain);

	/**
	 * Resets all information so we make sure we are up to date (used when we
	 * loose repository connection)
	 */
	void reset();

	/**
	 * A helper method to retrieve the concrete vendor data type mapped with an
	 * abstract data type if available.
	 * 
	 * @param type
	 *            the {@link IDatatype} for which we must search for a mapped
	 *            concrete vendor data type
	 * @return a {@link IDatatype} representing the concrete vendor data type if
	 *         available, the specified data type otherwise
	 */
	IDatatype getDatatype(DBVendor generationVendor, IDatatype type);

	/**
	 * Retrieves the datatype provider for the specified database vendor.
	 * 
	 * @param vendor
	 *            database vendor
	 * @return the datatype provider instance
	 */
	IDatatypeProvider getDatatypeProvider(DBVendor vendor);

	/**
	 * Generates the string representation of this datatype for the given vendor
	 * 
	 * @param d
	 *            the {@link IDatatype} to process
	 * @param vendor
	 *            the {@link DBVendor} for which we need a label
	 * @return the "generable" datatype string representation that could be put
	 *         in SQL statements
	 */
	String getDatatypeLabel(IDatatype d, DBVendor vendor);

	/**
	 * Generates the string representation of this datatype
	 * 
	 * @param d
	 *            the {@link IDatatype} to process
	 * @return the "generable" datatype string representation that could be put
	 *         in SQL statements
	 */
	String getDatatypeLabel(IDatatype d);
}
