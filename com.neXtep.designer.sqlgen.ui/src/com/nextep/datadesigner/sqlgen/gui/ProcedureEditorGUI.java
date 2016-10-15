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
package com.nextep.datadesigner.sqlgen.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.dbgm.model.IProcedure;
import com.nextep.datadesigner.dbgm.model.LanguageType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class ProcedureEditorGUI extends ObservableEditorGUI {

	private Combo languageCombo;
	
	public ProcedureEditorGUI(IProcedure procedure, ITypedObjectUIController controller) {
		super(procedure,controller);
	}
	
	@Override
	protected Control createSWTControl(Composite parent) {
		Composite editor = (Composite)super.createSWTControl(parent);
		Label langLabel = new Label(editor,SWT.NONE);
		langLabel.setText("Language : ");
		langLabel.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
		
		languageCombo = new Combo(editor,SWT.READ_ONLY);
		for(LanguageType t : LanguageType.values()) {
			if(t.isSupported(DBGMHelper.getCurrentVendor())) {
				languageCombo.add(t.getLabel());
				languageCombo.setData(t.getLabel(),t);
			}
		}
		
		languageCombo.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
		ComboEditor.handle(languageCombo, ChangeEvent.CUSTOM_13, this);
		
		return editor;
	}
	
	
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IProcedure proc = (IProcedure)getModel();
		switch(event) {
		case CUSTOM_13:
			LanguageType t = (LanguageType)languageCombo.getData((String)data);
			if(t!=null) {
				proc.setLanguageType(t);
			} else {
				proc.setLanguageType(null);
			}
		}
		super.handleEvent(event, source, data);
	}
	
	@Override
	public void refreshConnector() {
		super.refreshConnector();
		IProcedure proc = (IProcedure)getModel();
		final LanguageType t = proc.getLanguageType();
		if(t!=null) {
			for(int i = 0 ; i < languageCombo.getItemCount() ; i++) {
				if(t.getLabel().equals(languageCombo.getItem(i))) {
					languageCombo.select(i);
					break;
				}
			}
		}
	}
}
