/**
 * Copyright (c) 2012 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.installer.model.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.nextep.installer.helpers.ServicesHelper;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.base.AbstractJDBCDatabaseConnector;
import com.nextep.installer.services.ILoggingService;

/**
 * @author Bruno Gautier
 */
public class PostgreSQLDatabaseConnector extends AbstractJDBCDatabaseConnector {

	public void doPostConnectionSettings(IDatabaseTarget target, Connection conn)
			throws SQLException {
		final ILoggingService logger = ServicesHelper.getLoggingService();
		final String schema = target.getSchema();

		if (schema != null && !"".equals(schema.trim())) { //$NON-NLS-1$
			Statement stmt = null;
			try {
				stmt = conn.createStatement();
				stmt.execute("SET SEARCH_PATH TO " + schema + ",public"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (SQLException sqle) {
				logger.error("Unable to set the SEARCH_PATH variable: " + sqle.getMessage(), sqle);
				throw sqle;
			} finally {
				try {
					if (stmt != null)
						stmt.close();
				} catch (SQLException sqle) {
					logger.log("Unable to close statement: " + sqle.getMessage());
				}
			}
		}
	}

}
