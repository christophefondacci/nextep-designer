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
package com.nextep.datadesigner.dbgm.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.dbgm.model.IDomainVendorType;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;

/**
 * @see IDomain
 * @author Christophe Fondacci
 */
public class Domain extends IDNamedObservable implements IDomain {

	private Collection<IDomainVendorType> vendorTypes;
	private String lengthExpr = null;
	private String precisionExpr = null;

	public Domain() {
		vendorTypes = new HashSet<IDomainVendorType>();
		nameHelper.setFormatter(IFormatter.UPPERSTRICT);
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public void addVendorType(IDomainVendorType vendorType) {
		if (this.vendorTypes.add(vendorType)) {
			vendorType.setDomain(this);
			notifyListeners(ChangeEvent.GENERIC_CHILD_ADDED, vendorType);
		}
	}

	@Override
	public Collection<IDomainVendorType> getVendorTypes() {
		return vendorTypes;
	}

	@Override
	public void removeVendorType(IDomainVendorType vendorType) {
		if (this.vendorTypes.remove(vendorType)) {
			vendorType.setDomain(null);
			notifyListeners(ChangeEvent.GENERIC_CHILD_REMOVED, vendorType);
		}
	}

	/**
	 * Hibernate dedicated hidden setter of vendor types
	 * 
	 * @param vendorTypes
	 */
	protected void setVendorTypes(Set<IDomainVendorType> vendorTypes) {
		this.vendorTypes = vendorTypes;
	}

	@Override
	public void setLengthExpr(String lengthExpr) {
		if (this.lengthExpr != lengthExpr) {
			this.lengthExpr = lengthExpr;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public String getLengthExpr() {
		return lengthExpr;
	}

	@Override
	public void setPrecisionExpr(String precisionExpr) {
		if (this.precisionExpr != precisionExpr) {
			this.precisionExpr = precisionExpr;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public String getPrecisionExpr() {
		return precisionExpr;
	}
}
