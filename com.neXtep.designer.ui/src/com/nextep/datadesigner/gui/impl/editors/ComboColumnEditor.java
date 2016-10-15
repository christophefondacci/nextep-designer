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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.model.ColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.INamedObject;

public class ComboColumnEditor extends ColumnEditor {

	private CCombo typeEditor = null;
	private ChangeEvent triggeredEvent = null;
	private IEventListener listener = null;
	private List<String> textProposals;
	private Map<String,Object> dataProposals;
	private ComboColumnEditor(NextepTableEditor editor, int columnIndex, ChangeEvent triggeredEvent, IEventListener listener,List<?> proposals) {
		super(editor);
		this.triggeredEvent=triggeredEvent;
		this.listener=listener;
		setProposals(proposals);
		editor.addColumnEditor(columnIndex, this);
	}
	public static ComboColumnEditor handle(NextepTableEditor editor, int columnIndex, ChangeEvent triggeredEvent, IEventListener listener,List<?> proposals) {
		return new ComboColumnEditor(editor,columnIndex,triggeredEvent,listener, proposals);
	}
	public void setProposals(List<?> proposals) {
		if(proposals == null) {
			this.textProposals = Collections.EMPTY_LIST;
			this.dataProposals = Collections.EMPTY_MAP;
		} else {
			textProposals = new ArrayList<String>();
			dataProposals = new HashMap<String, Object>();
			for(Object o : proposals) {
				if( o instanceof String ) {
					textProposals.add((String)o);
				} else if( o instanceof INamedObject) {
					textProposals.add(((INamedObject)o).getName());
					dataProposals.put(((INamedObject)o).getName(), o);
				} else {
					throw new ErrorException("Invalid proposal for combo editor.");
				}
			}
		}
	}
	@Override
	public void disposeEditor() {
		if(typeEditor!=null && !typeEditor.isDisposed()) {
			typeEditor.dispose();
		}
		typeEditor=null;
	}

	@Override
	public Control getEditor(Table parent,String editedString) {
		typeEditor = new CCombo(parent,SWT.NONE);
		for(String s : textProposals) {
			typeEditor.add(s);
		}
		if(!textProposals.contains(editedString)) {
			textProposals.add(editedString);
			typeEditor.add(editedString);
		}
		typeEditor.setText(editedString);
		return typeEditor;
	}

	@Override
	public void publish() {
		listener.handleEvent(triggeredEvent, editedData, dataProposals.containsKey(typeEditor.getText()) ? dataProposals.get(typeEditor.getText()) : typeEditor.getText());
	}

}
