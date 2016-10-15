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
/**
 *
 */
package com.nextep.datadesigner.dbgm.gui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import com.nextep.datadesigner.Designer;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.ForeignKeyAction;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseRawObject;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.UnresolvedItemException;
import com.nextep.datadesigner.gui.impl.ColorFocusListener;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.TableDisplayConnector;
import com.nextep.datadesigner.gui.impl.editors.ComboColumnEditor;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.gui.impl.editors.TextEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.external.VersionedTableEditor;
import com.nextep.datadesigner.vcs.services.NamingService;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.controllers.ForeignKeyUIController;
import com.nextep.designer.helper.DatatypeHelper;
import com.nextep.designer.ui.UIImages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * This class is the graphical representation of a foreign key edition. It handles remote constraint
 * selection (navigating through containers / tables), column mapping and foreign key naming.
 * 
 * @author Christophe Fondacci
 * @author Bruno Gautier
 */
public class ForeignKeyNewEditorGUI extends TableDisplayConnector {

	private static final Log log = LogFactory.getLog(ForeignKeyNewEditorGUI.class);
	private Composite group = null; // @jve:decl-index=0:visual-constraint="10,10"
	private CLabel nameLabel = null;
	private Text nameText = null;
	private CLabel remoteLabel = null;
	private Text remoteTableText = null;
	private Button changeRemoteTableButton = null;
	private CLabel joinToLabel = null;
	private Combo remoteConstraintCombo = null;
	private Label enforcedLabel = null;
	private Text enforcedText = null;
	private Button createCols;
	private Table columnsTable;
	private TableColumn remoteColumn;
	private TableColumn tableColumn;
	private TableColumn warnColumn;
	private NextepTableEditor editor;
	private ComboColumnEditor columnProposals;
	private Combo onUpdateCombo;
	private Combo onDeleteCombo;

	/**
	 * Default constructor
	 * 
	 * @param constraint the foreign key constraint to edit
	 * @param controller the foreign key controller
	 */
	public ForeignKeyNewEditorGUI(ForeignKeyConstraint constraint, ForeignKeyUIController controller) {
		super(constraint, controller);
	}

