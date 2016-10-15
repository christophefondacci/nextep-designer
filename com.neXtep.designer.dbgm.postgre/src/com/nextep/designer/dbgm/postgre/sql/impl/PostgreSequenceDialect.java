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
package com.nextep.designer.dbgm.postgre.sql.impl;

import com.nextep.designer.dbgm.sql.impl.SequenceDialect;

/**
 * @author Bruno Gautier
 */
public final class PostgreSequenceDialect extends SequenceDialect {

	@Override
	public String getNoCacheClause() {
		return null;
	}

	@Override
	public String getOrderClause() {
		return null;
	}

	@Override
	public String getNoOrderClause() {
		return null;
	}

}
