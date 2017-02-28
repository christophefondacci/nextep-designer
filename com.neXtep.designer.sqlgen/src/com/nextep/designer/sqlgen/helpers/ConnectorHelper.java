/**
 * Copyright (c) 2015 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.designer.sqlgen.helpers;

import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;

/**
 * @author Bruno Gautier
 */
public final class ConnectorHelper {

	/**
	 * Returns the password of the specified connection with reserved characters escaped.
	 * 
	 * @param conn a {@link IConnection}
	 * @return a <code>String</code> representing the escaped password of the specified connection
	 */
	public static String getEscapedPassword(IConnection conn) {
		final DBVendor vendor = conn.getDBVendor();
		String escapedPassword = conn.getPassword();

		switch (vendor) {
		case ORACLE:
			escapedPassword = "\"" + escapedPassword + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}

		return escapedPassword;
	}

}
