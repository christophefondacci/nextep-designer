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
package com.nextep.designer.dbgm.oracle.factories;

import com.nextep.designer.dbgm.factories.UserTypeFactory;
import com.nextep.designer.dbgm.oracle.impl.OracleUserType;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.vcs.model.IVersionable;

public class OracleUserTypeFactory extends UserTypeFactory {

	@Override
	public IVersionable<?> createVersionable() {
		return new OracleUserType();
	}
	@Override
	public void rawCopy(IVersionable<?> source, IVersionable<?> destination) {
		super.rawCopy(source, destination);
		// Copying Oracle-specific information
		IOracleUserType src = (IOracleUserType)source.getVersionnedObject().getModel();
		IOracleUserType tgt = (IOracleUserType)destination.getVersionnedObject().getModel();
		tgt.setTypeBody(src.getTypeBody());
	}
}
