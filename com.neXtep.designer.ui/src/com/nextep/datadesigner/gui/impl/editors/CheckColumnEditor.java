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
package com.nextep.datadesigner.gui.impl.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.gui.model.ColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

public class CheckColumnEditor extends ColumnEditor {

	private Button editor = null;
	private ChangeEvent triggeredEvent = null;
	private IEventListener listener = null;
	private boolean permanent = false;
	private boolean initial;
	public CheckColumnEditor(NextepTableEditor editor, int columnIndex, ChangeEvent triggeredEvent, IEventListener listener, Table table, boolean permanent, boolean initial) {
		super(editor);
		this.triggeredEvent=triggeredEvent;
		this.listener=listener;
		this.permanent=permanent;
		this.initial=initial;
		if(permanent) {
			createEditor(table);
			setDisposeOnFocusOut(false);
		}
		editor.addColumnEditor(columnIndex, this);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.ColumnEditor#disposeEditor()
	 */
	@Override
	public void disposeEditor() {
		editor.dispose();
		editor=null;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ColumnEditor#getEditor(org.eclipse.swt.widgets.Table, java.lang.String)
	 */
	@Override
	public Control getEditor(Table table, String editedString) {
		if(!permanent) {
			this.createEditor(table);
		}
//		if( editedString != null ) {
//			editor.setSelection(true);
//		}
		return editor;
	}
	/**
	 * Creates the editor control button
	 *
	 * @param table table in which the button will be created
	 */
	private void createEditor(Table table) {
		editor = new Button(table,SWT.CHECK);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.CENTER;
		gridData.verticalAlignment = GridData.CENTER;
		editor.setLayoutData(gridData);
		editor.setSelection(initial);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.ColumnEditor#publish()
	 */
	@Override
	public void publish() {
		listener.handleEvent(triggeredEvent, editedData, new Boolean(editor.getSelection()));
	}

}
