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
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class JdbcUtils {

	public static String getPromptTag(DBVendor vendor) {
		switch (vendor) {
		case DB2:
			return "ECHO"; //$NON-NLS-1$
		case POSTGRE:
			return "\\echo"; //$NON-NLS-1$
		case MYSQL:
		case ORACLE:
		default:
			return "Prompt"; //$NON-NLS-1$
		}
	}

	public static String getScriptCallerTag(DBVendor vendor) {
		switch (vendor) {
		case DB2:
		case POSTGRE:
		case MYSQL:
			return "source"; //$NON-NLS-1$
		case ORACLE:
			return "@@"; //$NON-NLS-1$
		default:
			return null;
		}
	}

	public static String getStatementEndTag(DBVendor vendor) {
		switch (vendor) {
		case DB2:
		case POSTGRE:
		case MYSQL:
		case ORACLE:
			return "\r\n/\r\n"; //$NON-NLS-1$
		default:
			return ";"; //$NON-NLS-1$
		}
	}

	public static String getCommentStartSequence(DBVendor vendor) {
		switch (vendor) {
		case DB2:
		case POSTGRE:
		case MYSQL:
		case ORACLE:
		default:
			return "--"; //$NON-NLS-1$
		}
	}

}
