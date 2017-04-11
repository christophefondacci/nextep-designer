/**
 * Copyright (c) 2012 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.installer.services.impl;

import java.sql.Connection;
import com.nextep.installer.NextepInstaller;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.handlers.DatabaseConnectorHandler;
import com.nextep.installer.model.DBVendor;
import com.nextep.installer.model.IDatabaseConnector;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.services.IConnectionService;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Bruno Gautier
 */
public class ConnectionService implements IConnectionService {

	public Connection connect(IDatabaseTarget target) throws InstallerException {
		final DBVendor vendor = target.getVendor();
		IDatabaseConnector connector = DatabaseConnectorHandler.getDatabaseConnector(vendor);
		Connection conn = connector.getConnection(target);
		connector.doPostConnectionSettings(target, conn);
		return conn;
	}

	protected ILoggingService getLoggingService() {
		return NextepInstaller.getService(ILoggingService.class);
	}

	/**
	 * Service injection setter, used when the installer is invoked from neXtep designer IDE through
	 * DS injection. This setter registers the service globally on the {@link NextepInstaller}
	 * static bean for compatibility with standalone mode.
	 * 
	 * @param service logging service implementation
	 */
	public void setLoggingService(ILoggingService service) {
		NextepInstaller.registerService(ILoggingService.class, service);
	}

}
