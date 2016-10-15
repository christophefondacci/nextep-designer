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

import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.dbgm.model.IDomainVendorType;
import com.nextep.datadesigner.impl.IDNamedObservable;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.model.DBVendor;

/**
 * @see IDomainVendorType
 * @author Christophe Fondacci
 */
public class DomainVendorType extends IDNamedObservable implements
		IDomainVendorType {

	private DBVendor vendor;
	private IDomain domain;
	private IDatatype datatype;
	
	@Override
	public DBVendor getDBVendor() {
		return vendor;
	}

	@Override
	public IDatatype getDatatype() {
		return datatype;
	}

	@Override
	public IDomain getDomain() {
		return domain;
	}

	@Override
	public void setDBVendor(DBVendor vendor) {
		if(vendor != this.vendor) {
			this.vendor = vendor;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setDatatype(IDatatype datatype) {
		if(datatype!=this.datatype) {
			this.datatype = datatype;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public void setDomain(IDomain domain) {
		if(this.domain!=domain) {
			this.domain=domain;
			notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	@Override
	public IElementType getType() {
		return IElementType.getInstance(TYPE_ID);
	}

	@Override
	public String getName() {
		if(getDatatype() !=null) {
			return getDatatype().getName();
		}
		return null;
	}
}
