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
package com.nextep.designer.dbgm.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.nextep.designer.dbgm.ui.editors.DomainEditorComponent;

/**
 * @author Christophe Fondacci
 */
public class DomainPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private final DomainEditorComponent domainEditorComponent;

	public DomainPreferencePage() {
		super();
		domainEditorComponent = new DomainEditorComponent();
		setTitle(domainEditorComponent.getAreaTitle());
		setDescription(domainEditorComponent.getDescription());

	}

	@Override
	protected Control createContents(Composite parent) {
		return domainEditorComponent.create(parent);
	}

	@Override
	public void init(IWorkbench workbench) {

	}

}
