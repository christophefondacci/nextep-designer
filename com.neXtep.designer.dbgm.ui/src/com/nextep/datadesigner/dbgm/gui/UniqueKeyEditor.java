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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.editors.ButtonEditor;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.controllers.UniqueKeyUIController;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class UniqueKeyEditor extends ControlledDisplayConnector implements SelectionListener {

	private Composite editor;
	private Text tableName;
	private FieldEditor nameEditor;
	private FieldEditor descEditor;
	private Button primaryCheck;
	private IDisplayConnector columnEditor;
	private Button downButton = null;
	private IDisplayConnector keyColumnEditor;
	private Button orderDownButton = null;
	private Button orderUpButton = null;
	private Button upButton = null;

	public UniqueKeyEditor(UniqueKeyConstraint c, ITypedObjectUIController controller) {
		super(c, controller);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2, false));

		// Creating name / desc controls
		nameEditor = new FieldEditor(
				editor,
				DBGMUIMessages.getString("uniqueKey.editor.name"), 1, 1, true, this, ChangeEvent.NAME_CHANGED); //$NON-NLS-1$
		nameEditor.getText().setTextLimit(60);
		descEditor = new FieldEditor(editor, DBGMUIMessages
				.getString("uniqueKey.editor.description"), 1, 1, true, this, //$NON-NLS-1$
				ChangeEvent.DESCRIPTION_CHANGED);
		descEditor.getText().setTextLimit(200);
		// Associated table
		Label tableLbl = new Label(editor, SWT.NONE);
		tableLbl.setText(DBGMUIMessages.getString("uniqueKey.editor.tableName")); //$NON-NLS-1$
		tableLbl.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		tableName = new Text(editor, SWT.BORDER); // new
		// FieldEditor(editor,"Table name : ",1,1,true,this,ChangeEvent.PARENT_CHANGED);
		tableName.setFont(FontFactory.FONT_BOLD);
		tableName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		tableName.setEditable(false);

		// Creating the primary check box
		new Label(editor, SWT.NONE);
		// primaryLbl.setText("Primary ? ");
		// primaryLbl.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,false));
		primaryCheck = new Button(editor, SWT.CHECK);
		primaryCheck.setText(DBGMUIMessages.getString("uniqueKey.editor.pk")); //$NON-NLS-1$
		ButtonEditor.handle(primaryCheck, ChangeEvent.FLAGGED_PRIMARY, this);

		createKeyColumnsEditor();
		select((IKeyConstraint) getModel());
		// returning main editor composite control
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		UniqueKeyConstraint c = (UniqueKeyConstraint) getModel();
		tableName.setText(c.getConstrainedTable() != null ? c.getConstrainedTable().getName()
				: DBGMUIMessages.getString("uniqueKey.editor.unresolved")); //$NON-NLS-1$
		nameEditor.setText(c.getName());
		descEditor.setText(notNull(c.getDescription()));
		primaryCheck.setSelection(c.getConstraintType() == ConstraintType.PRIMARY);

		columnEditor.refreshConnector();
		keyColumnEditor.refreshConnector();

		boolean e = !c.updatesLocked();
		nameEditor.getText().setEnabled(e);
		descEditor.getText().setEnabled(e);
		primaryCheck.setEnabled(e);
		orderDownButton.setEnabled(e);
		orderUpButton.setEnabled(e);
		upButton.setEnabled(e);
		downButton.setEnabled(e);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case FLAGGED_PRIMARY:
			((UniqueKeyUIController) getController()).togglePrimaryKey((IKeyConstraint) getModel());
			break;
		default:
			super.handleEvent(event, source, data);
		}
	}

	private void createKeyColumnsEditor() {
		Composite c = new Composite(editor, SWT.BORDER);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		c.setLayout(new GridLayout(4, false));
		c.setBackground(FontFactory.WHITE);
		Label infoLabel = new Label(c, SWT.WRAP);
		infoLabel.setBackground(FontFactory.WHITE);
		GridData labelData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		labelData.widthHint = 200;
		infoLabel.setLayoutData(labelData);
		infoLabel.setText(DBGMUIMessages.getString("uniqueKeyCreationInfo")); //$NON-NLS-1$
		createEligibleTables(c);
		createAddColumnToIndexButton(c);
		createKeyColumns(c);
		createOrderUpButton(c);
		createRemoveColumnFromIndexButton(c);
		createOrderDownButton(c);
	}

	private void addColumnToConstraint() {
		final IKeyConstraint constraint = (IKeyConstraint) getModel();
		if (constraint == null)
			return;
		if (constraint.updatesLocked()) {
			return;
		}
		// Retrieving column selection
		final IBasicColumn c = (IBasicColumn) columnEditor.getModel();
		if (c != null) {
			constraint.addColumn(c);
			columnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, c, null);
			keyColumnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, c, null);
			// Notifying column changes
			c.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	private void createAddColumnToIndexButton(Composite parent) {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.END;
		gridData5.grabExcessVerticalSpace = true;
		downButton = new Button(parent, SWT.NONE);
		downButton.setLayoutData(gridData5);
		downButton.setImage(ImageFactory.ICON_RIGHT_TINY);
		downButton.addSelectionListener(this);
	}

	/**
	 * Creates the eligible lists (table columns & foreign keys)
	 */
	private void createEligibleTables(Composite parent) {
		UniqueKeyConstraint uk = (UniqueKeyConstraint) getModel();
		// Adding columns displayer
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessVerticalSpace = true;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalSpan = 1;
		gridData3.verticalSpan = 2;
		gridData3.verticalAlignment = GridData.FILL;
		gridData3.minimumHeight = 100;

		columnEditor = new ColumnsDisplayConnector(uk.getConstrainedTable().getColumns(),
				DBGMUIMessages.getString("uniqueKey.editor.eligibleColumns")); //$NON-NLS-1$
		Control c = columnEditor.create(parent);
		c.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				addColumnToConstraint();
			}
		});
		c.setLayoutData(gridData3);
	}

	/**
	 * Creates the key columns table list
	 */
	private void createKeyColumns(Composite parent) {
		GridData gridData11 = new GridData();
		gridData11.verticalAlignment = GridData.FILL;
		gridData11.grabExcessHorizontalSpace = true;
		gridData11.verticalSpan = 2;
		gridData11.grabExcessVerticalSpace = true;
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.minimumHeight = 100;
		keyColumnEditor = new ColumnsDisplayConnector(null, DBGMUIMessages
				.getString("uniqueKey.editor.keyColumns")); //$NON-NLS-1$
		Control c = keyColumnEditor.create(parent);
		c.setLayoutData(gridData11);
	}

	private void createOrderDownButton(Composite parent) {
		GridData gridData41 = new GridData();
		gridData41.grabExcessVerticalSpace = true;
		gridData41.verticalAlignment = GridData.CENTER;
		orderDownButton = new Button(parent, SWT.LEFT | SWT.UP);
		orderDownButton.setText(DBGMUIMessages.getString("uniqueKey.editor.down")); //$NON-NLS-1$
		orderDownButton.setLayoutData(gridData41);
		orderDownButton.setImage(ImageFactory.ICON_DOWN_TINY);
		orderDownButton.addSelectionListener(this);
	}

	private void createOrderUpButton(Composite parent) {
		GridData gridData31 = new GridData();
		gridData31.grabExcessVerticalSpace = true;
		gridData31.verticalAlignment = GridData.CENTER;
		orderUpButton = new Button(parent, SWT.LEFT | SWT.BOTTOM);
		orderUpButton.setText(DBGMUIMessages.getString("uniqueKey.editor.up")); //$NON-NLS-1$
		orderUpButton.setLayoutData(gridData31);
		orderUpButton.setImage(ImageFactory.ICON_UP_TINY);
		orderUpButton.addSelectionListener(this);
	}

	private void createRemoveColumnFromIndexButton(Composite parent) {
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.verticalAlignment = GridData.BEGINNING;
		gridData6.grabExcessVerticalSpace = true;
		upButton = new Button(parent, SWT.NONE);
		upButton.setLayoutData(gridData6);
		upButton.setImage(ImageFactory.ICON_LEFT_TINY);
		upButton.addSelectionListener(this);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		IKeyConstraint constraint = (IKeyConstraint) getModel();
		if (e.getSource() == downButton) {
			addColumnToConstraint();
		} else if (e.getSource() == upButton) {
			// Retrieving column selection
			IBasicColumn c = (IBasicColumn) keyColumnEditor.getModel();
			if (c != null) {
				constraint.removeColumn(c);
				columnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, c, null);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, c, null);
				c.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			}
		} else if (e.getSource() == orderUpButton) {
			// Retrieving selection
			IBasicColumn selection = (IBasicColumn) keyColumnEditor.getModel();
			if (selection != null) {
				constraint.up(selection);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, selection, null);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, selection, constraint
						.getColumns().indexOf(selection));

			}
		} else if (e.getSource() == orderDownButton) {
			// Retrieving selection
			IBasicColumn selection = (IBasicColumn) keyColumnEditor.getModel();
			if (selection != null) {
				constraint.down(selection);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, selection, null);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, selection, constraint
						.getColumns().indexOf(selection));
			}
		}
	}

	private void select(IKeyConstraint c) {
		keyColumnEditor.handleEvent(ChangeEvent.SET_COLUMNS, null, c.getColumns());
		columnEditor.handleEvent(ChangeEvent.SET_COLUMNS, null, c.getConstrainedTable()
				.getColumns());
		// Removing already constrained columns from column editor
		for (IBasicColumn col : c.getColumns()) {
			columnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, col, null);
		}
	}

	@Override
	public Image getConnectorIcon() {
		return DBGMImages.WIZARD_KEY;
	}
}
