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
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.dbgm.gui.IndexEditorGUI;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.external.VersionedTableEditor;
import com.nextep.designer.dbgm.oracle.model.IOracleIndex;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class OracleIndexEditor extends IndexEditorGUI {

	private TableColumn funcCol;
	private Table colsTable;

	public OracleIndexEditor(IOracleIndex index, ITypedObjectUIController controller) {
		super(index, controller);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		Control ctrl = super.createSWTControl(parent);

		// Retrieving index columns SWT table
		final IDisplayConnector colConnector = getIndexColumnsEditor();
		colsTable = (Table) colConnector.getSWTConnector();

		// And adding a column for function definition
		funcCol = new TableColumn(colsTable, SWT.NONE);
		funcCol.setWidth(200);
		funcCol.setText("Function definition");
		NextepTableEditor editor = VersionedTableEditor.handle(colsTable, getModel());
		TextColumnEditor.handle(editor, colsTable.indexOf(funcCol), ChangeEvent.CUSTOM_4, this);
		editor.setVersionedParent(getModel());
		return ctrl;
	}

	@Override
	public void refreshConnector() {
		super.refreshConnector();
		final IOracleIndex index = (IOracleIndex) getModel();
		// colsTable.setEnabled(!index.updatesLocked());
		for (TableItem i : colsTable.getItems()) {
			if (i.getData() instanceof IBasicColumn) {
				final String func = index.getFunction(((IBasicColumn) i.getData()).getReference());
				i.setText(colsTable.indexOf(funcCol), notNull(func));
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		final IOracleIndex index = (IOracleIndex) getModel();
		switch (event) {
		case CUSTOM_4:
			final IBasicColumn c = (IBasicColumn) source;
			index.setFunction(c.getReference(), (String) data);
			break;
		}
		super.handleEvent(event, source, data);
	}

	@Override
	public void widgetDisposed(DisposeEvent event) {
		super.widgetDisposed(event);
	}
}
