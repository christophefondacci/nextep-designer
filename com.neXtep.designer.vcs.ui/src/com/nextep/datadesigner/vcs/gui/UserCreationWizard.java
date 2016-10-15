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

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.hibernate.HibernateUtil;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.vcs.impl.RepositoryUser;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.base.AbstractUIController;
import com.nextep.designer.vcs.model.IRepositoryUser;
import com.nextep.designer.vcs.ui.VCSImages;
import com.nextep.designer.vcs.ui.VCSUIMessages;
import com.nextep.designer.vcs.ui.jface.UsersContentProvider;
import com.nextep.designer.vcs.ui.jface.UsersLabelProvider;
import com.nextep.designer.vcs.ui.jface.UsersTable;

/**
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class UserCreationWizard extends WizardDisplayConnector implements SelectionListener {

	private static final Log LOGGER = LogFactory.getLog(UserCreationWizard.class);

	private Composite editor;
	private Button addUserButton;
	private Button editUserButton;
	private Button disableUserButton;
	private TableViewer viewer;

	public UserCreationWizard() {
		super("userManagement", VCSUIMessages.getString("userCreationWizardPage"), ImageDescriptor //$NON-NLS-1$ //$NON-NLS-2$
				.createFromImage(VCSImages.WIZ_USERS));
		setMessage(VCSUIMessages.getString("userCreationWizardPageDesc")); //$NON-NLS-1$
	}

	@Override
	public Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2, false));

		Table userTable = UsersTable.createTable(editor);
		viewer = new TableViewer(userTable);
		viewer.setLabelProvider(new UsersLabelProvider());
		viewer.setContentProvider(new UsersContentProvider(viewer));
		viewer.setSorter(new ViewerSorter());

		addUserButton = new Button(editor, SWT.PUSH);
		addUserButton.setText(VCSUIMessages.getString("userAdd")); //$NON-NLS-1$
		addUserButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		addUserButton.addSelectionListener(this);
		editUserButton = new Button(editor, SWT.PUSH);
		editUserButton.setText(VCSUIMessages.getString("userEdit")); //$NON-NLS-1$
		editUserButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		editUserButton.addSelectionListener(this);
		disableUserButton = new Button(editor, SWT.PUSH);
		disableUserButton.setText(VCSUIMessages.getString("userDisable")); //$NON-NLS-1$
		disableUserButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		disableUserButton.addSelectionListener(this);
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				try {
					editUser();
				} catch (CancelException e) {
					// It's ok
				}
			}
		});
		refreshUsersList();
		refreshConnector();

		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	public void refreshConnector() {
		super.refreshConnector();
		viewer.refresh();
		if (viewer.getInput() instanceof List<?>) {
			final List<?> users = (List<?>) viewer.getInput();
			if (users.size() < 2) {
				setErrorMessage(VCSUIMessages.getString("userNotCreatedError")); //$NON-NLS-1$
			} else {
				setErrorMessage(null);
				final IWizardContainer container = getContainer();
				if (container != null) {
					container.updateButtons();
				}
			}
		}
	}

	@Override
	public boolean isPageComplete() {
		if (viewer.getInput() == null) {
			refreshUsersList();
		}
		if (viewer.getInput() instanceof List<?>) {
			final List<?> users = (List<?>) viewer.getInput();
			return users.size() >= 2;
		}
		return false;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget == addUserButton) {
			UIControllerFactory.getController(IElementType.getInstance(IRepositoryUser.TYPE_ID))
					.newInstance(null);
			// Load everything
			refreshUsersList();
			refreshConnector();
		} else if (e.widget == editUserButton) {
			editUser();
			refreshConnector();
		} else if (e.widget == disableUserButton) {
			final IRepositoryUser user = getSelectedUser();
			if (user != null) {
				user.setEnabled(!user.isEnabled());
				viewer.refresh();
			}
		}

	}

	/**
	 * Edits the currently selected user
	 */
	private void editUser() {
		final IRepositoryUser user = getSelectedUser();
		if (user != null) {
			try {
				AbstractUIController.newWizardEdition(
						VCSUIMessages.getString("user.wizard.title"), UIControllerFactory //$NON-NLS-1$
								.getController(user).initializeEditor(user));
			} catch (CancelException e) {
				HibernateUtil.getInstance().getSession().refresh(user);
				throw e;
			}
			CorePlugin.getIdentifiableDao().save(user);
			viewer.refresh();
		}
	}

	/**
	 * Returns the currently selected user
	 * 
	 * @return the currently selected user or <code>null</code> if none
	 */
	private IRepositoryUser getSelectedUser() {
		ISelection s = viewer.getSelection();
		if (s instanceof IStructuredSelection && !s.isEmpty()) {
			final IRepositoryUser user = (IRepositoryUser) ((IStructuredSelection) s)
					.getFirstElement();
			return user;
		}
		return null;
	}

	@Override
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem()
				.displayHelp("com.neXtep.designer.vcs.ui.UserCreation"); //$NON-NLS-1$
	}

	private void refreshUsersList() {
		// Load everything
		List<? extends RepositoryUser> users = null;
		try {
			users = CorePlugin.getIdentifiableDao().loadAll(RepositoryUser.class);
		} catch (Exception e) {
			LOGGER.warn("Could not retrieve the list of repository users", e);
		}
		viewer.setInput(users);
	}

}
