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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.headless.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.app.IApplicationContext;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.ITypedObjectFactory;
import com.nextep.designer.headless.HeadlessMessages;
import com.nextep.designer.headless.exceptions.BatchException;
import com.nextep.designer.headless.model.HeadlessConstants;

/**
 * This class provides helper methods common to headless application needs.
 * 
 * @author Christophe Fondacci
 */
public final class HeadlessHelper {

	private static final Log LOGGER = LogFactory.getLog(HeadlessHelper.class);

	private HeadlessHelper() {
	}

	/**
	 * This helper method extracts command line arguments from the {@link IApplicationContext}
	 * element and generates an argument map. This method expects arguments to be in the following
	 * format :<br>
	 * <code>-<i>option</i>[=<i>value</i>]</code>
	 * 
	 * @param context the {@link IApplicationContext} of the headless application
	 * @return an argument map where the key is the option and value is the option value
	 */
	public static Map<String, String> processArgs(IApplicationContext context)
			throws BatchException {
		final Map<?, ?> rawArgsMap = context.getArguments();
		final String[] args = (String[]) rawArgsMap.get(IApplicationContext.APPLICATION_ARGS);
		final Map<String, String> argsMap = new HashMap<String, String>();
		for (String arg : args) {
			String[] splitArg = arg.split("="); //$NON-NLS-1$
			if (splitArg.length > 0) {
				// Accepting arguments starting with a - or not
				String argKey = splitArg[0];
				if (argKey.startsWith("-")) { //$NON-NLS-1$
					argKey = argKey.substring(1);
				}
				// Extracting argument value
				String argValue = null;
				if (splitArg.length > 1) {
					argValue = splitArg[1];
				}
				argsMap.put(argKey, splitArg.length > 1 ? splitArg[1] : null);
				// Processing config file
				if (HeadlessConstants.CONFIG_FILE_ARG.equalsIgnoreCase(argKey)) {
					final Map<String, String> configProps = loadConfigFile(argValue);
					argsMap.putAll(configProps);
				}
			} else {
				LOGGER.warn(MessageFormat.format(
						HeadlessMessages.getString("application.invalidArgumentWarning"), arg)); //$NON-NLS-1$
			}
		}
		return argsMap;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Map<String, String> loadConfigFile(String location) throws BatchException {
		File configFile = new File(location);
		if (!configFile.exists()) {
			throw new BatchException(MessageFormat.format(
					HeadlessMessages.getString("helper.configNotFound"), location)); //$NON-NLS-1$
		} else {
			InputStream is = null;
			try {
				is = new FileInputStream(configFile);
				final Properties props = new Properties();
				props.load(is);
				return (Map) props;
			} catch (IOException e) {
				throw new BatchException(MessageFormat.format(HeadlessMessages
						.getString("helper.configReadError"), location, e.getMessage()), e); //$NON-NLS-1$
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						throw new BatchException(MessageFormat.format(
								HeadlessMessages.getString("helper.configCloseError"), //$NON-NLS-1$
								location, e.getMessage()), e);
					}
				}
			}
		}
	}

	/**
	 * Builds the repository {@link IConnection} from command line arguments.
	 * 
	 * @param argsMap the command line arguments map
	 * @return the repository {@link IConnection} or <code>null</code> if none provided
	 */
	public static IConnection getConnection(String context, Map<String, String> argsMap) {
		// Retrieving arguments from our map
		final String user = argsMap.get(getContextualArgument(context,
				HeadlessConstants.DB_USER_ARG));
		final String password = argsMap.get(getContextualArgument(context,
				HeadlessConstants.DB_PASSWORD_ARG));
		final String database = argsMap.get(getContextualArgument(context,
				HeadlessConstants.DB_DATABASE_ARG));
		final String port = argsMap.get(getContextualArgument(context,
				HeadlessConstants.DB_PORT_ARG));
		final String server = argsMap.get(getContextualArgument(context,
				HeadlessConstants.DB_SERVER_ARG));
		final String vendorStr = argsMap.get(getContextualArgument(context,
				HeadlessConstants.DB_VENDOR_ARG));
		return buildConnection(user, password, database, port, server, vendorStr);
	}

	private static String getContextualArgument(String context, String argument) {
		return context + "." + argument; //$NON-NLS-1$
	}

	private static IConnection buildConnection(String user, String password, String database,
			String port, String server, String vendorStr) {
		// Processing database vendor validation
		DBVendor vendor = null;
		try {
			vendor = DBVendor.valueOf(vendorStr);
		} catch (IllegalArgumentException e) {
			LOGGER.error(MessageFormat.format(
					HeadlessMessages.getString("application.invalidVendorError"), vendorStr)); //$NON-NLS-1$
			return null;
		} catch (NullPointerException e) {
			LOGGER.error(HeadlessMessages.getString("application.noVendorError")); //$NON-NLS-1$
			return null;
		}
		// Initializing database connection from provided command line information
		final ITypedObjectFactory objectFactory = CorePlugin.getTypedObjectFactory();
		final IConnection conn = objectFactory.create(IConnection.class);
		conn.setLogin(user);
		conn.setPassword(password);
		conn.setDBVendor(vendor);
		conn.setDatabase(database);
		conn.setServerPort(port == null ? String.valueOf(vendor.getDefaultPort()) : port);
		conn.setPasswordSaved(true);
		conn.setServerIP(server == null ? "127.0.0.1" : server); //$NON-NLS-1$
		return conn;
	}
}
