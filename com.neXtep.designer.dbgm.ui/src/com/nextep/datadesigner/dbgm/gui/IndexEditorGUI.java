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
package com.nextep.datadesigner.dbgm.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IndexType;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.editors.ButtonEditor;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IFormatter;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.model.IReferenceable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * Editor component for updating standard indexes.
 * 
 * @author Christophe Fondacci
 */
public class IndexEditorGUI extends ControlledDisplayConnector implements SelectionListener {

	private final static Log log = LogFactory.getLog(IndexEditorGUI.class);
	private Composite editor = null;
	private Composite indexEditor = null; // @jve:decl-index=0:visual-constraint="10,10"
	private Label nameLabel = null;
	private Text nameText = null;
	private Label descLabel = null;
	private Text descText = null;
	private Link tableLabel;
	private Text tableText;
	private Button changeTableButton;
	private Label typeLabel = null;
	private Collection<Button> indexTypeButtons = new ArrayList<Button>();
	private IDisplayConnector columnEditor;
	private IDisplayConnector keyColumnEditor;
	private Button orderUpButton;
	private Button orderDownButton;
	private Button upButton;
	private Button downButton;

	public IndexEditorGUI(IIndex index, ITypedObjectUIController controller) {
		super(index, controller);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		final IIndex index = (IIndex) getModel();
		editor = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		editor.setLayout(layout);
		GridData gridData9 = new GridData();
		gridData9.horizontalSpan = 1;
		GridData gridData8 = new GridData();
		gridData8.horizontalSpan = 1;
		GridData gridData7 = new GridData();
		gridData7.horizontalSpan = 1;
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.CENTER;
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.verticalAlignment = GridData.CENTER;
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.verticalAlignment = GridData.CENTER;
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.CENTER;
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.horizontalSpan = 4;
		gridData1.verticalAlignment = GridData.CENTER;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 4;
		gridData.verticalAlignment = GridData.CENTER;

		indexEditor = new Composite(editor, SWT.NONE);
		addNoMarginLayout(indexEditor, 5);
		GridData indData = new GridData();
		indData.horizontalSpan = 4;
		indData.grabExcessHorizontalSpace = true;
		indData.horizontalAlignment = GridData.FILL;
		indexEditor.setLayoutData(indData);
		nameLabel = new Label(indexEditor, SWT.RIGHT);
		nameLabel.setText(DBGMUIMessages.getString("editor.index.name")); //$NON-NLS-1$
		nameLabel.setLayoutData(gridData2);

		nameText = new Text(indexEditor, SWT.BORDER);
		nameText.setLayoutData(gridData1);
		ColorFocusListener.handle(nameText);
		TextEditor.handle(nameText, ChangeEvent.NAME_CHANGED, this);
		descLabel = new Label(indexEditor, SWT.RIGHT);
		descLabel.setText(DBGMUIMessages.getString("editor.index.description")); //$NON-NLS-1$
		descLabel.setLayoutData(gridData3);
		descText = new Text(indexEditor, SWT.BORDER);
		descText.setLayoutData(gridData);
		ColorFocusListener.handle(descText);
		TextEditor.handle(descText, ChangeEvent.DESCRIPTION_CHANGED, this);
		tableLabel = new Link(indexEditor, SWT.NONE);
		tableLabel.setText(DBGMUIMessages.getString("editor.index.indexedTable")); //$NON-NLS-1$
		tableLabel.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (index.getIndexedTableRef() != null) {
					try {
						final IBasicTable t = index.getIndexedTable();
						UIControllerFactory.getController(t).defaultOpen(t);
					} catch (Exception ex) {
						log.error("Cannot open table", ex); //$NON-NLS-1$
					}
				}
			}
		});
		GridData comboData = new GridData();
		comboData.horizontalSpan = 3;
		comboData.horizontalAlignment = GridData.FILL;
		comboData.grabExcessHorizontalSpace = true;

		tableText = new Text(indexEditor, SWT.BORDER);
		tableText.setEditable(false);
		tableText.setFont(FontFactory.FONT_BOLD);
		tableText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));

		changeTableButton = new Button(indexEditor, SWT.PUSH);
		changeTableButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeTableButton.setToolTipText(DBGMUIMessages.getString("toolTipChangeRemoteTable")); //$NON-NLS-1$
		changeTableButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));
		changeTableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ITypedObject obj = VCSUIPlugin.getService(ICommonUIService.class).findElement(
						indexEditor.getShell(),
						DBGMUIMessages.getString("editor.index.selectIndexedTableTitle"), //$NON-NLS-1$
						IElementType.getInstance(IBasicTable.TYPE_ID));
				if (obj != null) {
					handleEvent(ChangeEvent.PARENT_CHANGED, (IObservable) getModel(), obj);
				}
			}
		});
		// We don't want to see the change table button when index already has a table
		if (index.getIndexedTableRef() != null) {
			changeTableButton.setVisible(false);
		}
		typeLabel = new Label(indexEditor, SWT.RIGHT);
		typeLabel.setText(DBGMUIMessages.getString("editor.index.type")); //$NON-NLS-1$
		typeLabel.setLayoutData(gridData4);
		createIndexTypeButtons(indexEditor);
		new Label(indexEditor, SWT.NONE);
		createIndexColumnsEditor();
		return editor;
	}

	private void createIndexTypeButtons(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		addNoMarginLayout(c, IndexType.values().length);
		for (IndexType type : IndexType.values()) {
			if (type.isAvailableFor(DBGMHelper.getVendorFor((ITypedObject) getModel()))) {
				final Button typeButton = new Button(c, SWT.RADIO);
				typeButton.setText(IFormatter.PROPPER_LOWER.format(type.name()).replace('_', ' '));
				typeButton.setData(type);
				typeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
				ButtonEditor.handle(typeButton, ChangeEvent.CUSTOM_5, this);
				indexTypeButtons.add(typeButton);
			}
		}
	}

	private void createIndexColumnsEditor() {
		Composite c = new Composite(editor, SWT.BORDER);
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		c.setLayout(new GridLayout(4, false));
		c.setBackground(FontFactory.WHITE);
		Label infoLabel = new Label(c, SWT.WRAP);
		infoLabel.setBackground(FontFactory.WHITE);
		GridData labelData = new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1);
		labelData.widthHint = 400;
		infoLabel.setLayoutData(labelData);
		infoLabel.setText(DBGMUIMessages.getString("indexCreationInfo")); //$NON-NLS-1$
		createEligibleTables(c);
		createAddColumnToIndexButton(c);
		createKeyColumns(c);
		createOrderUpButton(c);
		createRemoveColumnFromIndexButton(c);
		createOrderDownButton(c);
	}

	/**
	 * Creates the eligible lists (table columns & foreign keys)
	 */
	private void createEligibleTables(Composite parent) {
		// Adding columns displayer
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessVerticalSpace = true;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.horizontalSpan = 1;
		gridData3.verticalSpan = 2;
		gridData3.verticalAlignment = GridData.FILL;
		gridData3.minimumHeight = 100;

		IIndex index = (IIndex) getModel();
		List<IBasicColumn> eligibleColumns = null;
		if (index.getIndexedTableRef() != null) {
			try {
				eligibleColumns = index.getIndexedTable().getColumns();
			} catch (UnresolvedItemException e) {
				eligibleColumns = Collections.emptyList();
			} catch (ErrorException e) {
				eligibleColumns = Collections.emptyList();
			}
		}
		columnEditor = new ColumnsDisplayConnector(eligibleColumns,
				DBGMUIMessages.getString("editor.index.eligibleColumns")); //$NON-NLS-1$
		Control c = columnEditor.create(parent);
		c.addListener(SWT.MouseDoubleClick, new Listener() {

			@Override
			public void handleEvent(Event event) {
				addColumnToIndex();
			}
		});
		c.setLayoutData(gridData3);
	}

	private void createAddColumnToIndexButton(Composite parent) {
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = GridData.FILL;
		gridData5.verticalAlignment = GridData.END;
		gridData5.grabExcessVerticalSpace = true;
		downButton = new Button(parent, SWT.NONE);
		downButton.setLayoutData(gridData5);
		downButton.setImage(ImageFactory.ICON_RIGHT_TINY);
		downButton.addSelectionListener(this);
	}

	private void createRemoveColumnFromIndexButton(Composite parent) {
		GridData gridData6 = new GridData();
		gridData6.horizontalAlignment = GridData.FILL;
		gridData6.verticalAlignment = GridData.BEGINNING;
		gridData6.grabExcessVerticalSpace = true;
		upButton = new Button(parent, SWT.NONE);
		upButton.setLayoutData(gridData6);
		upButton.setImage(ImageFactory.ICON_LEFT_TINY);
		upButton.addSelectionListener(this);
	}

	/**
	 * Creates the key columns table list
	 */
	private void createKeyColumns(Composite parent) {
		GridData gridData11 = new GridData();
		gridData11.verticalAlignment = GridData.FILL;
		gridData11.grabExcessHorizontalSpace = true;
		gridData11.verticalSpan = 2;
		gridData11.grabExcessVerticalSpace = true;
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.minimumHeight = 100;

		keyColumnEditor = new ColumnsDisplayConnector(null,
				DBGMUIMessages.getString("editor.index.indexedColumns")); //$NON-NLS-1$
		Control c = keyColumnEditor.create(parent);
		c.setLayoutData(gridData11);
	}

	private void createOrderUpButton(Composite parent) {
		GridData gridData31 = new GridData();
		gridData31.grabExcessVerticalSpace = true;
		gridData31.verticalAlignment = GridData.CENTER;
		orderUpButton = new Button(parent, SWT.LEFT | SWT.BOTTOM);
		orderUpButton.setText(DBGMUIMessages.getString("editor.index.up")); //$NON-NLS-1$
		orderUpButton.setLayoutData(gridData31);
		orderUpButton.setImage(ImageFactory.ICON_UP_TINY);
		orderUpButton.addSelectionListener(this);
	}

	private void createOrderDownButton(Composite parent) {
		GridData gridData41 = new GridData();
		gridData41.grabExcessVerticalSpace = true;
		gridData41.verticalAlignment = GridData.CENTER;
		orderDownButton = new Button(parent, SWT.LEFT | SWT.UP);
		orderDownButton.setText(DBGMUIMessages.getString("editor.index.down")); //$NON-NLS-1$
		orderDownButton.setLayoutData(gridData41);
		orderDownButton.setImage(ImageFactory.ICON_DOWN_TINY);
		orderDownButton.addSelectionListener(this);
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
		IIndex index = (IIndex) getModel();
		boolean enabled = !index.updatesLocked();
		// NAme / desc management
		nameText.setText(notNull(index.getIndexName()));
		descText.setText(notNull(index.getDescription()));
		// Handling index type
		for (Button b : indexTypeButtons) {
			final IndexType buttonType = (IndexType) b.getData();
			if (buttonType == index.getIndexType()) {
				b.setSelection(true);
			} else {
				b.setSelection(false);
			}
			b.setEnabled(enabled);
		}
		// Refreshing columns connectors
		columnEditor.refreshConnector();
		keyColumnEditor.refreshConnector();

		nameText.setEnabled(enabled);
		descText.setEnabled(enabled);
		changeTableButton.setEnabled(enabled);

		final IReference tableRef = index.getIndexedTableRef();
		try {
			if (tableRef != null) {
				IBasicTable t = (IBasicTable) VersionHelper.getReferencedItem(tableRef);
				tableText.setText(t.getName());
			} else {
				tableText.setText(""); //$NON-NLS-1$
			}
		} catch (UnresolvedItemException e) {
			tableText.setText(DBGMUIMessages.getString("editor.index.unresolvedTable")); //$NON-NLS-1$
		} catch (ErrorException e) {
			tableText.setText(DBGMUIMessages.getString("editor.index.errorTable")); //$NON-NLS-1$
		}
	}

	/**
	 * @see com.nextep.datadesigner.model.IEventListener#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IIndex index = (IIndex) getModel();
		switch (event) {
		case NAME_CHANGED:
			index.setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			index.setDescription((String) data);
			break;
		case CUSTOM_5:
			if (data instanceof Button) {
				final Button b = (Button) data;
				if (b.getData() instanceof IndexType) {
					index.setIndexType((IndexType) b.getData());
				}
			}
			break;
		case PARENT_CHANGED:
			IReference tableRef = null;
			if (data instanceof IReference) {
				tableRef = (IReference) data;
			} else if (data instanceof IReferenceable) {
				tableRef = ((IReferenceable) data).getReference();
			}
			index.setIndexedTableRef(tableRef);
			final List<IReference> columns = new ArrayList<IReference>(index.getIndexedColumnsRef());
			for (IReference r : columns) {
				index.removeColumnRef(r);
			}
			initialize();
			break;
		case COLUMN_ADDED:
			columnAdded((IBasicColumn) VersionHelper.getReferencedItem((IReference) data));
			break;
		case COLUMN_REMOVED:
			columnRemoved((IBasicColumn) VersionHelper.getReferencedItem((IReference) data));
			break;
		}
		refreshConnector();
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
	}

	/**
	 * Adds the currently selected column (if any) to the edited index
	 */
	private void addColumnToIndex() {
		IIndex index = (IIndex) getModel();
		index = VCSUIPlugin.getVersioningUIService().ensureModifiable(index);
		// Retrieving column selection
		IBasicColumn c = (IBasicColumn) columnEditor.getModel();
		if (c != null) {
			index.addColumnRef(c.getReference());
			// Notifying column (which may have flags to show index / pk status, etc)
			c.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		IIndex index = (IIndex) getModel();
		// Following actions need to modify the model so we must
		// ensure we are allowed to alter the model
		index = VCSUIPlugin.getVersioningUIService().ensureModifiable(index);
		if (e.getSource() == downButton) {
			addColumnToIndex();
		} else if (e.getSource() == upButton) {
			// Retrieving column selection
			IBasicColumn c = (IBasicColumn) keyColumnEditor.getModel();
			if (c != null) {
				index.removeColumnRef(c.getReference());
				// Propagate column changes
				c.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			}
		} else if (e.getSource() == orderUpButton) {
			// Retrieving selection
			final IBasicColumn selection = (IBasicColumn) keyColumnEditor.getModel();
			if (selection != null) {
				indexColumnUp(index, selection);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, selection, null);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, selection, index
						.getIndexedColumnsRef().indexOf(selection.getReference()));
			}
		} else if (e.getSource() == orderDownButton) {
			// Retrieving selection
			IBasicColumn selection = (IBasicColumn) keyColumnEditor.getModel();
			if (selection != null) {
				indexColumnDown(index, selection);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, selection, null);
				keyColumnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, selection, index
						.getIndexedColumnsRef().indexOf(selection.getReference()));
			}
		}
	}

	private void columnAdded(IBasicColumn c) {
		columnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, c, null);
		keyColumnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, c, null);
	}

	private void columnRemoved(IBasicColumn c) {
		columnEditor.handleEvent(ChangeEvent.COLUMN_ADDED, c, null);
		keyColumnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, c, null);
	}

	public void initialize() {
		IIndex index = (IIndex) getModel();
		// Getting index columns
		List<IBasicColumn> indexColumns = null;
		try {
			indexColumns = index.getColumns();
		} catch (UnresolvedItemException e) {
			indexColumns = Collections.emptyList();
		}
		keyColumnEditor.handleEvent(ChangeEvent.SET_COLUMNS, null, indexColumns);
		// Getting eligible columns (aka non-indexed columns)
		List<IBasicColumn> eligibleColumns = null;
		if (index.getIndexedTableRef() != null) {
			try {
				eligibleColumns = index.getIndexedTable().getColumns();
			} catch (UnresolvedItemException e) {
				eligibleColumns = Collections.emptyList();
			}
		}
		columnEditor.handleEvent(ChangeEvent.SET_COLUMNS, null, eligibleColumns);
		// Removing already constrained columns from column editor
		for (IBasicColumn col : indexColumns) {
			columnEditor.handleEvent(ChangeEvent.COLUMN_REMOVED, col, null);
		}
		// Superclass
		super.initialize();
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.ListeningConnector#setModel(java.lang.Object)
	 */
	@Override
	public void setModel(Object model) {
		super.setModel(model);
		if (getSWTConnector() != null) {
			initialize();
		}
	}

	protected IDisplayConnector getIndexColumnsEditor() {
		return keyColumnEditor;
	}

	@Override
	public Image getConnectorIcon() {
		return DBGMImages.WIZARD_INDEX;
	}

	private void indexColumnUp(IIndex index, IBasicColumn column) {
		final IReference columnRef = column.getReference();
		final List<IReference> colRefs = index.getIndexedColumnsRef();
		int colIndex = colRefs.indexOf(columnRef);
		if (colIndex > 0) {
			final IBasicColumn swappedCol = index.getColumns().get(colIndex - 1);
			Collections.swap(colRefs, colIndex, colIndex - 1);
			index.notifyListeners(ChangeEvent.MODEL_CHANGED, column);
			column.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			swappedCol.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}

	private void indexColumnDown(IIndex index, IBasicColumn c) {
		final IReference columnRef = c.getReference();
		final List<IReference> colRef = index.getIndexedColumnsRef();
		int colIndex = colRef.indexOf(columnRef);
		if (colIndex < colRef.size() - 1) {
			final IBasicColumn swappedCol = index.getColumns().get(colIndex + 1);
			Collections.swap(colRef, colIndex, colIndex + 1);
			index.notifyListeners(ChangeEvent.MODEL_CHANGED, c);
			c.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
			swappedCol.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
		}
	}
}
