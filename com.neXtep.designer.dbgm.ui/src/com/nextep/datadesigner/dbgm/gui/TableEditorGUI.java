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
import java.util.Collections;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.impl.UniqueKeyConstraint;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.DynamicListConnector;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.IDisplayConnector;
import com.nextep.datadesigner.impl.NameComparator;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.ITypedObject;
import com.nextep.datadesigner.vcs.gui.external.DynamicListProvider;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITitleAreaComponent;
import com.nextep.designer.ui.model.ITypedObjectUIController;

public class TableEditorGUI extends ControlledDisplayConnector implements DisposeListener,
		ITitleAreaComponent {

	// private static final Log log = LogFactory.getLog(TableEditorGUI.class);
	private Text nameText = null;
	private Text shortNameText = null;
	private Text descriptionText = null;
	private TabFolder tabFolder = null;
	private Composite editorGroup = null;
	private IDisplayConnector colEditGUI = null;
	private List<IDisplayConnector> connectors = null;

	public TableEditorGUI(IBasicTable table, ITypedObjectUIController controller) {
		super(table, controller);

		// Initializing column editor
		colEditGUI = UIControllerFactory.getController(
				IElementType.getInstance(IBasicColumn.TYPE_ID)).initializeEditor(table);
		connectors = new ArrayList<IDisplayConnector>();
	}

	protected Composite createPropertiesGroup(Composite parent) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 2;
		editorGroup = new Composite(parent, SWT.NONE);
		editorGroup.setLayout(gridLayout1);
		editorGroup.setLayoutData(gridData);

		// Creating the table name text field
		createNameText();
		createShortNameText();

		// Creating the table description text field
		createDescriptionText();
		return editorGroup;
	}

	private void createNameText() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = GridData.CENTER;
		CLabel nameLabel = new CLabel(editorGroup, SWT.RIGHT);
		nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		final ITypedObject model = (ITypedObject) getModel();
		nameLabel.setText(model.getType().getName() + " name : "); //$NON-NLS-1$
		nameText = new Text(editorGroup, SWT.BORDER);
		nameText.setLayoutData(gridData2);
		nameText.setTextLimit(100);
		// Adding control listeners
		ColorFocusListener.handle(nameText);
		TextEditor.handle(nameText, ChangeEvent.NAME_CHANGED, this);
	}

	private void createShortNameText() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.grabExcessHorizontalSpace = true;
		gridData2.verticalAlignment = GridData.CENTER;
		CLabel shortNameLabel = new CLabel(editorGroup, SWT.RIGHT);
		shortNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		final ITypedObject model = (ITypedObject) getModel();
		shortNameLabel.setText(model.getType().getName() + " short name : "); //$NON-NLS-1$
		shortNameText = new Text(editorGroup, SWT.BORDER);
		shortNameText.setLayoutData(gridData2);
		shortNameText.setTextLimit(20);
		// Adding control listeners
		ColorFocusListener.handle(shortNameText);
		TextEditor.handle(shortNameText, ChangeEvent.SHORTNAME_CHANGED, this);
	}

	private void createDescriptionText() {
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.verticalAlignment = GridData.CENTER;
		CLabel descriptionLabel = new CLabel(editorGroup, SWT.RIGHT);
		descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		descriptionLabel.setText(DBGMUIMessages.getString("editor.table.description")); //$NON-NLS-1$
		descriptionText = new Text(editorGroup, SWT.BORDER);
		descriptionText.setLayoutData(gridData3);
		descriptionText.setTextLimit(200);
		// Adding control listeners
		ColorFocusListener.handle(descriptionText);
		TextEditor.handle(descriptionText, ChangeEvent.DESCRIPTION_CHANGED, this);
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		// Creating the new tab
		createPropertiesGroup(parent);
		// Creating the folder
		tabFolder = new TabFolder(editorGroup, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		TabItem colTab = new TabItem(tabFolder, SWT.NONE);
		colTab.setText(DBGMUIMessages.getString("columnEditorTab")); //$NON-NLS-1$

		// Creating the columns editor
		Control c = colEditGUI.create(tabFolder);
		colTab.setControl(c);

		GridData data = new GridData();
		data.horizontalSpan = 1;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		c.setLayoutData(data);

		// Creating the unique constraint editor
		TabItem keyTab = new TabItem(tabFolder, SWT.NONE);
		keyTab.setText(DBGMUIMessages.getString("uniqueKeyEditorTab")); //$NON-NLS-1$

		DynamicListConnector dynConnector = new DynamicListConnector((IObservable) getModel(),
				new DynamicListProvider() {

					@SuppressWarnings("unchecked")
					@Override
					public List<? extends IObservable> getList(Object fromObject) {
						if (fromObject != null) {
							List<IKeyConstraint> keys = new ArrayList<IKeyConstraint>();
							for (IKeyConstraint key : ((IBasicTable) fromObject).getConstraints()) {
								switch (key.getConstraintType()) {
								case UNIQUE:
								case PRIMARY:
									keys.add(key);
									break;
								}
							}
							Collections.sort(keys, NameComparator.getInstance());
							return keys;
						}
						return Collections.EMPTY_LIST;
					}

					@Override
					public IElementType getType() {
						return IElementType.getInstance(UniqueKeyConstraint.TYPE_ID);
					}
				});
		dynConnector.create(tabFolder);
		dynConnector.refreshConnector();
		connectors.add(dynConnector);
		keyTab.setControl(dynConnector.getSWTConnector());

		// Creating the foreign constraint editor
		TabItem fkTab = new TabItem(tabFolder, SWT.NONE);
		fkTab.setText(DBGMUIMessages.getString("foreignKeyEditorTab")); //$NON-NLS-1$

		DynamicListConnector fkConnector = new DynamicListConnector((IObservable) getModel(),
				new DynamicListProvider() {

					@SuppressWarnings("unchecked")
					@Override
					public List<? extends IObservable> getList(Object fromObject) {
						if (fromObject != null) {
							List<IKeyConstraint> keys = new ArrayList<IKeyConstraint>();
							for (IKeyConstraint key : ((IBasicTable) fromObject).getConstraints()) {
								switch (key.getConstraintType()) {
								case FOREIGN:
									keys.add(key);
									break;
								}
							}
							Collections.sort(keys, NameComparator.getInstance());
							return keys;
						}
						return Collections.EMPTY_LIST;
					}

					@Override
					public IElementType getType() {
						return IElementType.getInstance(ForeignKeyConstraint.TYPE_ID);
					}
				});
		fkConnector.create(tabFolder);
		fkConnector.refreshConnector();
		connectors.add(fkConnector);
		fkTab.setControl(fkConnector.getSWTConnector());

		// Creating index editor
		TabItem indexTab = new TabItem(tabFolder, SWT.NONE);
		indexTab.setText(DBGMUIMessages.getString("indexEditorTab")); //$NON-NLS-1$

		DynamicListConnector indDynConn = new DynamicListConnector((IObservable) getModel(),
				new DynamicListProvider() {

					@SuppressWarnings("unchecked")
					@Override
					public List<? extends IObservable> getList(Object fromObject) {
						if (fromObject != null) {
							List<IIndex> indexes = new ArrayList<IIndex>();
							indexes.addAll(((IBasicTable) fromObject).getIndexes());
							Collections.sort(indexes, NameComparator.getInstance());
							return indexes;
						}
						return Collections.EMPTY_LIST;
					}

					@Override
					public IElementType getType() {
						return IElementType.getInstance(IIndex.INDEX_TYPE);
					}
				});
		connectors.add(indDynConn);
		indDynConn.create(tabFolder);
		indDynConn.refreshConnector();
		indexTab.setControl(indDynConn.getSWTConnector());
		// Control ready
		return editorGroup;
	}

	public Image getConnectorIcon() {
		return DBGMImages.WIZARD_TABLE;
	}

	@Override
	public void refreshConnector() {
		IBasicTable table = (IBasicTable) getModel();
		nameText.setText(notNull(table.getName()));
		shortNameText.setText(table.getShortName() == null ? "" : table.getShortName()); //$NON-NLS-1$
		if (table.getDescription() != null) {
			descriptionText.setText(table.getDescription());
		}
		// Control enabling
		nameText.setEnabled(!table.updatesLocked());
		descriptionText.setEnabled(!table.updatesLocked());
		shortNameText.setEnabled(!table.updatesLocked());
		// Refreshing columns
		colEditGUI.refreshConnector();
		// for(IDisplayConnector conn : connectors) {
		// conn.refreshConnector();
		// }
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object o) {
		IBasicTable table = (IBasicTable) getModel();
		switch (event) {
		case NAME_CHANGED:
			table.setName((String) o);
			break;
		case SHORTNAME_CHANGED:
			table.setShortName((String) o);
			break;
		case DESCRIPTION_CHANGED:
			table.setDescription((String) o);
			break;
		case MODEL_CHANGED:
		case COLUMN_ADDED:
			refreshConnector();
			// TODO Check redundance with column.save
			// controller.modelChanged(table);
			break;
		case UPDATES_LOCKED:
			refreshConnector();
			// Dispatching message
			colEditGUI.handleEvent(event, source, o);
			break;
		case UPDATES_UNLOCKED:
			refreshConnector();
			// Dispatching message
			colEditGUI.handleEvent(event, source, o);
			break;

		}
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getSWTConnector()
	 */
	public Control getSWTConnector() {
		return editorGroup;
	}

	@Override
	public String getDescription() {
		return DBGMUIMessages.getString("editor.table.wizardDescription"); //$NON-NLS-1$
	}

	@Override
	public Image getImage() {
		return getConnectorIcon();
	}

}
