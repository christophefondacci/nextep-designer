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

import java.util.Arrays;
import java.util.List;
import com.nextep.designer.core.model.DBVendor;

public enum LanguageType {

	STANDARD("Native database language",DBVendor.ORACLE, DBVendor.MYSQL), JAVA("Java",DBVendor.ORACLE);
	
	private List<DBVendor> supportedVendors;
	private String label;
	LanguageType(String label, DBVendor... supportedVendors) {
		this.supportedVendors = Arrays.asList(supportedVendors);
		this.label=label;
	}
	public boolean isSupported(DBVendor vendor) {
		return supportedVendors.contains(vendor);
	}
	public String getLabel() {
		return label;
	}
}
