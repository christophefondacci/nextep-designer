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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;
import com.nextep.designer.ui.model.IValidatableUI;

/**
 * A small extension to the default wizard allowing to define whether we want to perform validation
 * when the next button is pressed.
 * 
 * @author Christophe Fondacci
 */
public class ComponentWizardDialog extends WizardDialog {

	private boolean validateOnNextPage;

	public ComponentWizardDialog(Shell parentShell, IWizard newWizard, boolean validateEachPage) {
		super(parentShell, newWizard);
		this.validateOnNextPage = validateEachPage;
	}

	@Override
	protected void nextPressed() {
		if (validateOnNextPage) {
			IWizardPage page = getCurrentPage();
			if (page instanceof IUIComponentContainer) {
				final IUIComponent component = ((IUIComponentContainer) page).getUIComponent();
				if (component instanceof IValidatableUI) {
					((IValidatableUI) component).validate();
				}
			}
		}
		super.nextPressed();
	}

}
