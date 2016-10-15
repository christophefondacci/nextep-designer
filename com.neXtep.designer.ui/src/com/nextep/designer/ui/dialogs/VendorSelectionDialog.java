/*******************************************************************************
 * Copyright (c) 2013 neXtep Software and contributors.
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
 * along with neXtep.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.nextep.designer.core.model.DBVendor;
import com.nextep.designer.ui.UIImages;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.model.ISizedComponent;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.IValidatableUI;
import com.nextep.designer.ui.model.base.AbstractUIComponent;

/**
 * This dialog prompts for database vendor selection
 * 
 * @author Christophe Fondacci
 * 
 */
public class VendorSelectionDialog extends AbstractUIComponent implements ITitleAreaComponent,
		ISizedComponent, IValidatableUI {

	private DBVendor currentVendor;
	private Combo activityCombo;

	public VendorSelectionDialog(DBVendor defaultVendor) {
		this.currentVendor = defaultVendor;
	}

	@Override
	public Control create(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 300;
		gd.heightHint = 200;
		c.setLayoutData(gd);
		GridLayout layout = new GridLayout(2, false);
		c.setLayout(layout);
		Label activityLabel = new Label(c, SWT.NONE);
		activityLabel.setText(UIMessages.getString("editor.connection.vendor")); //$NON-NLS-1$
		activityCombo = new Combo(c, SWT.BORDER | SWT.READ_ONLY);
		activityCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		activityCombo.setText(currentVendor.toString());
		for (DBVendor vendor : DBVendor.values()) {
			if (vendor != currentVendor) {
				activityCombo.add(vendor.toString());
				activityCombo.setData(vendor.toString(), vendor);
			}
		}
		activityCombo.select(0);
		currentVendor = null;
		return c;
	}

	@Override
	public String getAreaTitle() {
		return UIMessages.getString("dialog.vendor.selection.title"); //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return UIMessages.getString("dialog.vendor.selection.desc"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return UIImages.WIZARD_GENERIC;
	}

	@Override
	public void dispose() {

	}

	public DBVendor getCurrentVendor() {
		return currentVendor;
	}

	@Override
	public int getWidth() {
		return 600;
	}

	@Override
	public int getHeight() {
		return 200;
	}

	@Override
	public void cancel() {
		currentVendor = null;
	}

	@Override
	public boolean validate() {
		currentVendor = (DBVendor) activityCombo.getData(activityCombo.getText());
		return true;
	}
}
