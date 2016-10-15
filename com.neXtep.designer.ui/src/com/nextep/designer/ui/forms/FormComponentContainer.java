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
package com.nextep.designer.ui.forms;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.forms.IManagedForm;
import com.nextep.designer.ui.model.IFormComponentContainer;
import com.nextep.designer.ui.model.IUIComponent;

/**
 * @author Christophe Fondacci
 */
public class FormComponentContainer implements IFormComponentContainer {

	private IManagedForm form;

	public FormComponentContainer(IManagedForm form) {
		this.form = form;
	}

	@Override
	public IManagedForm getForm() {
		return form;
	}

	@Override
	public IUIComponent getUIComponent() {
		return null;
	}

	@Override
	public void run(boolean block, boolean cancellable, IRunnableWithProgress runnable) {

	}

	@Override
	public void setErrorMessage(String message) {
	}

}
