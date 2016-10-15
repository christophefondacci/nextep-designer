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
package com.nextep.datadesigner.gui.impl.swt;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import com.nextep.datadesigner.gui.impl.editors.CheckBoxEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

/**
 * @author Christophe Fondacci
 *
 */
public class CheckboxEditor {
	private Button checkBox;
	private Composite parent;
	private IEventListener eventReceiver;
	private ChangeEvent firedEvent;
	private boolean grab; 
	private String label;
	private int span;
	private int style;
	public CheckboxEditor(Composite parent, String label, int span, boolean grab, IEventListener eventReceiver, ChangeEvent firedEvent, int style) {
		this.parent = parent;
		this.firedEvent = firedEvent;
		this.grab = grab;
		this.label = label;
		this.span = span;
		this.eventReceiver = eventReceiver;
		this.style = style;
		create();
	}
	
	private void create() {
		
		GridData textData = new GridData();
		textData.horizontalAlignment = GridData.FILL;
		textData.grabExcessHorizontalSpace = grab;
		textData.horizontalSpan = span;
		textData.verticalAlignment = GridData.CENTER;
		checkBox = new Button(parent, style);
		checkBox.setText(label);
		checkBox.setLayoutData(textData);
		
		// Adding control listeners
		CheckBoxEditor.handle(checkBox, firedEvent, eventReceiver);
	}
	
	public void setText(String text) {
		checkBox.setText(text == null ? "" : text);
	}
	public void setSelection(boolean selected) {
		checkBox.setSelection(selected);
	}
	public Button getControl() {
		return checkBox;
	}
}
