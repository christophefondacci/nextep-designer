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



import org.eclipse.swt.widgets.Composite;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class TablePartitionEditor extends PartitionEditor {

	private FieldEditor highValEditor;
	
	public TablePartitionEditor(ITablePartition p, ITypedObjectUIController controller) {
		super(p,controller);
	}
	@Override
	protected void createSWTBeforePhysicals(Composite editor) {
		highValEditor = new FieldEditor(editor,"Partition value : ",1,1,true,this,ChangeEvent.CUSTOM_1);
	}
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		ITablePartition p = (ITablePartition)getModel();
		switch(event) {
		case CUSTOM_1:
			p.setHighValue((String)data);
			break;
		}
		// super class event handling
		super.handleEvent(event, source, data);
	}

	@Override
	public void refreshConnector() {
		super.refreshConnector();
		ITablePartition part = (ITablePartition)getModel();
		highValEditor.setText(part.getHighValue());
		//Enablement
		final boolean b = !part.updatesLocked();
		highValEditor.getText().setEnabled(b);
	}
}
