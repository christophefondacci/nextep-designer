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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.dbgm.model.IDatabaseObject;
import com.nextep.datadesigner.gui.impl.TableDisplayConnector;
import com.nextep.datadesigner.gui.impl.editors.ComboColumnEditor;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.sqlgen.gui.jface.SynchronizationFilterTable;
import com.nextep.datadesigner.sqlgen.impl.SynchronizationFilter;
import com.nextep.datadesigner.sqlgen.model.ISynchronizationFilter;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.sqlgen.ui.SQLGenImages;
import com.nextep.designer.sqlgen.ui.SQLMessages;
import com.nextep.designer.vcs.model.IVersionContainer;
import com.nextep.designer.vcs.model.IVersionable;

public class SynchronizationFilterEditor extends TableDisplayConnector {

	private Composite editor;
	private Table filtersTable;
	private TableViewer filtersViewer;
	private Button addFilterButton;
	private Button removeFilterButton;
	private NextepTableEditor tabEditor;
	private List<ISynchronizationFilter> filters;

	public SynchronizationFilterEditor() {
		super(null, null);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(1, false));

		Label overHelp = new Label(editor, SWT.WRAP);
		overHelp.setText(SQLMessages.getString("synchronizationFiltersHelp"));
		GridData overData = new GridData(GridData.FILL, GridData.FILL, true, false);
		overData.widthHint = 500;
		overHelp.setLayoutData(overData);

		Composite toolbox = new Composite(editor, SWT.NONE);
		GridLayout toolLayout = new GridLayout();
		toolLayout.marginBottom = toolLayout.marginHeight = toolLayout.marginLeft = toolLayout.marginRight = toolLayout.marginTop = toolLayout.marginWidth = 0;
		toolLayout.numColumns = 2;
		toolbox.setLayout(toolLayout);
		toolbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addFilterButton = new Button(toolbox, SWT.PUSH);
		addFilterButton.setImage(SQLGenImages.ICON_FILTER_ADD);
		addFilterButton.setToolTipText(SQLMessages.getString("synchronizationFiltersAdd"));
		addFilterButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IVersionContainer c = (IVersionContainer) getModel();
				ISynchronizationFilter filter = new SynchronizationFilter(VersionHelper
						.getVersionable(c), "", null);
				CorePlugin.getIdentifiableDao().save(filter);
				refreshFilters();
				refreshConnector();
			}
		});
		removeFilterButton = new Button(toolbox, SWT.PUSH);
		removeFilterButton.setToolTipText(SQLMessages.getString("synchronizationFiltersRemove"));
		removeFilterButton.setImage(SQLGenImages.ICON_FILTER_DEL);
		removeFilterButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem[] selItems = filtersTable.getSelection();
				if (selItems.length > 0) {
					ISynchronizationFilter filter = (ISynchronizationFilter) selItems[0].getData();
					CorePlugin.getIdentifiableDao().delete(filter);
					selItems[0].dispose();
					refreshFilters();
					// refreshConnector();
				}
			}
		});

		filtersTable = SynchronizationFilterTable.create(editor);
		filtersTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tabEditor = NextepTableEditor.handle(filtersTable);
		initializeTable(filtersTable, tabEditor);
		TextColumnEditor.handle(tabEditor, 0, ChangeEvent.NAME_CHANGED, this);
		List<IElementType> types = new ArrayList<IElementType>();
		for (IElementType t : IElementType.values()) {
			if (IDatabaseObject.class.isAssignableFrom(t.getInterface())) {
				types.add(t);
			}
		}
		Collections.sort(types, NameComparator.getInstance());
		ComboColumnEditor.handle(tabEditor, 1, ChangeEvent.CUSTOM_1, this, types);

		refreshFilters();
		return editor;
	}

	@Override
	public Control getSWTConnector() {
		return editor;
	}

	@Override
	public void refreshConnector() {

		for (ISynchronizationFilter filter : filters) {
			TableItem i = getOrCreateItem(filter);
			i.setText(notNull(filter.getName()));
			i.setText(1, filter.getType() == null ? "" : notNull(filter.getType().getName()));
			i.setImage(SQLGenImages.ICON_FILTER);
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (!(source instanceof ISynchronizationFilter)) {
			return;
		}
		ISynchronizationFilter filter = (ISynchronizationFilter) source;
		switch (event) {
		case NAME_CHANGED:
			filter.setName((String) data);
			CorePlugin.getIdentifiableDao().save(filter);
			refreshFilters();
			break;
		case CUSTOM_1:
			if (data instanceof String) {
				filter.setType(null);
			} else {
				filter.setType(((IElementType) data));
			}
			CorePlugin.getIdentifiableDao().save(filter);
			refreshFilters();
			break;
		}
		refreshConnector();
	}

	private void refreshFilters() {
		IVersionContainer c = (IVersionContainer) getModel();
		Collection<ISynchronizationFilter> f = getFilters(c);
		if (f != null) {
			filters = new ArrayList<ISynchronizationFilter>(f);
			Collections.sort(filters, NameComparator.getInstance());
		} else {
			filters = Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	public static Collection<ISynchronizationFilter> getFilters(IVersionContainer c) {
		// return Collections.EMPTY_LIST;
		IVersionable<IVersionContainer> v = VersionHelper.getVersionable(c);
		if (v == null || v.getReference() == null || v.getReference().getUID() == null)
			return Collections.emptyList();
		Collection<ISynchronizationFilter> filters = (Collection<ISynchronizationFilter>) CorePlugin.getIdentifiableDao().loadForeignKey(SynchronizationFilter.class,
						v.getReference().getUID(), "containerRef", false); //$NON-NLS-1$
		if (filters == null) {
			return Collections.emptyList();
		}
		return filters;
	}
}
