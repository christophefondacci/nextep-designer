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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.postgre.ui.editors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlInheritedTable;
import com.nextep.designer.dbgm.postgre.model.IPostgreSqlTable;
import com.nextep.designer.dbgm.postgre.ui.PostgreUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * The editor for table foreign keys.
 * 
 * @author Christophe Fondacci
 */
public class InheritedTablesFormEditor extends AbstractFormEditor<IPostgreSqlInheritedTable> {

	private static final Log log = LogFactory.getLog(InheritedTablesFormEditor.class);

	private Text remoteTableText;
	private Button changeRemoteTableButton;

	public InheritedTablesFormEditor() {
		super(
				PostgreUIMessages.getString("editor.postgre.inheritedDetails"), PostgreUIMessages.getString("editor.postgre.inheritedDetailsDesc"), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void createControls(final IManagedForm managedForm, FormToolkit toolkit,
			Composite editor) {
		editor.setLayout(new GridLayout(3, false));

		// Creating the remote table editor
		final Link remoteTableLink = new Link(editor, SWT.NONE);
		remoteTableLink.setText(PostgreUIMessages.getString("editor.postgre.inheritedTableLabel")); //$NON-NLS-1$
		remoteTableLink.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IPostgreSqlInheritedTable inheritedTable = getModel();
				if (inheritedTable != null && inheritedTable.getTable() != null) {
					final ITypedObjectUIController controller = UIControllerFactory
							.getController(IElementType.getInstance(IBasicTable.TYPE_ID));
					controller.defaultOpen(inheritedTable.getTable());
				}
			}
		});
		remoteTableText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		remoteTableText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		remoteTableText.setEditable(false);
		changeRemoteTableButton = toolkit.createButton(editor, "", SWT.PUSH); //$NON-NLS-1$
		changeRemoteTableButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeRemoteTableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changeRemoteTable(managedForm.getForm().getShell());
			}
		});

	}

	/**
	 * Prompts the user for a new remote table and defines this selection in the
	 * current foreign key.
	 */
	private void changeRemoteTable(Shell shell) {
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final IBasicTable remoteTable = (IBasicTable) uiService.findElement(shell,
				PostgreUIMessages.getString("editor.postgre.changeInheritedTable"), //$NON-NLS-1$
				IElementType.getInstance(IBasicTable.TYPE_ID));
		if (remoteTable != null) {
			final IPostgreSqlInheritedTable inheritedTable = getModel();
			final IPostgreSqlTable parentTable = inheritedTable.getParent();
			final IPostgreSqlTable currentTable = inheritedTable.getTable();

			currentTable.removeInheritance(parentTable);
			currentTable.addInheritance(remoteTable);
		}
	}

	@Override
	protected void doBindModel(DataBindingContext context) {

	}

	@Override
	protected void doRefresh() {
		final IBasicTable table = getModel().getParent();
		if (table != null) {
			remoteTableText.setText(table.getName());
		} else {
			remoteTableText.setText(""); //$NON-NLS-1$
		}
		remoteTableText.setEditable(false);
	}

}
