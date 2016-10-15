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
package com.nextep.datadesigner.dbgm.ctrl;

import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.DomainVendorType;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.dbgm.model.IDomainVendorType;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.INavigatorConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.ui.model.base.AbstractUIController;

/**
 * 
 * @author Christophe Fondacci
 */
public class DomainVendorTypeController extends AbstractUIController {

	public DomainVendorTypeController() {
		addSaveEvent(ChangeEvent.MODEL_CHANGED);
	}

	@Override
	public IDisplayConnector initializeEditor(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDisplayConnector initializeGraphical(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public INavigatorConnector initializeNavigator(Object model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDisplayConnector initializeProperty(Object content) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object newInstance(Object parent) {
		IDomain d = (IDomain) parent;
		IDomainVendorType vendorType = new DomainVendorType();
		vendorType.setDomain(d);
		vendorType.setDBVendor(DBVendor.getDefaultVendor());
		IDatatype datatype = new Datatype("VARCHAR");
		datatype.setLength(null);
		datatype.setPrecision(null);
		vendorType.setDatatype(datatype);
		d.addVendorType(vendorType);
		return vendorType;
	}

}
