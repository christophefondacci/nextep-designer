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

import java.text.MessageFormat;
import java.util.ArrayList;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.oracle.model.IOracleTablePhysicalProperties;
import com.nextep.designer.dbgm.oracle.model.PhysicalOrganisation;
import com.nextep.designer.dbgm.oracle.services.DBOMHelper;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.dbgm.ui.editors.PhysicalPropertiesEditor;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class TablePhysicalPropertiesEditor extends PhysicalPropertiesEditor {

	private Label organizationLabel = null;
	private Combo orgCombo = null;
	private Label partLabel = null;
	private Combo partCombo = null;
	private Label partColLabel = null;
	private Combo partColCombo = null;
	private IDisplayConnector partitionableEditor = null;

	public TablePhysicalPropertiesEditor(IOracleTablePhysicalProperties properties,
			ITypedObjectUIController controller) {
		super(properties, controller, true, true);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		Composite editor = (Composite) super.createSWTControl(parent);
		// We are partitionable, we want to show partition editor
		partitionableEditor = new PartitionableEditor((IPartitionable) getModel(), getController());
		partitionableEditor.create(editor);
		return editor;
	}

	/**
	 * @see com.nextep.designer.dbgm.ui.editors.PhysicalPropertiesEditor#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		final ICoreService coreService = CorePlugin.getService(ICoreService.class);

		IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) getModel();
		super.refreshConnector();
		if (props.getPhysicalOrganisation() == null) {
			orgCombo.setText(PhysicalOrganisation.HEAP.getLabel());
		} else {
			orgCombo.setText(props.getPhysicalOrganisation().getLabel());
		}
		if (props.getPartitioningMethod() == null) {
			partCombo.setText(PartitioningMethod.NONE.name());
		} else {
			partCombo.setText(props.getPartitioningMethod().name());
		}
		// Partitioned column
		for (IReference r : props.getPartitionedColumnsRef()) {
			IBasicColumn c = (IBasicColumn) VersionHelper.getReferencedItem(r);
			partColCombo.setText(notNull(c.getName()));
		}

		// Enablement
		boolean locked = coreService.isLocked(props);
		final boolean b = (props.getParent() == null ? true : !locked);
		orgCombo.setEnabled(b);
		partCombo.setEnabled(b);
		partColCombo.setEnabled(b);
		partitionableEditor.refreshConnector();

		// Warning for clustered tables
		final IBasicTable parentTable = coreService.getFirstTypedParent(props, IBasicTable.class);
		IOracleClusteredTable clusterTab = DBOMHelper.getClusterFor(parentTable);
		if (clusterTab != null) {
			MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell(), MessageFormat.format(DBOMUIMessages
					.getString("warnPhysicalsOverriddenByClusterTitle"), clusterTab.getCluster()
					.getName()), MessageFormat.format(DBOMUIMessages
					.getString("warnPhysicalsOverriddenByCluster"), props.getParent().getName(),
					clusterTab.getCluster().getName()));
		}
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
		IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) getModel();
		switch (event) {
		case CUSTOM_10:
			props.setPhysicalOrganisation((PhysicalOrganisation) orgCombo.getData((String) data));
			break;
		case CUSTOM_11:
			props.setPartitioningMethod((PartitioningMethod) partCombo.getData((String) data));
			break;
		case CUSTOM_12:
			// Removing previous (to handle future multiple column support)
			for (IReference r : new ArrayList<IReference>(props.getPartitionedColumnsRef())) {
				IBasicColumn c = (IBasicColumn) VersionHelper.getReferencedItem(r);
				props.removePartitionedColumn(c);
			}
			// Adding new column
			props.addPartitionedColumn((IBasicColumn) partColCombo.getData((String) data));
			break;
		case PARTITION_ADDED:
		case PARTITION_REMOVED:
			// Transferring to partitionable editor
			partitionableEditor.handleEvent(event, source, data);
			break;
		}
		super.handleEvent(event, source, data);
	}

	private void createOrganizationCombo(Composite editor) {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.minimumWidth = 300;
		orgCombo = new Combo(editor, SWT.READ_ONLY);
		orgCombo.setLayoutData(gridData);

		for (PhysicalOrganisation o : PhysicalOrganisation.values()) {
			orgCombo.add(o.getLabel());
			orgCombo.setData(o.getLabel(), o);
		}
		ComboEditor.handle(orgCombo, ChangeEvent.CUSTOM_10, this);
		new Label(editor, SWT.NONE);
		new Label(editor, SWT.NONE);
		new Label(editor, SWT.NONE);
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
			if (m != PartitioningMethod.INDEX_LOCAL) {
				partCombo.add(m.name());
				partCombo.setData(m.name(), m);
			}
		}
		ComboEditor.handle(partCombo, ChangeEvent.CUSTOM_11, this);
		// new Label(editor,SWT.NONE);
	}

	private void createPartColCombo(Composite editor) {
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		gridData.verticalAlignment = GridData.CENTER;
		gridData.grabExcessHorizontalSpace = false;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.minimumWidth = 300;
		partColCombo = new Combo(editor, SWT.READ_ONLY);
		partColCombo.setLayoutData(gridData);

		IOracleTablePhysicalProperties props = (IOracleTablePhysicalProperties) getModel();
		for (IBasicColumn c : ((IBasicTable) props.getParent()).getColumns()) {
			partColCombo.add(c.getName());
			partColCombo.setData(c.getName(), c);
		}
		partColCombo.setText("");
		ComboEditor.handle(partColCombo, ChangeEvent.CUSTOM_12, this);
	}

	// /**
	// * @see
	// com.nextep.designer.dbgm.oracle.ui.impl.PhysicalPropertiesEditor#createSWTControl(org.eclipse.swt.widgets.Composite)
	// */
	// @Override
	// protected Control createSWTControl(Composite parent) {
	// // Super-initialization
	// Composite editor = (Composite)super.createSWTControl(parent);
	// // Table-specific editors
	// GridData gridData14 = new GridData();
	// gridData14.horizontalSpan = 2;
	// GridData gridData13 = new GridData();
	// gridData13.horizontalSpan = 2;
	// GridData gridData12 = new GridData();
	// gridData12.horizontalSpan = 2;
	//
	//
	//
	// return editor;
	// }

	@Override
	protected void createSWTControlBeforeAttributes(Composite editor) {
		GridData gridData1 = new GridData();
		gridData1.horizontalSpan = 2;
		organizationLabel = new Label(editor, SWT.RIGHT);
		organizationLabel.setText("Organization : ");
		organizationLabel.setLayoutData(gridData1);
		createOrganizationCombo(editor);

		partLabel = new Label(editor, SWT.RIGHT);
		partLabel.setText("Partioning : ");
		partLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
		createPartCombo(editor);

		partColLabel = new Label(editor, SWT.RIGHT);
		partColLabel.setText("Partitioned column : ");
		createPartColCombo(editor);
	}

}
