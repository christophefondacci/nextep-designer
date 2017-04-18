/**
 * Copyright (c) 2012 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.installer.model;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Bruno Gautier
 */
public interface IDatabaseConnector {

	Connection getConnection(IDatabaseTarget target) throws SQLException;

	void doPostConnectionSettings(IDatabaseTarget target, Connection conn)
			throws SQLException;

}
