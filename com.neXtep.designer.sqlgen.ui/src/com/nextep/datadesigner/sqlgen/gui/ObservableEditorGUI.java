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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 *
 */
public class ObservableEditorGUI extends ControlledDisplayConnector {

	private Composite editor;
	private Label nameLabel;
	private Label descLabel;
	private Text nameText;
	private Text descText;

	public ObservableEditorGUI(IObservable pkg, ITypedObjectUIController controller ){
		super(pkg,controller);
	}
	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent,SWT.NONE);
		GridLayout grid = new GridLayout();
		grid.numColumns=2;
		editor.setLayout(grid);

		nameLabel = new Label(editor,SWT.RIGHT);
		nameLabel.setText("Name : ");
		GridData nameData = new GridData();
		nameData.horizontalAlignment=GridData.FILL;
		nameLabel.setLayoutData(nameData);

		nameText = new Text(editor,SWT.BORDER);
		GridData nameTData = new GridData();
		nameTData.horizontalAlignment=GridData.FILL;
		nameTData.grabExcessHorizontalSpace=true;
		nameText.setLayoutData(nameTData);
		ColorFocusListener.handle(nameText);
		TextEditor.handle(nameText, ChangeEvent.NAME_CHANGED, this);

		descLabel = new Label(editor,SWT.RIGHT);
		descLabel.setText("Description : ");
		GridData descData = new GridData();
		descData.horizontalAlignment=GridData.FILL;
		descLabel.setLayoutData(descData);

		descText = new Text(editor,SWT.BORDER);
		GridData descTData = new GridData();
		descTData.horizontalAlignment=GridData.FILL;
		descTData.grabExcessHorizontalSpace=true;
		descText.setLayoutData(descTData);
		ColorFocusListener.handle(descText);
		TextEditor.handle(descText, ChangeEvent.DESCRIPTION_CHANGED, this);

		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		INamedObject pkg = (INamedObject)getModel();

		nameText.setText(notNull(pkg.getName()));
		descText.setText(notNull(pkg.getDescription()));

	}


}
