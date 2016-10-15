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
package com.nextep.designer.sqlgen.ui.dbgm;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import com.nextep.datadesigner.dbgm.model.IView;
import com.nextep.datadesigner.gui.impl.swt.FieldEditor;
import com.nextep.datadesigner.gui.model.ControlledDisplayConnector;
import com.nextep.datadesigner.gui.model.NextepTableEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.dbgm.sql.ISelectStatement;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class ViewEditorGUI extends ControlledDisplayConnector implements SelectionListener {

	// private QueryEditorGUI queryGUI;

	private Composite editor = null; // @jve:decl-index=0:visual-constraint="10,10"
	private FieldEditor nameEditor;
	private FieldEditor descEditor;
	private NextepTableEditor tabEditor;

	public ViewEditorGUI(IView view, ITypedObjectUIController controller) {
		super(view, controller);
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#createSWTControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createSWTControl(Composite parent) {
		IView view = (IView) getModel();
		// Creating composite control
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		editor = new Composite(parent, SWT.NONE);
		editor.setLayout(gridLayout);
		editor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// Creating name and description editors
		nameEditor = new FieldEditor(editor, "Name : ", 1, 1, true, this, ChangeEvent.NAME_CHANGED);
		nameEditor.getText().setTextLimit(30);
		descEditor = new FieldEditor(editor, "Description : ", 1, 1, true, this,
				ChangeEvent.DESCRIPTION_CHANGED);
		descEditor.getText().setTextLimit(200);

		// Creating query editor
		// queryGUI = new QueryEditorGUI(view.getSQLDefinition());
		// Control c = queryGUI.create(editor);
		// Designer.getListenerService().registerListener(this,
		// (ISelectStatement)queryGUI.getModel(), this);
		// GridData queryData = new GridData();
		// queryData.horizontalSpan=2;
		// queryData.grabExcessHorizontalSpace=true;
		// queryData.horizontalAlignment=GridData.FILL;
		// queryData.grabExcessVerticalSpace=true;
		// queryData.verticalAlignment=GridData.FILL;
		// c.setLayoutData(queryData);

		// // Adding view alias column
		// TableColumn viewAliasColumn = new TableColumn(queryGUI.getSelectedColsTable(),SWT.NONE);
		// viewAliasColumn.setWidth(60);
		// viewAliasColumn.setText("View alias");
		// tabEditor = NextepTableEditor.handle(queryGUI.getSelectedColsTable());
		// TextColumnEditor.handle(tabEditor, 2, ChangeEvent.ALIAS_CHANGED, this);

		return editor;
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
		IView view = (IView) getModel();
		nameEditor.setText(view.getName());
		descEditor.setText(view.getDescription());

		boolean enabled = !view.updatesLocked();
		nameEditor.getText().setEnabled(enabled);
		descEditor.getText().setEnabled(enabled);
		// // queryGUI.refreshConnector();
		// // queryGUI.setModel(view.getSQLDefinition());
		// Iterator<String> aliasIt = view.getColumnAliases().iterator();
		// int i=0;
		// for(TableItem item : queryGUI.getSelectedColsTable().getItems()) {
		// if(aliasIt.hasNext()) {
		// item.setText(2,notNull(aliasIt.next()));
		// } else {
		// item.setText(2,"");
		// }
		// i++;
		// }
		// while(aliasIt.hasNext()) {
		// view.removeColumnAlias(i++);
		// }
	}

	/**
	 * @see com.nextep.datadesigner.gui.model.ControlledDisplayConnector#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object data) {
		IView view = (IView) getModel();
		switch (event) {
		case NAME_CHANGED:
			view.setName(notNull((String) data));
			break;
		case DESCRIPTION_CHANGED:
			view.setDescription(notNull((String) data));
			break;
		case MODEL_CHANGED:
			if (source instanceof ISelectStatement) {
				view.setSQLDefinition(((ISelectStatement) source).getSql());
				break;
			}
			refreshConnector();
			break;
		case ALIAS_CHANGED:
			view.setColumnAlias(tabEditor.getSelectedIndex(), (String) data);
			break;
		}
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

	}
}
