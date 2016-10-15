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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.model.ColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;

/**
 * This dynamic combo editor displays a readonly combo proposing
 * compatible columns of a table which should be the data of the
 * edited table item.
 * 
 * @author Christophe
 *
 */
public class DynamicColumnComboEditor extends ColumnEditor {

	private Combo editor;
	private ChangeEvent event;
	private IEventListener listener;
	private IBasicColumn matchingColumn;
	private DynamicColumnComboEditor(NextepTableEditor editor, int columnIndex, ChangeEvent triggeredEvent, IEventListener listener,IBasicColumn matchingColumn) {
		super(editor);
		this.event = triggeredEvent;
		this.listener = listener;
		this.matchingColumn = matchingColumn;
		editor.addColumnEditor(columnIndex, this);
	}
	
	public static ColumnEditor handle(NextepTableEditor editor, int columnIndex, ChangeEvent triggeredEvent, IEventListener listener,IBasicColumn matchingColumn) {
		return new DynamicColumnComboEditor(editor,columnIndex,triggeredEvent,listener,matchingColumn);
	}
	@Override
	public void disposeEditor() {
		if(editor!=null && !editor.isDisposed()) {
			editor.dispose();
		}
	}

	@Override
	public Control getEditor(Table table, String editedString) {
		editor = new Combo(table,SWT.READ_ONLY);
		
		// Retrieving current selection (assuming a table)
		IOracleClusteredTable ct = (IOracleClusteredTable)editedData;
		IBasicTable t = (IBasicTable)VersionHelper.getReferencedItem(ct.getTableReference());
		for(IBasicColumn c : t.getColumns()) {
			if(c.getDatatype().toString().equals(matchingColumn.getDatatype().toString())) {
				editor.add(c.getName());
				editor.setData(c.getName(), c);
			}
		}
		editor.setText(editedString);
		return editor;
	}

	@Override
	public void publish() {
//		listener.handleEvent(event, editedData, editor.getData(editor.getText()));
		final IBasicColumn mappedCol = (IBasicColumn)editor.getData(editor.getText());
		if(mappedCol!=null) {
			((IOracleClusteredTable)editedData).setColumnReferenceMapping(matchingColumn.getReference(), mappedCol.getReference());
			listener.handleEvent(ChangeEvent.MODEL_CHANGED, editedData, null);
		}
	}

}
