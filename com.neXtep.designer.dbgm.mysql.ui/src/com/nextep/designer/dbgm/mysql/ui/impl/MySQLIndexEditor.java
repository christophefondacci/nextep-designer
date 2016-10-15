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
package com.nextep.designer.dbgm.mysql.ui.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.dbgm.gui.IndexEditorGUI;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.external.VersionedTableEditor;
import com.nextep.designer.dbgm.mysql.model.IMySQLIndex;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class MySQLIndexEditor extends IndexEditorGUI {

	private TableColumn prefixLengthCol;
	private Table colsTable;

	public MySQLIndexEditor(IMySQLIndex index, ITypedObjectUIController controller) {
		super(index, controller);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		Control ctrl = super.createSWTControl(parent);

		// Retrieving index columns SWT table
		final IDisplayConnector colConnector = getIndexColumnsEditor();
		colsTable = (Table) colConnector.getSWTConnector();

		// And adding a column for function definition
		prefixLengthCol = new TableColumn(colsTable, SWT.NONE);
		prefixLengthCol.setWidth(200);
		prefixLengthCol.setText("Prefix length");
		NextepTableEditor editor = VersionedTableEditor.handle(colsTable, getModel());
		TextColumnEditor.handle(editor, colsTable.indexOf(prefixLengthCol), ChangeEvent.CUSTOM_4, this);
		editor.setVersionedParent(getModel());
		return ctrl;
	}

	@Override
	public void refreshConnector() {
		super.refreshConnector();
		final IMySQLIndex index = (IMySQLIndex) getModel();
		// colsTable.setEnabled(!index.updatesLocked());
		for (TableItem i : colsTable.getItems()) {
			if (i.getData() instanceof IBasicColumn) {
				final Integer prefixLength = index.getColumnPrefixLength(((IBasicColumn) i
						.getData()).getReference());
				if (prefixLength == null) {
					i.setText(colsTable.indexOf(prefixLengthCol), "");
				} else {
					i.setText(colsTable.indexOf(prefixLengthCol), notNull(prefixLength.toString()));
				}
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		final IMySQLIndex index = (IMySQLIndex) getModel();
		switch (event) {
		case CUSTOM_4:
			final IBasicColumn c = (IBasicColumn) source;
			Integer prefixLength = null;
			try {
				if (data != null && !"".equals(data)) {
					prefixLength = Integer.valueOf((String) data);
				}
			} catch (NumberFormatException e) {
				throw new ErrorException("Prefix length must be an integer", e);
			}
			index.setColumnPrefixLength(c.getReference(), prefixLength);
			break;
		}
		super.handleEvent(event, source, data);
	}

}
