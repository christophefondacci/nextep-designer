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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.editors.CheckBoxEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.oracle.model.IMaterializedViewLog;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.dbgm.ui.editors.PhysicalPropertiesEditor;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class MaterializedViewLogEditor extends ControlledDisplayConnector {

	private Composite editor;
	private Button pkButton;
	private Button rowidButton;
	private Button sequenceButton;
	private Button newValuesButton;
	private Text tableText;
	private IDisplayConnector propsConn;
	public MaterializedViewLogEditor(IMaterializedViewLog log, ITypedObjectUIController controller) {
		super(log,controller);
	}
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent,SWT.NONE);
		editor.setLayout(new GridLayout(2,false));
		
		Label tabLbl = new Label(editor,SWT.NONE);
		tabLbl.setText("Related table : ");
		tableText = new Text(editor,SWT.BORDER);
		tableText.setFont(FontFactory.FONT_BOLD);
		tableText.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
		
		Label info = new Label(editor, SWT.NONE);
		info.setText(DBOMUIMessages.getString("materializedViewLogContent"));
		info.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false,2,1));

		new Label(editor,SWT.NONE);
		pkButton = new Button(editor,SWT.CHECK);
		pkButton.setText("Primary key");
		CheckBoxEditor.handle(pkButton, ChangeEvent.CUSTOM_1, this);
		
		new Label(editor,SWT.NONE);
		rowidButton = new Button(editor,SWT.CHECK);
		rowidButton.setText("Row ID");
		CheckBoxEditor.handle(rowidButton, ChangeEvent.CUSTOM_2, this);
		
		new Label(editor,SWT.NONE);
		sequenceButton = new Button(editor,SWT.CHECK);
		sequenceButton.setText("Sequence ordering");
		CheckBoxEditor.handle(sequenceButton, ChangeEvent.CUSTOM_3, this);
		
		new Label(editor,SWT.NONE);
		newValuesButton = new Button(editor,SWT.CHECK);
		newValuesButton.setText("New values");
		CheckBoxEditor.handle(newValuesButton, ChangeEvent.CUSTOM_4, this);

		
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		final IMaterializedViewLog log = (IMaterializedViewLog)getModel();
		
		IBasicTable t = (IBasicTable)VersionHelper.getReferencedItem(log.getTableReference());
		tableText.setText(t.getName());
		
		pkButton.setSelection(log.isPrimaryKey());
		rowidButton.setSelection(log.isRowId());
		sequenceButton.setSelection(log.isSequence());
		newValuesButton.setSelection(log.isIncludingNewValues());
		
		if(propsConn==null && log.getPhysicalProperties()!=null) {
			propsConn = UIControllerFactory.getController(log.getPhysicalProperties()).initializeEditor(log.getPhysicalProperties());
			((PhysicalPropertiesEditor)propsConn).setShowParent(false);
			Control c = propsConn.create(editor);
			c.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true,2,1));
			editor.layout();	
		}
		
		if(propsConn!=null) {
			propsConn.refreshConnector();
		}
		final boolean e = !log.updatesLocked();
		pkButton.setEnabled(e);
		rowidButton.setEnabled(e);
		sequenceButton.setEnabled(e);
		newValuesButton.setEnabled(e);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		final IMaterializedViewLog log = (IMaterializedViewLog)getModel();
		switch(event) {
		case CUSTOM_1:
			log.setPrimaryKey(pkButton.getSelection());
			break;
		case CUSTOM_2:
			log.setRowId(rowidButton.getSelection());
			break;
		case CUSTOM_3:
			log.setSequence(sequenceButton.getSelection());
			break;
		case CUSTOM_4:
			log.setIncludingNewValues(newValuesButton.getSelection());
			break;
		case GENERIC_CHILD_ADDED:
			refreshConnector();
			break;
		case GENERIC_CHILD_REMOVED:
			propsConn.getSWTConnector().dispose();
			break; 
		default:
			super.handleEvent(event, source, data);
		}
	}
	@Override
	public String getTitle() {
		return ((IBasicTable)VersionHelper.getReferencedItem(((IMaterializedViewLog)getModel()).getTableReference())).getName();
	}
	
}
