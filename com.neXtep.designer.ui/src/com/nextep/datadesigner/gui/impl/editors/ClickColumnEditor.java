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
package com.nextep.datadesigner.gui.impl.editors;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.gui.model.ColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;

/**
 * This editor will only fire the specified event on a click.
 * This could be used for boolean based columns (flags)
 * @author Christophe Fondacci
 *
 */
public class ClickColumnEditor extends ColumnEditor {

	private ChangeEvent triggeredEvent = null;
	private IEventListener listener = null;
	private ClickColumnEditor(NextepTableEditor editor, int columnIndex, ChangeEvent triggeredEvent, IEventListener listener) {
		super(editor);
		this.triggeredEvent=triggeredEvent;
		this.listener=listener;
		editor.addColumnEditor(columnIndex, this);
	}
	public static ClickColumnEditor handle(NextepTableEditor editor, int columnIndex, ChangeEvent triggeredEvent, IEventListener listener) {
		return new ClickColumnEditor(editor,columnIndex,triggeredEvent,listener);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.ColumnEditor#disposeEditor()
	 */
	@Override
	public void disposeEditor() {}
	/**
	 * @see com.nextep.datadesigner.gui.model.ColumnEditor#getEditor(org.eclipse.swt.widgets.Table, java.lang.String)
	 */
	@Override
	public Control getEditor(Table table, String editedString) {
		publish();
		return null;
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.ColumnEditor#publish()
	 */
	@Override
	public void publish() {
		listener.handleEvent(triggeredEvent, editedData, editedDataObj );
	}

}