	/**
	 * This method initializes the remote table combo which will hold the eligible tables for
	 * foreign key edition.
	 */
	private void createRemoteTableCombo() {
		GridData gridData3 = new GridData();
		gridData3.horizontalAlignment = GridData.FILL;
		gridData3.grabExcessHorizontalSpace = true;
		gridData3.verticalAlignment = GridData.CENTER;
		remoteTableText = new Text(group, SWT.BORDER | SWT.BOLD);
		remoteTableText.setEditable(false);
		remoteTableText.setLayoutData(gridData3);

		changeRemoteTableButton = new Button(group, SWT.PUSH);
		changeRemoteTableButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeRemoteTableButton
				.setToolTipText(DBGMUIMessages.getString("toolTipChangeRemoteTable")); //$NON-NLS-1$
		changeRemoteTableButton.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false, 2, 1));
		changeRemoteTableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final Object elt = VCSUIPlugin.getService(ICommonUIService.class).findElement(
						changeRemoteTableButton.getShell(),
						DBGMUIMessages.getString("changeRemoteTableTitle"), //$NON-NLS-1$
						IElementType.getInstance(IBasicTable.TYPE_ID));
				if (elt != null) {
					IBasicTable t = (IBasicTable) elt;
					remoteTableText.setText(t.getName());
					refreshConstraintCombo(t);
					remoteConstraintChanged();
					refreshConnector();
				}
			}
		});
	}

	/**
	 * This method initializes the remote constraint combo which will propose the remote unique key
	 * constraints to join this foreign key to.
	 */
	private void createRemoteConstraintCombo() {
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.horizontalSpan = 1;
		gridData4.verticalAlignment = GridData.CENTER;
		remoteConstraintCombo = new Combo(group, SWT.READ_ONLY);
		remoteConstraintCombo.setLayoutData(gridData4);
		Label filler = new Label(group, SWT.NONE);
		filler.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		// Handling combo edition
		ComboEditor.handle(remoteConstraintCombo, ChangeEvent.REMOTE_CONSTRAINT_CHANGED, this);

	}

	protected Control createSWTControl(Composite parent) {
		ForeignKeyConstraint constraint = (ForeignKeyConstraint) getModel();

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		group = new Composite(parent, SWT.NONE);
		group.setLayout(gridLayout);
		createName();
		createRemoteTable();
		createRemoteConstraint();
		createEnforcedBy();
		createOnUpdateOnDeleteCombos();
		createToolbox();
		createRemoteColumnTable();
		// Registering table
		initializeTable(columnsTable, editor);
		try {
			constraint.getRemoteConstraint();
		} catch (UnresolvedItemException e) {
			// If unresolved constraint we say it
			remoteTableText.setText("Unresolved parent constraint");
			remoteConstraintCombo.setText("Unresolved remote constraint");
		}
		// Creating table editor
		editor = VersionedTableEditor.handle(columnsTable, constraint.getConstrainedTable());
		initializeTable(columnsTable, editor);
		columnProposals = ComboColumnEditor.handle(editor, 1, ChangeEvent.COLUMN_CHANGED, this,
				null);
		refreshColumnsProposals();

		return group;
	}

	/**
	 * Creates the On Update and On delete label and combos
	 */
	private void createOnUpdateOnDeleteCombos() {
		final Label onUpdateLabel = new Label(group, SWT.RIGHT);
		onUpdateLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		onUpdateLabel.setText(DBGMUIMessages.getString("fk.editor.onUpdate"));

		onUpdateCombo = new Combo(group, SWT.READ_ONLY);
		onUpdateCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		final Label onDeleteLabel = new Label(group, SWT.RIGHT);
		onDeleteLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		onDeleteLabel.setText(DBGMUIMessages.getString("fk.editor.onDelete"));

		onDeleteCombo = new Combo(group, SWT.READ_ONLY);
		onDeleteCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		// Filling read-only action values
		for (ForeignKeyAction action : ForeignKeyAction.values()) {
			onUpdateCombo.add(action.getLabel());
			onUpdateCombo.setData(action.getLabel(), action);
			onDeleteCombo.add(action.getLabel());
			onDeleteCombo.setData(action.getLabel(), action);
		}
		// Listening to selection changes
		ComboEditor.handle(onUpdateCombo, ChangeEvent.CUSTOM_1, this);
		ComboEditor.handle(onDeleteCombo, ChangeEvent.CUSTOM_2, this);
	}

	/**
	 * Refreshes the combo box containing column name proposals that will be used to define column
	 * mappings.
	 */
	private void refreshColumnsProposals() {
		ForeignKeyConstraint constraint = (ForeignKeyConstraint) getModel();
		if (columnProposals != null) {
			List<String> proposals = new ArrayList<String>();
			for (IBasicColumn c : constraint.getConstrainedTable().getColumns()) {
				proposals.add(c.getName());
			}
			columnProposals.setProposals(proposals);
		}
	}

	/**
	 * Creates the name edition controls
	 */
	private void createName() {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = GridData.FILL;
		gridData1.verticalAlignment = GridData.CENTER;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 3;
		gridData.verticalAlignment = GridData.CENTER;
		nameLabel = new CLabel(group, SWT.RIGHT);
		nameLabel.setText(DBGMUIMessages.getString("fk.editor.name"));
		nameLabel.setLayoutData(gridData1);
		nameText = new Text(group, SWT.BORDER);
		nameText.setLayoutData(gridData);
		nameText.setTextLimit(60);
		ColorFocusListener.handle(nameText);
		TextEditor.handle(nameText, ChangeEvent.NAME_CHANGED, this);
	}

	/**
	 * Creates the remote table selection controls
	 */
	private void createRemoteTable() {
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.verticalAlignment = GridData.CENTER;
		remoteLabel = new CLabel(group, SWT.RIGHT);
		remoteLabel.setText(DBGMUIMessages.getString("fk.editor.remoteTable"));
		remoteLabel.setLayoutData(gridData2);
		createRemoteTableCombo();
	}

	/**
	 * Creates the remote constraint selection controls
	 */
	private void createRemoteConstraint() {
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.verticalAlignment = GridData.CENTER;
		joinToLabel = new CLabel(group, SWT.RIGHT);
		joinToLabel.setText(DBGMUIMessages.getString("fk.editor.joinedKey"));
		joinToLabel.setLayoutData(gridData11);
		createRemoteConstraintCombo();
	}

	/**
	 * Creates the "Enforced by index" label and text display controls
	 */
	private void createEnforcedBy() {
		GridData gridData11 = new GridData();
		gridData11.horizontalAlignment = GridData.FILL;
		gridData11.verticalAlignment = GridData.CENTER;
		enforcedLabel = new Label(group, SWT.RIGHT);
		enforcedLabel.setText(DBGMUIMessages.getString("fk.editor.enforcingIndex"));
		enforcedLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		GridData gridData4 = new GridData();
		gridData4.horizontalAlignment = GridData.FILL;
		gridData4.grabExcessHorizontalSpace = true;
		gridData4.horizontalSpan = 3;
		gridData4.verticalAlignment = GridData.CENTER;
		enforcedText = new Text(group, SWT.BORDER);
		enforcedText.setFont(FontFactory.FONT_BOLD);
		enforcedText.setEditable(false);
		enforcedText.setLayoutData(gridData4);
	}

	private void createToolbox() {
		final Composite toolbox = new Composite(group, SWT.NONE);
		addNoMarginLayout(toolbox, 1);
		toolbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		createCols = new Button(toolbox, SWT.PUSH);
		createCols.setText(DBGMUIMessages.getString("fk.editor.createUnmappedCols"));
		createCols.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				cloneUnmappedColumns();
			}
		});
	}

	/**
	 * Creates the table controls used to define mappings between remote key table columns and
	 * current table columns
	 */
	private void createRemoteColumnTable() {
		GridData gridData21 = new GridData();
		gridData21.horizontalSpan = 4;
		gridData21.verticalAlignment = GridData.FILL;
		gridData21.grabExcessHorizontalSpace = true;
		gridData21.grabExcessVerticalSpace = true;
		gridData21.horizontalAlignment = GridData.FILL;
		columnsTable = new Table(group, SWT.FULL_SELECTION | SWT.BORDER);
		remoteColumn = new TableColumn(columnsTable, SWT.NONE);
		remoteColumn.setWidth(150);
		remoteColumn.setText(DBGMUIMessages.getString("fk.editor.remoteTableColumns"));
		tableColumn = new TableColumn(columnsTable, SWT.NONE);
		tableColumn.setWidth(150);
		tableColumn.setText(DBGMUIMessages.getString("fk.editor.tableColumns"));
		columnsTable.setLayoutData(gridData21);
		columnsTable.setHeaderVisible(true);
		columnsTable.setLinesVisible(true);
		warnColumn = new TableColumn(columnsTable, SWT.CENTER);
		warnColumn.setText(DBGMUIMessages.getString("fk.editor.warnings"));
		warnColumn.setWidth(200);
	}

	public Control getSWTConnector() {
		return group;
	}

	public void refreshConnector() {
		final ForeignKeyConstraint constraint = (ForeignKeyConstraint) getModel();
		if (constraint != null) {
			nameText.setText(notNull(constraint.getName()));
		}
		// If a remote constraint is defined
		boolean unmappedCols = false;
		IKeyConstraint currentRemoteConstraint = null;
		try {
			currentRemoteConstraint = constraint.getRemoteConstraint();
		} catch (UnresolvedItemException e) {
			log.error(e.getMessage(), e);
		}
		if (currentRemoteConstraint != null) {
			/*
			 * If the remote constraint is defined but not the foreign key columns, we try to find
			 * matching columns.
			 */
			if (constraint.getColumns().size() == 0) {
				adjustForeignKeyColumns(true);
			}
			// Then we synchronize our 2 columns collection (remote and local) to add correspondent
			// items
			final Iterator<IBasicColumn> remoteIt = currentRemoteConstraint.getColumns()
					.iterator();
			final Iterator<IBasicColumn> fkIt = constraint.getColumns().iterator();
			// Pre-cleanup
			this.clean(columnsTable);
			// Looping on remote constraint columns
			while (remoteIt.hasNext()) {
				final IBasicColumn remoteColumn = remoteIt.next();
				final TableItem i = this.getOrCreateItem(remoteColumn);
				// Remote (left) section setup
				i.setText(remoteColumn.getName());
				i.setImage(DatatypeHelper.getDatatypeIcon(remoteColumn.getDatatype(), true));
				// FK (right) mapped section setup (we may have no mapping)
				if (fkIt.hasNext()) {
					IBasicColumn tableColumn = fkIt.next();
					if (isMapped(constraint, tableColumn)) {
						// If we have a table mapping, we set the mapped column information
						Designer.getListenerService().registerListener(i, tableColumn,
								new IEventListener() {

									@Override
									public void handleEvent(ChangeEvent event, IObservable source,
											Object data) {
										refreshConnector();
									}
								});
						i.setText(1, tableColumn.getName());
						i.setImage(1,
								DatatypeHelper.getDatatypeIcon(tableColumn.getDatatype(), true));
						// Checking datatype "perfect" match for warning
						final String remoteColType = remoteColumn.getDatatype().toString();
						final String fkColType = tableColumn.getDatatype().toString();
						if (!remoteColType.equals(fkColType)) {
							i.setText(2, MessageFormat.format(DBGMUIMessages
									.getString("fk.editor.warnings.inconsistenDatatypes"),
									remoteColType, fkColType));
							i.setImage(2, UIImages.ICON_MARKER_WARNING);
						} else {
							i.setText(2, "");
							i.setImage(2, null);
						}
					} else {
						// Incorrect mapping, we display an error
						displayMappingError(i);
						unmappedCols = true;
					}
				} else {
					// Not enough FK columns to fulfill the mapping with the remote key, we display
					// an error
					displayMappingError(i);
					unmappedCols = true;
				}
			}
		}
		// Refreshing enforcing index
		Collection<IDatabaseRawObject> enforcerIndexes = constraint.getEnforcingIndex();
		enforcedText.setText(enforcerIndexes.isEmpty() ? DBGMUIMessages
				.getString("fk.editor.noEnforcingIndex") : getCommaSeparatedNames(enforcerIndexes)); //$NON-NLS-1$
		if (currentRemoteConstraint != null) {
			final IBasicTable t = currentRemoteConstraint.getConstrainedTable();
			remoteTableText.setText(t.getName());
			refreshConstraintCombo(t);
		}
		// Refreshing on update / delete combos
		onUpdateCombo.setText(constraint.getOnUpdateAction().getLabel());
		onDeleteCombo.setText(constraint.getOnDeleteAction().getLabel());

		// Checking parent table locking status
		if (constraint.getConstrainedTable() != null) {
			boolean enabled = !constraint.getConstrainedTable().updatesLocked();
			changeRemoteTableButton.setEnabled(enabled);
			remoteConstraintCombo.setEnabled(enabled);
			onUpdateCombo.setEnabled(enabled);
			onDeleteCombo.setEnabled(enabled);
			nameText.setEnabled(enabled);
			createCols.setEnabled(enabled && unmappedCols);
		}
		// Refreshing columns proposals
		refreshColumnsProposals();
	}

	/**
	 * Converts the collection of {@link INamedObject} elements into a comma seperated String with
	 * the names of all elements, preserving the input ordering.
	 * 
	 * @param elements a {@link Collection} of {@link INamedObject}
	 * @return a comma seperated String with elements names
	 */
	private String getCommaSeparatedNames(Collection<? extends INamedObject> elements) {
		if (elements == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		String seperator = "";
		for (INamedObject namedObj : elements) {
			buf.append(seperator + namedObj.getName());
			seperator = ", ";
		}
		return buf.toString();
	}

	/**
	 * Displays a mapping error on the foreign key section of the columns mapping table for the
	 * specified item.
	 * 
	 * @param item {@link TableItem} on which the error should be displayed
	 */
	private void displayMappingError(TableItem item) {
		item.setText(1, DBGMUIMessages.getString("fk.editor.unmappedColumn"));
		item.setImage(1, ImageFactory.ICON_ERROR_TINY);
	}

	@Override
	protected Object getTableVersionedParent(Object model) {
		return ((ForeignKeyConstraint) getModel()).getConstrainedTable();
	}

	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		ForeignKeyConstraint constraint = (ForeignKeyConstraint) getModel();
		// Each of the following catched event messages will trigger model
		// updates. Therefore we first ensure that we can update it.
		if (!VersionHelper.ensureModifiable(constraint.getConstrainedTable(), false)) {
			return;
		}
		switch (event) {
		case NAME_CHANGED:
			constraint.setName((String) data);
			break;
		case REMOTE_CONSTRAINT_CHANGED:
			remoteConstraintChanged();
			break;
		case CUSTOM_1:
			constraint.setOnUpdateAction((ForeignKeyAction) onUpdateCombo.getData((String) data));
			break;
		case CUSTOM_2:
			constraint.setOnDeleteAction((ForeignKeyAction) onDeleteCombo.getData((String) data));
			break;
		case COLUMN_CHANGED:
			// With this call we ensure we work on an equal set of columns between remote and fk
			// columns
			adjustForeignKeyColumns(false);
			// Which column are we working on (= line index of the table) ?
			final int columnIndex = columnsTable.getSelectionIndex();
			// Retrieving selected table column
			final IBasicColumn newCol = getConstrainedColumn(constraint.getConstrainedTable(),
					(String) data);
			// Removing listener on previous column
			if (newCol != null) {
				// Bug #332: Previous column mapping on this column index may not be defined
				if (constraint.getColumns().size() > columnIndex) {
					final IBasicColumn previousCol = constraint.getColumns().get(
							columnIndex);
					if (previousCol != null) {
						Designer.getListenerService().unregisterListener(previousCol, this);
						constraint.removeColumn(previousCol);
					}
				}
				constraint.addConstrainedColumn(columnIndex, newCol);
			}
			break;
		}
		refreshConnector();

	}

	/**
	 * Refreshes the remote constraint selection combo box. The combo will be re-initialized with
	 * the unique constraints of the specified parent table.<br>
	 * If the remote constraint currently associated to the edited foreign key is a constraint of
	 * the specified table, it will be automatically selected. Otherwise the primary key will be
	 * automatically selected if defined.
	 * 
	 * @param t
	 */
	private void refreshConstraintCombo(IBasicTable t) {
		final ForeignKeyConstraint fk = (ForeignKeyConstraint) getModel();
		remoteConstraintCombo.removeAll();
		IKeyConstraint currentRemoteConstraint = null;
		try {
			currentRemoteConstraint = fk.getRemoteConstraint();
		} catch (UnresolvedItemException e) {
			log.error(e.getMessage(), e);
		}
		if (t != null) {
			int i = 0;
			for (IKeyConstraint c : t.getConstraints()) {
				switch (c.getConstraintType()) {
				case PRIMARY:
				case UNIQUE:
					remoteConstraintCombo.add(c.getName());
					remoteConstraintCombo.setData(String.valueOf(i), c);
					if (currentRemoteConstraint != null && c.equals(currentRemoteConstraint)
							|| c.getConstraintType() == ConstraintType.PRIMARY) {
						remoteConstraintCombo.select(i);
					}
					i++;
				}
			}
		}
	}

	/**
	 * Updates the current foreign key model to setup the constraint defined by the user
	 * primary/unique constraints combo.<br>
	 * <b>WARNING:</b> This method may alter the foreign key definition and must only be called by
	 * user actions.
	 */
	private void remoteConstraintChanged() {
		final ForeignKeyConstraint fk = (ForeignKeyConstraint) getModel();
		IKeyConstraint currentRemoteConstraint = null;
		try {
			currentRemoteConstraint = fk.getRemoteConstraint();
		} catch (UnresolvedItemException e) {
			log.error(e.getMessage(), e);
		}
		int s = remoteConstraintCombo.getSelectionIndex();
		// Retrieving selected constraint
		IKeyConstraint userRemoteConstraint = (IKeyConstraint) remoteConstraintCombo.getData(String
				.valueOf(s));
		if (userRemoteConstraint != currentRemoteConstraint) {
			fk.setRemoteConstraint(userRemoteConstraint);
			// Adjusting our foreign key name
			NamingService.getInstance().adjustName(fk);
			// Constraint changed so we reset our columns mappings
			// First we remove the previous constrained columns
			removeSubsequentForeignKeyColumns(fk.getColumns().iterator());
			// Then we try to find a new mapping for the new remote constraint
			adjustForeignKeyColumns(true);
			// And we cleanup our columns mapping table before refreshing
			clean(columnsTable);
			refreshConnector();
		}
	}

	/**
	 * Retrieves the constrained column from its name string.
	 * 
	 * @param constraint constraint which reference the named column
	 * @param columnName string value of the column name
	 * @return the {@link IBasicColumn} instance
	 */
	private IBasicColumn getConstrainedColumn(IBasicTable table, String columnName) {
		for (IBasicColumn c : table.getColumns()) {
			if (c.getName().equals(columnName)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Clones the given column.
	 * 
	 * @param c constraint containing the column to clone
	 * @param columnName name of the cloned column
	 * @param reference reference column to clone
	 * @return a clone of the reference column with the specified column name
	 */
	private IBasicColumn cloneColumn(IKeyConstraint c, String columnName, IBasicColumn reference) {
		IBasicTable parentTable = c.getConstrainedTable();
		IBasicColumn clone = getConstrainedColumn(parentTable, columnName);
		if (clone == null) {
			clone = (IBasicColumn) UIControllerFactory.getController(
					IElementType.getInstance("COLUMN")).newInstance(parentTable);
			clone.setName(columnName);
			clone.setDatatype(reference.getDatatype());
		}
		return clone;
	}

	private void cloneUnmappedColumns() {
		final ForeignKeyConstraint fk = (ForeignKeyConstraint) getModel();
		final boolean confirmed = MessageDialog.openQuestion(group.getShell(), MessageFormat
				.format(DBGMUIMessages.getString("fk.editor.dialog.confirmColumnCreationTitle"), fk
						.getConstrainedTable().getName()), MessageFormat.format(DBGMUIMessages
				.getString("fk.editor.dialog.confirmColumnCreation"), fk.getConstrainedTable()
				.getName()));
		if (confirmed) {
			final IKeyConstraint remote = fk.getRemoteConstraint();
			final Iterator<IBasicColumn> remoteColIt = remote.getColumns().iterator();
			final Iterator<IBasicColumn> fkColIt = new ArrayList<IBasicColumn>(
					fk.getColumns()).iterator();
			while (remoteColIt.hasNext()) {
				final IBasicColumn remoteCol = remoteColIt.next();
				IBasicColumn mappedCol = null;
				if (fkColIt.hasNext()) {
					final IBasicColumn fkCol = fkColIt.next();
					if (!isMapped(fk, fkCol)) {
						mappedCol = cloneColumn(fk, remoteCol.getName(), remoteCol);
						int colIndex = fk.getColumns().indexOf(fkCol);
						fk.removeColumn(mappedCol);
						fk.addConstrainedColumn(colIndex, mappedCol);
					}
				} else {
					mappedCol = cloneColumn(fk, remoteCol.getName(), remoteCol);
					fk.addColumn(mappedCol);
				}
			}
			refreshConnector();
		}
	}

	/**
	 * Refreshing current columns mappings from remote constraint to current table columns.
	 * 
	 * @param guessMappedColumn should this method try to "guess" the foreign key column which
	 *        matches remote column
	 */
	private void adjustForeignKeyColumns(boolean guessMappedColumn) {
		final ForeignKeyConstraint constraint = (ForeignKeyConstraint) getModel();
		final IKeyConstraint currentRemoteConstraint = constraint.getRemoteConstraint();

		if (currentRemoteConstraint != null) {
			final Iterator<IBasicColumn> remoteColIt = currentRemoteConstraint
					.getColumns().iterator();
			final Iterator<IBasicColumn> fkColIt = new ArrayList<IBasicColumn>(
					constraint.getColumns()).iterator();
			// Iterating over 2 collection synchronously to :
			// - remove any overflowing fk column
			// - guess any remote constraint column we could match
			while (remoteColIt.hasNext()) {
				final IBasicColumn remoteCol = remoteColIt.next();
				if (fkColIt.hasNext()) {
					fkColIt.next();
				} else {
					IBasicColumn guessedCol = null;
					// Guessing any corresponding column by its name if needed
					if (guessMappedColumn) {
						guessedCol = getConstrainedColumn(constraint.getConstrainedTable(),
								remoteCol.getName());
					}
					if (guessedCol == null) {
						// Forcing inconsistency by setting current col to remote col
						guessedCol = remoteCol;
					}
					constraint.addColumn(guessedCol);
				}
			}
			// Removing any overflowing fk column
			removeSubsequentForeignKeyColumns(fkColIt);
		}
	}

	/**
	 * Convenience method to remove from the list of constrained columns of the current foreign key
	 * constraint all columns indicated by the specified <code>Iterator</code>.
	 * 
	 * @param fkColsIt an {@link Iterator} pointing to the {@link IBasicColumn} to remove from the
	 *        list of the constrained columns of the current foreign key
	 */
	private void removeSubsequentForeignKeyColumns(Iterator<IBasicColumn> fkColsIt) {
		final ForeignKeyConstraint constraint = (ForeignKeyConstraint) getModel();

		while (fkColsIt.hasNext()) {
			IBasicColumn fkCol = fkColsIt.next();
			constraint.removeColumn(fkCol);
		}
	}

	/**
	 * Indicates whether the specified table column of a foreign key should be considered as an
	 * appropriate column mapping.
	 * 
	 * @param foreignKey foreign key on which consistency should be verified
	 * @param tableColumn table column to check (a column of the foreign key)
	 * @return <code>true</code> if the column is a valid mapping, else <code>false</code>
	 */
	private boolean isMapped(ForeignKeyConstraint foreignKey, IBasicColumn tableColumn) {
		return tableColumn != null && tableColumn.getParent() != null
				&& tableColumn.getParent().equals(foreignKey.getConstrainedTable());
	}

	@Override
	public Image getConnectorIcon() {
		return DBGMImages.WIZARD_KEY;
	}
}
