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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;

import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.model.ScriptType;
import com.nextep.datadesigner.sqlgen.services.BuildResult;
import com.nextep.datadesigner.sqlgen.services.SQLGenUtil;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.SQLGenMessages;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public abstract class AbstractExternalSQLSubmitter extends AbstractSQLSubmitter {

	private String binaryProgram;

	@Override
	public BuildResult submit(IProgressMonitor monitor, ISQLScript script, IConnection conn) {
		initializeExternalProcess(script);
		return super.submit(monitor, script, conn);
	}

	/**
	 * Generates the {@link ISQLScript} on the local file system so that
	 * external database clients could use them to submit the provided SQL.<br>
	 * Typically, this will be used when using vendor-specific SQL-client
	 * programs such as sqlplus, mysql or pgsql.
	 */
	private void initializeExternalProcess(ISQLScript script) {
		final String binary = SQLGenUtil.getGeneratorBinary(getVendor());
		if (binary == null || "".equals(binary)) { //$NON-NLS-1$
			throw new ErrorException(SQLGenMessages.getString("Generator.NotFound")); //$NON-NLS-1$
		}
		setBinaryProgram(binary);

		// Displaying generator's binary location
		getConsole().log(""); //$NON-NLS-1$
		getConsole().log("Using generator binary '" + binary + "'..."); //$NON-NLS-2$

		// Generating default name for external file save
		SimpleDateFormat f = new SimpleDateFormat("yyMMddHHmmss"); //$NON-NLS-1$
		final String defaultName = "internalBuild_" + f.format(new Date()); //$NON-NLS-1$

		// If our script is not external we make it external and save it
		persistInternalScript(script, defaultName);
	}

	/**
	 * Persists the generation result on the local temporary neXtep folder.<br>
	 * Useful for <i>external</i> generator processing which need a concrete
	 * file.
	 * 
	 * @param script
	 *            the {@link ISQLScript} to save on disk
	 * @param defaultName
	 *            the name of the persisted script
	 */
	private void persistInternalScript(ISQLScript script, String defaultName) {
		// Saving out script in temp directory
		script.setDirectory(SQLGenUtil.getPreference(PreferenceConstants.TEMP_FOLDER));
		if (script.getName() == null || "".equals(script.getName())) { //$NON-NLS-1$
			script.setName(defaultName);
		}

		// First persisting child scripts of any wrapper so that when the
		// wrapper
		// will be persisted it will generate the proper script links
		if (script instanceof SQLWrapperScript) {
			for (ISQLScript s : ((SQLWrapperScript) script).getChildren()) {
				if (!s.isExternal()) {
					if (s.getScriptType() == ScriptType.CUSTOM) {
						persistInternalScript(s, defaultName + "i"); //$NON-NLS-1$
					} else {
						persistInternalScript(s, defaultName);
					}
				}
			}
		}

		// Saving to filesystem
		saveScriptToFilesystem(script);
	}

	protected final String getEncodingPreference() {
		return SQLGenUtil.getPreference(PreferenceConstants.SQL_SCRIPT_ENCODING).toUpperCase();
	}

	/**
	 * Defines the binary program location
	 * 
	 * @param binaryProgram
	 *            binary program location
	 */
	protected void setBinaryProgram(String binaryProgram) {
		this.binaryProgram = binaryProgram;
	}

	/**
	 * @return the binary program location
	 */
	protected String getBinaryProgram() {
		return binaryProgram;
	}

	/**
	 * Saves the specified script to the file system
	 * 
	 * @param s
	 *            the script to be saved to the file system.
	 */
	private void saveScriptToFilesystem(ISQLScript s) {
		// We save the current external status of the SQL Script to restore it
		// after saving it to
		// the file system
		boolean external = s.isExternal();
		try {
			// For the script to be saved on the file system by the controller
			// we need to previously
			// set the SQL script as external
			s.setExternal(true);
			ControllerFactory.getController(s).save(s);
		} finally {
			s.setExternal(external);
		}
	}

	/**
	 * Outputs a message indicating the number of errors
	 * 
	 * @param errorCount
	 *            the number of errors during submission
	 */
	protected void logErrors(int errorCount) {
		if (errorCount > 0) {
			getConsole()
					.log("\nERROR: " + errorCount + " errors returned by database, check the script for errors.\n"); //$NON-NLS-1$//$NON-NLS-2$
		} else {
			getConsole().log("\nSUCCESS: 0 errors detected\n"); //$NON-NLS-1$
		}
	}
}
