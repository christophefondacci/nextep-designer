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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.IIndex;
import com.nextep.datadesigner.dbgm.model.IKeyConstraint;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.exception.CancelException;
import com.nextep.datadesigner.exception.ErrorException;
import com.nextep.datadesigner.gui.impl.FontFactory;
import com.nextep.datadesigner.gui.impl.TableDisplayConnector;
import com.nextep.datadesigner.gui.impl.editors.ClickColumnEditor;
import com.nextep.datadesigner.gui.impl.editors.ComboColumnEditor;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IEventListener;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.gui.external.VersionedTableEditor;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.ui.DBGMImages;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.ui.VCSUIPlugin;
import com.nextep.designer.vcs.ui.services.IWorkspaceUIService;

/**
 * This class is the display connector for table columns edition.
 * 
 * @author Christophe Fondacci
 */
public class ColumnEditorGUI extends TableDisplayConnector implements IEventListener,
		SelectionListener {

	// private static final Log log = LogFactory.getLog(ColumnEditorGUI.class);
	private ColumnSWTComposite group = null;
	private TableColumn primaryFlagColumn = null;
	private TableColumn indexFlagColumn = null;
	private TableColumn uniqueFlagColumn = null;
	private TableColumn colNotNullColumn = null;
	private TableColumn colDefaultColumn = null;
	private NextepTableEditor editor;

	protected class ColoredText {

		private Color c;
		private String s;
		private MultiKey key;

		public ColoredText(Color c, String s) {
			this.c = c;
			this.s = s;
			key = new MultiKey(c, s);
		}

		public Color getColor() {
			return c;
		}

		public String getText() {
			return s;
		}

		public void appendText(String s) {
			this.s = this.s + s;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}
	}

	public ColumnEditorGUI(IBasicTable table, ITypedObjectUIController controller) {
		super(table, controller);
	}

	private void createPrimaryFlagColumn() {
		primaryFlagColumn = new TableColumn(group.getColumnsTable(), SWT.NONE, 1);
		primaryFlagColumn.setWidth(20);
		primaryFlagColumn.setText("P");
		primaryFlagColumn.setResizable(false);
	}

	private void createIndexFlagColumn() {
		indexFlagColumn = new TableColumn(group.getColumnsTable(), SWT.NONE, 2);
		indexFlagColumn.setWidth(25);
		indexFlagColumn.setText("I");
		indexFlagColumn.setResizable(true);
	}

	private void createUniqueFlagColumn() {
		uniqueFlagColumn = new TableColumn(group.getColumnsTable(), SWT.NONE, 3);
		uniqueFlagColumn.setWidth(25);
		uniqueFlagColumn.setText("U");
		uniqueFlagColumn.setResizable(false);
	}

	private void createColNotNullColumn() {
		colNotNullColumn = new TableColumn(group.getColumnsTable(), SWT.CENTER, 8);
		colNotNullColumn.setWidth(60);
		colNotNullColumn.setText("Not Null");
		// colNotNullColumn.setAlignment(SWT.CENTER);
	}

	private void createColDefaultColumn() {
		colDefaultColumn = new TableColumn(group.getColumnsTable(), SWT.NONE, 9);
		colDefaultColumn.setWidth(100);
		colDefaultColumn.setText("Default");
	}

	@Override
	protected Control createSWTControl(Composite parent) {
		group = new ColumnSWTComposite(parent, 1, this);

		// Custom columns creation
		createCustomColumns(group);
		// Initializing table editors
		initTableEditor();

		// Registering table
		initializeTable(group.getColumnsTable(), getTableEditor());

		// Deletion listener
		group.getColumnsTable().addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				final IBasicTable table = (IBasicTable) getModel();
				if (table.updatesLocked() || !VersionHelper.ensureModifiable(table, false)) {
					return;
				}
				final TableColumn delCol = group.getColumnsTable().getColumn(0);
				final int delWidth = delCol.getWidth();
				final TableItem[] sel = group.getColumnsTable().getSelection();
				if (sel.length > 0 && sel[0].getData() instanceof IBasicColumn) {
					if (event.x < delWidth) {
						final IBasicColumn column = (IBasicColumn) sel[0].getData();
						final boolean confirmed = MessageDialog.openQuestion(group.getShell(),
								MessageFormat.format(DBGMUIMessages
										.getString("table.editor.confirmDelColumnTitle"), column
										.getName()), MessageFormat.format(
										DBGMUIMessages.getString("table.editor.confirmDelColumn"),
										column.getName(), column.getParent().getName()));
						if (confirmed) {
							ColumnEditorGUI.this.handleEvent(ChangeEvent.COLUMN_REMOVED,
									(IBasicColumn) sel[0].getData(), null);
						}
					}
				}

			}
		});
		return group;
	}

	/**
	 * Creates custom columns added to the default {@link ColumnSWTComposite} component. Extensions
	 * should override this method to create columns before table editors are set.
	 */
	protected void createCustomColumns(ColumnSWTComposite group) {
		// Creating the primary flag column
		createPrimaryFlagColumn();
		// Creating the index flag column
		createIndexFlagColumn();
		// Creating the unique flag column
		createUniqueFlagColumn();
		createColNotNullColumn();
		createColDefaultColumn();
	}

	/**
	 * Initializes the table editors.
	 */
	protected void initTableEditor() {
		editor = VersionedTableEditor.handle(group.getColumnsTable(), getModel());
		TextColumnEditor.handle(editor, 4, ChangeEvent.NAME_CHANGED, this);
		List<String> datatypes = DBGMHelper.getDatatypeProvider(
				VersionHelper.getCurrentView().getDBVendor()).listSupportedDatatypes();
		Collections.sort(datatypes);
		ComboColumnEditor.handle(editor, 5, ChangeEvent.COLUMN_TYPE_CHANGED, this, datatypes);
		TextColumnEditor.handle(editor, 6, ChangeEvent.COLUMN_LENGTH_CHANGED, this);
		TextColumnEditor.handle(editor, 7, ChangeEvent.COLUMN_PRECISION_CHANGED, this);
		ClickColumnEditor.handle(editor, 8, ChangeEvent.COLUMN_NOTNULL_CHANGED, this);
		TextColumnEditor.handle(editor, 9, ChangeEvent.COLUMN_DEFAULT_CHANGED, this);
		TextColumnEditor.handle(editor, 10, ChangeEvent.DESCRIPTION_CHANGED, this);
	}

	/**
	 * Gives access to the table editor to class extensions
	 * 
	 * @return table editor
	 */
	protected NextepTableEditor getTableEditor() {
		return editor;
	}

	@Override
	public Image getConnectorIcon() {
		return DBGMImages.ICON_COLUMN;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @see com.nextep.datadesigner.gui.impl.TableDisplayConnector#addPersistentEditors(org.eclipse.swt.widgets.TableItem)
	 */
	public void addPersistentEditors(TableItem i) {
		// addPersistentEditor(i,0,new
		// ButtonColumnEditor(getTableEditor(),0,ChangeEvent.COLUMN_REMOVED,this,group.getColumnsTable(),ImageFactory.ICON_DELETE,null,true));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void refreshConnector() {
		IBasicTable table = (IBasicTable) getModel();
		// Retrieving primary key columns
		List<IBasicColumn> pkColumns = Collections.EMPTY_LIST;
		Map<IReference, ColoredText> idxColumns = new HashMap<IReference, ColoredText>();
		Map<IReference, ColoredText> ukColumns = new HashMap<IReference, ColoredText>();
		for (IKeyConstraint key : table.getConstraints()) {
			switch (key.getConstraintType()) {
			case PRIMARY:
				pkColumns = key.getColumns();
				break;
			case UNIQUE:
				for (IReference r : key.getConstrainedColumnsRef()) {
					ColoredText colorText = ukColumns.get(r);
					if (colorText != null) {
						colorText.appendText(" "
								+ Integer.toString(key.getConstrainedColumnsRef().indexOf(r)));
					} else {
						ukColumns.put(
								r,
								new ColoredText(FontFactory.BLACK, Integer.toString(key
										.getConstrainedColumnsRef().indexOf(r))));
					}
				}
			}
		}
		int i = 0;
		for (IIndex index : table.getIndexes()) {
			for (IReference r : index.getIndexedColumnsRef()) {
				ColoredText colorText = idxColumns.get(r);
				if (colorText != null) {
					colorText.appendText(" "
							+ Integer.toString(index.getIndexedColumnsRef().indexOf(r)));
				} else {
					idxColumns.put(
							r,
							new ColoredText(DBGMImages.INDEX_COLORS[i], Integer.toString(index
									.getIndexedColumnsRef().indexOf(r))));
				}
			}
			i = (i + 1) % DBGMImages.INDEX_COLORS.length;
		}
		// Table Columns
		for (IBasicColumn c : table.getColumns()) {
			TableItem item = getOrCreateItem(c, c.getParent().getColumns().indexOf(c));
			item.setImage(ImageFactory.ICON_DELETE);
			refreshCustomColumns(item, c, pkColumns, ukColumns, idxColumns);
		}
		// Disabling editors on update lock flag
		boolean enabled = !table.updatesLocked();
		setPersistentEditorsEnable(enabled);
		group.getAddNewButton().setEnabled(enabled);
		group.getUpButton().setEnabled(enabled);
		group.getDownButton().setEnabled(enabled);

	}

	/**
	 * Refreshes column display.
	 * 
	 * @param c column to refresh
	 * @param pkColumns
	 */
	protected void refreshCustomColumns(TableItem i, IBasicColumn c, List<IBasicColumn> pkColumns,
			Map<IReference, ColoredText> ukColumns, Map<IReference, ColoredText> idxColumns) {

		// Handling primary key flag
		if (pkColumns.contains(c)) {
			i.setText(1, "#");
		} else {
			i.setText(1, "");
		}
		// Indexed columns
		if (idxColumns.keySet().contains(c.getReference())) {
			final ColoredText text = idxColumns.get(c.getReference());
			i.setText(2, text.getText());
			i.setForeground(2, text.getColor());
			i.setFont(2, FontFactory.FONT_BOLD);
		} else {
			i.setText(2, "");
			i.setForeground(2, FontFactory.BLACK);
		}
		// Unique key columns
		if (ukColumns.keySet().contains(c.getReference())) {
			final ColoredText text = ukColumns.get(c.getReference());
			i.setText(3, text.getText());
			i.setForeground(3, text.getColor());
			i.setFont(3, FontFactory.FONT_BOLD);
		} else {
			i.setText(3, "");
			i.setForeground(3, FontFactory.BLACK);
		}
		// Setting fields
		i.setText(4, c.getName());
		// Setting datatype
		i.setText(5, c.getDatatype().getName());
		// i.setImage(DatatypeHelper.getDatatypeIcon(c.getDatatype()));

		if (c.getDatatype().getLength() != 0) {
			i.setText(6, String.valueOf(c.getDatatype().getLength()));
		} else {
			i.setText(6, "");
		}
		if (c.getDatatype().getPrecision() != 0) {
			i.setText(7, String.valueOf(c.getDatatype().getPrecision()));
		} else {
			i.setText(7, "");
		}
		// Setting not null flag
		if (c.isNotNull()) {
			i.setText(8, "X");
		} else {
			i.setText(8, "");
		}
		// Setting default expression
		i.setText(9, c.getDefaultExpr() == null ? "" : c.getDefaultExpr());
		if (c.getDescription() != null) {
			i.setText(10, c.getDescription());
		}
	}

	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IBasicColumn column;

		switch (event) {
		case COLUMN_PK_CHANGED:
			break;
		case NAME_CHANGED:
			column = (IBasicColumn) source;
			if ("".equals(data)) {
				throw new ErrorException("Cannot set an empty name.");
			}
			column.setName((String) data);
			break;
		case COLUMN_TYPE_CHANGED:
			column = (IBasicColumn) source;
			IDatatype t1 = new Datatype(column.getDatatype());
			t1.setName((String) data);
			column.setDatatype(t1);
			break;
		case COLUMN_LENGTH_CHANGED:
			column = (IBasicColumn) source;
			IDatatype sizeType = new Datatype(column.getDatatype());
			try {
				sizeType.setLength("".equals(data) ? 0 : Integer.valueOf((String) data));
			} catch (NumberFormatException e) {
				// In case we got invalid numbers, setting to 0
				sizeType.setLength(0);
			}

			column.setDatatype(sizeType);
			break;
		case COLUMN_PRECISION_CHANGED:
			column = (IBasicColumn) source;
			IDatatype t3 = new Datatype(column.getDatatype());
			try {
				t3.setPrecision("".equals(data) ? 0 : Integer.valueOf((String) data));
			} catch (NumberFormatException e) {
				// Invalid numbers => resetting to 0
				t3.setPrecision(0);
			}

			column.setDatatype(t3);
			break;
		case COLUMN_NOTNULL_CHANGED:
			column = (IBasicColumn) source;
			column.setNotNull(!column.isNotNull());
			break;
		case COLUMN_DEFAULT_CHANGED:
			column = (IBasicColumn) source;
			column.setDefaultExpr((String) data);
			break;
		case DESCRIPTION_CHANGED:
			column = (IBasicColumn) source;
			column.setDescription((String) data);
			break;
		case MODEL_CHANGED:
			column = (IBasicColumn) source;
			refreshConnector();
			// controller.modelChanged(column);
			break;
		case COLUMN_REMOVED:
			column = (IBasicColumn) source;
			// Checking dependencies
			// VCSPlugin.getService(IDependencyService.class).checkDeleteAllowed(column);
			try {
				VCSUIPlugin.getService(IWorkspaceUIService.class).remove(column);
				removeTableItem(column);
			} catch (CancelException e) {
				// Doing nothing, catching only as a workaround for Cocoa RuntimeException bugs
			}
			break;

		}

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IDisplayConnector#getSWTConnector()
	 */
	public Control getSWTConnector() {
		return group.getColumnsTable();
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		IBasicTable t = (IBasicTable) getModel();
		// Ensuring we are allowed to modify our model
		t = VCSUIPlugin.getVersioningUIService().ensureModifiable(t);
		if (e.getSource() == group.getAddNewButton()) {
			getController().newInstance(getModel());
		} else if (e.getSource() == group.getUpButton()) {
			TableItem[] sel = group.getColumnsTable().getSelection();
			if (sel.length > 0) {
				IBasicColumn c = (IBasicColumn) sel[0].getData();
				int pos = c.getParent().getColumns().indexOf(c);
				if (pos > 0) {
					final IBasicColumn otherCol = c.getParent().getColumns().get(pos - 1);
					Collections.swap(c.getParent().getColumns(), pos, pos - 1);
					// Disposing to force recreation
					removeTableItem(c);
					// clean(group.getColumnsTable());
					refreshConnector();
					TableItem i = getTableItem(c);
					group.getColumnsTable().setFocus();
					group.getColumnsTable().setSelection(i);
					group.getUpButton().setFocus();
					c.setRank(pos - 1);
					otherCol.setRank(pos);
					CorePlugin.getIdentifiableDao().save(c.getParent());
				}
			}
		} else if (e.getSource() == group.getDownButton()) {
			TableItem[] sel = group.getColumnsTable().getSelection();
			if (sel.length > 0) {
				IBasicColumn c = (IBasicColumn) sel[0].getData();
				int pos = c.getParent().getColumns().indexOf(c);
				if (pos < c.getParent().getColumns().size() - 1) {
					final IBasicColumn otherCol = c.getParent().getColumns().get(pos + 1);
					Collections.swap(c.getParent().getColumns(), pos, pos + 1);
					// Disposing to force recreation
					removeTableItem(c);
					// clean(group.getColumnsTable());
					refreshConnector();
					TableItem i = getTableItem(c);
					group.getColumnsTable().setSelection(i);
					group.getDownButton().setFocus();
					c.setRank(pos + 1);
					otherCol.setRank(pos);
					CorePlugin.getIdentifiableDao().save(c.getParent());
				}
			}
		}

	}

}
