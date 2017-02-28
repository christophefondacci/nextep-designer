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
package com.nextep.installer.factories;

import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseObjectCheck;
import com.nextep.installer.model.IDatabaseStructure;
import com.nextep.installer.model.IDatabaseStructureBuilder;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IRelease;
import com.nextep.installer.model.impl.DatabaseObjectCheck;
import com.nextep.installer.model.impl.DatabaseTarget;
import com.nextep.installer.model.impl.DefaultInstallerConfigurator;
import com.nextep.installer.model.impl.JdbcStructureBuilder;
import com.nextep.installer.model.impl.MySQLStructureBuilder;
import com.nextep.installer.model.impl.OracleStructureBuilder;
import com.nextep.installer.model.impl.PostgreSqlStructureBuilder;
import com.nextep.installer.model.impl.Release;

/**
 * A simple factory for building any element of the installer.
 * 
 * @author Christophe Fondacci
 */
public final class InstallerFactory {

	private InstallerFactory() {
	}

	/**
	 * Builds a database object checker which can compare database objects with an expected object
	 * set for the specified database vendor.
	 * 
	 * @param v the vendor to get the database object checker for
	 * @return the appropriate {@link IDatabaseObjectCheck} implementation
	 */
	public static IDatabaseObjectCheck buildDatabaseObjectCheckerFor(DBVendor v) {
		return new DatabaseObjectCheck();
	}

	/**
	 * Returns the object which is able to build a {@link IDatabaseStructure} from a database
	 * connection.
	 * 
	 * @param v the vendor for which the builder should be returned
	 * @return the corresponding {@link IDatabaseStructureBuilder}
	 */
	public static IDatabaseStructureBuilder getDatabaseStructureBuilder(DBVendor v) {
		if (v == DBVendor.ORACLE) {
			return new OracleStructureBuilder();
		} else if (v == DBVendor.MYSQL) {
			return new MySQLStructureBuilder();
		} else if (v == DBVendor.POSTGRE) {
			return new PostgreSqlStructureBuilder();
		} else {
			return new JdbcStructureBuilder();
		}
	}

	/**
	 * Builds a new release with the provided version information.
	 * 
	 * @param major major number of the version
	 * @param minor minor number of the version
	 * @param iteration iteration number of the version
	 * @param patch patch number of the version
	 * @param revision revision number of the version
	 * @return an {@link IRelease} implementation for the specified version number
	 */
	public static IRelease buildRelease(int major, int minor, int iteration, int patch, int revision) {
		return new Release(major, minor, iteration, patch, revision);
	}

	/**
	 * Creates a new empty {@link IInstallConfigurator} to use with the installer.
	 * 
	 * @return an {@link IInstallConfigurator} implementation
	 */
	public static IInstallConfigurator createConfigurator() {
		return new DefaultInstallerConfigurator();
	}

	/**
	 * Creates a new database target from the provided information.
	 * 
	 * @param user
	 * @param password
	 * @param database
	 * @param host
	 * @param port
	 * @param vendor
	 * @param serviceName TNS alias name of the database. Only relevant for Oracle databases,
	 *        specify <code>null</code> for other databases.
	 * @return a {@link IDatabaseTarget} filled with provided information
	 */
	public static IDatabaseTarget createTarget(String user, String password, String database,
			String host, String port, DBVendor vendor, String serviceName) {
		return new DatabaseTarget(user, password, database, host, port, vendor, serviceName);
	}

}
