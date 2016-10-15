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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;


public abstract class PartitionEditor extends ControlledDisplayConnector {
	private Composite editor;
	private FieldEditor nameEditor;
	private IDisplayConnector physicalsEditor;
	public PartitionEditor(IPartition p, ITypedObjectUIController controller) {
		super(p,controller);
	}
	@Override
	protected Control createSWTControl(Composite parent) {
		// Creating composite container
		editor = new Composite(parent,SWT.NONE);
		editor.setLayout(new GridLayout(2,false));
		// Name edition
		nameEditor = new FieldEditor(editor,"Name : ",1,1,true,this,ChangeEvent.NAME_CHANGED);
		// Sub-class additions
		createSWTBeforePhysicals(editor);
		// Physical editor
		final IPartition p = (IPartition)getModel();
		physicalsEditor = UIControllerFactory.getController(IElementType.getInstance(IPartitionPhysicalProperties.TYPE_ID)).initializeEditor(p.getPhysicalProperties());
		
		Control c = physicalsEditor.create(editor);
		c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));
		return editor;
	}
	/**
	 * Extend to add controls before the physical properties editor
	 * @param editor
	 */
	protected void createSWTBeforePhysicals(Composite editor) {}
	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		IPartition part = (IPartition)getModel();
		nameEditor.setText(notNull(part.getName()));
		physicalsEditor.refreshConnector();
		//Enablement
		final boolean b = !part.updatesLocked();
		nameEditor.getText().setEnabled(b);
	}

	@Override
	public void setModel(Object model) {
		super.setModel(model);
		if(physicalsEditor==null) return;
		// updating physicals model 
		if(model != null) {
			physicalsEditor.setModel(((IPartition)model).getPhysicalProperties());
		} else {
			physicalsEditor.setModel(null);
		}
	}
}
