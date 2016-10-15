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
package com.nextep.installer.util;

import com.nextep.installer.model.DBVendor;

/**
 *
 * @author Christophe Fondacci
 */
public class JdbcUtils {

	public static String getPromptTag(DBVendor vendor) {
		switch(vendor) {
		case DB2:
			return "ECHO";
		case POSTGRE:
			return "\\echo";
		case MYSQL:
		case ORACLE:
		default:
			return "Prompt";
		}
	}
	public static String getScriptCallerTag(DBVendor vendor) {
		if(vendor==DBVendor.ORACLE) {
			return "@@";
		} else if(vendor==DBVendor.MYSQL) {
			return "source";
		} else {
			return null;
		}
	}
	public static String getStatementEndTag(DBVendor vendor) {
		if(vendor == DBVendor.ORACLE) {
			return "\r\n/\r\n";
		}
		return ";";
	}
}
