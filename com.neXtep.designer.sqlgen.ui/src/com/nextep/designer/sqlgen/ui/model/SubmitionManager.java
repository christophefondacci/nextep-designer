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
package com.nextep.designer.sqlgen.ui.model;

import java.util.Date;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.sqlgen.model.IGenerationSubmitter;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.datadesigner.sqlgen.services.BuildResult;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.sqlgen.SQLGenPlugin;
import com.nextep.designer.sqlgen.model.IGenerationResult;
import com.nextep.designer.sqlgen.preferences.PreferenceConstants;
import com.nextep.designer.sqlgen.services.IGenerationService;
import com.nextep.designer.sqlgen.ui.Activator;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.sqlgen.ui.impl.GenerationResultSubmitter;
import com.nextep.designer.sqlgen.ui.views.GenerationConsole;

/**
 * This manager provides convenience methods to submit scripts to a database connection.
 * 
 * @author Christophe Fondacci
 */
public class SubmitionManager {

	/**
	 * This runnable is made to be run in the UI thread and to ask user for confirmation that the
	 * "not preferred" submitter could be used instead of the preferred one. It keeps the user
	 * response (yes / no) so that we could trigger the appropriate actions.<br>
	 * This is because we cannot know whether or not we are in the UI thread (since generation could
	 * be fired through background job).<br>
	 * TODO: Refactor this, as the check should be performed before firing any job
	 */
	private static class UserConfirmSubmitter implements Runnable {

		private boolean confirmed = false;

		@Override
		public void run() {
			// final IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(),
			// SQLGenPlugin.PLUGIN_ID);
			MessageDialogWithToggle msgDialog = MessageDialogWithToggle
					.openYesNoQuestion(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							SQLMessages.getString("submitionDefaultJdbcTitle"), //$NON-NLS-1$
							SQLMessages.getString("submitionDefaultJdbc"), //$NON-NLS-1$
							SQLMessages.getString("submitionToggle"), //$NON-NLS-1$
							false,
							Activator.getDefault().getPreferenceStore(),
							com.nextep.designer.sqlgen.preferences.PreferenceConstants.SUBMITION_JDBC_NOWARN);
			// store.setValue(
			// com.nextep.designer.sqlgen.preferences.PreferenceConstants.SUBMITION_JDBC_NOWARN,
			// msgDialog.getToggleState());
			confirmed = (msgDialog.getReturnCode() == 2);
		}

		public boolean isConfirmed() {
			return confirmed;
		}
	}

	/**
	 * Submits the given script to the specified database connection
	 * 
	 * @param conn database connection
	 * @param script script to submit
	 * @param result generation result which generated the script
	 * @return the build result
	 */
	public static BuildResult submit(IConnection conn, ISQLScript script, IGenerationResult result,
			IProgressMonitor monitor) {
		IGenerationSubmitter submitter = getSQLSubmitter(conn, script);
		submitter.setGenerationResult(result);
		return submitter.submit(monitor, script, conn);
	}

	/**
	 * Submits the generation result to the database
	 * 
	 * @param conn database connection to submit to
	 * @param result generation result to submit
	 * @return <code>null</code>
	 */
	public static void submit(IConnection conn, IGenerationResult result, IProgressMonitor monitor) {
		IGenerationSubmitter submitter = getSQLSubmitter(conn, null);
		GenerationResultSubmitter resultSubmitter = new GenerationResultSubmitter(submitter, result);
		resultSubmitter.submit(monitor, conn);
	}

	public static IGenerationSubmitter getSQLSubmitter(IConnection conn, ISQLScript script) {
		IGenerationSubmitter submitter = SQLGenPlugin.getService(IGenerationService.class)
				.getGenerationSubmitter(conn.getDBVendor());
		checkSubmitter(submitter, conn);
		submitter.setConsole(new GenerationConsole(script == null ? new Date().toString() : script
				.getName(), true));
		return submitter;
	}

	private static void checkSubmitter(IGenerationSubmitter submitter, IConnection conn) {
		if (submitter.getVendor() != conn.getDBVendor()) {
			boolean notifyUser = Activator.getDefault().getPreferenceStore()
					.getBoolean(PreferenceConstants.SUBMITION_JDBC_NOWARN);
			if (notifyUser) {
				UserConfirmSubmitter confirmProcess = new UserConfirmSubmitter();
				Display.getDefault().syncExec(confirmProcess);
				if (!confirmProcess.isConfirmed()) {
					throw new CancelException(
							SQLMessages.getString("dialog.synchUnpreferredSubmitter")); //$NON-NLS-1$
				}
			}
		}
	}

}
