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
/**
 *
 */
package com.nextep.designer.vcs.ui.impl;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.hibernate.HibernateException;
import org.hibernate.exception.JDBCConnectionException;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.ExitDesignerException;
import com.nextep.datadesigner.exception.OutOfDateObjectException;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IWorkspaceService;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;
import com.nextep.designer.vcs.ui.services.VersionUIHelper;

/**
 * All thrown exceptions should be redirected to this handler which will either log a message in the
 * console or raise a runtime exception.
 * 
 * @author Christophe Fondacci
 */
public class ExceptionHandler {

	private static final Log log = LogFactory.getLog(ExceptionHandler.class);

	/**
	 * Handles exception within the neXtep designer environment. This method returns a boolean
	 * indicating whether the exception handling should go on with Eclipse exception handling or if
	 * it has been absorbed by neXtep.
	 * 
	 * @param t the exception to handle
	 * @return <code>true</code> when the exception has been completely handled and should not be
	 *         raised, else <code>false</code>.
	 */
	public static boolean handle(Throwable t) {
		if (t instanceof OutOfDateObjectException) {
			VersionUIHelper.promptObjectSynched(((OutOfDateObjectException) t).getStaleObject());
		} else if (t instanceof CancelException) {
			log.info(t.getMessage());
		} else if (t instanceof ErrorException) {
			if (t.getCause() instanceof InvocationTargetException) {
				t = t.getCause().getCause();
				return handle(t);
			}
			log.error(t.getMessage(), t);
			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell(), "Error when performing user action", t.getMessage());
			// if("true".equals(Designer.getInstance().getProperty("nextep.designer.debug"))) {
			// log.error(t);
			// }
		} else if (t instanceof ExitDesignerException) {
			log.info(t.getMessage());
		} else if (t instanceof JDBCConnectionException) {
			if (!Designer.getTerminationSignal()) {
				Designer.setTerminationSignal(true);
				try {
					// Ensuring a UI Thread
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							// We are in a situation where we've lost our connection to the
							// repository
							MessageDialog.openError(
									null,
									UIMessages
											.getString("exceptionHandler.repositoryConnectionLost.title"), //$NON-NLS-1$
									UIMessages
											.getString("exceptionHandler.repositoryConnectionLost.message")); //$NON-NLS-1$
							// Closing perpective
							IWorkbenchPage page = PlatformUI.getWorkbench()
									.getActiveWorkbenchWindow().getActivePage();
							page.closeAllPerspectives(true, true);
							// Closing and reopening session
							HibernateUtil.getInstance().reconnectAll();
							// Changing view
							final IWorkspaceUIService viewUiService = VCSUIPlugin
									.getService(IWorkspaceUIService.class);
							final IWorkspaceService viewService = VCSPlugin
									.getService(IWorkspaceService.class);
							viewUiService.changeWorkspace(viewService.getCurrentWorkspace().getUID());
						}
					});
				} finally {
					Designer.setTerminationSignal(false);
				}
			} else {
				log.error(
						"Error thrown while reinitializing repository connection: "
								+ t.getMessage(), t);
			}
			return true;
		} else {
			log.error("An error has been raised: " + t.getMessage(), t); //$NON-NLS-1$
			if (t instanceof HibernateException && PlatformUI.getWorkbench() != null
					&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getShell(), UIMessages.getString("systemInstableTitle"), UIMessages //$NON-NLS-1$
						.getString("systemInstable")); //$NON-NLS-1$
			}
		}
		if (Designer.isDebugging()) {
			t.printStackTrace();
		}
		// By default we consider that we haven't handled completely the problem
		return false;
	}
}
