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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.TableDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.ILockable;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.dbgm.model.IPartition;
import com.nextep.designer.dbgm.model.IPartitionable;
import com.nextep.designer.dbgm.model.IPhysicalProperties;
import com.nextep.designer.dbgm.model.ITablePartition;
import com.nextep.designer.dbgm.model.PartitioningMethod;
import com.nextep.designer.dbgm.oracle.model.IPartitionPhysicalProperties;
import com.nextep.designer.dbgm.oracle.ui.DBOMImages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IDependencyService;

/**
 * An editor for partitionable objects whish build a sash form with partitions list on the left and
 * storage information on the right which refreshes depending on the partition selection.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class PartitionableEditor extends TableDisplayConnector implements SelectionListener {

	// Partitioning section
	private Table partitionsTable = null;
	private SashForm partSash = null;
	private Text partStorageText = null;
	private IDisplayConnector partStorage = null;
	private Composite storagePane = null;
	private Button addPartButton;
	private Button delPartButton;

	public PartitionableEditor(IPartitionable p, ITypedObjectUIController controller) {
		super(p, controller);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		// Partition toolbox
		partSash = new SashForm(parent, SWT.HORIZONTAL);
		partSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));

		Composite partList = new Composite(partSash, SWT.BORDER);
		partList.setLayout(new GridLayout(6, false));
		Composite toolbox = new Composite(partList, SWT.NONE);
		GridLayout toolLayout = new GridLayout();
		toolLayout.marginBottom = toolLayout.marginHeight = toolLayout.marginLeft = toolLayout.marginRight = toolLayout.marginTop = toolLayout.marginWidth = 0;
		toolLayout.numColumns = 2;
		toolbox.setLayout(toolLayout);
		toolbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 6, 1));
		addPartButton = new Button(toolbox, SWT.PUSH);
		addPartButton.setImage(DBOMImages.ICON_ADD_PARTITION);
		addPartButton.setToolTipText("Add partition");
		addPartButton.addSelectionListener(this);
		delPartButton = new Button(toolbox, SWT.PUSH);
		delPartButton.setImage(DBOMImages.ICON_DEL_PARTITION);
		delPartButton.setToolTipText("Remove partition");
		delPartButton.addSelectionListener(this);
		// Partition table
		partitionsTable = new Table(partList, SWT.BORDER | SWT.FULL_SELECTION);
		partitionsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 6, 1));
		partitionsTable.setLinesVisible(true);
		partitionsTable.setHeaderVisible(true);
		// Registering SWT table
		initializeTable(partitionsTable, null);

		TableColumn nameCol = new TableColumn(partitionsTable, SWT.NONE);
		nameCol.setText("Partition name");
		nameCol.setWidth(130);
		// TODO: properly handle partition types by extending this editor
		final IPartitionable model = (IPartitionable) getModel();
		if (model.getPartitionType() == IElementType.getInstance(ITablePartition.TYPE_ID)) {
			TableColumn valCol = new TableColumn(partitionsTable, SWT.NONE);
			valCol.setText("Less than");
			valCol.setWidth(80);
		}
		partitionsTable.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (partitionsTable.getSelection().length > 0) {
					final IPartition part = (IPartition) partitionsTable.getSelection()[0]
							.getData();
					partStorageText.setText(part.getName());
					partStorage.setModel(part.getPhysicalProperties());
					partStorage.getSWTConnector().setVisible(true);
				}
			}
		});
		partitionsTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (partitionsTable.getSelection().length > 0) {
					final IPartition part = (IPartition) partitionsTable.getSelection()[0]
							.getData();
					UIControllerFactory.getController(part.getType()).defaultOpen(part);
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});

		// Partition storage editor

		storagePane = new Composite(partSash, SWT.BORDER);
		storagePane.setLayout(new GridLayout(2, false));
		Label partStorageLabel = new Label(storagePane, SWT.NONE);
		partStorageLabel.setText("Storage settings for partition : ");
		partStorageText = new Text(storagePane, SWT.BORDER);
		partStorageText.setEditable(false);
		partStorageText.setFont(FontFactory.FONT_BOLD);
		partStorageText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		partStorage = UIControllerFactory.getController(
				IElementType.getInstance(IPartitionPhysicalProperties.TYPE_ID)).initializeEditor(
				null);
		Control s = partStorage.create(storagePane);
		s.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		s.setVisible(false);
		// Our sash form is our global container
		return partSash;
	}

	@Override
	public Control getSWTConnector() {
		return partSash;
	}

	@Override
	public void refreshConnector() {
		IPartitionable partitionable = (IPartitionable) getModel();
		for (IPartition p : partitionable.getPartitions()) {
			TableItem i = getOrCreateItem(p);
			i.setText(p.getName());
			if (p instanceof ITablePartition) {
				i.setText(1, notNull(((ITablePartition) p).getHighValue()));
			}
			// i.setData(p);
		}

		// Enablement
		if (partitionable instanceof IPhysicalProperties) {
			final boolean partitioned = (partitionable.getPartitioningMethod() != PartitioningMethod.NONE);
			if (!partitioned) {
				addPartButton.setEnabled(false);
				delPartButton.setEnabled(false);
				partitionsTable.setEnabled(false);
			} else {
				boolean enabled = true;
				// Must cast to access lock status
				// TODO: do not cast to physical properties to determine lock status
				final IPhysicalProperties props = (IPhysicalProperties) partitionable;
				if (props.getParent() instanceof ILockable) {
					enabled = !((ILockable<?>) props.getParent()).updatesLocked();
				}
				addPartButton.setEnabled(enabled);
				delPartButton.setEnabled(enabled);
				partitionsTable.setEnabled(enabled);
			}
		}
	}

	public void enablePartitionEditor(boolean enabled) {
		addPartButton.setEnabled(enabled);
		delPartButton.setEnabled(enabled);
		partitionsTable.setEnabled(enabled);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		IPartitionable props = (IPartitionable) getModel();
		if (e.getSource() == addPartButton) {
			UIControllerFactory.getController(props.getPartitionType()).newInstance(props);
			refreshConnector();
		} else if (e.getSource() == delPartButton) {
			if (partitionsTable.getSelection().length > 0) {
				IPartition part = (IPartition) partitionsTable.getSelection()[0].getData();
				VCSPlugin.getService(IDependencyService.class).checkDeleteAllowed(part);
				ControllerFactory.getController(part).modelDeleted(part);
				removeTableItem(part);
			}
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		super.handleEvent(event, source, data);
		switch (event) {
		case PARTITION_REMOVED:
			removeTableItem((IPartition) data);
			break;
		}
		refreshConnector();
	}

}
