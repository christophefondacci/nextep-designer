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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.designer.ui.dialogs.CheckedWizardPageWrapper;
import com.nextep.designer.ui.model.IValidatableUI;

/**
 * This class adapts a collection of IDisplayConnector
 * into a RCP paged wizard.
 *
 * @author Christophe Fondacci
 *
 */
public class DefaultWizard extends Wizard {

	public DefaultWizard(String title, IDisplayConnector... pages) {
		super();
		setWindowTitle(title);
		for(IDisplayConnector c : pages) {
			if(c instanceof WizardDisplayConnector) {
				addPage((WizardDisplayConnector)c);
			} else {
				addPage(new CheckedWizardPageWrapper(c));
			}
		}
	}
	/**
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if(getContainer() != null) {
			final IWizardPage page = getContainer().getCurrentPage();
			if(page instanceof IValidatableUI) {
				((IValidatableUI) page).validate();
			}
		}
		return true;
	}
	
	@Override
	public boolean performCancel() {
//		final IWizardPage page = getContainer().getCurrentPage();
//		if(page instanceof IValidatedPage) {
//			
//		}
		return super.performCancel();
	}

}
