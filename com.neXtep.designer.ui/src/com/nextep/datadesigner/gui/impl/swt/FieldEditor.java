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

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

/**
 * @author Christophe Fondacci
 *
 */
public class FieldEditor {

	private Text nameText;
	private Composite parent;
	private IEventListener eventReceiver;
	private ChangeEvent firedEvent;
	private boolean textGrab; 
	private String label;
	private int labelSpan;
	private int textSpan;
	private boolean password = false;
	public FieldEditor(Composite parent, String label, int labelSpan, int textSpan, boolean textGrab, IEventListener eventReceiver, ChangeEvent firedEvent) {
		this(parent,label,labelSpan,textSpan,textGrab,eventReceiver,firedEvent,false);
	}
	public FieldEditor(Composite parent, String label, int labelSpan, int textSpan, boolean textGrab, IEventListener eventReceiver, ChangeEvent firedEvent, boolean password) {
		this.parent = parent;
		this.firedEvent = firedEvent;
		this.textGrab = textGrab;
		this.label = label;
		this.labelSpan = labelSpan;
		this.textSpan = textSpan;
		this.eventReceiver = eventReceiver;
		this.password = password;
		create();
	}
	public FieldEditor(Composite parent, int textSpan, boolean textGrab, IEventListener eventReceiver, ChangeEvent firedEvent) {
		this(parent,null,0,textSpan,textGrab,eventReceiver,firedEvent);
	}
	private void create() {
		if(label!=null) {
			GridData labelData = new GridData();
			labelData.horizontalAlignment = GridData.FILL;
			labelData.horizontalSpan = labelSpan;
			labelData.verticalAlignment = GridData.CENTER;
			
			CLabel nameLabel = new CLabel(parent, SWT.RIGHT);
			nameLabel.setText(label);
			nameLabel.setLayoutData(labelData);
		}
		
		GridData textData = new GridData();
		textData.horizontalAlignment = GridData.FILL;
		textData.grabExcessHorizontalSpace = textGrab;
		textData.horizontalSpan = textSpan;
		textData.verticalAlignment = GridData.CENTER;
		if(!password) {
			nameText = new Text(parent, SWT.BORDER);
		} else {
			nameText = new Text(parent, SWT.BORDER | SWT.PASSWORD);
		}
		nameText.setLayoutData(textData);
		
		// Adding control listeners
		ColorFocusListener.handle(nameText);
		TextEditor.handle(nameText, firedEvent, eventReceiver);
	}
	
	public void setText(String text) {
		if(nameText != null && !nameText.isDisposed()) {
			nameText.setText(text == null ? "" : text);
		}
	}
	public Text getText() {
		return nameText;
	}
	
}
