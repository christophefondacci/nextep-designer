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
package com.nextep.datadesigner.vcs.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.gui.impl.editors.ClickColumnEditor;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.CoreUiPlugin;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.model.UserRight;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.jface.UsersTable;

public class UserEditorGUI extends WizardDisplayConnector {

	private Composite editor;
	private FieldEditor loginEditor, nameEditor, passwordEditor, confirmPassEditor, descEditor;
	private Table userRights;
	private IRepositoryUser user;

	public UserEditorGUI(IRepositoryUser user) {
		super("userEditor", VCSUIMessages.getString("userEditorWizardTitle"), null); //$NON-NLS-1$ //$NON-NLS-2$
		setDescription(VCSUIMessages.getString("userEditorWizardDesc")); //$NON-NLS-1$
		this.user = user;
	}

	@Override
	public Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2, false));

		loginEditor = new FieldEditor(editor,
				VCSUIMessages.getString("user.editor.login"), 1, 1, true, this, //$NON-NLS-1$
				ChangeEvent.LOGIN_CHANGED);
		nameEditor = new FieldEditor(editor,
				VCSUIMessages.getString("user.editor.username"), 1, 1, true, this, //$NON-NLS-1$
				ChangeEvent.NAME_CHANGED);
		passwordEditor = new FieldEditor(editor,
				VCSUIMessages.getString("user.editor.password"), 1, 1, true, this, //$NON-NLS-1$
				ChangeEvent.PASSWORD_CHANGED, true);
		confirmPassEditor = new FieldEditor(editor, VCSUIMessages
				.getString("user.editor.passwordConfirm"), 1, 1, true, this, //$NON-NLS-1$
				ChangeEvent.CUSTOM_10, true);
		descEditor = new FieldEditor(editor,
				VCSUIMessages.getString("user.editor.comments"), 1, 1, true, this, //$NON-NLS-1$
				ChangeEvent.DESCRIPTION_CHANGED);

		Label rightLbl = new Label(editor, SWT.NONE);
		rightLbl.setText(VCSUIMessages.getString("user.editor.rights")); //$NON-NLS-1$
		rightLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		userRights = UsersTable.createUserRights(editor);

		NextepTableEditor tabEditor = NextepTableEditor.handle(userRights);
		ClickColumnEditor.handle(tabEditor, 1, ChangeEvent.CUSTOM_1, this);

		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		IRepositoryUser user = (IRepositoryUser) getModel();

		loginEditor.setText(notNull(user.getLogin()));
		nameEditor.setText(notNull(user.getName()));
		passwordEditor.setText(notNull(user.getSecuredPassword()));
		confirmPassEditor.setText(notNull(user.getPasswordBis()));
		descEditor.setText(notNull(user.getDescription()));
		userRights.removeAll();
		for (UserRight r : UserRight.values()) {
			TableItem i = new TableItem(userRights, SWT.NONE);
			i.setText(r.name());
			i.setData(r);
			i.setText(1, user.getUserRights().contains(r) ? "X" : ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IRepositoryUser user = (IRepositoryUser) getModel();
		switch (event) {
		case LOGIN_CHANGED:
			user.setLogin((String) data);
			break;
		case NAME_CHANGED:
			user.setName((String) data);
			break;
		case PASSWORD_CHANGED:
			// Encrypting password
			final String encryptedPassword = CoreUiPlugin.getRepositoryUIService().encryptPassword(
					((String) data).toUpperCase());
			// For backward compatibility and security enforcement, we erase password
			user.setPassword(null);
			user.setSecuredPassword(encryptedPassword);
			break;
		case CUSTOM_10:
			// Encrypting password validation so they can match
			final String encryptedPasswordBis = CoreUiPlugin.getRepositoryUIService()
					.encryptPassword(((String) data).toUpperCase());
			// For backward compatibility and security enforcement, we erase password
			user.setPassword(null);
			user.setPasswordBis(encryptedPasswordBis);
			break;
		case CUSTOM_1:
			final UserRight right = (UserRight) data;
			if (user.getUserRights().contains(right)) {
				user.removeUserRight(right);
			} else {
				user.addUserRight(right);
			}
			break;
		}
		super.handleEvent(event, source, data);
	}

	@Override
	public Object getModel() {
		return user;
	}

	@Override
	public boolean validate() {
		CorePlugin.getPersistenceAccessor().save((IRepositoryUser) getModel());
		return true;
	}
}
