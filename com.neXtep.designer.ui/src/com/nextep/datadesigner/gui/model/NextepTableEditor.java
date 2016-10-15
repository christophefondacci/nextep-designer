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
/**
 *
 */
package com.nextep.datadesigner.gui.model;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * A table editor is an automatic editor for data contained in a SWT table. It generalizes default
 * nextep implementation of a table view, mainly by registering listeners, handling maps to match
 * table items with the object model, and handling proper disposal methods. <br>
 * <br>
 * 
 * @author Christophe Fondacci
 */
public class NextepTableEditor implements Listener {

	protected Table table;
	private Map<Integer, ColumnEditor> editors;
	private int selectedColumn = -1;
	private int selectedIndex = -1;

	/**
	 * Private default constructor. This constructor will remain private. Callers should use the
	 * static method <code>handle</code> to initialize a new table editor.
	 * 
	 * @param table SWT table object to handle
	 */
	protected NextepTableEditor(Table table) {
		this.table = table;
		table.addListener(SWT.MouseDown, this);
		editors = new HashMap<Integer, ColumnEditor>();
	}

	/**
	 * Returns the editor defined for the specified table column, or <code>null</code> if none.
	 * 
	 * @param column column index of the editor to retrieve.
	 * @return the corresponding column editor.
	 */
	public ColumnEditor getColumnEditor(int column) {
		return editors.get(column);
	}

	/**
	 * Handles the specified table by this table editor.
	 * 
	 * @param t SWT table object to handle
	 * @return a new TableEditor object handling this table
	 */
	public static NextepTableEditor handle(Table t) {
		return new NextepTableEditor(t);
	}

	/**
	 * Adds a column editor at the specified column index. Should the user click on any line within
	 * this column, the specified editor will be shown and correctly positioned. Data validation /
	 * cancellation will be automatically handled.
	 * 
	 * @param columnIndex index of the column for which this editor should be used
	 * @param colEditor editor to show when the column is edited.
	 */
	public void addColumnEditor(int columnIndex, ColumnEditor colEditor) {
		editors.put(columnIndex, colEditor);
	}

	/**
	 * Sets a new versioned parent to this table editor. This method will mainly be used when
	 * version control system changes any of the model object on checkout. This allows the table
	 * editor to point on the correct view versioned object.
	 * 
	 * @param versionedParent new parent version.
	 */
	public void setVersionedParent(Object versionedParent) {
		table.setData(versionedParent);
	}

	/**
	 * @return the currently selected column in the table or -1 if no selected column
	 */
	public int getSelectedColumn() {
		return selectedColumn;
	}

	/**
	 * Defines the column currently being edited. Used for column editors to notify the current
	 * column selection when the editor automatically moves from one cell to the next one without
	 * passing in the {@link NextepTableEditor}.
	 * 
	 * @param selectedColumn new column selection
	 */
	public void setSelectedColumn(int selectedColumn) {
		this.selectedColumn = selectedColumn;
	}

	/**
	 * @return the index of the item currently being edited
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * @return the currently selected object model from the TableItem.getData() method
	 */
	public Object getSelectedModel() {
		TableItem[] selection = table.getSelection();
		if (selection != null && selection.length > 0) {
			return selection[0].getData();
		}
		return null;
	}

	/**
	 * Handles the edition of the table. This method will retrieve the clicked line and will fire
	 * the corresponding editor, if existing.
	 * 
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		switch (event.type) {
		case SWT.MouseDown:
			TableItem selectedItem = table.getItem(new Point(5, event.y));
			// If no selection clicked, we add a new column
			if (selectedItem != null) {
				// Setting selected index of the edited item
				for (int k = 0; k < table.getItems().length; k++) {
					if (table.getItem(k) == selectedItem) {
						selectedIndex = k;
						break;
					}
				}
				// Retrieving selected column
				for (int i = 0; i < table.getColumns().length; i++) {
					Rectangle itemBounds = selectedItem.getBounds(i);
					if (itemBounds.contains(event.x, event.y)) {
						selectedColumn = i;
						ColumnEditor currentEditor = editors.get(i);
						if (currentEditor != null) {
							currentEditor.edit(table, selectedItem, i);
							return;
						}
					}
				}
			}
		}

	}

}
