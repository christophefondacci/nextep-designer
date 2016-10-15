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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.dbgm.impl.Datatype;
import com.nextep.datadesigner.dbgm.impl.TypeColumn;
import com.nextep.datadesigner.dbgm.model.IDatatype;
import com.nextep.datadesigner.dbgm.model.ITypeColumn;
import com.nextep.datadesigner.dbgm.model.IUserType;
import com.nextep.datadesigner.gui.impl.TableDisplayConnector;
import com.nextep.datadesigner.gui.impl.editors.ComboColumnEditor;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.vcs.gui.external.VersionedTableEditor;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.factories.ControllerFactory;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.ui.factories.ImageFactory;
import com.nextep.designer.ui.model.ITypedObjectUIController;
import com.nextep.designer.vcs.VCSPlugin;
import com.nextep.designer.vcs.services.IDependencyService;

/**
 * @author Christophe Fondacci
 */
public class TypeColumnEditorGUI extends TableDisplayConnector implements SelectionListener {

	/** Columns management */
	private ColumnSWTComposite columns;
	/** Table edition */
	private NextepTableEditor editor;

	public TypeColumnEditorGUI(IUserType type, ITypedObjectUIController controller) {
		super(type, controller);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		columns = new ColumnSWTComposite(parent, 2, this);
		editor = VersionedTableEditor.handle(columns.getColumnsTable(), getModel());
		TextColumnEditor.handle(editor, 1, ChangeEvent.NAME_CHANGED, this);
		ComboColumnEditor.handle(editor, 2, ChangeEvent.COLUMN_TYPE_CHANGED, this,
				Datatype.getTypes());
		TextColumnEditor.handle(editor, 3, ChangeEvent.COLUMN_LENGTH_CHANGED, this);
		TextColumnEditor.handle(editor, 4, ChangeEvent.COLUMN_PRECISION_CHANGED, this);

		// Initializing our table handlers
		initializeTable(columns.getColumnsTable(), editor);

		// Deletion listener
		columns.getColumnsTable().addListener(SWT.MouseUp, new Listener() {

			@Override
			public void handleEvent(Event event) {
				final IUserType type = (IUserType) getModel();
				if (type.updatesLocked() || !VersionHelper.ensureModifiable(type, false)) {
					return;
				}
				final TableColumn delCol = columns.getColumnsTable().getColumn(0);
				final int delWidth = delCol.getWidth();
				final TableItem[] sel = columns.getColumnsTable().getSelection();
				if (sel.length > 0 && sel[0].getData() instanceof TypeColumn) {
					if (event.x < delWidth) {
						final TypeColumn column = (TypeColumn) sel[0].getData();
						final boolean confirmed = MessageDialog.openQuestion(columns.getShell(),
								MessageFormat.format(DBGMUIMessages
										.getString("table.editor.confirmDelColumnTitle"), column
										.getName()), MessageFormat.format(
										DBGMUIMessages.getString("table.editor.confirmDelColumn"),
										column.getName(), column.getParent().getName()));
						if (confirmed) {
							TypeColumnEditorGUI.this.handleEvent(ChangeEvent.COLUMN_REMOVED,
									(TypeColumn) sel[0].getData(), null);
						}
					}
				}

			}
		});
		// Returning our columns table
		return columns;
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/**
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == columns.getAddNewButton()) {
			getController().newInstance(getModel());
		} else if (e.getSource() == columns.getUpButton()) {
			TableItem[] sel = columns.getColumnsTable().getSelection();
			if (sel.length > 0) {
				ITypeColumn c = (ITypeColumn) sel[0].getData();
				int pos = c.getParent().getColumns().indexOf(c);
				if (pos > 0) {
					Collections.swap(c.getParent().getColumns(), pos, pos - 1);
					// Disposing to force recreation
					// removeTableItem(c);
					clean(columns.getColumnsTable());
					refreshConnector();
					TableItem i = getTableItem(c);
					columns.getColumnsTable().setFocus();
					columns.getColumnsTable().setSelection(i);
					columns.getUpButton().setFocus();
					ControllerFactory.getController(c).save(c);
				}
			}
		} else if (e.getSource() == columns.getDownButton()) {
			TableItem[] sel = columns.getColumnsTable().getSelection();
			if (sel.length > 0) {
				ITypeColumn c = (ITypeColumn) sel[0].getData();
				int pos = c.getParent().getColumns().indexOf(c);
				if (pos < c.getParent().getColumns().size() - 1) {
					Collections.swap(c.getParent().getColumns(), pos, pos + 1);
					// Disposing to force recreation
					// removeTableItem(c);
					clean(columns.getColumnsTable());
					refreshConnector();
					TableItem i = getTableItem(c);
					columns.getColumnsTable().setSelection(i);
					columns.getDownButton().setFocus();
					ControllerFactory.getController(c).save(c);
				}
			}
		}

	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#getSWTConnector()
	 */
	@Override
	public Control getSWTConnector() {
		return columns;
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.IConnector#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		IUserType type = (IUserType) getModel();
		for (ITypeColumn col : type.getColumns()) {
			TableItem i = getOrCreateItem(col);
			i.setImage(ImageFactory.ICON_DELETE);
			i.setText(1, col.getName());
			if (col.getDatatype() != null) {
				i.setText(2, notNull(col.getDatatype().getName()));
				if (col.getDatatype().getLength() != -1) {
					i.setText(3, String.valueOf(col.getDatatype().getLength()));
				} else {
					i.setText(3, "");
				}
				if (col.getDatatype().getPrecision() != -1) {
					i.setText(4, String.valueOf(col.getDatatype().getPrecision()));
				} else {
					i.setText(4, "");
				}
			} else {
				i.setText(2, "");
				i.setText(3, "");
				i.setText(4, "");
			}
			i.setText(5, notNull(col.getDescription()));
		}
		// Disabling editors on update lock flag
		boolean enabled = !type.updatesLocked();
		setPersistentEditorsEnable(enabled);
		columns.getAddNewButton().setEnabled(enabled);
		columns.getUpButton().setEnabled(enabled);
		columns.getDownButton().setEnabled(enabled);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		ITypeColumn column = null;
		switch (event) {
		case NAME_CHANGED:
			((ITypeColumn) source).setName((String) data);
			break;
		case DESCRIPTION_CHANGED:
			((ITypeColumn) source).setDescription((String) data);
			break;
		case COLUMN_TYPE_CHANGED:
			column = (ITypeColumn) source;
			IDatatype t1 = new Datatype(column.getDatatype());
			t1.setName((String) data);
			column.setDatatype(t1);
			break;
		case COLUMN_LENGTH_CHANGED:
			column = (ITypeColumn) source;
			IDatatype sizeType = new Datatype(column.getDatatype());
			sizeType.setLength("".equals(data) ? -1 : Integer.valueOf((String) data));
			column.setDatatype(sizeType);
			break;
		case COLUMN_PRECISION_CHANGED:
			column = (ITypeColumn) source;
			IDatatype t3 = new Datatype(column.getDatatype());
			t3.setPrecision("".equals(data) ? -1 : Integer.valueOf((String) data));
			column.setDatatype(t3);
			break;
		case COLUMN_REMOVED:
			column = (ITypeColumn) source;
			// Checking dependencies
			VCSPlugin.getService(IDependencyService.class).checkDeleteAllowed(column);
			// Cleaning up all items
			clean(columns.getColumnsTable());
			// i.dispose();
			// colItemsMap.remove(column);
			ControllerFactory.getController(column).modelDeleted(column);
			refreshConnector();
			break;
		default:
			refreshConnector();
		}
	}

}
