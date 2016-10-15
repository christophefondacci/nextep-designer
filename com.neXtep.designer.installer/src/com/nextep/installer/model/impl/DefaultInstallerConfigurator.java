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

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IDelivery;
import com.nextep.installer.model.IInstallConfiguration;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.InstallerOption;

public class DefaultInstallerConfigurator implements IInstallConfigurator {

	private Map<InstallerOption, String> optionsMap;
	private Map<String, String> propertyMap;
	private Connection adminConnection;
	private Connection targetConnection;
	private String nextepHome;
	private IDelivery delivery;
	private IDatabaseTarget targetDatabase;
	private String deliveryPath;
	private boolean isAdminInTarget = false;

	public DefaultInstallerConfigurator() {
		optionsMap = new HashMap<InstallerOption, String>();
		propertyMap = new HashMap<String, String>();
	}

	/**
	 * Copies the specified configuration into this configurator, <u>except delivery
	 * information</u>.
	 * 
	 * @param c configuration to copy
	 */
	public DefaultInstallerConfigurator(IInstallConfiguration c) {
		this();
		setAdminConnection(c.getAdminConnection());
		setNextepHome(c.getNextepHome());
		final DefaultInstallerConfigurator dc = (DefaultInstallerConfigurator) c;
		optionsMap = new HashMap<InstallerOption, String>(dc.getInstallerOptionsMap());
		propertyMap = new HashMap<String, String>(dc.getPropertyMap());
		setTarget(c.getTarget());
		setTargetConnection(c.getTargetConnection());
		setAdminInTarget(c.isAdminInTarget());
	}

	public void defineOption(InstallerOption option) {
		setOption(option, null);
	}

	public void setAdminConnection(Connection admin) {
		this.adminConnection = admin;
	}

	public void setOption(InstallerOption option, String value) {
		optionsMap.put(option, value);
	}

	public void setTargetConnection(Connection target) {
		this.targetConnection = target;
	}

	public Connection getAdminConnection() {
		return adminConnection;
	}

	public String getOption(InstallerOption option) {
		return optionsMap.get(option);
	}

	public Connection getTargetConnection() {
		return targetConnection;
	}

	public boolean isOptionDefined(InstallerOption option) {
		return optionsMap.containsKey(option);
	}

	public String getProperty(String property) {
		return propertyMap.get(property);
	}

	public void setProperty(String property, String value) {
		propertyMap.put(property, value);
	}

	public String getNextepHome() {
		return nextepHome;
	}

	public void setNextepHome(String nextepHome) {
		this.nextepHome = nextepHome;
	}

	public IDelivery getDelivery() {
		return delivery;
	}

	public void setDelivery(IDelivery delivery) {
		this.delivery = delivery;
	}

	public IDatabaseTarget getTarget() {
		return targetDatabase;
	}

	public void setTarget(IDatabaseTarget targetDatabase) {
		this.targetDatabase = targetDatabase;
	}

	public String getDeliveryPath() {
		return deliveryPath;
	}

	public void setDeliveryPath(String deliveryPath) {
		this.deliveryPath = deliveryPath;
	}

	protected Map<InstallerOption, String> getInstallerOptionsMap() {
		return optionsMap;
	}

	protected Map<String, String> getPropertyMap() {
		return propertyMap;
	}

	public void setAdminInTarget(boolean adminIsTarget) {
		this.isAdminInTarget = adminIsTarget;
	}

	public boolean isAdminInTarget() {
		return isAdminInTarget;
	}

	public boolean isAnyOptionDefined(InstallerOption... options) {
		// If no option we return true if at least one option is defined, whatever the option
		if (options == null || options.length == 0) {
			return !optionsMap.isEmpty();
		}
		for (InstallerOption option : options) {
			if (isOptionDefined(option)) {
				return true;
			}
		}
		return false;
	};
}
