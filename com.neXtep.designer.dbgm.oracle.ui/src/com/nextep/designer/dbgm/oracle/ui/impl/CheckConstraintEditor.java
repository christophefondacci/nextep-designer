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
package com.nextep.designer.dbgm.oracle.ui.impl;



import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.model.ICheckConstraint;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * The editor of check constraints.
 * 
 * @author Christophe
 */
public class CheckConstraintEditor extends ControlledDisplayConnector {

	private FieldEditor nameField;
	private FieldEditor descField;
	private FieldEditor conditionField;
	private Composite editor;
	
	public CheckConstraintEditor(ICheckConstraint c, ITypedObjectUIController controller) {
		super(c,controller);
	}
	
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent,SWT.NONE);
		editor.setLayout(new GridLayout(2,false));
		
		nameField 		= new FieldEditor(editor,"Name : ",1,1,true,this,ChangeEvent.NAME_CHANGED);
		descField 		= new FieldEditor(editor,"Description : ",1,1,true,this,ChangeEvent.DESCRIPTION_CHANGED);
		conditionField	= new FieldEditor(editor,"Condition to verify : ",1,1,true,this,ChangeEvent.CUSTOM_12);
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		ICheckConstraint c = (ICheckConstraint)getModel();
		nameField.setText(notNull(c.getName()));
		descField.setText(notNull(c.getDescription()));
		conditionField.setText(notNull(c.getCondition()));
		final boolean e = !c.updatesLocked();
		nameField.getText().setEnabled(e);
		descField.getText().setEnabled(e);
		conditionField.getText().setEnabled(e);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		ICheckConstraint c = (ICheckConstraint)getModel();
		switch(event) {
		case CUSTOM_12:
			c.setCondition((String)data);
			break;
		}
		super.handleEvent(event, source, data);
		refreshConnector();
	}
}
