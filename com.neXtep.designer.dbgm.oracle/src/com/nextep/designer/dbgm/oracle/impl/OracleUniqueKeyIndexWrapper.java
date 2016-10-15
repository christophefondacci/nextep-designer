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
package com.nextep.designer.dbgm.oracle.impl;

import com.nextep.datadesigner.dbgm.impl.UniqueKeyIndexWrapper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.IOracleIndex;

public class OracleUniqueKeyIndexWrapper extends UniqueKeyIndexWrapper implements IOracleIndex {

	private OracleUniqueConstraint uk;

	public OracleUniqueKeyIndexWrapper(OracleUniqueConstraint uk) {
		super(uk);
		this.uk = uk;
	}

	@Override
	public IPhysicalProperties getPhysicalProperties() {
		return uk.getPhysicalProperties();
	}

	@Override
	public void setPhysicalProperties(IPhysicalProperties properties) {
	}

	@Override
	public String getFunction(IReference r) {
		return null;
	}

	@Override
	public void setFunction(IReference r, String func) {
		// TODO Auto-generated method stub

	}

	@Override
	public IElementType getPhysicalPropertiesType() {
		return uk.getPhysicalPropertiesType();
	}
}
