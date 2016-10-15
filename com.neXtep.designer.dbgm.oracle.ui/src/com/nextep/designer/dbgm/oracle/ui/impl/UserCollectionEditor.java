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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IDatatypeProvider;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.impl.swt.CheckboxEditor;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.model.CollectionType;
import com.nextep.designer.dbgm.model.IUserCollection;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.model.IVersionable;

/**
 * @author Christophe Fondacci
 */
public class UserCollectionEditor extends ControlledDisplayConnector {

	private Composite editor;
	private FieldEditor name;
	private FieldEditor desc;
	private FieldEditor size;
	private CheckboxEditor varrayType;
	private CheckboxEditor tableType;
	private Combo contentTypeCombo;
	private FieldEditor length;
	private FieldEditor precision;

	public UserCollectionEditor(IUserCollection collection, ITypedObjectUIController controller) {
		super(collection, controller);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2, false));
		name = new FieldEditor(editor, "Name : ", 1, 1, true, this, ChangeEvent.NAME_CHANGED);
		desc = new FieldEditor(editor, "Description : ", 1, 1, true, this,
				ChangeEvent.DESCRIPTION_CHANGED);
		// Collection type editor
		Composite collType = new Composite(editor, SWT.BORDER);

		collType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		collType.setLayout(new GridLayout(4, false));
		Label collTypeLabel = new Label(collType, SWT.NONE);
		collTypeLabel.setText("Collection type : ");
		collTypeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		tableType = new CheckboxEditor(collType, "Nested table", 4, true, this,
				ChangeEvent.CUSTOM_1, SWT.CHECK);
		varrayType = new CheckboxEditor(collType, "VARRAY", 1, false, this, ChangeEvent.CUSTOM_2,
				SWT.CHECK);
		size = new FieldEditor(collType, "Size : ", 1, 1, false, this, ChangeEvent.CUSTOM_3);
		new Label(collType, SWT.NONE);
		// Content type editor
		Label contentLabel = new Label(collType, SWT.NONE);
		contentLabel.setText("Collection of : ");
		GridData d = new GridData();
		d.horizontalSpan = 2;
		contentLabel.setLayoutData(d);
		Label lengthLabel = new Label(collType, SWT.NONE);
		lengthLabel.setText("Length : ");
		Label precisionLabel = new Label(collType, SWT.NONE);
		precisionLabel.setText("Precision : ");
		contentTypeCombo = new Combo(collType, SWT.NONE);
		GridData comboData = new GridData();
		comboData.horizontalSpan = 2;
		contentTypeCombo.setLayoutData(comboData);
		// Filling allowed content
		List<String> datatypes = new ArrayList<String>();
		Collection<IVersionable<?>> usrTypes = VersionHelper.getAllVersionables(
				VersionHelper.getCurrentView(), IElementType.getInstance(IUserType.TYPE_ID));
		for (IVersionable<?> v : usrTypes) {
			datatypes.add(v.getName());
		}
		IDatatypeProvider provider = DBGMHelper.getDatatypeProvider(DBGMHelper.getCurrentVendor());
		for (String typeName : provider.listSupportedDatatypes()) {
			datatypes.add(typeName);
		}
		Collections.sort(datatypes);
		for (String type : datatypes) {
			contentTypeCombo.add(type);
			contentTypeCombo.setData(type, type);
		}
		ComboEditor.handle(contentTypeCombo, ChangeEvent.CUSTOM_4, this);

		length = new FieldEditor(collType, 1, false, this, ChangeEvent.COLUMN_LENGTH_CHANGED);
		precision = new FieldEditor(collType, 1, false, this, ChangeEvent.COLUMN_PRECISION_CHANGED);

		// Returning editor control
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		IUserCollection col = (IUserCollection) getModel();
		name.setText(col.getName());
		desc.setText(col.getDescription());
		switch (col.getCollectionType()) {
		case NESTED_TABLE:
			tableType.setSelection(true);
			varrayType.setSelection(false);
			size.getText().setEnabled(false);
			size.setText("");
			break;
		case VARRAY:
			tableType.setSelection(false);
			varrayType.setSelection(true);
			size.getText().setEnabled(true);
			size.setText(String.valueOf(col.getSize()));
			break;
		}

		if (col.getDatatype() != null) {
			contentTypeCombo.setText(col.getDatatype().getName());
			if (col.getDatatype().getLength() != -1) {
				length.setText(strVal(col.getDatatype().getLength()));
			} else {
				length.setText("");
			}
			if (col.getDatatype().getPrecision() != -1) {
				precision.setText(strVal(col.getDatatype().getPrecision()));
			} else {
				precision.setText("");
			}
		}
		// Handling enablement
		boolean enabled = !col.updatesLocked();
		name.getText().setEnabled(enabled);
		desc.getText().setEnabled(enabled);
		if (!enabled)
			size.getText().setEnabled(enabled);
		contentTypeCombo.setEnabled(enabled);
		length.getText().setEnabled(enabled);
		precision.getText().setEnabled(enabled);
		tableType.getControl().setEnabled(enabled);
		varrayType.getControl().setEnabled(enabled);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IUserCollection coll = (IUserCollection) getModel();
		switch (event) {
		case NAME_CHANGED:
			coll.setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			coll.setDescription((String) data);
			break;
		case CUSTOM_1:
			coll.setCollectionType(CollectionType.NESTED_TABLE);
			break;
		case CUSTOM_2:
			coll.setCollectionType(CollectionType.VARRAY);
			break;
		case CUSTOM_3:
			try {
				coll.setSize(Integer.valueOf((String) data));
			} catch (NumberFormatException e) {
				coll.setSize(0);
			}
			break;
		case CUSTOM_4:
			IDatatype dtype = new Datatype(coll.getDatatype());
			dtype.setName((String) data);
			coll.setDatatype(dtype);
			break;
		case COLUMN_LENGTH_CHANGED:
			IDatatype d = new Datatype(coll.getDatatype());
			try {
				d.setLength(Integer.valueOf((String) data));
			} catch (NumberFormatException e) {
				d.setLength(-1);
			}
			coll.setDatatype(d);
			break;
		case COLUMN_PRECISION_CHANGED:
			IDatatype d2 = new Datatype(coll.getDatatype());
			try {
				d2.setPrecision(Integer.valueOf((String) data));
			} catch (NumberFormatException e) {
				d2.setPrecision(-1);
			}
			coll.setDatatype(d2);
			break;
		default:
			refreshConnector();
		}
	}

	@Override
	public Image getConnectorIcon() {
		return DBGMImages.WIZARD_TYPE;
	}

}
