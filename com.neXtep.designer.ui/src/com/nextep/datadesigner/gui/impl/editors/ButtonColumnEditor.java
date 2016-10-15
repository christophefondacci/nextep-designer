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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.gui.model.ColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

public class ButtonColumnEditor extends ColumnEditor implements SelectionListener {

	private Button 			editor 			= null;
	private ChangeEvent 	triggeredEvent 	= null;
	private IEventListener 	listener 		= null;
	private boolean			permanent 		= false;
	private String 			text 			= null;
	private Image 			image 			= null;

	public ButtonColumnEditor(NextepTableEditor editor, int columnIndex, ChangeEvent triggeredEvent, IEventListener listener, Table table, Image image, String text, boolean permanent) {
		super(editor);
		this.triggeredEvent=triggeredEvent;
		this.listener=listener;
		this.permanent=permanent;
		this.text=text;
		this.image=image;
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
		if( editedString != null ) {
			editor.setSelection(true);
		}
		return editor;
	}
	/**
	 * Creates the editor control button
	 *
	 * @param table table in which the button will be created
	 */
	private void createEditor(Table table) {
		editor = new Button(table,SWT.PUSH);
		editor.addSelectionListener(this);
		if(text!=null) {
			editor.setText(text);
		}
		if(image != null ) {
			editor.setImage(image);
		}
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.ColumnEditor#publish()
	 */
	@Override
	public void publish() {
		// The activation is done by the selection listener rather than
		// by the standard "publish" action which will be triggered when
		// the control looses the focus
	}
	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		widgetSelected(arg0);

	}
	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent arg0) {
		listener.handleEvent(triggeredEvent, editedData, null);

	}

}
