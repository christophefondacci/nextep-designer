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
package com.nextep.designer.ui.editors;

import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.editors.CheckBoxEditor;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.services.IConnectionService;
import com.nextep.designer.ui.UIImages;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class ConnectionEditorGUI extends WizardDisplayConnector {

	private static final Log LOGGER = LogFactory.getLog(ConnectionEditorGUI.class);
	private static final IConnectionService connService = CorePlugin.getConnectionService();

	private CLabel dbVendorLabel = null;
	private Combo dbVendorCombo = null;
	private CLabel usernameLbl = null;
	private Text usernameText = null;
	private Button isSsoAuthButton = null;
	private CLabel passwordLabel = null;
	private Text passwordText = null;
	private Button isSavedButton = null;
	private CLabel instanceLabel = null;
	private Text instanceText = null;
	private CLabel databaseLabel = null;
	private Text databaseText = null;
	private CLabel schemaLabel = null;
	private Text schemaText = null;
	private CLabel hostLabel = null;
	private Text hostText = null;
	private CLabel portLabel = null;
	private Text portText = null;
	private Label tnsLabel = null;
	private Text tnsText = null;
	private Composite editor = null;
	private Button testButton = null;
	private Label testLabel = null;
	private Group advancedGroup;

	private final IConnection connection;

	public ConnectionEditorGUI(IConnection conn, ITypedObjectUIController controller) {
		super(UIMessages.getString("connectionWizardTitle"), //$NON-NLS-1$ 
				UIMessages.getString("connectionWizardTitle"), //$NON-NLS-1$
				ImageDescriptor.createFromImage(UIImages.WIZARD_NEW_CONNECTION));
		setMessage(UIMessages.getString("connectionWizardMsg")); //$NON-NLS-1$

		/*
		 * This initialization block is useful for repository connection because the #validate
		 * method is not called by the RepositoryConnectionEditor. So when a Windows SSO connection
		 * is saved with login and password filled (it happens when the SSO checkbox is checked
		 * after entering login and password), the login and password would be reloaded if not reset
		 * here.
		 */
		if (conn.isSsoAuthentication() && conn.getDBVendor() == DBVendor.MSSQL) {
			conn.setLogin(""); //$NON-NLS-1$
			conn.setPassword(""); //$NON-NLS-1$
			conn.setPasswordSaved(false);
		} else {
			conn.setSsoAuthentication(false);
		}
		if (!conn.isPasswordSaved()) {
			conn.setPassword(""); //$NON-NLS-1$
		}
		this.connection = conn;
	}

	@Override
	public Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		addNoMarginLayout(editor, 3);
		createDBVendorCombo();
		createUsername();
		createPassword();
		createDatabase();
		createHost();
		createPort();
		createAdvancedGroup();
		createTest();
		return editor;
	}

	private void createAdvancedGroup() {
		advancedGroup = new Group(editor, SWT.NONE);
		advancedGroup.setLayout(new GridLayout(3, false));
		advancedGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		advancedGroup.setText(UIMessages.getString("editor.connection.advancedSettingsTitle")); //$NON-NLS-1$
		createInstance(advancedGroup);
		createSchema(advancedGroup);
		createTNS(advancedGroup);
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		final IConnection conn = (IConnection) getModel();
		final DBVendor connVendor = conn.getDBVendor();
		final String connPort = conn.getServerPort();

		if (usernameText.isDisposed())
			return;

		usernameText.setText(notNull(conn.getLogin()));
		usernameText.setEnabled(!conn.isSsoAuthentication() || connVendor != DBVendor.MSSQL);
		isSavedButton.setSelection(conn.isPasswordSaved());
		isSavedButton.setEnabled(!conn.isSsoAuthentication() || connVendor != DBVendor.MSSQL);
		passwordText.setText(notNull(conn.getPassword()));
		passwordText.setEnabled(!conn.isSsoAuthentication() || connVendor != DBVendor.MSSQL);
		instanceText.setText(notNull(conn.getInstance()));
		instanceText.setEnabled(connVendor == DBVendor.MSSQL);
		databaseText.setText(notNull(conn.getDatabase()));
		hostText.setText(notNull(conn.getServerIP()));
		tnsText.setEnabled(connVendor == DBVendor.ORACLE);
		if (tnsText.isEnabled()) {
			tnsText.setText(notNull(conn.getTnsAlias()));
		} else {
			tnsText.setText(""); //$NON-NLS-1$
		}
		schemaText.setText(notNull(conn.getSchema()));

		advancedGroup.setVisible(connVendor == DBVendor.ORACLE || connVendor == DBVendor.MSSQL
				|| connVendor == DBVendor.DB2 || connVendor == DBVendor.POSTGRE);

		// Handling vendors
		// Weird Cocoa bug, combo seems to be disposed between the start and the
		// end of this method
		// So we need a check here
		if (dbVendorCombo.isDisposed()) {
			return;
		}

		selectVendorInCombo();

		/*
		 * Fix to handle connection initialization when ConnectionEditorGUI is called from a
		 * "Generic JDBC" workspace, because no specific vendor can be pre-selected by the preceding
		 * selectVendorInCombo method call. When no vendor has been set, we set the connection
		 * vendor to the default vendor, and initialize the vendor combo with this vendor.
		 */
		if (dbVendorCombo.getSelectionIndex() == -1) {
			conn.setDBVendor(DBVendor.getDefaultVendor());
			selectVendorInCombo();
		}

		// When no port is specified, we take the default from current vendor
		if (connPort == null || "".equals(connPort)) { //$NON-NLS-1$
			int port = connVendor.getDefaultPort();
			if (port > 0) {
				conn.setServerPort(String.valueOf(port));
			}
		}
		portText.setText(notNull(connPort));

		isSsoAuthButton.setVisible(connVendor == DBVendor.MSSQL);
		isSsoAuthButton.setSelection(conn.isSsoAuthentication());
	}

	private void selectVendorInCombo() {
		final IConnection conn = (IConnection) getModel();
		int i = 0;
		for (String s : dbVendorCombo.getItems()) {
			if ((DBVendor) dbVendorCombo.getData(s) == conn.getDBVendor()) {
				dbVendorCombo.select(i);
				break;
			}
			i++;
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		final IConnection conn = (IConnection) getModel();
		if (data == null) {
			return;
		}
		switch (event) {
		case NAME_CHANGED:
			conn.setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			conn.setDescription((String) data);
			break;
		case LOGIN_CHANGED:
			conn.setLogin((String) data);
			break;
		case PASSWORD_CHANGED:
			final String pass = (String) data;
			conn.setPassword(pass);
			break;
		case IP_CHANGED:
			conn.setServerIP((String) data);
			break;
		case INSTANCE_CHANGED:
			conn.setInstance((String) data);
			break;
		case DATABASE_CHANGED:
			conn.setDatabase((String) data);
			break;
		case PORT_CHANGED:
			try {
				conn.setServerPort((String) data);
			} catch (NumberFormatException e) {
				LOGGER.warn(UIMessages.getString("editor.connection.invalidPort")); //$NON-NLS-1$
			}
			break;
		case DBVENDOR_CHANGED:
			conn.setDBVendor((DBVendor) dbVendorCombo.getData((String) data));
			conn.setServerPort(String.valueOf(conn.getDBVendor().getDefaultPort()));
			/*
			 * When changing the current database vendor, the SSO property of the connection is
			 * automatically reset. This is useful for repository connection because the #validate
			 * method is not called by the RepositoryConnectionEditor, so when switching from a
			 * Windows SSO connection to a standard database connection, the SSO flag would stay the
			 * same if not updated here, and the login and password would not be saved.
			 */
			conn.setSsoAuthentication(conn.isSsoAuthentication()
					&& conn.getDBVendor() == DBVendor.MSSQL);
			break;
		case CUSTOM_12:
			conn.setTnsAlias((String) data);
			break;
		case CUSTOM_1:
			conn.setSchema((String) data);
			break;
		case CUSTOM_8:
			final boolean isSaved = (Boolean) data;
			conn.setPasswordSaved(isSaved);
			break;
		case CUSTOM_9:
			final boolean isSso = (Boolean) data;
			conn.setSsoAuthentication(isSso);
			break;
		}
		super.handleEvent(event, source, data);
	}

	private void createDBVendorCombo() {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.CENTER;

		dbVendorLabel = new CLabel(editor, SWT.RIGHT);
		dbVendorLabel.setText(UIMessages.getString("editor.connection.vendor")); //$NON-NLS-1$
		dbVendorLabel.setLayoutData(gridData5);
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.CENTER;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		dbVendorCombo = new Combo(editor, SWT.READ_ONLY);
		dbVendorCombo.setLayoutData(gridData);
		ComboEditor.handle(dbVendorCombo, ChangeEvent.DBVENDOR_CHANGED, this);
		int index = 0;
		for (DBVendor v : DBVendor.values()) {
			if (v != DBVendor.JDBC && !v.isInternal()) {
				dbVendorCombo.add(v.toString());
				dbVendorCombo.setData(v.toString(), v);
				index++;
			}
		}
	}

	private void createUsername() {
		GridData gridData42 = new GridData();
		gridData42.horizontalAlignment = GridData.FILL;
		gridData42.verticalAlignment = GridData.CENTER;
		usernameLbl = new CLabel(editor, SWT.RIGHT);
		usernameLbl.setText(UIMessages.getString("editor.connection.username")); //$NON-NLS-1$
		usernameLbl.setLayoutData(gridData42);
		usernameText = new Text(editor, SWT.BORDER);
		usernameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ColorFocusListener.handle(usernameText);
		TextEditor.handle(usernameText, ChangeEvent.LOGIN_CHANGED, this);
		isSsoAuthButton = new Button(editor, SWT.CHECK);
		isSsoAuthButton.setText(UIMessages.getString("editor.connection.username.windowsSso")); //$NON-NLS-1$
		CheckBoxEditor.handle(isSsoAuthButton, ChangeEvent.CUSTOM_9, this);
	}

	private void createSchema(Composite parent) {
		GridData gridData42 = new GridData();
		gridData42.horizontalAlignment = GridData.FILL;
		gridData42.verticalAlignment = GridData.CENTER;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.verticalAlignment = GridData.CENTER;
		gridData4.horizontalSpan = 2;
		schemaLabel = new CLabel(parent, SWT.RIGHT);
		schemaLabel.setText(UIMessages.getString("editor.connection.schema")); //$NON-NLS-1$
		schemaLabel.setLayoutData(gridData42);
		schemaText = new Text(parent, SWT.BORDER);
		schemaText.setLayoutData(gridData4);
		ColorFocusListener.handle(schemaText);
		TextEditor.handle(schemaText, ChangeEvent.CUSTOM_1, this);
	}

	private void createPassword() {
		passwordLabel = new CLabel(editor, SWT.RIGHT);
		passwordLabel.setText(UIMessages.getString("editor.connection.password")); //$NON-NLS-1$
		passwordLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		passwordText = new Text(editor, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		ColorFocusListener.handle(passwordText);
		TextEditor.handle(passwordText, ChangeEvent.PASSWORD_CHANGED, this);
		isSavedButton = new Button(editor, SWT.CHECK);
		isSavedButton.setText(UIMessages.getString("editor.connection.password.remember")); //$NON-NLS-1$
		CheckBoxEditor.handle(isSavedButton, ChangeEvent.CUSTOM_8, this);
	}

	private void createHost() {
		GridData gridData22 = new GridData();
		gridData22.horizontalAlignment = GridData.FILL;
		gridData22.verticalAlignment = GridData.CENTER;
		GridData gridData21 = new GridData();
		gridData21.horizontalAlignment = GridData.FILL;
		gridData21.grabExcessHorizontalSpace = true;
		gridData21.verticalAlignment = GridData.CENTER;
		gridData21.horizontalSpan = 2;
		hostLabel = new CLabel(editor, SWT.RIGHT);
		hostLabel.setText(UIMessages.getString("editor.connection.server")); //$NON-NLS-1$
		hostLabel.setLayoutData(gridData22);
		hostText = new Text(editor, SWT.BORDER);
		hostText.setLayoutData(gridData21);
		ColorFocusListener.handle(hostText);
		TextEditor.handle(hostText, ChangeEvent.IP_CHANGED, this);
	}

	private void createPort() {
		GridData gridData22 = new GridData();
		gridData22.horizontalAlignment = GridData.FILL;
		gridData22.verticalAlignment = GridData.CENTER;
		GridData gridData21 = new GridData();
		gridData21.horizontalAlignment = GridData.FILL;
		gridData21.grabExcessHorizontalSpace = true;
		gridData21.verticalAlignment = GridData.CENTER;
		gridData21.horizontalSpan = 2;
		portLabel = new CLabel(editor, SWT.RIGHT);
		portLabel.setText(UIMessages.getString("editor.connection.port")); //$NON-NLS-1$
		portLabel.setLayoutData(gridData22);
		portText = new Text(editor, SWT.BORDER);
		portText.setLayoutData(gridData21);
		ColorFocusListener.handle(portText);
		TextEditor.handle(portText, ChangeEvent.PORT_CHANGED, this);
	}

	private void createTNS(Composite parent) {
		tnsLabel = new Label(parent, SWT.RIGHT);
		tnsLabel.setText(UIMessages.getString("editor.connection.tnsalias")); //$NON-NLS-1$
		tnsText = new Text(parent, SWT.BORDER);
		tnsLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		tnsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		ColorFocusListener.handle(tnsText);
		TextEditor.handle(tnsText, ChangeEvent.CUSTOM_12, this);
	}

	private void createDatabase() {
		GridData gridData22 = new GridData();
		gridData22.horizontalAlignment = GridData.FILL;
		gridData22.verticalAlignment = GridData.CENTER;
		databaseLabel = new CLabel(editor, SWT.RIGHT);
		databaseLabel.setText(UIMessages.getString("editor.connection.database")); //$NON-NLS-1$
		databaseLabel.setLayoutData(gridData22);
		databaseText = new Text(editor, SWT.BORDER);
		databaseText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		ColorFocusListener.handle(databaseText);
		TextEditor.handle(databaseText, ChangeEvent.DATABASE_CHANGED, this);
	}

	private void createInstance(Composite parent) {
		GridData gridData22 = new GridData();
		gridData22.horizontalAlignment = GridData.FILL;
		gridData22.verticalAlignment = GridData.CENTER;
		instanceLabel = new CLabel(parent, SWT.RIGHT);
		instanceLabel.setText(UIMessages.getString("editor.connection.instance")); //$NON-NLS-1$
		instanceLabel.setLayoutData(gridData22);
		instanceText = new Text(parent, SWT.BORDER);
		instanceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		ColorFocusListener.handle(instanceText);
		TextEditor.handle(instanceText, ChangeEvent.INSTANCE_CHANGED, this);
	}

	private void createTest() {
		testLabel = new Label(editor, SWT.NONE);
		testLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		testButton = new Button(editor, SWT.PUSH);
		testButton.setText(UIMessages.getString("editor.connection.test")); //$NON-NLS-1$
		testButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		testButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// Cocoa bugfix : pushing a button will not make the control
				// loose the focus so we
				// enforce change validation by traversing RETURN on everything
				usernameText.traverse(SWT.TRAVERSE_RETURN);
				passwordText.traverse(SWT.TRAVERSE_RETURN);
				instanceText.traverse(SWT.TRAVERSE_RETURN);
				databaseText.traverse(SWT.TRAVERSE_RETURN);
				portText.traverse(SWT.TRAVERSE_RETURN);
				schemaText.traverse(SWT.TRAVERSE_RETURN);
				tnsText.traverse(SWT.TRAVERSE_RETURN);
				hostText.traverse(SWT.TRAVERSE_RETURN);
				testConnection();
			}
		});
	}

	@Override
	public Object getModel() {
		return connection;
	}

	/**
	 * Tests the connection currently being edited
	 * 
	 * @return <code>true</code> on a successful connection, else <code>false</code>
	 */
	private boolean testConnection() {
		final IConnection conn = (IConnection) getModel();

		// We change the status message so that the user can see that a test is in progress (in case
		// he already tried unsuccessfully to connect and an error message was displayed)
		testLabel.setText(UIMessages.getString("editor.connection.testInProgress")); //$NON-NLS-1$
		refreshDisplay();

		// Checks that a database connector is available for the specified connection
		if (null == connService.getDatabaseConnector(conn)) {
			testLabel.setText(UIMessages.getString("editor.connection.15") //$NON-NLS-1$
					+ conn.getDBVendor().toString() + UIMessages.getString("editor.connection.16")); //$NON-NLS-1$
			return false;
		}

		// Checks that a connection can be established with the database and then closed
		Connection jdbcConn = null;
		try {
			jdbcConn = connService.connect(conn);
			jdbcConn.close();
		} catch (SQLException sqle) {
			LOGGER.error("", sqle); //$NON-NLS-1$
			String sqleMessage = sqle.getMessage();
			testLabel.setText(UIMessages.getString("editor.connection.17") //$NON-NLS-1$
					+ (null == sqleMessage ? "" : sqleMessage.replaceAll("\n", " ").replaceAll( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							"  ", " "))); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}

		testLabel.setText(UIMessages.getString("editor.connection.18")); //$NON-NLS-1$
		return true;
	}

	/**
	 * Processes pending display events so that we could assume that what we have updated on the
	 * display becomes visible to the user.
	 */
	private void refreshDisplay() {
		try {
			while (Display.getDefault().readAndDispatch()) {
			}
		} catch (RuntimeException e) {
			// Logging silently for debug, this is no big deal if we fail this refresh
			LOGGER.debug(e);
		}
	}

	@Override
	public boolean validate() {
		final IConnection conn = (IConnection) getModel();
		if (conn.isSsoAuthentication() && conn.getDBVendor() == DBVendor.MSSQL) {
			conn.setLogin(""); //$NON-NLS-1$
			conn.setPassword(""); //$NON-NLS-1$
			conn.setPasswordSaved(false);
		} else {
			conn.setSsoAuthentication(false);
		}
		if (!conn.isPasswordSaved()) {
			conn.setPassword(""); //$NON-NLS-1$
		}
		return super.validate();
	}

	@Override
	public void setPageComplete(boolean complete) {
		testButton.setEnabled(complete);
		super.setPageComplete(complete);
	}

}
