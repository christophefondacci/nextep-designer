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
package com.nextep.designer.sqlgen.model;

/**
 * This bean holds raw-level information about an error fetched from the database. Depending on the
 * vendor features, all information may or may not be filled.
 * 
 * @author Christophe Fondacci
 */
public class ErrorInfo {

	private String name;
	private String typeName;
	private String message;
	private String attribute;
	private int line;
	private int col;

	public ErrorInfo(String objName, String objType, String msg, int line, int col, String attribute) {
		this.name = objName;
		this.typeName = objType;
		this.message = msg;
		this.line = line;
		this.col = col;
		this.attribute = attribute;
	}

	public String getObjectName() {
		return name;
	}

	public String getObjectTypeName() {
		return typeName;
	}

	public String getErrorMessage() {
		return message;
	}

	public int getLine() {
		return line;
	}

	public int getCol() {
		return col;
	}

	public String getAttribute() {
		return attribute;
	}

}
