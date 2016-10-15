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
package com.nextep.designer.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import com.nextep.designer.ui.UIImages;
import com.nextep.designer.ui.UIMessages;

/**
 * @author Christophe Fondacci
 */
public class ConnectionPasswordDialog extends Dialog {

	private String label;
	private String title;
	private int style;
	private Text text;
	private Button rememberButton;
	private String value;
	private boolean shouldRemember = false;

	public ConnectionPasswordDialog(Shell parent, String title, String label, int style) {
		super(parent);
		this.title = title;
		this.label = label;
		this.style = style;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite c = (Composite) super.createDialogArea(parent);
		c.setLayout(new GridLayout(2, false));
		final Label textLabel = new Label(c, SWT.NONE);
		textLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		textLabel.setText(label);
		final Label imgLabel = new Label(c, SWT.NONE);
		imgLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		imgLabel.setImage(UIImages.ICON_SECURITY);
		text = new Text(c, style);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		rememberButton = new Button(c, SWT.CHECK);
		rememberButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		rememberButton.setText(UIMessages.getString("editor.connection.password.remember")); //$NON-NLS-1$
		return c;
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			value = text.getText();
			shouldRemember = rememberButton.getSelection();
		} else {
			value = null;
			shouldRemember = false;
		}
		super.buttonPressed(buttonId);
	}

	public String getValue() {
		return value;
	}

	public boolean shouldRemember() {
		return shouldRemember;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

}
