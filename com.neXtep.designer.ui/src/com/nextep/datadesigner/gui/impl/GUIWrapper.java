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
package com.nextep.datadesigner.gui.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.gui.model.IDesignerGUI;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.InvokableController;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.ui.factories.ImageFactory;

/**
 * This small wrapper allows to display any <code>IDisplayConnector</code> connector as a dialog
 * box.
 * 
 * @author Christophe Fondacci
 */
public class GUIWrapper extends InvokableController implements IDesignerGUI, SelectionListener,
		DisposeListener {

	private static final Log log = LogFactory.getLog(GUIWrapper.class);
	private IDisplayConnector connector;
	private String title;
	private Shell sShell;
	private Composite contents;
	private Composite buttonsGroup;
	private Button okButton;
	private Button cancelButton;
	private int initialWidth;
	private int initialHeight;
	private boolean isOk = false;
	private boolean cancelled = false;
	private Image icon;
	private boolean showCancel = true;
	private boolean showOk = true;
	private boolean pack = false;

	public GUIWrapper(IDisplayConnector connector, String title, int width, int height) {
		this.connector = connector;
		this.title = title;
		this.initialWidth = width;
		this.initialHeight = height;
	}

	public GUIWrapper(IDisplayConnector connector, String title, int width, int height, Image icon) {
		this(connector, title, width, height);
		this.icon = icon;
	}

	public GUIWrapper(Shell rootShell, IDisplayConnector connector, String title, int width,
			int height) {
		this.connector = connector;
		this.title = title;
		this.initialWidth = width;
		this.initialHeight = height;
		setRootShell(rootShell);
	}

	public void hideCancel() {
		showCancel = false;
	}

	public void hideOk() {
		showOk = false;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#getDisplay()
	 */
	public Display getDisplay() {
		return sShell.getDisplay();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#getShell()
	 */
	public Shell getShell() {
		return sShell;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDesignerGUI#initializeGUI(org.eclipse.swt.widgets.Shell)
	 */
	public void initializeGUI(Shell parentGUI) {

		sShell = new Shell(parentGUI, SWT.APPLICATION_MODAL | SWT.SHELL_TRIM);
		sShell.setText(title);
		sShell.setImage(ImageFactory.ICON_DESIGNER_TINY);
		sShell.setLayout(new GridLayout());
		if (initialHeight != -1 && initialWidth != -1) {
			sShell.setSize(initialWidth, initialHeight);
		}
		sShell.addDisposeListener(this);
		if (icon != null) {
			sShell.setImage(icon);
		}
		GridData data = new GridData();
		data.grabExcessVerticalSpace = true;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		contents = new Composite(sShell, SWT.NONE);
		contents.setLayoutData(data);
		contents.setLayout(new GridLayout());
		sShell.layout();
		Control c = connector.create(contents);
		GridData data20 = new GridData();
		data20.grabExcessVerticalSpace = true;
		data20.grabExcessHorizontalSpace = true;
		data20.horizontalAlignment = GridData.FILL;
		data20.verticalAlignment = GridData.FILL;
		c.setLayoutData(data20);

		GridData data1 = new GridData();
		data1.grabExcessHorizontalSpace = true;
		data1.horizontalAlignment = GridData.FILL;
		data1.verticalAlignment = GridData.FILL;
		buttonsGroup = new Composite(sShell, SWT.NONE);
		buttonsGroup.setLayoutData(data1);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		// layout.makeColumnsEqualWidth = true;
		buttonsGroup.setLayout(layout);

		GridData data2 = new GridData();
		data2.grabExcessHorizontalSpace = true;
		data2.horizontalAlignment = GridData.END;
		if (showCancel) {
			cancelButton = new Button(buttonsGroup, SWT.PUSH);
			cancelButton.setText("Cancel");
			cancelButton.setLayoutData(data2);
			cancelButton.addSelectionListener(this);
		}
		if (showOk) {
			okButton = new Button(buttonsGroup, SWT.PUSH);
			okButton.setText("    OK    ");
			okButton.addSelectionListener(this);
			sShell.setDefaultButton(okButton);
		} else {
			isOk = true;
		}
		sShell.layout();
		if (pack) {
			sShell.pack();
		}
		connector.refreshConnector();
		if (initialHeight == -1 || initialWidth == -1) {
			sShell.pack();
		}

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.InvokableController#invoke(java.lang.Object)
	 */
	@Override
	public Object invoke(Object... model) {
		invokeGUI(this);
		return isCancelled();
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		widgetSelected(arg0);
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent arg0) {
		if (arg0.getSource() == cancelButton) {
			// sShell.dispose();
			try {
				cancel();
			} catch (CancelException e) {
				sShell.dispose();
				throw e;
			}
		} else if (arg0.getSource() == okButton) {
			connector.handleEvent(ChangeEvent.VALIDATE, null, null);
			isOk = true;
			sShell.dispose();
		}
	}

	/**
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(DisposeEvent arg0) {
		if (!isOk) {
			try {
				cancel();
			} catch (CancelException e) {
				log.debug(e);
			}
		}
	}

	private void cancel() {
		cancelled = true;
		isOk = true;
		throw new CancelException("Operation cancelled by user.");
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setPack(boolean pack) {
		this.pack = pack;
	}
}
