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

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class DatabaseTarget implements IDatabaseTarget {

	private final String user, password, database, schema, host, port;
	private final DBVendor vendor;
	private String tnsAlias;
	private boolean isSso = false;

	public DatabaseTarget(String user, String password, String database, String schema,
			String host, String port, DBVendor vendor, String serviceName) {
		this.user = user;
		this.password = password;
		this.database = database;
		this.schema = schema;
		this.host = host;
		this.port = port;
		this.vendor = vendor;

		// By default, TNS is the database id unless explicitly set
		this.tnsAlias = (serviceName != null && !"".equals(serviceName.trim()) ? serviceName //$NON-NLS-1$
				: database);

		/*
		 * [BGA] If user and password have been omitted, the connection to the target is considered
		 * to be based on a platform specific Single Sign On (SSO) mechanism. I think that for the
		 * moment there is no need to provide a specific command line argument for SSO connection.
		 */
		if (user == null && password == null) {
			this.isSso = true;
		}
	}

	public String getDatabase() {
		return database;
	}

	public String getSchema() {
		return schema;
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
		return (vendor != null ? vendor.buildConnectionURL(host, port, database, tnsAlias) : (user
				+ "@" + (tnsAlias != null && !"".equals(tnsAlias.trim()) ? tnsAlias : database) //$NON-NLS-1$ //$NON-NLS-2$
				+ ":" + port + " (" + host + ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	public String getTnsAlias() {
		return tnsAlias;
	}

	public void setTnsAlias(String tnsAlias) {
		this.tnsAlias = tnsAlias;
	}

	public boolean isSso() {
		return isSso;
	}

}
