/**
 * Copyright (c) 2013 neXtep Softwares.
 * All rights reserved. Terms of the neXtep license
 * are available at http://www.nextep-softwares.com
 */
package com.nextep.installer.impl.req;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import com.nextep.installer.exception.InstallerException;
import com.nextep.installer.helpers.ExternalProgramHelper;
import com.nextep.installer.model.IDatabaseTarget;
import com.nextep.installer.model.IInstallConfigurator;
import com.nextep.installer.model.IStatus;
import com.nextep.installer.model.base.AbstractVendorRequirement;
import com.nextep.installer.model.impl.Status;

/**
 * This requirement tries to connect to the MSSQL database with the information provided by the
 * command line arguments.
 * 
 * @author Bruno Gautier
 */
public final class MSSQLRequirement extends AbstractVendorRequirement {

	private static final String SQLCMD_BIN_NAME = "sqlcmd"; //$NON-NLS-1$

	public IStatus checkRequirement(IInstallConfigurator configurator) throws InstallerException {
		final IDatabaseTarget target = configurator.getTarget();
		final String login = target.getUser();
		final String password = target.getPassword();
		final String host = target.getHost();
		final String server = ((host != null && host.trim().length() > 0) ? host : "127.0.0.1"); //$NON-NLS-1$
		final String port = target.getPort();
		final String database = target.getDatabase();

		// Locating sqlcmd binary
		final String sqlcmdBinary = ExternalProgramHelper.getProgramLocation(configurator,
				SQLCMD_BIN_NAME);

		boolean passed = true;
		try {
			/*
			 * We try to connect to the database with the information provided by the command line
			 * arguments.
			 */
			List<String> connectCommandLine = new ArrayList<String>(5);
			connectCommandLine.add(sqlcmdBinary);
			if (login != null && login.trim().length() > 0) {
				connectCommandLine.add("-U" + login); //$NON-NLS-1$
			}
			if (password != null && password.trim().length() > 0) {
				connectCommandLine.add("-P" + password); //$NON-NLS-1$
			}
			String sqlcmdServer = server
					+ ((port != null && port.trim().length() > 0) ? "," + port : ""); //$NON-NLS-1$ //$NON-NLS-2$
			connectCommandLine.add("-S" + sqlcmdServer); //$NON-NLS-1$
			if (database != null && database.trim().length() > 0) {
				connectCommandLine.add("-d" + database); //$NON-NLS-1$
			}
			ProcessBuilder pb = new ProcessBuilder(connectCommandLine.toArray(new String[0]));
			pb.redirectErrorStream(true);
			Process process = pb.start();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					process.getOutputStream()));
			writer.write("QUIT"); //$NON-NLS-1$
			writer.newLine();
			writer.flush();
			passed = isOK(SQLCMD_BIN_NAME, process);
			writer.close();
		} catch (IOException e) {
			throw new InstallerException("I/O Exception occurred.", e);
		}

		// Returning our passed flag
		return new Status(passed);
	}

	public String getName() {
		return "sqlcmd native login"; //$NON-NLS-1$
	}

}
