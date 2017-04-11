/**
 * Copyright (c) 2012 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.installer.handlers;

import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseConnector;
import com.nextep.installer.model.impl.JDBCDatabaseConnector;
import com.nextep.installer.model.impl.PostgreSQLDatabaseConnector;

/**
 * @author Bruno Gautier
 */
public final class DatabaseConnectorHandler {

	public static IDatabaseConnector getDatabaseConnector(DBVendor vendor) {
		switch (vendor) {
		case DB2:
			return new JDBCDatabaseConnector();
		case DERBY:
			return new JDBCDatabaseConnector();
		case JDBC:
			return new JDBCDatabaseConnector();
		case MSSQL:
			return new JDBCDatabaseConnector();
		case MYSQL:
			return new JDBCDatabaseConnector();
		case ORACLE:
			return new JDBCDatabaseConnector();
		case POSTGRE:
			return new PostgreSQLDatabaseConnector();
		default:
			return new JDBCDatabaseConnector();
		}
	}

}
