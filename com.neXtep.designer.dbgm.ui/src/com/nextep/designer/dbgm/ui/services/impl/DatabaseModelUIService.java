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
 * along with neXtep designer.  
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     neXtep Softwares - initial API and implementation
 *******************************************************************************/
package com.nextep.designer.dbgm.ui.services.impl;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import com.nextep.datadesigner.dbgm.model.IColumnable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.jface.ColumnCellModifier;
import com.nextep.designer.dbgm.ui.jface.ColumnTableLabelProvider;
import com.nextep.designer.dbgm.ui.jface.ColumnsContentProvider;
import com.nextep.designer.dbgm.ui.services.IDatabaseModelUIService;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.IGlobalSelectionProvider;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * Default {@link IDatabaseModelUIService} implementation.
 * 
 * @author Christophe Fondacci
 */
public class DatabaseModelUIService implements IDatabaseModelUIService {

	@Override
	public Composite createColumnEditor(IWorkbenchPart part, Composite parentComposite,
			IColumnable parent) {
		final Table columnsTable = createColumnsTable(parentComposite);
		columnsTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		final TableViewer columnViewer = new TableViewer(columnsTable);
		columnViewer.setLabelProvider(new ColumnTableLabelProvider());
		columnViewer.setContentProvider(new ColumnsContentProvider());

		// Setting up editors for every column
		CellEditor[] editors = new CellEditor[6];
		editors[0] = new TextCellEditor(columnsTable);
		editors[1] = new TextCellEditor(columnsTable);
		editors[2] = new TextCellEditor(columnsTable);
		editors[3] = new TextCellEditor(columnsTable);
		editors[4] = new CheckboxCellEditor(columnsTable);
		editors[5] = new TextCellEditor(columnsTable);
		columnViewer.setCellEditors(editors);
		columnViewer.setColumnProperties(new String[] { ColumnCellModifier.PROP_NAME,
				ColumnCellModifier.PROP_DATATYPE, ColumnCellModifier.PROP_LENGTH,
				ColumnCellModifier.PROP_PRECISION, ColumnCellModifier.PROP_NOTNULL,
				ColumnCellModifier.PROP_DEFAULT });

		// Setting up the modifier which fetches information from model and set modifications back
		columnViewer.setCellModifier(new ColumnCellModifier());
		columnViewer.setInput(parent);
		registerContextMenu(columnViewer, part);
		columnViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					final ITypedObject o = (ITypedObject) ((IStructuredSelection) event
							.getSelection()).getFirstElement();
					final ITypedObjectUIController controller = UIControllerFactory
							.getController(o);
					controller.defaultOpen(o);
				}
			}
		});
		return columnsTable;
	}

	/**
	 * Creates the SWT table widget which will hosts the columns display.
	 * 
	 * @param parent parent composite to create the table in
	 * @return the created {@link Table}
	 */
	private Table createColumnsTable(Composite parent) {
		final Table t = new Table(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		t.setLinesVisible(true);
		t.setHeaderVisible(true);
		final TableColumn c1 = new TableColumn(t, SWT.NONE);
		c1.setText(DBGMUIMessages.getString("service.dbModelUI.colColName")); //$NON-NLS-1$
		c1.setWidth(120);
		final TableColumn c2 = new TableColumn(t, SWT.NONE);
		c2.setText(DBGMUIMessages.getString("service.dbModelUI.colDatatype")); //$NON-NLS-1$
		c2.setWidth(80);
		final TableColumn c3 = new TableColumn(t, SWT.RIGHT);
		c3.setText(DBGMUIMessages.getString("service.dbModelUI.colLength")); //$NON-NLS-1$
		c3.setWidth(40);
		final TableColumn c4 = new TableColumn(t, SWT.RIGHT);
		c4.setText(DBGMUIMessages.getString("service.dbModelUI.colPrecision")); //$NON-NLS-1$
		c4.setWidth(30);
		final TableColumn c5 = new TableColumn(t, SWT.CENTER);
		c5.setText(DBGMUIMessages.getString("service.dbModelUI.colNotNull")); //$NON-NLS-1$
		c5.setWidth(50);
		final TableColumn c6 = new TableColumn(t, SWT.NONE);
		c6.setText(DBGMUIMessages.getString("service.dbModelUI.colDefault")); //$NON-NLS-1$
		c6.setWidth(50);
		return t;
	}

	private void registerContextMenu(TableViewer viewer, IWorkbenchPart part) {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);

		// this is to work around complaints about missing standard groups.
		contextMenu.addMenuListener(new IMenuListener() {

			public void menuAboutToShow(IMenuManager manager) {
				manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
				manager.add(new GroupMarker("sql"));
				manager.add(new Separator());
				manager.add(new GroupMarker("actions"));
				manager.add(new Separator());
				manager.add(new GroupMarker("version")); //$NON-NLS-1$
			}
		});

		if (part != null) {
			final IWorkbenchPartSite menuSite = part.getSite();
			if (menuSite != null) {
				ISelectionProvider globalProvider = menuSite.getSelectionProvider();
				if (globalProvider instanceof IGlobalSelectionProvider) {
					((IGlobalSelectionProvider) globalProvider).registerSelectionProvider(part,
							viewer);
				}
				menuSite.registerContextMenu("columnsEditor_" + viewer.toString(), contextMenu,
						viewer);
			}
		}
		Menu menu = contextMenu.createContextMenu(viewer.getTable());
		viewer.getTable().setMenu(menu);
	}

}
