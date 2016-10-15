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
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class UserTypeEditorGUI extends ControlledDisplayConnector {

	/** The main SWT container */
	private Composite editor;
	/** Name management */
	private FieldEditor name;
	/** Description management */
	private FieldEditor description;
	/** ITypeColumn inner editor */
	private IDisplayConnector columnsConnector;

	public UserTypeEditorGUI(IUserType type, ITypedObjectUIController controller) {
		super(type, controller);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		// Creating main container
		editor = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout();
		grid.numColumns = 2;
		editor.setLayout(grid);
		// Creating name & desc
		name = new FieldEditor(editor, "Name : ", 1, 1, true, this, ChangeEvent.NAME_CHANGED);
		description = new FieldEditor(editor, "Description : ", 1, 1, true, this,
				ChangeEvent.DESCRIPTION_CHANGED);
		// Creating columns inner editir
		columnsConnector = UIControllerFactory.getController(
				IElementType.getInstance(ITypeColumn.TYPE_ID)).initializeEditor(getModel());
		columnsConnector.create(editor);
		// Returning container control
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
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		IUserType type = (IUserType) getModel();
		// Refreshing name & desc
		name.setText(notNull(type.getName()));
		description.setText(notNull(type.getDescription()));
		// Refreshing columns
		columnsConnector.refreshConnector();
		// Enablement
		boolean enabled = !type.updatesLocked();
		name.getText().setEnabled(enabled);
		description.getText().setEnabled(enabled);

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		super.handleEvent(event, source, data);
		switch (event) {
		case COLUMN_ADDED:
		case COLUMN_REMOVED:
			refreshConnector();
		}
	}

	@Override
	public Image getConnectorIcon() {
		return DBGMImages.WIZARD_TYPE;
	}
}
