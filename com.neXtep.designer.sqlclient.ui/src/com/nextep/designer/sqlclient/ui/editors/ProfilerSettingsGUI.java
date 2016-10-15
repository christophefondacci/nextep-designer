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
package com.nextep.designer.sqlclient.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.gui.impl.editors.CheckBoxEditor;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.sqlclient.ui.model.IProfilerSettings;

public class ProfilerSettingsGUI extends ControlledDisplayConnector {
	Composite editor;
	FieldEditor threadEditor;
	FieldEditor durationEditor;
	FieldEditor stepDurationEditor;
	Button fetchingCheck;
	Button randomOrderCheck;

	public ProfilerSettingsGUI(IProfilerSettings settings) {
		super(settings, null);
	}
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2,false));
		
		threadEditor = new FieldEditor(editor, "Threads number : ", 1, 1, true, this, ChangeEvent.CUSTOM_1);
		durationEditor = new FieldEditor(editor, "Time in seconds : ", 1, 1, true, this, ChangeEvent.CUSTOM_2);
		stepDurationEditor = new FieldEditor(editor, "Spawn new threads every (s) : ", 1, 1, true, this, ChangeEvent.CUSTOM_3);
		new Label(editor,SWT.NONE);
		fetchingCheck = new Button(editor, SWT.CHECK);
		fetchingCheck.setText("Fetch lines");
		CheckBoxEditor.handle(fetchingCheck, ChangeEvent.CUSTOM_4, this);

		new Label(editor,SWT.NONE);
		randomOrderCheck = new Button(editor, SWT.CHECK);
		randomOrderCheck.setText("Execute queries in random order");
		CheckBoxEditor.handle(randomOrderCheck, ChangeEvent.CUSTOM_5, this);
		
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		IProfilerSettings settings = (IProfilerSettings)getModel();
		
		threadEditor.setText(String.valueOf(settings.getThreadCount()));
		durationEditor.setText(String.valueOf(settings.getDuration()));
		stepDurationEditor.setText(String.valueOf(settings.getThreadStepDuration()));
		fetchingCheck.setSelection(settings.isFetching());
		randomOrderCheck.setSelection(settings.isRandomOrder());
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IProfilerSettings settings = (IProfilerSettings)getModel();
		switch(event) {
		case CUSTOM_1:
			settings.setThreadCount(Integer.valueOf((String)data));
			break;
		case CUSTOM_2:
			settings.setDuration(Integer.valueOf((String)data));
			break;
		case CUSTOM_3:
			settings.setThreadStepDuration(Integer.valueOf((String)data));
			break;
		case CUSTOM_4:
			settings.setFetching((Boolean)data);
			break;
		case CUSTOM_5:
			settings.setRandomOrder((Boolean)data);
			break;
		}
		refreshConnector();
	}
}
