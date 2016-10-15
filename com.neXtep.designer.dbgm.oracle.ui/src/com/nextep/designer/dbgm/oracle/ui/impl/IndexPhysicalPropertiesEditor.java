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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.model.IIndexPhysicalProperties;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.ui.editors.PhysicalPropertiesEditor;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class IndexPhysicalPropertiesEditor extends PhysicalPropertiesEditor {

	/** Partitions edition pane */
	private PartitionableEditor partitionableEditor = null;
	private Label partLabel = null;
	private Combo partCombo = null;

	public IndexPhysicalPropertiesEditor(IPhysicalProperties props,
			ITypedObjectUIController controller) {
		super(props, controller, true, true);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		Composite editor = (Composite) super.createSWTControl(parent);
		// We are partitionable, we want to show partition editor
		partitionableEditor = new PartitionableEditor((IPartitionable) getModel(), getController());
		partitionableEditor.create(editor);
		return editor;
	}

	@Override
	protected void createSWTControlBeforeAttributes(Composite editor) {
		partLabel = new Label(editor, SWT.NONE);
		partLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		partLabel.setText("Partitioning : ");
		createPartCombo(editor);
	}

	private void createPartCombo(Composite editor) {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.minimumWidth = 300;
		partCombo = new Combo(editor, SWT.READ_ONLY);
		partCombo.setLayoutData(gridData);

		for (PartitioningMethod m : PartitioningMethod.values()) {
			if (m == PartitioningMethod.INDEX_LOCAL || m == PartitioningMethod.NONE) {
				partCombo.add(m.name());
				partCombo.setData(m.name(), m);
			}
		}
		ComboEditor.handle(partCombo, ChangeEvent.CUSTOM_11, this);
		new Label(editor, SWT.NONE);
		new Label(editor, SWT.NONE);
		new Label(editor, SWT.NONE);
	}

	@Override
	public void refreshConnector() {
		super.refreshConnector();
		IPartitionable props = (IPartitionable) getModel();

		if (props.getPartitioningMethod() == null) {
			partCombo.setText(PartitioningMethod.NONE.name());
		} else {
			partCombo.setText(props.getPartitioningMethod().name());
		}
		partitionableEditor.refreshConnector();
	}

	@Override
	public void setModel(Object model) {
		super.setModel(model);
		// Dispatching to sub editor
		if (partitionableEditor != null) {
			partitionableEditor.setModel(model);
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IIndexPhysicalProperties props = (IIndexPhysicalProperties) getModel();
		switch (event) {
		case CUSTOM_11:
			if (props instanceof IPartitionable) {
				((IPartitionable) props).setPartitioningMethod((PartitioningMethod) partCombo
						.getData((String) data));
			}
			break;
		}
		// Super handling!
		super.handleEvent(event, source, data);
	}
}
