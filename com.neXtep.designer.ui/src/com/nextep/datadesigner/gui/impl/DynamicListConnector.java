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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.gui.model.IDynamicListProvider;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.designer.ui.UIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;

public class DynamicListConnector extends TableDisplayConnector {

	private Table listTable;
	private NextepTableEditor editor;
	private IDynamicListProvider provider;
	private Composite dynamicEditor;
	private SashForm form;
	private IDisplayConnector dynamicConnector = null;

	private Button newButton;
	private Button delButton;

	public DynamicListConnector(IObservable parent, IDynamicListProvider provider) {
		super(parent, null);
		this.provider = provider;
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		form = new SashForm(parent, SWT.HORIZONTAL);

		Composite listComposite = new Composite(form, SWT.NONE);
		addNoMarginLayout(listComposite, 1);

		Composite toolbar = new Composite(listComposite, SWT.NONE);
		addNoMarginLayout(toolbar, 2);

		newButton = new Button(toolbar, SWT.PUSH);
		newButton.setImage(ImageFactory.ICON_ADD_TINY);
		newButton.setToolTipText(UIMessages.getString("addGenericTooltip"));
		newButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				provider.add(getModel());
				refreshConnector();
			}
		});

		delButton = new Button(toolbar, SWT.PUSH);
		delButton.setImage(ImageFactory.ICON_DELETE);
		delButton.setToolTipText(UIMessages.getString("delGenericTooltip"));
		delButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IObservable o = getSelection();
				if (o != null) {
					// First of all we dispose the editor on the element about to be deleted
					if (dynamicConnector != null && dynamicConnector.getSWTConnector() != null) {
						dynamicConnector.getSWTConnector().dispose();
						dynamicConnector = null;
					}
					provider.remove(getModel(), o);
					removeTableItem(o);
				}
			}
		});
		listTable = new Table(listComposite, SWT.BORDER | SWT.FULL_SELECTION);
		listTable.setLinesVisible(true);
		listTable.setHeaderVisible(true);
		listTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableColumn nameCol = new TableColumn(listTable, SWT.NONE);
		nameCol.setWidth(120);
		nameCol.setText("Name");
		TableColumn typeColumn = new TableColumn(listTable, SWT.NONE);
		typeColumn.setWidth(70);
		typeColumn.setText("Type");
		listTable.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// First releasing the existing connector
				if (dynamicConnector != null && dynamicConnector.getSWTConnector() != null) {
					dynamicConnector.getSWTConnector().dispose();
				}
				// Retrieving table selection
				final IObservable selection = getSelection();
				if (selection != null && (selection instanceof ITypedObject)) {
					dynamicConnector = UIControllerFactory.getController(selection)
							.initializeEditor(selection);
					dynamicConnector.create(dynamicEditor);
					dynamicConnector.refreshConnector();
					Control c = dynamicConnector.getSWTConnector();
					c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
					c.setFocus();
					dynamicEditor.layout();// redraw();
				}
			}
		});
		editor = NextepTableEditor.handle(listTable);

		dynamicEditor = new Composite(form, SWT.BORDER);
		addNoMarginLayout(dynamicEditor, 1);

		initializeTable(listTable, editor);
		form.setWeights(new int[] { 1, 3 });
		return form;
	}

	@Override
	public Control getSWTConnector() {
		return form;
	}

	@Override
	public void refreshConnector() {
		final List<TableItem> items = new ArrayList<TableItem>();
		for (IObservable o : provider.getList(getModel())) {
			TableItem i = getOrCreateItem(o);
			items.add(i);
			String name = ((INamedObject) o).getName();
			// Specific process for [] prefixed names
			int ind = name.indexOf(']');
			if (ind >= 0) {
				name = name.substring(ind + 1);
			}
			i.setText(name);
			i.setText(1, ((ITypedObject) o).getType().getName());
			i.setImage(ImageFactory.getImage(((ITypedObject) o).getType().getIcon()));
		}
		for (TableItem i : listTable.getItems()) {
			if (!items.contains(i)) {
				removeTableItem((IObservable) i.getData());
			}
		}
	}

	@Override
	public void setModel(Object model) {
		super.setModel(model);
		// Model change, unselecting dynamic editor
		if (dynamicConnector != null && dynamicConnector.getSWTConnector() != null) {
			dynamicConnector.getSWTConnector().dispose();
			// Safety precaution, we unregister any listener which might have been directly
			// instigated by the connector.
			Designer.getListenerService().unregisterListeners(dynamicConnector);
			listTable.deselectAll();
		}
	}

	@Override
	protected IEventListener createModelListener(IObservable model) {
		return new ParentModelListener(this) {

			@Override
			public void handleEvent(ChangeEvent event, IObservable source, Object data) {
				// Display.getDefault().syncExec(new Runnable() {
				//
				// @Override
				// public void run() {
				refreshConnector();
				// }
				// });
			}
		};
	}
}
