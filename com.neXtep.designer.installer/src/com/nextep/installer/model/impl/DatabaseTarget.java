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
package com.nextep.installer.model.impl;

import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseTarget;

public class DatabaseTarget implements IDatabaseTarget {

	private String user, password, db, host, port;
	private DBVendor vendor;
	private String tnsAlias;

	public DatabaseTarget(String user, String password, String db, String host, String port,
			DBVendor vendor) {
		this.user = user;
		this.password = password;
		this.db = db;
		this.host = host;
		this.port = port;
		this.vendor = vendor;
		// By default, TNS is the database id unless explicitly set
		this.tnsAlias = db;
	}

	public String getDatabase() {
		return db;
	}

	public String getHost() {
		return host;
	}

	public String getPassword() {
		return password;
	}

	public String getPort() {
		return port;
	}

	public String getUser() {
		return user;
	}

	public DBVendor getVendor() {
		return vendor;
	}

	@Override
	public String toString() {
		return vendor != null ? vendor.buildConnectionURL(host, port, db) : (vendor + ":" + user
				+ "@" + db + ":" + port + " (" + host + ")");
	}

	public String getTnsAlias() {
		return tnsAlias;
	}

	public void setTnsAlias(String tnsAlias) {
		this.tnsAlias = tnsAlias;
	}
}
