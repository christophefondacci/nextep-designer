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
package com.nextep.designer.sqlgen.oracle.ui.impl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.MultiPageEditorPart;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.editors.TypedEditorInput;
import com.nextep.datadesigner.gui.impl.rcp.RCPTypedEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.oracle.model.IOracleUserType;
import com.nextep.designer.sqlgen.ui.editors.SQLEditor;

public class OracleUserTypeEditor extends MultiPageEditorPart {

	private SQLEditor bodyEditor;
	private IEventListener listener;
	@Override
	protected void createPages() {
		TypedEditorInput input = (TypedEditorInput)getEditorInput();
		if(input.getModel() instanceof IOracleUserType) {
			final IOracleUserType type = (IOracleUserType)input.getModel();
			setPartName(type.getName());
			try {
				RCPTypedEditor typeEditor = new RCPTypedEditor();
				setPageText(addPage(typeEditor,input),"Type definition");
				bodyEditor = new SQLEditor();
				final IEditorInput bodyInput = new TypeBodyEditorInput(type);
				setPageText(addPage(bodyEditor,bodyInput),"Type body");
				
				listener = 	new IEventListener() {
					@Override
					public void handleEvent(ChangeEvent event, IObservable source,
							Object data) {
						switch(event) {
						case CUSTOM_1:
							OracleUserTypeEditor.this.bodyEditor.getDocumentProvider().getDocument(bodyInput).set(type.getTypeBody()==null ? "" : type.getTypeBody() );
						}
					}
				};
				type.addListener(listener);
			} catch(PartInitException e) {
				throw new ErrorException(e);
			}
		}
	}
	@Override
	public void dispose() {
		super.dispose();
		if(bodyEditor.getEditorInput() instanceof TypeBodyEditorInput) {
			((TypeBodyEditorInput)bodyEditor.getEditorInput()).getModel().removeListener(listener);
		}
	}
	@Override
	public void doSave(IProgressMonitor monitor) {
		getActiveEditor().doSave(monitor);
	}

	@Override
	public void doSaveAs() {
		getActiveEditor().doSaveAs();
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
