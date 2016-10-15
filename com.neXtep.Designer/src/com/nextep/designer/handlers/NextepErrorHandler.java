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
package com.nextep.designer.handlers;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.WorkbenchErrorHandler;
import org.hibernate.exception.JDBCConnectionException;
import com.nextep.datadesigner.Designer;
import com.nextep.designer.vcs.ui.impl.ExceptionHandler;

/**
 * neXtep product-specific error handling. This implementation only extends the default eclipse
 * {@link WorkbenchErrorHandler} to make sure we still have eclipse facilities like error logging
 * etc. <br>
 * We need a specific error handling to handle some rare exception which may occur in background
 * jobs.
 * 
 * @author Christophe Fondacci
 */
public class NextepErrorHandler extends WorkbenchErrorHandler {

	/**
	 * Cannot find out why Eclipse sends the error status twice, this is to avoid double processing
	 */
	private JDBCConnectionException lastHandledEx = null;

	@Override
	public void handle(StatusAdapter statusAdapter, int style) {
		IStatus status = statusAdapter.getStatus();
		final Throwable ex = status.getException();
		// We could probably always delegate exception handling to
		// ExceptionHandler
		// class, but not sure of the impacts (mainly fearing a double handling)
		if (ex instanceof JDBCConnectionException) {
			if (ex == lastHandledEx) {
				return;
			}
			lastHandledEx = (JDBCConnectionException) ex;
			if (Designer.getTerminationSignal()) {
				return;
			}
			ExceptionHandler.handle(ex);
			// try {
			// Designer.setTerminationSignal(true);
			// Display.getDefault().syncExec(new Runnable() {
			//
			// @Override
			// public void run() {
			// // We are in a situation where we've lost our connection to
			// // the repository
			// MessageDialog
			// .openError(
			// null,
			// UIMessages
			//												.getString("exceptionHandler.repositoryConnectionLost.title"), //$NON-NLS-1$
			// UIMessages
			//												.getString("exceptionHandler.repositoryConnectionLost.message")); //$NON-NLS-1$
			// }
			// });
			// } finally {
			// Designer.setTerminationSignal(false);
			// }
			// // Handling the error within a nice progress dialog since reconnecting
			// // to database could take a while.
			// CommandProgress.runWithProgress(false, new ICommand() {
			//
			// @Override
			// public Object execute(Object... parameters) {
			// // Closing and reopening session
			// HibernateUtil.getInstance().reconnectAll();
			// return null;
			// }
			//
			// @Override
			// public String getName() {
			// return DesignerMessages
			//							.getString("nextepErrorHandler.reconnectingToRepo.title"); //$NON-NLS-1$
			// }
			// });
			//
			// if ((style & StatusManager.LOG) == StatusManager.LOG) {
			// StatusManager.getManager().addLoggedStatus(statusAdapter.getStatus());
			// }
			// Display.getDefault().syncExec(new Runnable() {
			//
			// @Override
			// public void run() {
			// VersionUIHelper.changeView(VersionHelper.getCurrentView().getUID());
			// }
			// });

		} else {
			super.handle(statusAdapter, style);
		}
	}
}
