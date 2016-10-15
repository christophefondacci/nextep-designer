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
package com.nextep.designer.dbgm.ui.dialogs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.designer.core.model.IConnection;
import com.nextep.designer.core.model.TargetType;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.vcs.VCSPlugin;

public class ConnectionSelector extends ControlledDisplayConnector {

	private Composite editor;
	private Combo connCombo;
	private IConnection selection = null;
	private String defaultConnection = null;

	public ConnectionSelector(TargetType type) {
		super(type, null);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2, false));
		Label connLabel = new Label(editor, SWT.RIGHT);
		connLabel.setText(DBGMUIMessages.getString("chooseConnectionLbl")); //$NON-NLS-1$
		connCombo = new Combo(editor, SWT.READ_ONLY);
		connCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		connCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				selection = (IConnection) connCombo.getData(connCombo.getText());
			}
		});
		// Initializing
		connCombo.removeAll();
		TargetType t = (TargetType) getModel();
		List<IConnection> connections = null;
		if (t == null) {
			connections = new ArrayList<IConnection>(VCSPlugin.getViewService()
					.getCurrentViewTargets().getConnections());
		} else {
			connections = new ArrayList<IConnection>(VCSPlugin.getViewService()
					.getCurrentViewTargets().getTarget(t));
		}
		Collections.sort(connections, NameComparator.getInstance());
		for (IConnection c : connections) {
			connCombo.add(c.getName());
			connCombo.setData(c.getName(), c);
			if (c.getName().equals(defaultConnection)) {
				selection = c;
			}
		}
		connCombo.select(0);
		if (selection == null) {
			selection = (IConnection) connCombo.getData(connCombo.getText());
		}
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	public void setDefaultConnection(String connectionName) {
		this.defaultConnection = connectionName;
	}

	@Override
	public void refreshConnector() {
		int i = 0;
		for (String s : connCombo.getItems()) {
			if (selection == connCombo.getData(s)) {
				connCombo.select(i);
				break;
			}
			i++;
		}

	}

	public IConnection getSelection() {
		return selection;
	}
}
