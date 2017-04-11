/**
 * Copyright (c) 2012 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.installer.model;

import java.sql.Connection;
import com.nextep.installer.exception.InstallerException;

/**
 * @author Bruno Gautier
 */
public interface IDatabaseConnector {

	Connection getConnection(IDatabaseTarget target) throws InstallerException;

	void doPostConnectionSettings(IDatabaseTarget target, Connection conn)
			throws InstallerException;

}
