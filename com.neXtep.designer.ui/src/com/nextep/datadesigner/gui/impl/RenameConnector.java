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
package com.nextep.datadesigner.gui.impl;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;

/**
 * This connector provides the most simple editor connector : one field which allows to rename any
 * {@link INamedObject}.
 * 
 * @author Christophe Fondacci
 */
public class RenameConnector extends ControlledDisplayConnector {

	private FieldEditor nameEditor;
	private Composite editor;
	private String newName;

	public RenameConnector(IObservable obj) {
		super(obj, UIControllerFactory.getController(obj));
		if (!(obj instanceof INamedObject)) {
			throw new ErrorException(UIMessages.getString("editor.rename.invalidObject")); //$NON-NLS-1$
		}
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(2, false));

		nameEditor = new FieldEditor(
				editor,
				UIMessages.getString("editor.rename.nameLabel"), 1, 1, true, this, ChangeEvent.CUSTOM_1); //$NON-NLS-1$

		nameEditor.getText().addListener(SWT.Modify, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (!nameEditor.getText().isDisposed()) {
					newName = nameEditor.getText().getText();
				}
			}
		});
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {
		INamedObject named = (INamedObject) getModel();
		nameEditor.setText(notNull(named.getName()));
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		switch (event) {
		case CUSTOM_1:
			newName = (String) data;
			break;
		default:
			super.handleEvent(event, source, data);
		}
	}

	public String getNewName() {
		return newName;
	}

	@Override
	public void dispose() {
		newName = nameEditor.getText().getText();
		super.dispose();
	}
}
