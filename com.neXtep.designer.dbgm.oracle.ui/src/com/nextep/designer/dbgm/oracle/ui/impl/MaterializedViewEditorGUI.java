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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.dbgm.gui.TableEditorGUI;
import com.nextep.datadesigner.gui.impl.editors.ButtonEditor;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.oracle.model.BuildType;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.MaterializedViewType;
import com.nextep.designer.dbgm.oracle.model.RefreshMethod;
import com.nextep.designer.dbgm.oracle.model.RefreshTime;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class MaterializedViewEditorGUI extends TableEditorGUI {

//	private Composite editor;
//	private FieldEditor nameEditor;
//	private FieldEditor descEditor;
	private Combo timeCombo;
	private Combo methodCombo;
	private Combo typeCombo;
	private FieldEditor startEditor;
	private FieldEditor nextEditor;
	private Combo buildCombo;
	private Button queryButton;
	public MaterializedViewEditorGUI(IMaterializedView view, ITypedObjectUIController controller) {
		super(view,controller);
	}
	@Override
	protected Composite createPropertiesGroup(Composite parent) {
		Composite editor = super.createPropertiesGroup(parent);
		Label timeLbl = new Label(editor,SWT.NONE);
		timeLbl.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
		timeLbl.setText("Refresh on : ");
		timeCombo = new Combo(editor,SWT.READ_ONLY);
		for(RefreshTime t : RefreshTime.values()) {
			timeCombo.add(t.name());
			timeCombo.setData(t.name(),t);
		}
		ComboEditor.handle(timeCombo, ChangeEvent.CUSTOM_5, this);
		
		Label methodLbl = new Label(editor,SWT.NONE);
		methodLbl.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
		methodLbl.setText("Refresh method : ");
		methodCombo = new Combo(editor,SWT.READ_ONLY);
		for(RefreshMethod m : RefreshMethod.values()) {
			methodCombo.add(m.name());
			methodCombo.setData(m.name(),m);
		}
		ComboEditor.handle(methodCombo,ChangeEvent.CUSTOM_6, this);
		
		Label typeLbl = new Label(editor,SWT.NONE);
		typeLbl.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
		typeLbl.setText("View type : ");
		typeCombo = new Combo(editor,SWT.READ_ONLY);
		for(MaterializedViewType t : MaterializedViewType.values()) {
			typeCombo.add(t.name());
			typeCombo.setData(t.name(),t);
		}
		ComboEditor.handle(typeCombo, ChangeEvent.CUSTOM_8, this);
		
		startEditor = new FieldEditor(editor, "SQL start expression : ", 1, 1, true, this, ChangeEvent.CUSTOM_9);
		nextEditor = new FieldEditor(editor,"SQL next expression : ", 1, 1, true, this, ChangeEvent.CUSTOM_10);

		Label buildLbl = new Label(editor, SWT.NONE);
		buildLbl.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
		buildLbl.setText("Build type : ");
		buildCombo = new Combo(editor,SWT.READ_ONLY);
		for(BuildType t : BuildType.values()) {
			buildCombo.add(t.name());
			buildCombo.setData(t.name(),t);
		}
		ComboEditor.handle(buildCombo, ChangeEvent.CUSTOM_11, this);
		
		Label queryLbl = new Label(editor,SWT.NONE);
		queryLbl.setText("Query rewrite : ");
		queryLbl.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
		queryButton = new Button(editor,SWT.CHECK);
		queryButton.setText("Enabled");
		ButtonEditor.handle(queryButton, ChangeEvent.CUSTOM_12, this);
		return editor;
	}
//	@Override
//	protected Control createSWTControl(Composite parent) {
//		editor = new Composite(parent,SWT.NONE);
//		editor.setLayout(new GridLayout(2,false));
//		
//		nameEditor = new FieldEditor(editor,"Name : ", 1, 1, true, this, ChangeEvent.NAME_CHANGED);
//		descEditor = new FieldEditor(editor,"Description : ", 1, 1, true, this, ChangeEvent.DESCRIPTION_CHANGED);
//	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IMaterializedView view = (IMaterializedView)getModel();
		switch(event) {
		case CUSTOM_5:
			view.setRefreshTime(RefreshTime.valueOf((String)data));
			break;
		case CUSTOM_6:
			view.setRefreshMethod(RefreshMethod.valueOf((String)data));
			break;
		case CUSTOM_8:
			view.setViewType(MaterializedViewType.valueOf((String)data));
			break;
		case CUSTOM_9:
			view.setStartExpr((String)data);
			break;
		case CUSTOM_10:
			view.setNextExpr((String)data);
			break;
		case CUSTOM_11:
			view.setBuildType(BuildType.valueOf((String)data));
			break;
		case CUSTOM_12:
			view.setQueryRewriteEnabled(queryButton.getSelection());
			break;
		default:
			super.handleEvent(event, source, data);
		}
		
	}
//	@Override
//	public Control getSWTConnector() {
//		return editor;
//	}

	@Override
	public void refreshConnector() {
		IMaterializedView view = (IMaterializedView)getModel(); 
		super.refreshConnector();
//		nameEditor.setText(view.getName());
//		descEditor.setText(notNull(view.getDescription()));
		
		int i = 0;
		if(view.getRefreshTime()!=null) {
			for(String s : timeCombo.getItems()) {
				if(view.getRefreshTime().name().equals(s)) {
					timeCombo.select(i);
				}
				i++;
			}
		} else {
			timeCombo.deselectAll();
		}
		
		i=0;
		if(view.getRefreshMethod()!=null) {
			for(String s : methodCombo.getItems()) {
				if(view.getRefreshMethod().name().equals(s)) {
					methodCombo.select(i);
				}
				i++;
			}
		} else {
			methodCombo.deselectAll();
		}
		
		i=0;
		if(view.getViewType()!=null) {
			for(String s : typeCombo.getItems()) {
				if(view.getViewType().name().equals(s)) {
					typeCombo.select(i);
				}
				i++;
			}
		} else {
			typeCombo.deselectAll();
		}
		startEditor.setText(notNull(view.getStartExpr()));
		nextEditor.setText(notNull(view.getNextExpr()));
		// Start and next times are enabled only on SPECIFIY refresh time setting
		boolean customTimeEnabled = (view.getRefreshTime()==RefreshTime.SPECIFY);
		startEditor.getText().setEnabled(customTimeEnabled);
		nextEditor.getText().setEnabled(customTimeEnabled);
		
		i=0;
		if(view.getBuildType()!=null) {
			for(String s : buildCombo.getItems()) {
				if(view.getBuildType().name().equals(s)) {
					buildCombo.select(i);
				}
				i++;
			}
		} else {
			buildCombo.deselectAll();
		}
		
		queryButton.setSelection(view.isQueryRewriteEnabled());
		boolean e = !view.updatesLocked();
//		nameEditor.getText().setEnabled(e);
//		descEditor.getText().setEnabled(e);
		timeCombo.setEnabled(e);
		methodCombo.setEnabled(e);
		typeCombo.setEnabled(e);
		if(!e) {
			startEditor.getText().setEnabled(e);
			nextEditor.getText().setEnabled(e);
		}
		buildCombo.setEnabled(e);
		queryButton.setEnabled(e);
	}

}
