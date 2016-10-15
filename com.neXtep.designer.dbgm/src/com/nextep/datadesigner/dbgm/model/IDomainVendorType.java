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

import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;
import com.nextep.designer.core.model.DBVendor;

/**
 * A domain vendor type is the implementation of an abstract {@link IDomain} for a specific vendor.
 * It defines the real type implementation for a vendor.
 * 
 * @author Christophe Fondacci
 */
public interface IDomainVendorType extends IdentifiedObject, IObservable, ITypedObject {

	public static final String TYPE_ID = "VENDOR_TYPE"; //$NON-NLS-1$

	/**
	 * Defines the vendor for which this type is mapped to the domain.<br>
	 * There should be only one {@link IDomainVendorType} for a same {@link IDomain} and
	 * {@link DBVendor}.
	 * 
	 * @param vendor database vendor
	 */
	public void setDBVendor(DBVendor vendor);

	/**
	 * @return the {@link DBVendor} for which this type is mapped to the {@link IDomain}
	 */
	public DBVendor getDBVendor();

	/**
	 * Defines the parent domain this type implements.
	 * 
	 * @param domain parent {@link IDomain}
	 */
	public void setDomain(IDomain domain);

	/**
	 * @return the parent {@link IDomain} this type implements
	 */
	public IDomain getDomain();

	/**
	 * @return the vendor's datatype corresponding to the {@link IDomain}
	 */
	public IDatatype getDatatype();

	/**
	 * Defines the vendor's datatype corresponding to the {@link IDomain}
	 * 
	 * @param datatype vendor-specific data type
	 */
	public void setDatatype(IDatatype datatype);

}
