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
package com.nextep.datadesigner.gui.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.gui.impl.editors.ClickColumnEditor;
import com.nextep.datadesigner.model.IObservable;

public abstract class ColumnEditor implements Listener {

	protected IObservable editedData = null;
	protected Object editedDataObj = null;
	private boolean disposeOnFocusOut = true;
	private Control activeEditor= null;
	private NextepTableEditor editor = null;
	//Temporary objects for linked editors
	private int editedColumn;
	private Table editedTable;
	private TableItem editedItem;
	private boolean alreadyProcessing = false;
	
	public ColumnEditor(NextepTableEditor editor) {
		this.editor = editor;
	}

	/**
	 * Creates the editor for the requested data.
	 * @param table table in which the editor will be created
	 * @param editedString a string which will appear in the editor
	 * @param editedData the TableItem data of the edited line
	 * @return the editor for this column
	 */
	public abstract Control getEditor(Table table, String editedString);
	/**
	 * Method called to dispose the SWT editor control
	 */
	public abstract void disposeEditor();
	/**
	 * Method called when the edition has been validated
	 * to publish the edited contents to the model
	 */
	public abstract void publish();

	/**
	 * Specifies whether the column editor should dispose
	 * the editor SWT control or not when changing focus
	 * <br><code>true</code> is default.<br>
	 * This should be used for permanent editors.
	 *
	 * @param flag pass false to avoid disposing the SWT control
	 */
	protected void setDisposeOnFocusOut(boolean flag) {
		disposeOnFocusOut = flag;
	}
	/**
	 * Edits the specified table item on the selected column.
	 * This method will create the SWT control editor and display
	 * it while listening on validation / cancellation / focus out
	 * of the editor to apply / cancel edition.
	 *
	 * @param table SWT table which is being edited
	 * @param selectedItem SWT TableItem being edited
	 * @param selectedColumn column of edition
	 */
	public void edit(Table table, TableItem selectedItem, int selectedColumn) {
		// Setting edited data
		if(selectedItem.getData() instanceof IObservable) {
			editedData = (IObservable)selectedItem.getData();
			editedDataObj=null;
		} else {
			editedData = null;
			editedDataObj = selectedItem.getData();
		}
		// Initializing editor
		TableEditor editor = new TableEditor(table);
		editor.grabHorizontal=true;
		//editor.horizontalAlignment=GridData.CENTER;
		editedColumn = selectedColumn;
		this.editor.setSelectedColumn(selectedColumn);
		editedTable = table;
		editedItem = selectedItem;
		
		activeEditor = getEditor(table,selectedItem.getText(selectedColumn));
		if(activeEditor != null) {
			activeEditor.addListener(SWT.FocusOut, this);
			activeEditor.addListener(SWT.Traverse, this);
//			activeEditor.addListener(SWT.KeyUp, this);
			editor.setEditor(activeEditor, selectedItem, selectedColumn);
			activeEditor.setFocus();
		}
	}
	/**
	 * Event listener of the editor control. This listener fires validation
	 * or cancellation actions depending on the focus / escape / return
	 * exit status.
	 *
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(final Event e) {
		if(activeEditor==null || alreadyProcessing) {
			return;
		}
		alreadyProcessing = true;
		try {
	        switch (e.type) {
	        	case SWT.FocusOut:
	       			this.publish();
	       			dispose();
	        	    break;
//	        	case SWT.KeyDown:
//	        		switch(e.keyCode) {
//	        		case SWT.ARROW_RIGHT:
//	        			this.publish();
//	        			spawnEditor(editedColumn+1,editedItem);
//	        			break;
//	        		case SWT.ARROW_LEFT:
//	        			this.publish();
//	        			spawnEditor(editedColumn-1,editedItem);
//	        			break;
//	        		}
		        case SWT.Traverse:
		        	switch (e.detail) {
		        		case SWT.TRAVERSE_RETURN:
		        			try {
		        				this.publish();
		        				dispose();
		        				spawnEditor(editedColumn+1,editedItem);
		        			} finally {
//		        				e.doit=false;
		        			}
		        			break;
		        		case SWT.TRAVERSE_ESCAPE:
		        			dispose();
		        			e.doit = false;
		        			break;
//		        		case SWT.TRAVERSE_ARROW_NEXT:
		        		case SWT.TRAVERSE_TAB_NEXT:
		        			this.publish();
		        			dispose();
		        			spawnEditor(editedColumn+1,editedItem);
		        			break;
//		        		case SWT.TRAVERSE_ARROW_PREVIOUS:
		        		case SWT.TRAVERSE_TAB_PREVIOUS:
		        			this.publish();
		        			dispose();
		        			spawnEditor(editedColumn-1,editedItem);
		        			break;
		        	}
		        	break;
	        }
		} finally {
	        alreadyProcessing=false;
		}
    
	}
	private void dispose() {
		if(disposeOnFocusOut) {
        	this.disposeEditor();
        	activeEditor=null;
        }		
	}

	private void spawnEditor(int column, TableItem item) {
		ColumnEditor nextEditor = editor.getColumnEditor(column);
		if(nextEditor!=null && !(nextEditor instanceof ClickColumnEditor)) {
			nextEditor.edit(editedTable,item,column);
		}
	}
}

