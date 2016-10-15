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
package com.nextep.designer.dbgm.ui.services;

import java.text.MessageFormat;
import java.util.Collection;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.GUIWrapper;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.TargetType;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.DbgmUIPlugin;
import com.nextep.designer.dbgm.ui.dialogs.ConnectionSelector;
import com.nextep.designer.ui.dialogs.ConnectionPasswordDialog;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.base.RunnableWithReturnedValue;
import com.nextep.designer.vcs.VCSPlugin;

/**
 * This class provides UI-level helper methods.
 * 
 * @author Christophe Fondacci
 */
public class DBGMUIHelper {

	private final static String PREF_LAST_CONNECTION = "com.neXtep.designer.dbgm.ui.lastConnection"; //$NON-NLS-1$

	/**
	 * This helper method returns a connection from a target type. If the specified target type
	 * contains several connections, the user will be prompted for connection selection, otherwise
	 * the connection is returned directly.
	 * 
	 * @param t database target type
	 * @return a connection for which {@link IConnection#getTargetType()} == the specified target
	 *         type.
	 */
	public static IConnection getConnection(final TargetType t) {
		RunnableWithReturnedValue<IConnection> runnable = new RunnableWithReturnedValue<IConnection>() {

			@Override
			public void run() {
				returnedValue = doGetConnection(t);
			}
		};
		Display.getDefault().syncExec(runnable);
		return runnable.returnedValue;
	}

	private static IConnection doGetConnection(TargetType t) {
		IConnection connection = null;

		Collection<IConnection> connections = null;
		if (t != null) {
			connections = VCSPlugin.getViewService().getCurrentViewTargets().getTarget(t);
		} else {
			connections = VCSPlugin.getViewService().getCurrentViewTargets().getConnections();
		}
		if (connections.isEmpty()) {
			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell(), DBGMUIMessages.getString("noExistingTargetConnectionTitle"), //$NON-NLS-1$
					MessageFormat.format(DBGMUIMessages.getString("noExistingTargetConnection"), //$NON-NLS-1$
							t == null ? DBGMUIMessages.getString("helper.all") : t.getLabel())); //$NON-NLS-1$
			connection = (IConnection) UIControllerFactory.getController(
					IElementType.getInstance(IConnection.TYPE_ID)).newInstance(
					VCSPlugin.getViewService().getCurrentViewTargets());
			if (connection == null) {
				throw new ErrorException(
						DBGMUIMessages.getString("helper.connection.noTarget") + t.getLabel()); //$NON-NLS-1$
			}
		} else if (connections.size() == 1) {
			connection = connections.iterator().next();
		} else {
			ConnectionSelector selector = new ConnectionSelector(t);
			final IPreferenceStore store = DbgmUIPlugin.getDefault().getPreferenceStore();
			String lastConnection = store.getString(PREF_LAST_CONNECTION);
			if (lastConnection != null) {
				selector.setDefaultConnection(lastConnection);
			}
			final GUIWrapper w = new GUIWrapper(selector,
					DBGMUIMessages.getString("chooseConnectionTitle"), 520, 130); //$NON-NLS-1$
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					w.invoke();
				}
			});
			if (!w.isCancelled()) {
				connection = selector.getSelection();
				store.putValue(PREF_LAST_CONNECTION, connection.getName());
			} else {
				throw new CancelException(DBGMUIMessages.getString("helper.cancelled")); //$NON-NLS-1$
			}
		}
		checkConnectionPassword(connection);
		return connection;
	}

	/**
	 * Checks the connection password of this connection and asks to the user for a password if this
	 * is a secured connection.
	 * 
	 * @param conn connection to check.
	 */
	public static void checkConnectionPassword(IConnection conn) {
		if (!conn.isPasswordSaved() && !conn.isSsoAuthentication()) {
			Shell parent = (Display.getCurrent() != null ? Display.getCurrent().getActiveShell()
					: Display.getDefault().getActiveShell());
			ConnectionPasswordDialog pwdDialog = new ConnectionPasswordDialog(parent,
					DBGMUIMessages.getString("repository.connection.passwordTitle"), MessageFormat ////$NON-NLS-1$
							.format(DBGMUIMessages.getString("repository.connection.passwordText"), ////$NON-NLS-1$
									conn.getName()), SWT.PASSWORD | SWT.BORDER);
			if (pwdDialog.open() == Window.OK) {
				conn.setPassword(pwdDialog.getValue());
				conn.setPasswordSaved(pwdDialog.shouldRemember());
			} else {
				throw new CancelException(
						DBGMUIMessages.getString("repository.connection.passwordCancelled")); ////$NON-NLS-1$
			}
		}
	}

}
