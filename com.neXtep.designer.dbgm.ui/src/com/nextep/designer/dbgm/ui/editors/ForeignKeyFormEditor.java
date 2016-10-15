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
package com.nextep.designer.dbgm.ui.editors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.dbgm.impl.ForeignKeyConstraint;
import com.nextep.datadesigner.dbgm.model.ConstraintType;
import com.nextep.datadesigner.dbgm.model.ForeignKeyAction;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatabaseRawObject;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.exception.InconsistentObjectException;
import com.nextep.datadesigner.model.IElementType;
import com.nextep.datadesigner.model.INamedObject;
import com.nextep.datadesigner.model.IReference;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.core.services.ICoreService;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.factories.ConverterFactory;
import com.nextep.designer.dbgm.ui.factories.ValidatorFactory;
import com.nextep.designer.dbgm.ui.jface.ColumnBindingLabelProvider;
import com.nextep.designer.dbgm.ui.jface.ColumnsContentProvider;
import com.nextep.designer.dbgm.ui.jface.ConstraintsContentProvider;
import com.nextep.designer.dbgm.ui.jface.DbgmLabelProvider;
import com.nextep.designer.dbgm.ui.jface.ForeignKeyColumnsContentProvider;
import com.nextep.designer.dbgm.ui.model.IColumnBinding;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.factories.UIControllerFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;
import com.nextep.designer.vcs.ui.services.ICommonUIService;

/**
 * The editor for table foreign keys.
 * 
 * @author Christophe Fondacci
 */
public class ForeignKeyFormEditor extends AbstractFormEditor<ForeignKeyConstraint> {

	private static final Log log = LogFactory.getLog(ForeignKeyFormEditor.class);

	private Text nameText, descText, remoteTableText, remoteConstraintText, enforcingIndexText;
	private Button changeRemoteTableButton, changeRemoteConstraintButton, editColumnButton,
			createEnforcingIndexButton;
	private CCombo onUpdateCombo, onDeleteCombo;
	private Table columnsTable;

	private TableViewer columnsViewer;

