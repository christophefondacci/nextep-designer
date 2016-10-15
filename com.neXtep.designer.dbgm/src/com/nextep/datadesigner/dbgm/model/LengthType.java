/*******************************************************************************
 * Copyright (c) 2012 neXtep Software and contributors.
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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.datadesigner.dbgm.model;

/**
 * @author Christophe Fondacci
 */
public enum LengthType {

	CHAR("C"), BYTE("B"), UNDEFINED("U"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

	private String dbCode;

	private LengthType(String dbCode) {
		this.dbCode = dbCode;
	}

	public String databaseCode() {
		return dbCode;
	}

	public static LengthType fromDatabaseCode(String dbCode) {
		for (LengthType type : LengthType.values()) {
			if (type.databaseCode().equals(dbCode)) {
				return type;
			}
		}
		return UNDEFINED;
	}
}
