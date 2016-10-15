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

import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import com.nextep.datadesigner.dbgm.gui.TableEditorGUI;
import com.nextep.datadesigner.gui.impl.editors.ComboEditor;
import com.nextep.datadesigner.model.ChangeEvent;
import com.nextep.datadesigner.model.IObservable;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;
import com.nextep.designer.dbgm.mysql.services.IMySqlModelService;
import com.nextep.designer.ui.model.ITypedObjectUIController;

/**
 * @author Christophe Fondacci
 */
public class MySQLTableEditorGUI extends TableEditorGUI {

	private Label engineLabel;
	private Combo engineCombo;
	private Label charsetLabel;
	private Combo charsetCombo;
	private Label collationLabel;
	private Combo collationCombo;
	private IMySqlModelService mySqlModelService;

	public MySQLTableEditorGUI(IMySQLTable table, ITypedObjectUIController controller) {
		super(table, controller);
		mySqlModelService = CorePlugin.getService(IMySqlModelService.class);
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.TableEditorGUI#createPropertiesGroup(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Composite createPropertiesGroup(Composite parent) {
		Composite editor = super.createPropertiesGroup(parent);
		GridData labelData = new GridData();
		labelData.horizontalAlignment = GridData.FILL;
		labelData.verticalAlignment = GridData.CENTER;
		engineLabel = new Label(editor, SWT.NONE);
		engineLabel.setText("MySQL storage engine : ");
		engineLabel.setLayoutData(labelData);

		engineCombo = new Combo(editor, SWT.NONE);
		engineCombo.add("InnoDB");
		engineCombo.add("MyISAM");
		engineCombo.select(0);
		ComboEditor.handle(engineCombo, ChangeEvent.CUSTOM_8, this);

		final Composite charsetComposite = new Composite(editor, SWT.NONE);
		addNoMarginLayout(charsetComposite, 4);
		charsetComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		charsetLabel = new Label(charsetComposite, SWT.NONE);
		charsetLabel.setText("Table character set : ");
		charsetLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		charsetCombo = new Combo(charsetComposite, SWT.READ_ONLY);
		final List<String> charsets = mySqlModelService.getCharsetsList();
		final List<String> charsetNames = mySqlModelService.getCharsetNamesList();
		for (int i = 0; i < charsets.size(); i++) {
			charsetCombo.add(charsetNames.get(i));
			charsetCombo.setData(charsetNames.get(i), charsets.get(i));
		}
		GridData charsetComboData = new GridData(SWT.FILL, SWT.FILL, true, false);
		charsetComboData.minimumWidth = 100;
		charsetCombo.setLayoutData(charsetComboData);
		ComboEditor.handle(charsetCombo, ChangeEvent.CUSTOM_9, this);

		collationLabel = new Label(charsetComposite, SWT.NONE);
		collationLabel.setText("Collation : ");
		collationLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		collationCombo = new Combo(charsetComposite, SWT.NONE);
		GridData collationComboData = new GridData(SWT.FILL, SWT.FILL, true, false);
		collationComboData.minimumWidth = 100;
		collationCombo.setLayoutData(collationComboData);
		ComboEditor.handle(collationCombo, ChangeEvent.CUSTOM_10, this);
		// charsetEditor = new
		// FieldEditor(editor,"Character set : ",1,1,true,this,ChangeEvent.CUSTOM_9);
		return editor;
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.TableEditorGUI#refreshConnector()
	 */
	@Override
	public void refreshConnector() {
		super.refreshConnector();
		IMySQLTable table = (IMySQLTable) getModel();
		collationCombo.removeAll();
		engineCombo.setText(notNull(table.getEngine()));
		final String charset = table.getCharacterSet();
		for (int i = 0; i < charsetCombo.getItemCount(); i++) {
			if (charsetCombo.getData(charsetCombo.getItem(i)).equals(charset)) {
				charsetCombo.select(i);
				break;
			}
		}

		final String defaultCollation = mySqlModelService.getDefaultCollation(charset);
		if (defaultCollation != null) {
			collationCombo.add(defaultCollation);
		}

		collationCombo.setText(notNull(table.getCollation()));
		engineCombo.setEnabled(!table.updatesLocked());
		charsetCombo.setEnabled(!table.updatesLocked());
		collationCombo.setEnabled(!table.updatesLocked());
	}

	/**
	 * @see com.nextep.datadesigner.dbgm.gui.TableEditorGUI#handleEvent(com.nextep.datadesigner.model.ChangeEvent,
	 *      com.nextep.datadesigner.model.IObservable, java.lang.Object)
	 */
	@Override
	public void handleEvent(ChangeEvent event, IObservable source, Object o) {
		IMySQLTable table = (IMySQLTable) getModel();
		switch (event) {
		case CUSTOM_8:
			table.setEngine((String) o);
			break;
		case CUSTOM_9:
			final String charset = (String) charsetCombo.getData((String) o);
			table.setCharacterSet(charset);
			String defaultCollation = mySqlModelService.getDefaultCollation(charset);
			table.setCollation(defaultCollation);
			break;
		case CUSTOM_10:
			table.setCollation((String) o);
			break;
		default:
			super.handleEvent(event, source, o);
		}
	}

}
