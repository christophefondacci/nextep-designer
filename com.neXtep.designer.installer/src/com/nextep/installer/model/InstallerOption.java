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

import java.text.MessageFormat;
import com.nextep.installer.InstallerMessages;

/**
 * This enumeration defines all options which can be set when launching the neXtep installer.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public enum InstallerOption {

	USER("user", "username", InstallerMessages.getString("installer.option.username")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	PASSWORD("pass", "password", InstallerMessages.getString("installer.option.password")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	DATABASE("db", "database", InstallerMessages.getString("installer.option.database")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	SCHEMA("schema", "schema name to connect to (optional)"), //$NON-NLS-1$ //$NON-NLS-2$
	HOST("host", InstallerMessages.getString("installer.option.host")), //$NON-NLS-1$ //$NON-NLS-2$
	PORT("port", InstallerMessages.getString("installer.option.port")), //$NON-NLS-1$ //$NON-NLS-2$
	VENDOR("vendor", //$NON-NLS-1$
			"vendor of the target database for generic JDBC deliveries: ORACLE, MYSQL or POSTGRE"), //$NON-NLS-1$
	TNS("tns", "[Oracle] the explicit TNS name to use when different from database SID (optional)"), //$NON-NLS-1$ //$NON-NLS-2$
	INSTALL("install", InstallerMessages.getString("installer.option.install")), //$NON-NLS-1$ //$NON-NLS-2$
	FULL_INSTALL("repository", InstallerMessages.getString("installer.option.repository")), //$NON-NLS-1$ //$NON-NLS-2$
	NOCHECK("nocheck", InstallerMessages.getString("installer.option.nocheck")), //$NON-NLS-1$ //$NON-NLS-2$
	VERBOSE("verbose", InstallerMessages.getString("installer.option.verbose")), //$NON-NLS-1$ //$NON-NLS-2$
	INFO("info", InstallerMessages.getString("installer.option.info")), //$NON-NLS-1$ //$NON-NLS-2$
	HELP("help", InstallerMessages.getString("installer.option.help")); //$NON-NLS-1$ //$NON-NLS-2$

	private String name;
	private String valueName;
	private String description;
	private boolean mandatory;

	/**
	 * Builds an optional option.
	 * 
	 * @param name option's name
	 * @param description option's description
	 */
	InstallerOption(String name, String description) {
		this.name = name;
		this.description = description;
		this.mandatory = false;
	}

	/**
	 * Builds a mandatory option.
	 * 
	 * @param name option name
	 * @param valueName option's value short name (for documentation)
	 * @param description option description (for documentation)
	 */
	InstallerOption(String name, String valueName, String description) {
		this.name = name;
		this.valueName = valueName;
		this.description = description;
		this.mandatory = true;

	}

	/**
	 * Return the option name.
	 * 
	 * @return the option name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The option's value name. Used only for displaying documentation of this option.
	 * 
	 * @return a short name describing the expected value of this option. Will return
	 *         <code>null</code> when {@link InstallerOption#isMandatory()} is <code>false</code>
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
	 * Says whether the option expects a value or not. Mandatory options need to be passed as:<br>
	 * <code>-<i>option</i>=<i>value</i></code>
	 * 
	 * @return <code>true</code> when the option needs a value, else <code>false</code>
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	public static InstallerOption parse(String optionName) {
		for (InstallerOption option : values()) {
			if (option.getName().equalsIgnoreCase(optionName)) {
				return option;
			}
		}
		throw new IllegalArgumentException(MessageFormat.format(
				InstallerMessages.getString("installer.invalidOptionException"), optionName)); //$NON-NLS-1$
	}

}
