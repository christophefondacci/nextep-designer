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

import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;

/**
 * This wrapper can adapt a neXtep {@link IUIComponent} into a {@link IWizardPage}.
 * 
 * @author Christophe Fondacci
 */
public class WizardPageWrapper extends WizardPage implements IUIComponentContainer {

	private IUIComponent component;
	private Control control;

	public WizardPageWrapper(IUIComponent component) {
		super(component.toString());
		this.component = component;
		if (component instanceof ITitleAreaComponent) {
			final ITitleAreaComponent titled = (ITitleAreaComponent) component;
			setTitle(titled.getAreaTitle());
			setMessage(titled.getDescription());
			if (titled.getImage() != null) {
				setImageDescriptor(ImageDescriptor.createFromImage(titled.getImage()));
			}
		} else if (component instanceof IDisplayConnector) {
			// Compatibility for pre-1.0.5 connectors
			final IDisplayConnector connector = (IDisplayConnector) component;
			setTitle(connector.getTitle());
			if (connector.getConnectorIcon() != null) {
				setImageDescriptor(ImageDescriptor.createFromImage(connector.getConnectorIcon()));
			}
		}
		component.setUIComponentContainer(this);
	}

	@Override
	public void createControl(Composite parent) {
		// create the top level composite for the dialog area
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		control = component.create(composite);
		control.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(composite);
	}

	@Override
	public IUIComponent getUIComponent() {
		return component;
	}

	@Override
	public void run(boolean block, boolean cancelable, IRunnableWithProgress runnable) {

		try {
			getContainer().run(!block, cancelable, runnable);
		} catch (InvocationTargetException e) {
			throw new ErrorException(e);
		} catch (InterruptedException e) {
			throw new ErrorException(e);
		}
	}
}
