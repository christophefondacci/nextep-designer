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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.gui.TableEditorGUI;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.external.VersionedTableEditor;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.oracle.model.IMaterializedView;
import com.nextep.designer.dbgm.oracle.model.IOracleCluster;
import com.nextep.designer.dbgm.oracle.model.IOracleClusteredTable;
import com.nextep.designer.dbgm.oracle.ui.DBOMImages;
import com.nextep.designer.dbgm.oracle.ui.DBOMUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.jface.TypedContentProvider;

public class OracleClusterEditor extends TableEditorGUI {

	private Table clusteredTables;
	private Button addClusteredTableButton;
	private Button delClusteredTableButton;
	private NextepTableEditor tabEditor;

	public OracleClusterEditor(IOracleCluster cluster, ITypedObjectUIController controller) {
		super(cluster, controller);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		Composite editor = (Composite) super.createSWTControl(parent);

		// Clustered tables toolbox
		Composite toolbox = new Composite(editor, SWT.NONE);
		toolbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		addNoMarginLayout(toolbox, 2);
		addClusteredTableButton = new Button(toolbox, SWT.PUSH);
		addClusteredTableButton.setImage(DBOMImages.ICON_ADD_CLUSTER_TABLE);
		addClusteredTableButton.setToolTipText("Add a table to the cluster");
		addClusteredTableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Object elt = Designer.getInstance().invokeSelection(
						"find.element",
						new TypedContentProvider(IElementType.getInstance(IBasicTable.TYPE_ID),
								IElementType.getInstance(IMaterializedView.VIEW_TYPE_ID)),
						VersionHelper.getCurrentView(),
						DBOMUIMessages.getString("addClusteredTableSelection"));
				if (elt != null) {
					IBasicTable t = (IBasicTable) elt;
					((IOracleCluster) getModel()).addClusteredTable(t.getReference());
				}
			}
		});
		delClusteredTableButton = new Button(toolbox, SWT.PUSH);
		delClusteredTableButton.setImage(DBOMImages.ICON_DEL_CLUSTER_TABLE);
		delClusteredTableButton.setToolTipText("Remove a table from the cluster");
		delClusteredTableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selItems = clusteredTables.getSelection();
				if (selItems.length > 0) {
					final IOracleClusteredTable t = (IOracleClusteredTable) selItems[0].getData();
					((IOracleCluster) getModel()).removeClusteredTable(t.getTableReference());
				}
			}
		});
		clusteredTables = new Table(editor, SWT.BORDER | SWT.FULL_SELECTION);
		GridData tabData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		tabData.minimumHeight = 100;
		clusteredTables.setLayoutData(tabData);
		clusteredTables.setHeaderVisible(true);
		clusteredTables.setLinesVisible(true);
		TableColumn tabNameCol = new TableColumn(clusteredTables, SWT.NONE);
		tabNameCol.setWidth(180);
		tabNameCol.setText("Table cluster mappings");
		// TableColumn tabDescCol = new TableColumn(clusteredTables,SWT.NONE);
		// tabDescCol.setWidth(300);
		// tabDescCol.setText("Description");
		tabEditor = VersionedTableEditor.handle(clusteredTables, getModel());
		return editor;
	}

	@Override
	public void refreshConnector() {
		final IOracleCluster cluster = (IOracleCluster) getModel();
		super.refreshConnector();

		// Refreshing tables
		clusteredTables.removeAll();
		// Rebuilding columns
		for (int i = clusteredTables.getColumnCount() - 1; i > 0; i--) {
			clusteredTables.getColumn(i).dispose();
		}
		int colIndex = 1;
		for (IBasicColumn col : cluster.getColumns()) {
			TableColumn colItem = new TableColumn(clusteredTables, SWT.NONE);
			colItem.setText(col.getName() + " Mapping");
			colItem.setWidth(150);
			DynamicColumnComboEditor.handle(tabEditor, colIndex++, ChangeEvent.MAPPING_CHANGED,
					this, col);
		}
		for (IOracleClusteredTable ct : cluster.getClusteredTables()) {
			IBasicTable t = (IBasicTable) VersionHelper.getReferencedItem(ct.getTableReference());
			TableItem i = new TableItem(clusteredTables, SWT.NONE);
			i.setImage(ImageFactory.getImage(t.getType().getIcon()));
			i.setText(t.getName());
			colIndex = 1;
			for (IBasicColumn c : cluster.getColumns()) {
				final IBasicColumn mappedCol = ct.getColumnMapping(c.getReference());
				i.setText(colIndex, mappedCol == null ? "[No match]" : mappedCol.getName());
				if (mappedCol == null) {
					i.setFont(colIndex, FontFactory.FONT_ITALIC);
				} else {
					i.setFont(colIndex, Display.getDefault().getSystemFont());
				}
				colIndex++;
			}
			// i.setText(1,t.getDescription());
			i.setData(ct);
		}

		// Enablement
		final boolean e = !cluster.updatesLocked();
		addClusteredTableButton.setEnabled(e);
		delClusteredTableButton.setEnabled(e);
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object o) {
		super.handleEvent(event, source, o);
		switch (event) {
		case GENERIC_CHILD_ADDED:
		case GENERIC_CHILD_REMOVED:
			refreshConnector();
		}
	}
}
