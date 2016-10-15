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
package com.nextep.datadesigner.sqlgen.impl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.sqlgen.model.IGenerationConsole;
import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.Status;
import com.nextep.datadesigner.sqlgen.services.BuildResult;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.services.IMarkerService;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.model.IGenerationResult;

/**
 * A base class for SQL submit processing.<br>
 * SQL-Submitters are called when user submits a SQL script to a database
 * connection. They process the SQL script to send it to a database connection.
 * Depending on the vendor, it might be performed through an external client
 * program in charge of sending the SQL.
 * 
 * @author Christophe Fondacci
 */
public abstract class AbstractSQLSubmitter implements IGenerationSubmitter {

	private IGenerationConsole console;
	private IGenerationResult genResult;
	private volatile boolean isRunning = false;

	@Override
	public IGenerationConsole getConsole() {
		return console;
	}

	@Override
	public void setConsole(IGenerationConsole console) {
		this.console = console;
	}

	@Override
	public BuildResult submit(final IProgressMonitor monitor, ISQLScript script, IConnection conn) {
		// Checking connection
		if (conn == null) {
			throw new ErrorException(SQLGenMessages.getString("noDefaultTarget")); //$NON-NLS-1$
		}

		getConsole().log(
				"Generating \"" + getScriptDisplayName(script) + "\" on " + conn.getName()
						+ " database...");
		// Initializing the build result
		BuildResult result = new BuildResult(new Date(), script, script.getDirectory() + "/" //$NON-NLS-1$
				+ script.getName() + ".log"); //$NON-NLS-1$
		result.setStatus(Status.OK); // Default is ok, until an error is
										// detected
		result.buildContents(genResult);
		// Starting
		isRunning = true;
		// The control thread allows proper cancellation of the child process
		// from user action. Since this UI thread would wait on the readLine()
		// method.
		Thread controlThread = new Thread() {

			@Override
			public void run() {
				while (isRunning()) {
					if (monitor.isCanceled()) {
						abort();
						getConsole().log(SQLGenMessages.getString("submitionStopped"));
						return;
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		};
		controlThread.start();
		try {
			if (doSubmit(monitor, script, conn)) {
				result.setStatus(Status.OK);
			} else {
				result.setStatus(Status.KO);
			}
		} catch (Exception err) {
			getConsole().log("Error during execution: " + err.getMessage());
			result.setStatus(Status.KO);
		} finally {
			stopRunning();
			getConsole().end();
		}
		// Adding our build to current build list
		if (genResult != null) {
			SQLGenUtil.getInstance().addBuildResult(result);
		}

		// Invalidating errors
		CorePlugin.getService(IMarkerService.class).computeAllMarkers();
		CorePlugin.getService(IMarkerService.class).fetchMarkersFor(script);
		return result;
	}

	/**
	 * Submits the specified {@link ISQLScript} to the {@link IConnection}. All
	 * initialization has been done and implementors should only care about
	 * deploying the SQL to the database.
	 * 
	 * @param monitor
	 *            monitors the execution, handling cancellation events
	 * @param script
	 *            the {@link ISQLScript} to submit
	 * @param conn
	 *            the {@link IConnection} to submit to
	 * @return <code>true</code> for no errors, else <code>false</code>
	 */
	protected abstract boolean doSubmit(IProgressMonitor monitor, ISQLScript script,
			IConnection conn) throws IOException, SQLException;

	/**
	 * Default implementation is the script's filename, may be overridden.
	 * 
	 * @param s
	 *            generated {@link ISQLScript}
	 * @return the string to display on console when submitting the script
	 */
	protected String getScriptDisplayName(ISQLScript s) {
		return s.getFilename();
	}

	@Override
	public IGenerationResult getGenerationResult() {
		return genResult;
	}

	@Override
	public void setGenerationResult(IGenerationResult result) {
		this.genResult = result;
	}

	protected boolean isRunning() {
		return isRunning;
	}

	protected void stopRunning() {
		isRunning = false;
	}

	/**
	 * Aborts the current running process, asking to kill any external thread or
	 * to stop current SQL submit process.
	 */
	protected abstract void abort();
}
