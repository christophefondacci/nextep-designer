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
package com.nextep.datadesigner.sqlgen.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.WizardDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.model.ISQLScript;
import com.nextep.designer.sqlgen.ui.SQLGenImages;

/**
 * @author Christophe Fondacci
 */
public class SQLScriptWizardEditor extends WizardDisplayConnector {

	Composite editor = null;
	ISQLScript model = null;
	FieldEditor nameEditor = null;
	FieldEditor descEditor = null;

	/**
	 * 
	 */
	public SQLScriptWizardEditor(ISQLScript script) {
		super("SQL Script creation wizard...", "SQL Script creation wizard...", ImageDescriptor
				.createFromImage(SQLGenImages.WIZARD_SCRIPT));
		this.model = script;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2, false));
		nameEditor = new FieldEditor(editor, "Name : ", 1, 1, true, this, ChangeEvent.NAME_CHANGED);
		descEditor = new FieldEditor(editor, "Description : ", 1, 1, true, this,
				ChangeEvent.DESCRIPTION_CHANGED);
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.model.IModelOriented#getModel()
	 */
	@Override
	public Object getModel() {
		return model;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		nameEditor.setText(strVal(model.getName()));
		descEditor.setText(strVal(model.getDescription()));

		// Delegating to super class
		super.refreshConnector();
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.WizardDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case NAME_CHANGED:
			model.setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			model.setDescription((String) data);
			break;
		}
		// TODO Auto-generated method stub
		super.handleEvent(event, source, data);
	}
}
