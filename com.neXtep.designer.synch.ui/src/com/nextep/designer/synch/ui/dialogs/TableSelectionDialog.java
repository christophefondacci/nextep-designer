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
package com.nextep.designer.synch.ui.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import com.nextep.datadesigner.gui.impl.swt.TableColumnSorter;
import com.nextep.designer.synch.ui.SynchUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.base.AbstractUIComponent;
import com.nextep.designer.vcs.model.IVersionable;
import com.nextep.designer.vcs.ui.jface.VersionableNewLabelProvider;
import com.nextep.designer.vcs.ui.jface.VersionableTableContentProvider;

public class TableSelectionDialog extends AbstractUIComponent implements ITitleAreaComponent {

	private Collection<IVersionable<?>> selected;
	private Collection<IVersionable<?>> eligibles;
	private Button addButton, removeButton;

	public TableSelectionDialog(Collection<IVersionable<?>> selected,
			Collection<IVersionable<?>> eligibles) {
		this.selected = selected;
		this.eligibles = eligibles;
	}

	@Override
	public Control create(Composite parent) {
		final Composite editor = new Composite(parent, SWT.NONE);
		editor.setLayout(new GridLayout(3, false));
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Creating eligible versionable viewer
		final TableViewer eligibleViewer = createViewer(editor, SynchUIMessages.getString("dialog.dataTableSelction.eligibleTables")); //$NON-NLS-1$
		Collection<IVersionable<?>> unselected = new ArrayList<IVersionable<?>>();
		unselected.addAll(eligibles);
		unselected.removeAll(selected);
		eligibleViewer.setInput(unselected);
		eligibleViewer.setComparator(new TableColumnSorter(eligibleViewer.getTable(),
				eligibleViewer));

		addButton = new Button(editor, SWT.PUSH);
		addButton.setImage(ImageFactory.ICON_RIGHT_TINY);
		addButton.setToolTipText(SynchUIMessages.getString("dialog.dataTableSelction.addSelection")); //$NON-NLS-1$
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));

		final TableViewer viewer = createViewer(editor, SynchUIMessages.getString("dialog.dataTableSelction.synchronizedTables")); //$NON-NLS-1$
		viewer.setInput(selected);
		viewer.setComparator(new TableColumnSorter(viewer.getTable(), viewer));

		removeButton = new Button(editor, SWT.PUSH);
		removeButton.setImage(ImageFactory.ICON_LEFT_TINY);
		removeButton.setToolTipText(SynchUIMessages.getString("dialog.dataTableSelction.removeSelection")); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));

		addButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final Collection<IVersionable<?>> transferred = transferSelection(eligibleViewer,
						viewer);
				selected.addAll(transferred);
			}
		});

		eligibleViewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				final Collection<IVersionable<?>> transferred = transferSelection(eligibleViewer,
						viewer);
				selected.addAll(transferred);
			}
		});
		removeButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final Collection<IVersionable<?>> transferred = transferSelection(viewer,
						eligibleViewer);
				selected.removeAll(transferred);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				final Collection<IVersionable<?>> transferred = transferSelection(viewer,
						eligibleViewer);
				selected.removeAll(transferred);
			}
		});
		return editor;
	}

	private TableViewer createViewer(Composite parent, String title) {
		final Composite labeledEditor = new Composite(parent, SWT.NONE);
		GridData editorData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 2);
		editorData.widthHint = 150;
		editorData.heightHint = 250;
		labeledEditor.setLayoutData(editorData);
		final Table table = new Table(labeledEditor, SWT.BORDER | SWT.MULTI);
		final TableColumn col = new TableColumn(table, SWT.NONE);
		col.setWidth(150);
		col.setText(title);
		table.setHeaderVisible(true);
		// Using this specific layout to make sure columns are always 100%
		TableColumnLayout colLayout = new TableColumnLayout();
		colLayout.setColumnData(col, new ColumnWeightData(100));
		labeledEditor.setLayout(colLayout);

		// Initializing our viewer
		final TableViewer viewer = new TableViewer(table);
		viewer.setLabelProvider(new DecoratingStyledCellLabelProvider(
				new VersionableNewLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager()
						.getLabelDecorator(), null));
		viewer.setContentProvider(new VersionableTableContentProvider());

		return viewer;
	}

	private Collection<IVersionable<?>> transferSelection(TableViewer sourceViewer,
			TableViewer targetViewer) {
		final ISelection s = sourceViewer.getSelection();
		final Collection<IVersionable<?>> transferredElts = new ArrayList<IVersionable<?>>();
		if (s != null && !s.isEmpty() && s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			Iterator<?> it = sel.iterator();
			while (it.hasNext()) {
				IVersionable<?> v = (IVersionable<?>) it.next();
				targetViewer.add(v);
				sourceViewer.remove(v);
				transferredElts.add(v);
			}
		}
		return transferredElts;
	}

	public Collection<IVersionable<?>> getSelection() {
		return selected;
	}

	@Override
	public String getAreaTitle() {
		return SynchUIMessages.getString("dialog.dataTableSelction.title"); //$NON-NLS-1$
	}

	@Override
	public String getDescription() {
		return SynchUIMessages.getString("dialog.dataTableSelction.description"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return null;
	}

}
