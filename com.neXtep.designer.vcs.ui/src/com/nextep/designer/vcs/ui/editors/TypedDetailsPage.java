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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.vcs.ui.editors;

import java.util.List;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.designer.ui.forms.FormComponentContainer;
import com.nextep.designer.ui.model.IUIChildComponent;
import com.nextep.designer.ui.model.IUIComponent;
import com.nextep.designer.ui.model.IUIComponentContainer;

/**
 * @author Christophe Fondacci
 */
public class TypedDetailsPage implements IDetailsPage {

	private List<IUIComponent> components;
	private ISelectionProvider masterSelectionProvider;
	private IManagedForm managedForm;

	public TypedDetailsPage(List<IUIComponent> components,
			ISelectionProvider masterSelectionProvider) {
		this.components = components;
		this.masterSelectionProvider = masterSelectionProvider;
	}

	@Override
	public void initialize(IManagedForm form) {
		this.managedForm = form;
	}

	@Override
	public void dispose() {
		for (IUIComponent component : components) {
			component.dispose();
		}
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void commit(boolean onSave) {

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean setFormInput(Object input) {
		for (IUIComponent component : components) {
			if (component instanceof IModelOriented<?>) {
				((IModelOriented) component).setModel(input);
			}
		}
		return false;
	}

	@Override
	public void setFocus() {

	}

	@Override
	public boolean isStale() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			final IStructuredSelection s = (IStructuredSelection) selection;
			setFormInput(s.getFirstElement());
		}
	}

	@Override
	public void createContents(Composite parent) {
		IUIComponentContainer container = new FormComponentContainer(managedForm);
		for (IUIComponent component : components) {
			component.setUIComponentContainer(container);
			if (component instanceof IUIChildComponent) {
				((IUIChildComponent) component).setSelectionProvider(masterSelectionProvider);
			}
			component.create(parent);
		}
	}

}
