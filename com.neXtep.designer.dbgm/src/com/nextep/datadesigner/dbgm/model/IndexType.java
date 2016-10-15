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
import java.util.Collection;
import com.nextep.designer.core.model.DBVendor;

/**
 * An enumerated type which represents available index types.
 * 
 * @author Christophe Fondacci
 */
public enum IndexType {

	// For enums always add to end to get new index value, otherwise may corrupt data values stored
	// in previous model loads.
	NON_UNIQUE(DBVendor.ORACLE, DBVendor.JDBC, DBVendor.MYSQL, DBVendor.POSTGRE, DBVendor.DB2, DBVendor.MSSQL),
	UNIQUE(DBVendor.ORACLE, DBVendor.JDBC, DBVendor.MYSQL, DBVendor.POSTGRE, DBVendor.DB2, DBVendor.MSSQL),
	BITMAP(DBVendor.ORACLE),
	// MySQL Index types
	FULLTEXT(DBVendor.MYSQL, DBVendor.MSSQL),
	SPATIAL(DBVendor.MYSQL),
	HASH(DBVendor.MYSQL, DBVendor.POSTGRE),
	// MS Sql index type
	CLUSTER(DBVendor.MSSQL),
	// PostgreSQL index types
	GIST(DBVendor.POSTGRE),
	GIN(DBVendor.POSTGRE);

	private Collection<DBVendor> supportedVendors;

	IndexType(DBVendor... supportedVendors) {
		this.supportedVendors = Arrays.asList(supportedVendors);
	}

	public boolean isAvailableFor(DBVendor vendor) {
		return supportedVendors.contains(vendor);
	}

}