	public ForeignKeyFormEditor() {
		super(
				DBGMUIMessages.getString("editor.foreignKey.details"), DBGMUIMessages.getString("editor.foreignKey.detailsDesc"), true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected void createControls(final IManagedForm managedForm, FormToolkit toolkit,
			Composite editor) {
		editor.setLayout(new GridLayout(3, false));

		// Creating the name editor
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.foreignKey.name")); //$NON-NLS-1$
		nameText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		// Creating the description editor
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.foreignKey.desc")); //$NON-NLS-1$
		descText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		descText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		// Creating the remote table editor
		final Link remoteTableLink = new Link(editor, SWT.NONE);
		remoteTableLink.setText(DBGMUIMessages.getString("editor.foreignKey.remoteTableLink")); //$NON-NLS-1$
		remoteTableLink.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IKeyConstraint remoteKey = getModel().getRemoteConstraint();
				if (remoteKey != null) {
					final IBasicTable remoteTable = remoteKey.getConstrainedTable();
					if (remoteTable != null) {
						final ITypedObjectUIController controller = UIControllerFactory
								.getController(IElementType.getInstance(IBasicTable.TYPE_ID));
						controller.defaultOpen(remoteTable);
					}
				}
			}
		});
		remoteTableText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		remoteTableText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		remoteTableText.setEditable(false);
		changeRemoteTableButton = toolkit.createButton(editor, "", SWT.PUSH); //$NON-NLS-1$
		changeRemoteTableButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeRemoteTableButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changeRemoteTable(managedForm.getForm().getShell());
			}
		});

		// Creating remote constraint combo
		final Link remoteConstraintLink = new Link(editor, SWT.NONE);
		remoteConstraintLink
				.setText(DBGMUIMessages.getString("editor.foreignKey.remoteConstraint")); //$NON-NLS-1$
		remoteConstraintLink.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				final IKeyConstraint remoteKey = getModel().getRemoteConstraint();
				if (remoteKey != null) {
					final ITypedObjectUIController controller = UIControllerFactory
							.getController(remoteKey);
					controller.defaultOpen(remoteKey);
				}
			}
		});
		remoteConstraintText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		remoteConstraintText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		remoteConstraintText.setEditable(false);
		changeRemoteConstraintButton = toolkit.createButton(editor, "", SWT.PUSH); //$NON-NLS-1$
		changeRemoteConstraintButton.setImage(ImageFactory.ICON_EDIT_TINY);
		changeRemoteConstraintButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				changeRemoteConstraint(managedForm.getForm().getShell());
			}
		});

		// Enforcing index
		final Link enforcingIndexLink = new Link(editor, SWT.NONE);
		enforcingIndexLink.setText(DBGMUIMessages.getString("fk.editor.enforcingIndex")); //$NON-NLS-1$
		enforcingIndexLink.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				// Getting current enforcing index
				Collection<IDatabaseRawObject> indexes = getModel().getEnforcingIndex();
				// If existing we open the corresponding editor
				if (indexes != null && !indexes.isEmpty()) {
					final IDatabaseRawObject index = indexes.iterator().next();
					UIControllerFactory.getController(index).defaultOpen(index);
				} else {
					createEnforcingIndex();
				}
			}
		});
		enforcingIndexText = toolkit.createText(editor, "", SWT.BOLD); //$NON-NLS-1$
		enforcingIndexText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		enforcingIndexText.setEditable(false);
		createEnforcingIndexButton = toolkit.createButton(editor, "", SWT.PUSH); //$NON-NLS-1$
		createEnforcingIndexButton.setImage(ImageFactory.ICON_ADD_TINY);
		createEnforcingIndexButton.setText("Create");
		createEnforcingIndexButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				createEnforcingIndex();
			}
		});
		// On update combo
		toolkit.createLabel(editor, DBGMUIMessages.getString("fk.editor.onUpdate")); //$NON-NLS-1$
		onUpdateCombo = new CCombo(editor, SWT.READ_ONLY | SWT.BORDER);
		onUpdateCombo.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, false, 2, 1));
		toolkit.adapt(onUpdateCombo);
		for (ForeignKeyAction action : ForeignKeyAction.values()) {
			onUpdateCombo.add(action.getLabel());
		}

		// On delete combo
		toolkit.createLabel(editor, DBGMUIMessages.getString("fk.editor.onDelete")); //$NON-NLS-1$
		onDeleteCombo = new CCombo(editor, SWT.READ_ONLY | SWT.BORDER);
		onDeleteCombo.setLayoutData(new GridData(SWT.NONE, SWT.FILL, false, false, 2, 1));
		toolkit.adapt(onDeleteCombo);
		for (ForeignKeyAction action : ForeignKeyAction.values()) {
			onDeleteCombo.add(action.getLabel());
		}

		// Foreign key columns component
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 200;
		columnsTable = new Table(editor, SWT.FULL_SELECTION | SWT.BORDER);
		toolkit.adapt(columnsTable);
		final TableColumn remoteColumn = new TableColumn(columnsTable, SWT.NONE);
		remoteColumn.setWidth(150);
		remoteColumn.setText(DBGMUIMessages.getString("fk.editor.remoteTableColumns")); //$NON-NLS-1$
		final TableColumn tableColumn = new TableColumn(columnsTable, SWT.NONE);
		tableColumn.setWidth(150);
		tableColumn.setText(DBGMUIMessages.getString("fk.editor.tableColumns")); //$NON-NLS-1$
		columnsTable.setLayoutData(gd);
		columnsTable.setHeaderVisible(true);
		columnsTable.setLinesVisible(true);
		final TableColumn warnColumn = new TableColumn(columnsTable, SWT.NONE);
		warnColumn.setText(DBGMUIMessages.getString("fk.editor.warnings")); //$NON-NLS-1$
		warnColumn.setWidth(200);

		editColumnButton = toolkit.createButton(editor,
				DBGMUIMessages.getString("editor.foreignKey.editColumnButton"), SWT.PUSH); //$NON-NLS-1$
		editColumnButton.setImage(ImageFactory.ICON_EDIT_TINY);
		editColumnButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));
		editColumnButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				editSelectedColumn();
			}
		});
		columnsTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				editSelectedColumn();
			}
		});

		columnsViewer = new TableViewer(columnsTable);
		columnsViewer.setContentProvider(new ForeignKeyColumnsContentProvider());
		columnsViewer.setLabelProvider(new ColumnBindingLabelProvider());
		columnsViewer.setInput(getModel());
	}

	/**
	 * Prompts the user for the selection of a column to map to the currently selected remote
	 * constraint column. After the user has selected a column, it will replace the previously
	 * mapped foreign key column.
	 */
	private void editSelectedColumn() {
		final ISelection s = columnsViewer.getSelection();
		final int columnIndex = columnsViewer.getTable().getSelectionIndex();
		if (!s.isEmpty() && s instanceof IStructuredSelection) {
			final IStructuredSelection sel = (IStructuredSelection) s;
			final IColumnBinding columnBinding = (IColumnBinding) sel.getFirstElement();
			final IBasicColumn remoteColumn = columnBinding.getColumn();
			final IBasicColumn fkColumn = columnBinding.getAssociatedColumn();
			final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
			final IContentProvider columnsProvider = new ColumnsContentProvider();
			final ILabelProvider labelProvider = new DbgmLabelProvider();
			final ForeignKeyConstraint fk = getModel();

			if (!CorePlugin.getService(ICoreService.class).isLocked(fk)) {
				final IBasicColumn newFkColumn = (IBasicColumn) uiService.findElement(
						columnsTable.getShell(),
						MessageFormat.format(
								DBGMUIMessages.getString("editor.foreignKey.editColumnDlgTitle"), //$NON-NLS-1$
								remoteColumn.getParent().getName(), remoteColumn.getName()),
						fk.getConstrainedTable(), columnsProvider, labelProvider);

				// Removing listener on previous column
				if (newFkColumn != null) {
					// Bug #332: Previous column mapping on this column index may not be defined
					if (columnIndex >= 0 && fk.getColumns().size() > columnIndex) {
						final IBasicColumn previousCol = fk.getColumns().get(columnIndex);
						if (previousCol != null) {
							fk.removeColumn(previousCol);
						}
					}
					// Adding the constraint (minimum position is 0 when not found before)
					fk.addConstrainedColumn(Math.max(columnIndex, 0), newFkColumn);
				}
			}
		}
	}

	/**
	 * Refreshing current columns mappings from remote constraint to current table columns.
	 * 
	 * @param guessMappedColumn should this method try to "guess" the foreign key column which
	 *        matches remote column
	 */
	private void adjustForeignKeyColumns(boolean guessMappedColumn) {
		final ForeignKeyConstraint constraint = getModel();
		final IKeyConstraint currentRemoteConstraint = constraint.getRemoteConstraint();

		if (currentRemoteConstraint != null) {
			final Iterator<IBasicColumn> remoteColIt = currentRemoteConstraint.getColumns()
					.iterator();
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
	 * Prompts the user for a new remote table and defines this selection in the current foreign
	 * key.
	 */
	private void changeRemoteTable(Shell shell) {
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final IBasicTable remoteTable = (IBasicTable) uiService.findElement(shell,
				DBGMUIMessages.getString("changeRemoteTableTitle"), //$NON-NLS-1$
				IElementType.getInstance(IBasicTable.TYPE_ID));
		if (remoteTable != null) {
			final Collection<IKeyConstraint> constraints = remoteTable.getConstraints();
			updateRemoteConstraint(constraints.toArray(new IKeyConstraint[constraints.size()]));
		} else {
			getModel().setRemoteConstraintRef(null);
			changeRemoteConstraintButton.setEnabled(false);
		}
	}

	/**
	 * Prompts the user for a new remote table and defines this selection in the current foreign
	 * key.
	 */
	private void changeRemoteConstraint(Shell shell) {
		final ICommonUIService uiService = CorePlugin.getService(ICommonUIService.class);
		final IKeyConstraint currentConstraint = getModel().getRemoteConstraint();
		if (currentConstraint != null) {
			final IBasicTable remoteTable = currentConstraint.getConstrainedTable();
			final IKeyConstraint remoteConstraint = (IKeyConstraint) uiService.findElement(shell,
					DBGMUIMessages.getString("changeRemoteConstraintTitle"), remoteTable, //$NON-NLS-1$
					new ConstraintsContentProvider(ConstraintType.UNIQUE, ConstraintType.PRIMARY),
					new DbgmLabelProvider());
			if (remoteConstraint != null) {
				updateRemoteConstraint(remoteConstraint);
			}
		}
	}

	/**
	 * Updates the remote constraint of the current foreign key by selecting the first eligible
	 * unique or primary key constraint from the provided list. This method will reset the remote
	 * constraint when no constraint is provided.
	 * 
	 * @param constraints a list of eligible constraint
	 */
	private void updateRemoteConstraint(IKeyConstraint... constraints) {
		if (constraints == null || constraints.length == 0) {
			// No constraint, emptying current remote constraint
			getModel().setRemoteConstraintRef(null);
		} else {
			// Selecting first eligible unique or primary key
			for (IKeyConstraint constraint : constraints) {
				switch (constraint.getConstraintType()) {
				case UNIQUE:
				case PRIMARY:
					getModel().setRemoteConstraint(constraint);
					// Adjusting foreign key columns (making equal size, guessing mapped columns)
					adjustForeignKeyColumns(true);
					return;
				}
			}
			// We fall here when no eligible constraint has been selected
			getModel().setRemoteConstraintRef(null);
			// Adjusting foreign key columns (making equal size, guessing mapped columns)
			adjustForeignKeyColumns(true);
		}

	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		IObservableValue selectionValue = ViewersObservables
				.observeSingleSelection(getSelectionProvider());

		// Binding name
		IObservableValue widgetValue = WidgetProperties.text(SWT.FocusOut).observe(nameText);
		IObservableValue modelValue = PojoObservables.observeDetailValue(selectionValue, "name", //$NON-NLS-1$
				String.class);

		UpdateValueStrategy targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setAfterConvertValidator(ValidatorFactory.createNameValidator(false));
		Binding boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy, null);
		ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT);

		// Binding description
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(descText);
		modelValue = PojoObservables
				.observeDetailValue(selectionValue, "description", String.class); //$NON-NLS-1$
		boundValue = context.bindValue(widgetValue, modelValue, null, null);

		// Binding on update action
		widgetValue = WidgetProperties.text().observe(onUpdateCombo);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "onUpdateAction", //$NON-NLS-1$
				ForeignKeyAction.class);
		UpdateValueStrategy modelUpdateStrategy = new UpdateValueStrategy();
		modelUpdateStrategy.setConverter(ConverterFactory.createForeignKeyActionModelConverter());
		UpdateValueStrategy targetUpdateStrategy = new UpdateValueStrategy();
		targetUpdateStrategy.setConverter(ConverterFactory.createForeignKeyActionTargetConverter());
		boundValue = context.bindValue(widgetValue, modelValue, modelUpdateStrategy,
				targetUpdateStrategy);

		// Binding on delete action
		widgetValue = WidgetProperties.text().observe(onDeleteCombo);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "onDeleteAction", //$NON-NLS-1$
				ForeignKeyAction.class);
		modelUpdateStrategy = new UpdateValueStrategy();
		modelUpdateStrategy.setConverter(ConverterFactory.createForeignKeyActionModelConverter());
		targetUpdateStrategy = new UpdateValueStrategy();
		targetUpdateStrategy.setConverter(ConverterFactory.createForeignKeyActionTargetConverter());
		boundValue = context.bindValue(widgetValue, modelValue, modelUpdateStrategy,
				targetUpdateStrategy);
	}

	@Override
	public void setModel(ForeignKeyConstraint model) {
		super.setModel(model);
		if (columnsViewer != null) {
			columnsViewer.setInput(model);
		}
	}

	@Override
	protected void doRefresh() {
		IKeyConstraint remoteConstraint = null;
		try {
			remoteConstraint = getModel().getRemoteConstraint();
		} catch (ErrorException e) {
			log.error("Error while retrieving FK remote constraint : " + e.getMessage(), e); //$NON-NLS-1$
			remoteConstraint = null;
		}
		if (remoteConstraint != null) {
			remoteTableText.setText(remoteConstraint.getConstrainedTable().getName());
			remoteConstraintText.setText(remoteConstraint.getName());
		} else {
			remoteTableText.setText(""); //$NON-NLS-1$
			remoteConstraintText.setText(""); //$NON-NLS-1$
		}
		// Refreshing enforcing index
		Collection<IDatabaseRawObject> enforcerIndexes = getModel().getEnforcingIndex();
		enforcingIndexText.setText(enforcerIndexes.isEmpty() ? DBGMUIMessages
				.getString("fk.editor.noEnforcingIndex") : getCommaSeparatedNames(enforcerIndexes)); //$NON-NLS-1$
		if (enforcerIndexes.isEmpty()) {
			createEnforcingIndexButton.setEnabled(true);
		} else {
			createEnforcingIndexButton.setEnabled(false);
		}

		columnsViewer.refresh();
		remoteTableText.setEditable(false);
		remoteConstraintText.setEditable(false);
		enforcingIndexText.setEditable(false);
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
	 * Creates an enforcing index for the current fk
	 */
	private void createEnforcingIndex() {
		final ForeignKeyConstraint fk = getModel();
		try {
			fk.checkConsistency();
		} catch (InconsistentObjectException e) {
			MessageDialog
					.openWarning(
							createEnforcingIndexButton.getShell(),
							"Fix " + fk.getName() + " problems first",
							"Foreign key '"
									+ fk.getName()
									+ "' has problems. You need to fix those problems before you can create its enforcing index.");
			return;
		}
		IIndex i = (IIndex) UIControllerFactory.getController(
				IElementType.getInstance(IIndex.INDEX_TYPE)).emptyInstance(fk.getName(),
				fk.getConstrainedTable());
		// i.setIndexedTableRef(c.getConstrainedTable().getReference());
		for (IReference colRef : fk.getConstrainedColumnsRef()) {
			i.addColumnRef(colRef);
		}
		refresh();
	}
}
