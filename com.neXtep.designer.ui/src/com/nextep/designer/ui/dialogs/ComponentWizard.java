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

import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;
import com.nextep.designer.ui.model.IValidatableUI;

/**
 * A specific wizard holding neXtep UI components as wizard pages.
 * 
 * @author Christophe Fondacci
 */
public class ComponentWizard extends Wizard {

	private final List<IUIComponent> components;
	private boolean isCanceled = false;

	public ComponentWizard(String title, List<IUIComponent> components) {
		setWindowTitle(title);
		for (IUIComponent c : components) {
			if (c instanceof IDisplayConnector) {
				addPage(new CheckedWizardPageWrapper((IDisplayConnector) c));
			} else if (c instanceof ITitleAreaComponent) {
				addPage(new WizardPageWrapper(c));
			}
		}
		this.components = components;
		setNeedsProgressMonitor(true);
	}

	@Override
	public boolean performFinish() {
		boolean validated = true;
		for (IWizardPage p : getPages()) {
			if (p instanceof IUIComponentContainer) {
				final IUIComponent component = ((IUIComponentContainer) p).getUIComponent();
				if (component instanceof IValidatableUI) {
					validated = validated && ((IValidatableUI) component).validate();
				}
			}
		}
		return validated;
	}

	@Override
	public boolean performCancel() {
		for (IWizardPage p : getPages()) {
			if (p instanceof IUIComponentContainer) {
				final IUIComponent component = ((IUIComponentContainer) p).getUIComponent();
				if (component instanceof IValidatableUI) {
					((IValidatableUI) component).cancel();
				}
			}
		}
		isCanceled = true;
		return true;
	}

	@Override
	public boolean canFinish() {
		final IWizardPage page = getContainer().getCurrentPage();
		if (page instanceof IUIComponentContainer) {
			final IUIComponent currentComponent = ((IUIComponentContainer) page).getUIComponent();
			return (components.indexOf(currentComponent) == components.size() - 1);
		}
		return false;
	}

	public boolean isCanceled() {
		return isCanceled;
	}
}
