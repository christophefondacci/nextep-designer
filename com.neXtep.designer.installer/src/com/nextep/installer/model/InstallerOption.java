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

/**
 * This enumeration defines all options which can be set when launching the neXtep installer.
 * 
 * @author Christophe Fondacci
 */
public enum InstallerOption {

	USER("user", "username", "user name to log in the database server"),
	PASSWORD("pass", "password", "password to log in the database server"),
	DATABASE("db", "database", "database identifier to connect to"),
	HOST("host", "hostname", "host name or ip address of the database server (default is localhost)"),
	PORT("port", "port", "the port on which the database server is listening to (default is vendor default)"),
	VENDOR("vendor", "vendor", "vendor of the target database for generic JDBC deliveries: ORACLE, MYSQL or POSTGRE"),
	TNS("tns", "tns_name", "[Oracle] the explicit TNS name to use when different from database SID"),
	INSTALL("install", "deploys the admin schema on the target database"),
	FULL_INSTALL("repository", "indicates that the delivery is a neXtep IDE repository delivery"),
	NOCHECK("nocheck", "will not perform a structure check before allowing deployment (use carefully)"),
	VERBOSE("verbose", "causes the installer to log stack traces of any error"),
	INFO("info", "displays installed modules and their consistency on the target database"),
	HELP("help", "displays this help information");

	private String name;
	private String valueName;
	private String description;
	private boolean valued;

	/**
	 * Builds a non-valued option.
	 * 
	 * @param name option's name
	 * @param description option's description
	 */
	InstallerOption(String name, String description) {
		this.name = name;
		this.description = description;
		valued = false;
	}

	/**
	 * Builds a valued option
	 * 
	 * @param name option's name
	 * @param valueName option's value short name (for documentation)
	 * @param description option's description (for documentation)
	 */
	InstallerOption(String name, String valueName, String description) {
		this.name = name;
		this.valueName = valueName;
		this.description = description;
		this.valued = true;

	}

	/**
	 * The option's name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * The option's value name. Used only for displaying documentation of this option.
	 * 
	 * @return a short name describing the expected value of this option. Will return
	 *         <code>null</code> when {@link InstallerOption#isValued()} is <code>false</code>
	 */
	public String getValueName() {
		return valueName;
	}

	/**
	 * The description of the option. Used only for displaying documentation of this option.
	 * 
	 * @return the description of the option
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Says whether the option expects a value or not. Valued options need to be passed as :<br>
	 * <code>-<i>option</i>=<i>value</i></code>
	 * 
	 * @return <code>true</code> when the option needs a value, else <code>false</code>
	 */
	public boolean isValued() {
		return valued;
	}

	public static InstallerOption parse(String optionName) {
		for (InstallerOption option : values()) {
			if (option.getName().equalsIgnoreCase(optionName)) {
				return option;
			}
		}
		throw new IllegalArgumentException("Option '" + optionName + "' is not valid");
	}
}
