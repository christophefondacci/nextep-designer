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
package com.nextep.installer.test.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.impl.DatabaseTarget;

public final class TestConfiguration {

	private static boolean isLoaded = false;
	private static Properties properties;
	private final static String PROP_USER = ".target.user";
	private final static String PROP_PASSWORD = ".target.password";
	private final static String PROP_DB = ".target.db";
	private final static String PROP_VENDOR = ".target.vendor";
	private final static String PROP_HOST = ".target.host";
	private final static String PROP_PORT = ".target.port";
	public final static String PROP_ROOT_PASSWORD = ".target.rootpassword";
	public final static String PROP_DELIVERY_PATH = "delivery.location";
	public final static String PROP_NXTP_HOME = "nextep.home";

	private TestConfiguration() {
	}

	private static void configure() {
		if (!isLoaded) {
			InputStream is = null;
			try {
				is = TestConfiguration.class.getClassLoader().getResourceAsStream(
						"com/nextep/installer/test/configuration/testConfig.properties");
				properties = new Properties();
				properties.load(is);
			} catch (IOException e) {
				throw new RuntimeException("Unable to read test properties");
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

	public static String getProperty(String propertyKey) {
		configure();
		return properties.getProperty(propertyKey);
	}

	public static IDatabaseTarget getTestTarget(DBVendor vendor) {
		String prefix = vendor.getName().toLowerCase();
		final String user = getProperty(prefix + PROP_USER);
		final String pass = getProperty(prefix + PROP_PASSWORD);
		final String db = getProperty(prefix + PROP_DB);
		final String host = getProperty(prefix + PROP_HOST);
		final String port = getProperty(prefix + PROP_PORT);
		return new DatabaseTarget(user, pass, db, host, port, vendor, null);
	}

}
