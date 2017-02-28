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
package com.nextep.installer.model;

import com.nextep.installer.handlers.JdbcDeployHandler;
import com.nextep.installer.handlers.MySQLDeployHandler;
import com.nextep.installer.handlers.OracleDeployHandler;
import com.nextep.installer.handlers.PostgreSqlDeployHandler;
import com.nextep.installer.impl.req.MSSQLRequirement;
import com.nextep.installer.impl.req.MySQLRequirement;
import com.nextep.installer.impl.req.PostgreSqlRequirement;
import com.nextep.installer.impl.req.SQLPlusRequirement;
import com.nextep.installer.model.base.AbstractVendorRequirement;

/**
 * DBVendor enumeration used by the installer.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public enum DBVendor {

	ORACLE(
			"Oracle", "orcl", 1521, new SQLPlusRequirement(), new OracleDeployHandler(), "oracle.jdbc.driver.OracleDriver"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	MYSQL(
			"MySQL", "mysql", 3306, new MySQLRequirement(), new MySQLDeployHandler(), "com.mysql.jdbc.Driver"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	POSTGRE(
			"PostgreSQL", "psql", 5432, new PostgreSqlRequirement(), new PostgreSqlDeployHandler(), "org.postgresql.Driver"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DERBY(
			"Derby", null, 1111, null, new JdbcDeployHandler(), "org.apache.derby.jdbc.EmbeddedDriver"), //$NON-NLS-1$ //$NON-NLS-2$
	JDBC("Generic JDBC", "jdbc", -1, null, new JdbcDeployHandler(), null), //$NON-NLS-1$ //$NON-NLS-2$
	DB2("DB2", "db2", 50000, null, new JdbcDeployHandler(), "com.ibm.db2.jcc.DB2Driver"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	MSSQL(
			"Microsoft SQL Server", "", 1433, new MSSQLRequirement(), new JdbcDeployHandler(), "net.sourceforge.jtds.jdbc.Driver"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	private String label;
	private int defaultPort;
	private String deliveryPrefix;
	private AbstractVendorRequirement requirement;
	private IDeployHandler deployHandler;
	private String driverClass;

	/**
	 * Vendor constructor
	 * 
	 * @param vendorName database vendor name
	 * @param requirement requirement to check for this vendor
	 */
	private DBVendor(String label, String deliveryPrefix, int defaultPort,
			AbstractVendorRequirement requirement, IDeployHandler handler, String driverClass) {
		this.label = label;
		this.deliveryPrefix = deliveryPrefix;
		this.requirement = requirement;
		this.deployHandler = handler;
		this.defaultPort = defaultPort;
		this.driverClass = driverClass;
	}

	/**
	 * @return the name of this database vendor.
	 */
	@Deprecated
	public String getName() {
		return name();
	}

	/**
	 * @return the label of this vendor (user-readable)
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the deploy handler which is able to deploy scripts
	 */
	public IDeployHandler getDeployHandler() {
		return deployHandler;
	}

	/**
	 * @return the requirement to check for this vendor. This requirement will be checked prior to
	 *         any deployment.
	 */
	public IRequirement getRequirement() {
		return requirement;
	}

	/**
	 * @return the driver class of this vendor
	 */
	public String getDriverClass() {
		if (driverClass == null) {
			throw new RuntimeException("Unsupported vendor: " + this.toString()); //$NON-NLS-1$
		} else {
			return driverClass;
		}
	}

	/**
	 * Builds the proper connection URL for this vendor.
	 * 
	 * @param server
	 * @param port
	 * @param database
	 * @param serviceName TNS alias name of the database. Only relevant for Oracle databases,
	 *        specify <code>null</code> for other databases.
	 * @return a JDBC connection string for the current database vendor
	 */
	public String buildConnectionURL(String server, String port, String database, String serviceName) {
		if (this == ORACLE) {
			StringBuilder sb = new StringBuilder("jdbc:oracle:thin:@"); //$NON-NLS-1$
			sb.append("//").append(server).append(":").append(port).append("/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sb.append((serviceName != null && !"".equals(serviceName.trim()) ? serviceName : database)); //$NON-NLS-1$
			return sb.toString();
		} else if (this == MYSQL) {
			return "jdbc:mysql://" + server + ":" + port + "/" + database //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ "?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true"; //$NON-NLS-1$
		} else if (this == POSTGRE) {
			return "jdbc:postgresql://" + server + ":" + port + "/" + database; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (this == DERBY) {
			return "jdbc:derby:" + database; //$NON-NLS-1$
		} else if (this == DB2) {
			return "jdbc:db2://" + server + ":" + port + "/" + database; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else if (this == MSSQL) {
			return "jdbc:jtds:sqlserver://" + server + ":" + port + "/" + database; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} else {
			throw new RuntimeException("Unsupported vendor: " + this.toString()); //$NON-NLS-1$
		}
	}

	/**
	 * @return the delivery prefix for this vendor
	 */
	public String getDeliveryPrefix() {
		return deliveryPrefix;
	}

	/**
	 * @return the default server port for this vendor
	 */
	public int getDefaultPort() {
		return defaultPort;
	}

}
