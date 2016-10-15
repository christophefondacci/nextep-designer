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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.designer.ui.dialogs.CheckedWizardPageWrapper;
import com.nextep.designer.vcs.ui.VCSUIMessages;

/**
 * @author Christophe Fondacci
 */
public class VersionViewWizardPage extends CheckedWizardPageWrapper {

	private VersionViewEditorGUI editorGUI;
	private Label viewTypeLabel = null;
	private Button emptyRadio = null;
	private Button importRadio = null;
	private Button rulesRadio = null;
	private Button deliveryRadio = null;
	private Label infoLabel = null;

	/**
	 * 
	 */
	public VersionViewWizardPage(VersionViewEditorGUI viewEditor) {
		super(viewEditor);
		this.editorGUI = viewEditor;
		setMessage(VCSUIMessages.getString("newViewWizardDesc"));
	}

	/**
	 * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
	 */
	@Override
	public boolean canFlipToNextPage() {
		if (getCreationType() == 1) {
			return false;
		} else if (getCreationType() == 2) {
			return isPageComplete();
		} else if (getCreationType() == 3) {
			return isPageComplete();
		}
		return super.canFlipToNextPage();
	}

	/**
	 * @see com.nextep.designer.ui.dialogs.CheckedWizardPageWrapper#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite editor = (Composite) editorGUI.getSWTConnector();
		// Type of view creation
		GridData gridData30 = new GridData();
		gridData30.horizontalSpan = 2;
		gridData30.verticalAlignment = GridData.CENTER;
		gridData30.horizontalAlignment = GridData.FILL;
		gridData30.horizontalSpan = 3;
		GridData gridData20 = new GridData();
		gridData20.horizontalSpan = 2;
		gridData20.verticalAlignment = GridData.CENTER;
		gridData20.horizontalAlignment = GridData.FILL;
		GridData gridData10 = new GridData();
		gridData10.horizontalSpan = 2;
		gridData10.verticalAlignment = GridData.CENTER;
		gridData10.horizontalAlignment = GridData.FILL;
		GridData gridData0 = new GridData();
		gridData0.horizontalSpan = 2;
		gridData0.verticalAlignment = GridData.BEGINNING;
		gridData0.grabExcessVerticalSpace = true;
		GridData gridData40 = new GridData();
		gridData40.horizontalSpan = 2;
		gridData40.verticalAlignment = GridData.BEGINNING;
		gridData40.grabExcessVerticalSpace = true;
		// gridData0.horizontalAlignment = GridData.FILL;
		viewTypeLabel = new Label(editor, SWT.NONE);
		viewTypeLabel.setText(VCSUIMessages.getString("newViewWizardCreationType"));
		viewTypeLabel.setLayoutData(gridData30);
		emptyRadio = new Button(editor, SWT.RADIO);
		emptyRadio.setText(VCSUIMessages.getString("newViewWizardCreationTypeEmpty"));
		emptyRadio.setLayoutData(gridData20);

		// Creating the info label
		GridData gridData220 = new GridData();
		gridData220.horizontalAlignment = GridData.FILL;
		gridData220.grabExcessHorizontalSpace = true;
		gridData220.verticalAlignment = GridData.FILL;
		gridData220.grabExcessVerticalSpace = true;
		gridData220.verticalSpan = 5;
		gridData220.widthHint = 300;
		infoLabel = new Label(editor, SWT.WRAP | SWT.BORDER); // _SOLID);
		infoLabel.setBackground(FontFactory.LIGHT_YELLOW);
		infoLabel.setLayoutData(gridData220);
		infoLabel.setText(VCSUIMessages.getString("viewCreationEmpty"));
		emptyRadio.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				VersionViewWizardPage.this.getContainer().updateButtons();
				infoLabel.setText(VCSUIMessages.getString("viewCreationEmpty"));
			}
		});
		emptyRadio.setSelection(true);
		importRadio = new Button(editor, SWT.RADIO);
		importRadio.setText(VCSUIMessages.getString("newViewWizardCreationTypeReverse"));
		importRadio.setLayoutData(gridData10);
		importRadio.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				VersionViewWizardPage.this.getContainer().updateButtons();
				infoLabel.setText(VCSUIMessages.getString("viewCreationImport"));
			}
		});
		rulesRadio = new Button(editor, SWT.RADIO);
		rulesRadio.setText(VCSUIMessages.getString("newViewWizardCreationTypeRules"));
		rulesRadio.setLayoutData(gridData0);
		rulesRadio.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				VersionViewWizardPage.this.getContainer().updateButtons();
				infoLabel.setText(VCSUIMessages.getString("viewCreationRules"));
			}
		});
		deliveryRadio = new Button(editor, SWT.RADIO);
		deliveryRadio.setText(VCSUIMessages.getString("newViewWizardCreationTypeDelivery"));
		deliveryRadio.setLayoutData(gridData40);
		deliveryRadio.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				VersionViewWizardPage.this.getContainer().updateButtons();
				infoLabel.setText(VCSUIMessages.getString("viewCreationDelivery"));
			}
		});
	}

	public int getCreationType() {
		if (emptyRadio.getSelection()) {
			return 1;
		} else if (importRadio.getSelection()) {
			return 2;
		} else if (rulesRadio.getSelection()) {
			return 3;
		} else if (deliveryRadio.getSelection()) {
			return 4;
		}
		return 1;
	}

}
