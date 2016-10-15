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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.model.ColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

public class TextColumnEditor extends ColumnEditor {

	private IEventListener listener = null;
	private ChangeEvent event = null;
	protected Text textEditor = null;

	/**
	 * 
	 * @param editor
	 * @param columnIndex
	 * @param event
	 * @param listener
	 * 
	 */
	private TextColumnEditor(NextepTableEditor editor, int columnIndex, ChangeEvent event,IEventListener listener) {
		super(editor);
		this.event=event;
		this.listener=listener;
		editor.addColumnEditor(columnIndex, this);
	}
	public static TextColumnEditor handle(NextepTableEditor editor, int columnIndex, ChangeEvent event,IEventListener listener) {
		return new TextColumnEditor(editor,columnIndex,event,listener);
	}
	@Override
	public void publish() {
		listener.handleEvent(event, editedData, textEditor.getText());
	}
	public Control getEditor(Table parent, String editedString) {
		textEditor = new Text(parent,SWT.NONE);
		textEditor.setText(editedString);
		textEditor.selectAll();

		return textEditor;
	}

	@Override
	public void disposeEditor() {
		if(textEditor != null) {
			textEditor.dispose();
			textEditor=null;
		}
	}

}
