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
package com.nextep.datadesigner.vcs.gui.dialog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.IDesignerGUI;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.UID;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.IDatabaseConnector;
import com.nextep.designer.core.services.IConnectionService;
import com.nextep.designer.core.services.IRepositoryService;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.editors.RepositoryConnectionEditor;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.services.VersionUIHelper;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class UserLoginGUI implements IDesignerGUI, IEventListener {

	private static final Log LOGGER = LogFactory.getLog(UserLoginGUI.class);
	private static final String PROP_LAST_LOGIN = "com.neXtep.designer.login.last"; //$NON-NLS-1$

	private static final IRepositoryService REPO_UI_SERVICE = CoreUiPlugin.getRepositoryUIService();
	private static final IConnectionService CONNECTION_SERVICE = CorePlugin.getConnectionService();

	private Shell sShell;
	private FieldEditor loginEditor;
	private FieldEditor passwordEditor;
	private Button connectButton;
	private Button repositoryButton = null;
	private Button quitButton = null;
	private Label statusLabel;
	private boolean authenticated = false;
	private String repositoryLogin;
	private String repositoryPassword;

	@Override
	public Display getDisplay() {
		return sShell.getDisplay();
	}

	@Override
	public Shell getShell() {
		return sShell;
	}

	public void resetShell() {
		sShell = null;
	}

	@Override
	public void initializeGUI(Shell parentGUI) {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.END;
		gridData11.verticalAlignment = GridData.CENTER;
		sShell = new Shell(parentGUI, SWT.APPLICATION_MODAL | SWT.TITLE | SWT.RESIZE);
		sShell.setText(VCSUIMessages.getString("user.login.authentication")); //$NON-NLS-1$
		sShell.setLayout(gridLayout);
		sShell.setImage(VCSImages.ICON_USERS);

		// Creating login controls
		loginEditor = new FieldEditor(sShell,
				VCSUIMessages.getString("user.login.login"), 1, 2, true, this, //$NON-NLS-1$
				ChangeEvent.LOGIN_CHANGED);
		passwordEditor = new FieldEditor(sShell,
				VCSUIMessages.getString("user.login.password"), 1, 2, true, this, //$NON-NLS-1$
				ChangeEvent.PASSWORD_CHANGED, true);
		final String lastLogin = new InstanceScope().getNode(CorePlugin.PLUGIN_ID).get(
				PROP_LAST_LOGIN, ""); //$NON-NLS-1$
		loginEditor.setText(lastLogin);
		if (!"".equals(lastLogin)) { //$NON-NLS-1$
			repositoryLogin = lastLogin.toUpperCase();
			passwordEditor.getText().setFocus();
		}
		repositoryButton = new Button(sShell, SWT.NONE);
		repositoryButton.setText(VCSUIMessages.getString("user.login.repositoryEdition")); //$NON-NLS-1$
		GridData repButData = new GridData();
		repButData.horizontalSpan = 2;
		repositoryButton.setLayoutData(repButData);
		repositoryButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					editRepository();
					refreshGUI();
				} catch (ErrorException ex) {
					statusLabel.setText(ex.getMessage() == null ? VCSUIMessages
							.getString("user.login.configurationError") : ex.getMessage()); //$NON-NLS-1$
					LOGGER.error(VCSUIMessages.getString("user.login.configurationError") + ": " //$NON-NLS-1$ //$NON-NLS-2$
							+ ex.getMessage(), ex);
				}
			}
		});
		connectButton = new Button(sShell, SWT.NONE);
		connectButton.setText(VCSUIMessages.getString("user.login.connectButton")); //$NON-NLS-1$
		connectButton.setLayoutData(gridData11);
		quitButton = new Button(sShell, SWT.PUSH);
		quitButton.setText(VCSUIMessages.getString("user.login.quitButton")); //$NON-NLS-1$
		GridData quitData = new GridData(SWT.END, SWT.FILL, true, false, 3, 1);
		quitButton.setLayoutData(quitData);
		quitButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				System.exit(0);
			}
		});
		sShell.setDefaultButton(connectButton);
		connectButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Designer.getInstance()
						.setProperty(PROP_LAST_LOGIN, loginEditor.getText().getText());
				// Cocoa bugfix : controls do not loose focus when a button is pressed
				passwordEditor.getText().traverse(SWT.TRAVERSE_RETURN);
				// connectButton.setFocus();
				connect();
			}
		});
		// Creating status line
		GridData statusData = new GridData();
		statusData.horizontalAlignment = GridData.FILL;
		statusData.grabExcessHorizontalSpace = true;
		statusData.horizontalSpan = 3;

		statusLabel = new Label(sShell, SWT.BORDER);
		statusLabel.setLayoutData(statusData);
		sShell.pack();
		sShell.layout();
		// Centering dialog window
		Rectangle r = parentGUI.getBounds();
		Rectangle s = sShell.getBounds();
		Point loc = new Point(r.x + (r.width / 2) - (s.width / 2), r.y + (r.height / 2)
				- (s.height / 2));
		sShell.setLocation(loc);
		refreshGUI();
	}

	@SuppressWarnings("unchecked")
	private void refreshGUI() {
		final IProgressMonitor monitor = Designer.getProgressMonitor();
		statusLabel.setText(VCSUIMessages.getString("user.login.initializingConnection")); //$NON-NLS-1$

		// Loading all views
		try {
			final IDatabaseConnector<Connection> dbConnector = REPO_UI_SERVICE
					.getRepositoryConnector();
			final IConnection repoConn = REPO_UI_SERVICE.getRepositoryConnection();
			final String repoConnURL = dbConnector.getConnectionURL(repoConn);

			statusLabel.setText(MessageFormat.format(
					VCSUIMessages.getString("user.login.connectionAttempt"), repoConnURL)); //$NON-NLS-1$
			if (monitor != null) {
				monitor.setTaskName(MessageFormat.format(
						VCSUIMessages.getString("user.login.repositoryConnection"), repoConnURL)); //$NON-NLS-1$
			}
			boolean enabled = VersionUIHelper.startup();
			if (enabled) {
				statusLabel.setText(VCSUIMessages.getString("user.login.connected")); //$NON-NLS-1$
			} else {
				statusLabel.setText(VCSUIMessages.getString("user.login.noRepository")); //$NON-NLS-1$
			}
			if (monitor != null) {
				monitor.setTaskName(VCSUIMessages.getString("user.login.authenticating")); //$NON-NLS-1$
			}

			// Enabling controls
			loginEditor.getText().setEnabled(enabled);
			passwordEditor.getText().setEnabled(enabled);
			connectButton.setEnabled(enabled);
			sShell.setDefaultButton(connectButton);
		} catch (Exception e) {
			statusLabel.setText(VCSUIMessages.getString("user.login.connectionFailed") //$NON-NLS-1$
					+ e.getMessage());
			LOGGER.error(VCSUIMessages.getString("user.login.connectionFailed") + e.getMessage(), e); //$NON-NLS-1$
			loginEditor.getText().setEnabled(false);
			passwordEditor.getText().setEnabled(false);
			connectButton.setEnabled(false);
			sShell.setDefaultButton(repositoryButton);
		}
	}

	/**
	 * Shows up the repository edition window. This method returns only after the user has finished
	 * to setup the repository connection and clicks ok or cancel.
	 */
	private void editRepository() {
		AbstractUIController.newWizardEdition(UIMessages.getString("repositoryConnectionWizard"), //$NON-NLS-1$
				new RepositoryConnectionEditor());
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case LOGIN_CHANGED:
			repositoryLogin = ((String) data).toUpperCase();
			break;
		case PASSWORD_CHANGED:
			final String securedPassword = REPO_UI_SERVICE.encryptPassword(((String) data)
					.toUpperCase());
			repositoryPassword = securedPassword;
			break;
		}
	}

	protected void connect() {
		final IConnection repoConn = REPO_UI_SERVICE.getRepositoryConnection();

		VCSPlugin.getViewService().setCurrentUser(null);
		authenticated = false;

		// Loading all views
		statusLabel.setText(VCSUIMessages.getString("user.login.initializingConnection")); //$NON-NLS-1$
		statusLabel.setText(VCSUIMessages.getString("user.login.authenticating")); //$NON-NLS-1$

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rset = null;
		try {
			conn = CONNECTION_SERVICE.connect(repoConn);
			secureUserAuthentication(conn);
			stmt = conn.prepareStatement("SELECT " //$NON-NLS-1$
					+ "    ru.user_id " //$NON-NLS-1$
					+ "  , ru.username " //$NON-NLS-1$
					+ "  , ru.description " //$NON-NLS-1$
					//					+ "  , ru.is_admin " //$NON-NLS-1$
					+ "FROM rep_users ru " //$NON-NLS-1$ 
					+ "WHERE UPPER(ru.login) = ? " //$NON-NLS-1$
					+ "  AND ru.secured_password = ? "); //$NON-NLS-1$
			stmt.setString(1, repositoryLogin.toUpperCase());
			stmt.setString(2, repositoryPassword);

			rset = stmt.executeQuery();
			if (rset.next()) {
				final Long userId = rset.getLong("user_id"); //$NON-NLS-1$
				final String userName = rset.getString("username"); //$NON-NLS-1$
				final String userDesc = rset.getString("description"); //$NON-NLS-1$

				statusLabel.setText(MessageFormat.format(
						VCSUIMessages.getString("user.login.success"), userName)); //$NON-NLS-1$
				IRepositoryUser user = new RepositoryUser(repositoryLogin, repositoryPassword,
						userName, userDesc);
				user.setUID(new UID(userId));
				VCSPlugin.getViewService().setCurrentUser(user);
				statusLabel.setText(VCSUIMessages.getString("user.login.connecting")); //$NON-NLS-1$
				HibernateUtil.getInstance();
				statusLabel.setText(VCSUIMessages.getString("user.login.connected")); //$NON-NLS-1$
				authenticated = true;
			}

			if (!authenticated) {
				statusLabel.setText(VCSUIMessages.getString("user.login.authenticationFailed")); //$NON-NLS-1$
			}
		} catch (SQLException e) {
			statusLabel.setText(VCSUIMessages.getString("user.login.authenticationFailed.2") //$NON-NLS-1$
					+ e.getMessage());
		} finally {
			try {
				if (rset != null) {
					rset.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Problems while closing resource: " + e.getMessage(), e); //$NON-NLS-1$
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Problems while closing resource: " + e.getMessage(), e); //$NON-NLS-1$
			}
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Problems while closing resource: " + e.getMessage(), e); //$NON-NLS-1$
			}
		}
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	private void secureUserAuthentication(Connection conn) throws SQLException {
		PreparedStatement stmt = null;
		PreparedStatement updStmt = null;
		try {
			stmt = conn.prepareStatement("SELECT " //$NON-NLS-1$
					+ "    ru.user_id " //$NON-NLS-1$
					+ "  , ru.password " //$NON-NLS-1$
					+ "  , ru.secured_password " //$NON-NLS-1$
					+ "FROM rep_users ru " //$NON-NLS-1$
					+ "WHERE UPPER(ru.login) = ? "); //$NON-NLS-1$
			stmt.setString(1, repositoryLogin);

			ResultSet rset = stmt.executeQuery();
			if (rset.next()) {
				final Long userId = rset.getLong("user_id"); //$NON-NLS-1$
				final String clearPassword = rset.getString("password"); //$NON-NLS-1$
				String securedPassword = rset.getString("secured_password"); //$NON-NLS-1$

				if (clearPassword != null && !"".equals(clearPassword)) { //$NON-NLS-1$
					securedPassword = REPO_UI_SERVICE.encryptPassword(clearPassword.toUpperCase());

					/*
					 * Columns names in the SET clause cannot be qualified with an alias name
					 * because it would fail in Postgres.
					 */
					updStmt = conn.prepareStatement("UPDATE REP_USERS ru " //$NON-NLS-1$
							+ "  SET password = NULL " //$NON-NLS-1$
							+ "    , secured_password = ? " //$NON-NLS-1$
							+ "WHERE ru.user_id = ? "); //$NON-NLS-1$
					updStmt.setString(1, securedPassword);
					updStmt.setLong(2, userId);
					updStmt.execute();
				}
			}
		} finally {
			try {
				if (updStmt != null) {
					updStmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Problems while closing resource: " + e.getMessage(), e); //$NON-NLS-1$
			}
			try {
				if (stmt != null) {
					stmt.close();
				}
			} catch (SQLException e) {
				LOGGER.error("Problems while closing resource: " + e.getMessage(), e); //$NON-NLS-1$
			}
		}
	}

}
