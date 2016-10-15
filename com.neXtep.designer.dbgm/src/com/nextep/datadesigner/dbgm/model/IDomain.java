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

import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.model.IdentifiedObject;

/**
 * A domain defines an abstraction of a database type. This abstraction allows
 * to implement vendor-independent data-models which can rely on this abstract
 * type layer.<br>
 * This type can be mapped to different database vendors and the "real" vendor
 * specific type will be mapped immediately before deployment.
 * 
 * @author Christophe Fondacci
 */
public interface IDomain extends INamedObject, IdentifiedObject, ITypedObject, IObservable {

	String TYPE_ID = "DOMAIN"; //$NON-NLS-1$

	/**
	 * @return the collection of all vendor-specific implementation defined for
	 *         this domain.
	 */
	Collection<IDomainVendorType> getVendorTypes();

	/**
	 * Adds the specified vendor type implementation to this domain.
	 * Implementors should raise if an implementation already exists for the
	 * same vendor.
	 * 
	 * @param vendorType
	 *            new vendor-specific implementation of this domain
	 */
	void addVendorType(IDomainVendorType vendorType);

	/**
	 * Removes the specified vendor type implementation of this domain.
	 * 
	 * @param vendorType
	 *            vendor-specific implementation to remove from this domain
	 */
	void removeVendorType(IDomainVendorType vendorType);

	/**
	 * Provides the length expression for this domain. The expression should be
	 * evaluated against the actual datatype to generate and the datatype will
	 * be converted when it matches. Expressions include explicit number
	 * definition, wildcards (* for any) and absence (0).
	 * 
	 * @return the length expression
	 */
	String getLengthExpr();

	/**
	 * Provides the precision expression for this domain. The expression should
	 * be evaluated against the actual datatype to generate and the datatype
	 * will be converted when it matches. Expressions include explicit number
	 * definition, wildcards (* for any) and absence (0).
	 * 
	 * @return the precision expression
	 */
	String getPrecisionExpr();

	/**
	 * Defines the length expression of this domain
	 * 
	 * @see IDomain#getLengthExpr()
	 * 
	 * @param lengthExpr
	 *            expression to use when matching a datatype length
	 */
	void setLengthExpr(String lengthExpr);

	/**
	 * Defines the precision expression for this domain
	 * 
	 * @see IDomain#getPrecisionExpr()
	 * @param precisionExpr
	 *            expression to use when matching a datatype precision
	 */
	void setPrecisionExpr(String precisionExpr);
}
