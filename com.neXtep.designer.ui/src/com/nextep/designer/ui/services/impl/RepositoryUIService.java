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
package com.nextep.designer.ui.services.impl;

import java.text.MessageFormat;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.dialogs.ConnectionPasswordDialog;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class RepositoryUIService implements IRepositoryService {

	private static final IRepositoryService coreService = CorePlugin.getRepositoryService();
	private static IRepositoryService instance = null;
	/** Flag for detecting that user has entered an empty password */
	private boolean emptyPasswordDefined = false;

	private RepositoryUIService() {
	}

	public static IRepositoryService getInstance() {
		if (instance == null) {
			instance = new RepositoryUIService();
		}
		return instance;
	}

	@Override
	public String decrytPassword(String encryptedPassword) {
		return coreService.decrytPassword(encryptedPassword);
	}

	@Override
	public String encryptPassword(String password) {
		return coreService.encryptPassword(password);
	}

	@Override
	public IConnection getRepositoryConnection() {
		final IConnection conn = coreService.getRepositoryConnection();
		if (conn != null
				&& !conn.isSsoAuthentication()
				&& !conn.isPasswordSaved()
				&& (("".equals(conn.getPassword()) || conn.getPassword() == null) && !emptyPasswordDefined)) { //$NON-NLS-1$
			Shell parent = Display.getCurrent() != null ? Display.getCurrent().getActiveShell()
					: Display.getDefault().getActiveShell();
			ConnectionPasswordDialog pwdDialog = new ConnectionPasswordDialog(parent,
					UIMessages.getString("repository.connection.passwordTitle"), //$NON-NLS-1$
					MessageFormat.format(
							UIMessages.getString("repository.connection.passwordText"), //$NON-NLS-1$
							conn.getName()), SWT.PASSWORD | SWT.BORDER);
			if (pwdDialog.open() == Window.OK) {
				conn.setPassword(pwdDialog.getValue());
				conn.setPasswordSaved(pwdDialog.shouldRemember());
				if ("".equals(conn.getPassword())) { //$NON-NLS-1$
					emptyPasswordDefined = true;
				} else {
					emptyPasswordDefined = false;
				}
				// Saving connection
				coreService.setRepositoryConnection(conn);
			} else {
				throw new CancelException(
						UIMessages.getString("repository.connection.passwordCancelled")); //$NON-NLS-1$
			}
		}
		return conn;
	}

	@Override
	public IDatabaseConnector getRepositoryConnector() {
		final IConnection conn = getRepositoryConnection();
		// Loading all views
		IDatabaseConnector connector = CorePlugin.getConnectionService().getDatabaseConnector(conn);
		return connector;
	}

	@Override
	public DBVendor getRepositoryVendor() {
		return coreService.getRepositoryVendor();
	}

	@Override
	public void setRepositoryConnection(IConnection repositoryConnection) {
		emptyPasswordDefined = false;
		coreService.setRepositoryConnection(repositoryConnection);
	}

	@Override
	public String getProperty(String name) {
		return coreService.getProperty(name);
	}

	@Override
	public void setProperty(String name, String value) {
		coreService.setProperty(name, value);
	}

}
