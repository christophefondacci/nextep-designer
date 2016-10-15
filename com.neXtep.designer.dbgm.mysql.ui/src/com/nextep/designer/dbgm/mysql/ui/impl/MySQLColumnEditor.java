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
package com.nextep.designer.dbgm.mysql.ui.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import com.nextep.datadesigner.dbgm.gui.ColumnEditorGUI;
import com.nextep.datadesigner.dbgm.gui.ColumnSWTComposite;
import com.nextep.datadesigner.dbgm.model.IBasicColumn;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.dbgm.services.DBGMHelper;
import com.nextep.datadesigner.gui.impl.editors.ClickColumnEditor;
import com.nextep.datadesigner.gui.impl.editors.ComboColumnEditor;
import com.nextep.datadesigner.gui.impl.editors.TextColumnEditor;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.datadesigner.model.IReference;
import com.nextep.datadesigner.vcs.gui.external.VersionedTableEditor;
import com.nextep.datadesigner.vcs.services.VersionHelper;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.mysql.model.IMySQLColumn;
import com.nextep.designer.dbgm.mysql.services.IMySqlModelService;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class MySQLColumnEditor extends ColumnEditorGUI {

	private TableColumn colAutoInc;
	private TableColumn colUnsigned;
	private TableColumn colCharset;
	private TableColumn colCollation;
	private ColumnSWTComposite group;
	/** Table editor */
	private NextepTableEditor editor;
	private IMySqlModelService mySqlModelService;

	public MySQLColumnEditor(IBasicTable t, ITypedObjectUIController controller) {
		super(t, controller);
		this.mySqlModelService = CorePlugin.getService(IMySqlModelService.class);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.ColumnEditorGUI#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		group = (ColumnSWTComposite) super.createSWTControl(parent);
		return group;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.ColumnEditorGUI#createCustomColumns()
	 */
	@Override
	protected void createCustomColumns(ColumnSWTComposite group) {
		super.createCustomColumns(group);
		this.group = group;
		colAutoInc = new TableColumn(group.getColumnsTable(), SWT.CENTER, 9);
		colAutoInc.setWidth(60);
		colAutoInc.setText("Auto-inc.");
		colUnsigned = new TableColumn(group.getColumnsTable(), SWT.CENTER, 10);
		colUnsigned.setWidth(60);
		colUnsigned.setText("Unsigned");
		colCharset = new TableColumn(group.getColumnsTable(), SWT.CENTER, 11);
		colCharset.setWidth(60);
		colCharset.setText("Charset");
		colCollation = new TableColumn(group.getColumnsTable(), SWT.CENTER, 12);
		colCollation.setWidth(60);
		colCollation.setText("Collation");
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
		ClickColumnEditor.handle(editor, 9, ChangeEvent.CUSTOM_1, this);
		ClickColumnEditor.handle(editor, 10, ChangeEvent.CUSTOM_2, this);
		ComboColumnEditor.handle(editor, 11, ChangeEvent.CUSTOM_3, this,
				mySqlModelService.getCharsetsList());
		TextColumnEditor.handle(editor, 12, ChangeEvent.CUSTOM_4, this);
		TextColumnEditor.handle(editor, 13, ChangeEvent.COLUMN_DEFAULT_CHANGED, this);
		TextColumnEditor.handle(editor, 14, ChangeEvent.DESCRIPTION_CHANGED, this);
	}

	/**
	 * Gives access to the table editor to class extensions
	 * 
	 * @return table editor
	 */
	protected NextepTableEditor getTableEditor() {
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.ColumnEditorGUI#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		if (source instanceof IMySQLColumn) {
			final IMySQLColumn col = (IMySQLColumn) source;

			switch (event) {
			case CUSTOM_1:
				col.setAutoIncremented(!col.isAutoIncremented());
				refreshConnector();
				return;
			case CUSTOM_2:
				col.getDatatype().setUnsigned(!col.getDatatype().isUnsigned());
				col.notifyListeners(ChangeEvent.MODEL_CHANGED, null);
				// refreshConnector();
				return;
			case CUSTOM_3:
				final String charset = (String) data;
				col.setCharacterSet(charset);
				final String collation = mySqlModelService.getDefaultCollation(charset);
				col.setCollation(collation);
				break;
			case CUSTOM_4:
				col.setCollation((String) data);
				break;
			default:
				super.handleEvent(event, source, data);
			}
		} else {
			super.handleEvent(event, source, data);
		}
	}

	/**
	 * FIXME: remove this copy/paste of super class (because of TableItem columns indexes)
	 * 
	 * @see com.nextep.datadesigner.dbgm.gui.ColumnEditorGUI#refreshCustomColumns(org.eclipse.swt.widgets.TableItem,
	 *      com.nextep.datadesigner.dbgm.model.IBasicColumn, java.util.List)
	 */
	@Override
	protected void refreshCustomColumns(TableItem i, IBasicColumn c, List<IBasicColumn> pkColumns,
			Map<IReference, ColoredText> ukColumns, Map<IReference, ColoredText> idxColumns) {
		super.refreshCustomColumns(i, c, pkColumns, ukColumns, idxColumns);
		final IMySQLColumn mysqlCol = (IMySQLColumn) c;
		// Setting auto increment
		i.setText(9, mysqlCol.isAutoIncremented() ? "X" : "");
		i.setText(10, mysqlCol.getDatatype().isUnsigned() ? "X" : "");
		i.setText(11, notNull(mysqlCol.getCharacterSet()));
		i.setText(12, notNull(mysqlCol.getCollation()));
		// Setting default expression
		i.setText(13, c.getDefaultExpr() == null ? "" : c.getDefaultExpr());
		if (c.getDescription() != null) {
			i.setText(14, c.getDescription());
		}
	}
}
