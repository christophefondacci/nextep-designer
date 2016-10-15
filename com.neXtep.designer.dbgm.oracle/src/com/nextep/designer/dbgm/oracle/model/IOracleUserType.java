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
package com.nextep.designer.dbgm.oracle.model;

import com.nextep.datadesigner.dbgm.model.IParseable;
import com.nextep.datadesigner.dbgm.model.IProcedureContainer;
import com.nextep.datadesigner.dbgm.model.IUserType;

/**
 * Oracle user type extension to support type body.
 * 
 * @author Christophe
 *
 */
public interface IOracleUserType extends IUserType, IParseable, IProcedureContainer {

	/**
	 * Defines the body source code of this Oracle user type.
	 * 
	 * @param bodySQL new type body source code
	 */
	public abstract void setTypeBody(String bodySQL);
	/**
	 * @return the SQL source code of the type body
	 */
	public abstract String getTypeBody();
	
}
