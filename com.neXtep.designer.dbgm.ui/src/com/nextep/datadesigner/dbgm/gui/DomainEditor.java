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
package com.nextep.datadesigner.dbgm.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.dbgm.model.IDomain;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 *
 * @author Christophe Fondacci
 */
public class DomainEditor extends ControlledDisplayConnector {
	private Composite editor;
	private FieldEditor nameEditor;
	private FieldEditor descEditor;
	public DomainEditor(IDomain domain, ITypedObjectUIController controller) {
		super(domain,controller);
	}
	
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent,SWT.NONE);
		editor.setLayout(new GridLayout(2,false));
		
		nameEditor = new FieldEditor(editor,"Name : ",1,1,true,this,ChangeEvent.NAME_CHANGED);
		descEditor = new FieldEditor(editor,"Description : ",1,1,true,this,ChangeEvent.DESCRIPTION_CHANGED);
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		final IDomain domain = (IDomain)getModel();
		nameEditor.setText(notNull(domain.getName()));
		descEditor.setText(notNull(domain.getDescription()));
	}

}
