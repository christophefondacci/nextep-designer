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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.gui.model.IDisplayConnector;

public class DialogGUIWrapper extends Dialog {

	private IDisplayConnector gui;
	private String title;
	private IDialogValidator validator;
	private Object result;

	public DialogGUIWrapper(IDisplayConnector gui, String title, IDialogValidator validator) {
		this(null,gui,title,validator);
	}
	
	public DialogGUIWrapper(Shell parentShell, IDisplayConnector gui, String title, IDialogValidator validator) {
		super(parentShell == null ? getRootShell() : parentShell);
		this.gui = gui;
		this.title = title;
		this.validator = validator;
		setShellStyle(SWT.CLOSE | SWT.RESIZE | SWT.MIN | SWT.MAX);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		gui.create(parent);
		gui.initialize();
		gui.refreshConnector();

		return gui.getSWTConnector();
	}

	private static Shell getRootShell() {
		if (PlatformUI.getWorkbench() != null
				&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		} else {
			return Display.getCurrent().getActiveShell();
		}
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			result = validator.getSelection(gui);
		} else {
			result = null;
		}
		super.buttonPressed(buttonId);
	}

	public Object getResult() {
		return result;
	}
}
