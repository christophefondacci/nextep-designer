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
package com.nextep.datadesigner.vcs.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.vcs.model.IWorkspace;

/**
 * @author Christophe Fondacci
 */
public class VersionViewEditorGUI extends ControlledDisplayConnector {

	private Composite editor;
	private CLabel nameLabel = null;
	private Text nameText = null;
	private CLabel descLabel = null;
	private Text descText = null;
	private Label vendorLabel = null;
	private Combo vendorCombo = null;

	// private IVersionView view;

	public VersionViewEditorGUI(IWorkspace view) {
		super(view, null); // "Create a new view","View creation wizard",null);
		// this.view = view;
		// setMessage("This wizard allows you to create a new repository view of versioned content.\nDefine the view "
		// +
		// "attributes first and choose a view creation method which will set the initial contents of your new repository view.");
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createSWTControl(Composite parent) {

		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.verticalAlignment = GridData.CENTER;
		gridData3.horizontalSpan = 2;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.CENTER;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.verticalAlignment = GridData.CENTER;
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.verticalAlignment = GridData.CENTER;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.horizontalSpan = 2;
		GridData gridData22 = new GridData();
		gridData22.horizontalAlignment = GridData.FILL;
		gridData22.grabExcessHorizontalSpace = true;
		gridData22.verticalAlignment = GridData.CENTER;
		gridData22.horizontalSpan = 2;
		editor = new Composite(parent, SWT.NONE);
		addNoMarginLayout(editor, 3); // ,false);
		nameLabel = new CLabel(editor, SWT.RIGHT);
		nameLabel.setText("View name : ");
		nameLabel.setLayoutData(gridData1);
		nameText = new Text(editor, SWT.BORDER);
		nameText.setLayoutData(gridData);
		nameText.setTextLimit(30);
		ColorFocusListener.handle(nameText);
		TextEditor.handle(nameText, ChangeEvent.NAME_CHANGED, this);
		descLabel = new CLabel(editor, SWT.RIGHT);
		descLabel.setText("Description : ");
		descLabel.setLayoutData(gridData2);
		descText = new Text(editor, SWT.BORDER);
		descText.setLayoutData(gridData3);
		ColorFocusListener.handle(descText);
		TextEditor.handle(descText, ChangeEvent.DESCRIPTION_CHANGED, this);

		// Vendor combo
		vendorLabel = new Label(editor, SWT.NONE);
		vendorLabel.setText("Database vendor : ");
		vendorLabel.setLayoutData(gridData11);
		vendorCombo = new Combo(editor, SWT.READ_ONLY);
		vendorCombo.setLayoutData(gridData22);
		ComboEditor.handle(vendorCombo, ChangeEvent.DBVENDOR_CHANGED, this);
		for (DBVendor v : DBVendor.values()) {
			if (!v.isInternal()) {
				vendorCombo.add(v.toString());
				vendorCombo.setData(v.toString(), v);
			}
		}

		editor.addDisposeListener(this);
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getTitle()
	 */
	@Override
	public String getTitle() {
		return ((IWorkspace) getModel()).getName() + " View Editor";
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		IWorkspace view = (IWorkspace) getModel();
		nameText.setText(view.getName());
		descText.setText(view.getDescription() == null ? "" : view.getDescription());
		// Refreshing vendor
		int index = 0;
		for (DBVendor v : DBVendor.values()) {
			if (view.getDBVendor() == v) {
				vendorCombo.select(index);
				break;
			}
			index++;
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IWorkspace view = (IWorkspace) getModel();
		switch (event) {
		case NAME_CHANGED:
			view.setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			view.setDescription((String) data);
			break;
		case DBVENDOR_CHANGED:
			view.setDBVendor((DBVendor) vendorCombo.getData((String) data));
			break;
		}
		super.handleEvent(event, source, data);

	}

	// /**
	// * @see com.nextep.datadesigner.model.IModelOriented#getModel()
	// */
	// @Override
	// public Object getModel() {
	// return view;
	// }

	// /**
	// * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	// */
	// @Override
	// public boolean canFlipToNextPage() {
	// if(emptyRadio.getSelection()) {
	// return false;
	// } else if( importRadio.getSelection() ) {
	// return true;
	// } else if( rulesRadio.getSelection()) {
	// return true;
	// }
	// return super.canFlipToNextPage();
	// }

}
