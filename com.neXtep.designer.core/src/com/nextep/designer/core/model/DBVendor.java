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
package com.nextep.designer.core.model;

import com.nextep.datadesigner.model.IFormatter;

/**
 * Database supported vendors enumerator.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public enum DBVendor {
	/*
	 * FIXME [BGA]: For DB2, naming rules state that names must be in lowercase
	 * on UNIX platform, and can be mixed-case on WINDOWS platform. Another rule
	 * also state that when used in most names characters A through Z are
	 * converted from lowercase to uppercase. It is therefore difficult to
	 * choose a default name formatter. As object names seem to be stored in
	 * uppercase in the dictionary tables, we will choose the UPPERCASE
	 * formatter for now.
	 */
	/*
	 * TODO [CFO] Refactor formatters, a hack was made for Mysql to handle this
	 * exact same problematic which has the exact same need, handled by a
	 * NOFORMAT and MergStrategy hack for synchronization.
	 */
	ORACLE("Oracle", IFormatter.UPPERCASE, "sqlplus", 1521), //$NON-NLS-1$ //$NON-NLS-2$
	MYSQL("MySQL", IFormatter.NOFORMAT, "mysql", 3306), //$NON-NLS-1$ //$NON-NLS-2$
	POSTGRE("PostgreSQL", IFormatter.NOFORMAT, "psql", 5432), //$NON-NLS-1$ //$NON-NLS-2$
	DB2("DB2", IFormatter.UPPERCASE, "db2", 50000), //$NON-NLS-1$ //$NON-NLS-2$
	JDBC("Vendor-neutral", IFormatter.NOFORMAT, "", 0), //$NON-NLS-1$ //$NON-NLS-2$
	DERBY("Derby", IFormatter.NOFORMAT, "", 0), //$NON-NLS-1$ //$NON-NLS-2$
	MSSQL("Microsoft SQL Server", IFormatter.NOFORMAT, "", 1433);
	//	, SQLITE("SQLite", IFormatter.NOFORMAT, "", 0); //$NON-NLS-1$ //$NON-NLS-2$

	public static final DBVendor getDefaultVendor() {
		return ORACLE;
	}

	private String vendorName;
	private IFormatter nameFormatter;
	private String defaultExecutableName;
	private int defaultPort;

	DBVendor(String vendorName, IFormatter nameFormatter, String executableName, int defaultPort) {
		this.vendorName = vendorName;
		this.nameFormatter = nameFormatter;
		this.defaultExecutableName = executableName;
		this.defaultPort = defaultPort;
	}

	@Override
	public String toString() {
		return vendorName;
	}

	public IFormatter getNameFormatter() {
		return nameFormatter;
	}

	public String getDefaultExecutableName() {
		return defaultExecutableName;
	}

	public int getDefaultPort() {
		return defaultPort;
	}

	public boolean isInternal() {
		return this == DERBY;
	}

}
