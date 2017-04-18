/**
 * Copyright (c) 2012 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.installer.model.impl;

import java.sql.Connection;
import java.sql.SQLException;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.base.AbstractJDBCDatabaseConnector;

/**
 * @author Bruno Gautier
 */
public class JDBCDatabaseConnector extends AbstractJDBCDatabaseConnector {

	public void doPostConnectionSettings(IDatabaseTarget target, Connection conn)
			throws SQLException {
		// Do nothing
	}

}
