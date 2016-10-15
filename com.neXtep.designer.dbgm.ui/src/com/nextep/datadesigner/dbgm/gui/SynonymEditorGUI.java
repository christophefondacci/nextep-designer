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
package com.nextep.datadesigner.dbgm.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.dbgm.impl.Synonym;
import com.nextep.datadesigner.dbgm.model.ISynonym;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * An editor to display and change the properties of a {@link Synonym}.
 * 
 * @author Bruno Gautier
 */
public class SynonymEditorGUI extends ControlledDisplayConnector {

	private Composite synEditor = null;
	private FieldEditor nameField = null;
	private FieldEditor descField = null;
	private FieldEditor refDbObjNameField = null;
	private FieldEditor refDbObjSchemaNameField = null;

	/**
	 * Creates a new instance of <code>SynonymEditorGUI</code> for the specified
	 * <code>ISynonym</code> object.
	 * 
	 * @param synonym the <code>Synonym</code> object to display in this editor.
	 * @param controller the controller associated with this typed object.
	 */
	public SynonymEditorGUI(ISynonym synonym, ITypedObjectUIController controller) {
		super(synonym, controller);
	}

	@Override
	public ISynonym getModel() {
		return (ISynonym) super.getModel();
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		synEditor = new Composite(parent, SWT.NONE);
		synEditor.setLayout(new GridLayout(2, false));

		// We define a specific hook for the Oracle implementation to be able to declare controls at
		// the top of the editor.
		setTopControls(synEditor);

		nameField = new FieldEditor(synEditor, DBGMUIMessages.getString("synonym.editor.name"), 1,
				1, true, this, ChangeEvent.NAME_CHANGED);
		// We explicitly set the focus on the synonym name field since it's potentially not the
		// first field in the editor.
		nameField.getText().setFocus();

		descField = new FieldEditor(synEditor, DBGMUIMessages
				.getString("synonym.editor.description"), 1, 1, true, this,
				ChangeEvent.DESCRIPTION_CHANGED);
		refDbObjNameField = new FieldEditor(synEditor, DBGMUIMessages
				.getString("synonym.editor.refDbObjName"), 1, 1, true, this, ChangeEvent.CUSTOM_1);
		refDbObjSchemaNameField = new FieldEditor(synEditor, DBGMUIMessages
				.getString("synonym.editor.refDbObjSchemaName"), 1, 1, true, this,
				ChangeEvent.CUSTOM_2);

		return synEditor;
	}

	protected void setTopControls(Composite editor) {
		// Does nothing by default.
	}

	@Override
	public Control getSWTConnector() {
		return synEditor;
	}

	@Override
	public void refreshConnector() {
		// Setting fields values according to model values.
		ISynonym synonym = getModel();
		nameField.setText(notNull(synonym.getName()));
		descField.setText(notNull(synonym.getDescription()));
		refDbObjNameField.setText(notNull(synonym.getRefDbObjName()));
		refDbObjSchemaNameField.setText(notNull(synonym.getRefDbObjSchemaName()));

		// Enabling/Disabling fields edition according to the synonym lock status.
		boolean isCheckedOut = !synonym.updatesLocked();
		nameField.getText().setEnabled(isCheckedOut);
		descField.getText().setEnabled(isCheckedOut);
		refDbObjNameField.getText().setEnabled(isCheckedOut);
		refDbObjSchemaNameField.getText().setEnabled(isCheckedOut);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		ISynonym synonym = getModel();
		switch (event) {
		case CUSTOM_1:
			synonym.setRefDbObjName((String) data);
			break;
		case CUSTOM_2:
			synonym.setRefDbObjSchemaName((String) data);
			break;
		}

		/*
		 * For the CUSTOM_1 event, corresponding to a change of the referenced object name, the
		 * refreshConnector() method will be called by the default statement of the switch statement
		 * in the ControlledDisplayConnector#handleEvent() method of the superclass.
		 */
		super.handleEvent(event, source, data);
	}

	@Override
	public Image getConnectorIcon() {
		return DBGMImages.WIZARD_SYNONYM;
	}
}
