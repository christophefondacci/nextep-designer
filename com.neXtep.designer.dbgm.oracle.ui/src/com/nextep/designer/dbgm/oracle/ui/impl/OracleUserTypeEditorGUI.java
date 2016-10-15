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
package com.nextep.designer.dbgm.oracle.ui.impl;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.dbgm.gui.UserTypeEditorGUI;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class OracleUserTypeEditorGUI extends UserTypeEditorGUI {

	private Button hasBodyButton;
	public OracleUserTypeEditorGUI(IOracleUserType type, ITypedObjectUIController controller) {
		super(type,controller);
	}
	@Override
	protected Control createSWTControl(Composite parent) {
		final Composite editor = (Composite)super.createSWTControl(parent);
		
		hasBodyButton = new Button(editor,SWT.CHECK);
		hasBodyButton.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false,2,1));
		hasBodyButton.setText("This type has methods in a body");
		hasBodyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final IOracleUserType type = (IOracleUserType)getModel();
				if(hasBodyButton.getSelection()) {
					type.setTypeBody("TYPE BODY " + type.getName() + " IS\n\t\nEND;\n");
					type.notifyListeners(ChangeEvent.CUSTOM_1, null);
				} else {
					boolean confirmed = MessageDialog.openQuestion(editor.getShell(), DBOMUIMessages.getString("typeBodyWillBeDeletedTitle"), DBOMUIMessages.getString("typeBodyWillBeDeleted"));
					if(confirmed) {
						type.setTypeBody(null);
					} else {
						hasBodyButton.setSelection(true);
					}
				}
			}
		});
		return editor;
	}
	
	@Override
	public void refreshConnector() {
		super.refreshConnector();
		final IOracleUserType type = (IOracleUserType)getModel();
		hasBodyButton.setSelection( type.getTypeBody()!=null );
		hasBodyButton.setEnabled(!VersionHelper.getVersionable(type).getVersionnedObject().updatesLocked());
	}
	
}
