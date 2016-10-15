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
package com.nextep.designer.dbgm.mysql.ui.impl;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.mysql.model.IMySQLTable;
import com.nextep.designer.dbgm.mysql.services.IMySqlModelService;
import com.nextep.designer.dbgm.mysql.ui.DBMYMUIMessages;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * MySQL-specific table editor allowing edition of {@link IMySQLTable} information.
 * 
 * @author Christophe Fondacci
 */
public class MySQLTableFormEditor extends AbstractFormEditor<IMySQLTable> implements
		IModelOriented<IMySQLTable> {

	private CCombo engineCombo, charsetCombo, collationCombo;

	public MySQLTableFormEditor() {
		super(DBMYMUIMessages.getString("editor.table.sectionTitle"), DBMYMUIMessages //$NON-NLS-1$
				.getString("editor.table.sectionDesc"), false); //$NON-NLS-1$
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		editor.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		editor.setLayoutData(gd);

		final IMySqlModelService modelService = CorePlugin.getService(IMySqlModelService.class);

		// Creating the storage engine editor
		toolkit.createLabel(editor, DBMYMUIMessages.getString("editor.table.storage")); //$NON-NLS-1$
		engineCombo = new CCombo(editor, SWT.BORDER);
		toolkit.adapt(engineCombo);
		engineCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (String engine : modelService.getEngineList()) {
			engineCombo.add(engine);
		}

		// Creating the charset editor
		toolkit.createLabel(editor, DBMYMUIMessages.getString("editor.table.charset")); //$NON-NLS-1$
		charsetCombo = new CCombo(editor, SWT.BORDER);
		toolkit.adapt(charsetCombo);
		charsetCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (String charset : modelService.getCharsetNamesList()) {
			charsetCombo.add(charset);
		}

		// Creating the collation editor
		toolkit.createLabel(editor, DBMYMUIMessages.getString("editor.table.collation")); //$NON-NLS-1$
		collationCombo = new CCombo(editor, SWT.BORDER);
		toolkit.adapt(collationCombo);
		collationCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		final IMySQLTable table = getModel();
		// Engine binding
		IObservableValue widgetValue = WidgetProperties.text().observe(engineCombo);
		IObservableValue modelValue = PojoProperties.value(IMySQLTable.class, "engine").observe( //$NON-NLS-1$
				table);
		context.bindValue(widgetValue, modelValue);

		// Charset binding
		widgetValue = WidgetProperties.text().observe(charsetCombo);
		modelValue = PojoProperties.value(IMySQLTable.class, "characterSet").observe(table); //$NON-NLS-1$
		UpdateValueStrategy modelUpdateStrategy = new UpdateValueStrategy();
		modelUpdateStrategy.setConverter(MySQLConverterFactory
				.createNameToCodeCharsetConverter(collationCombo));
		UpdateValueStrategy targetUpdateStrategy = new UpdateValueStrategy();
		targetUpdateStrategy.setConverter(MySQLConverterFactory.createCodeToNameCharsetConverter());
		context.bindValue(widgetValue, modelValue, modelUpdateStrategy, targetUpdateStrategy);

		// Collation binding
		widgetValue = WidgetProperties.text().observe(collationCombo);
		modelValue = PojoProperties.value(IMySQLTable.class, "collation").observe(table); //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue);
	}

	@Override
	protected void doRefresh() {

	}
}
