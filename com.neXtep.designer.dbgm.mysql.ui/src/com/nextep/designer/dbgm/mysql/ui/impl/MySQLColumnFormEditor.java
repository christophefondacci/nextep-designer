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
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.designer.core.CorePlugin;
import com.nextep.designer.dbgm.mysql.model.IMySQLColumn;
import com.nextep.designer.dbgm.mysql.services.IMySqlModelService;
import com.nextep.designer.dbgm.mysql.ui.DBMYMUIMessages;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * MySQL-specific editor allowing the edition of {@link IMySQLColumn} specific information.
 * 
 * @author Christophe Fondacci
 */
public class MySQLColumnFormEditor extends AbstractFormEditor<IMySQLColumn> {

	private CCombo charsetCombo, collationCombo;
	private Button autoIncButton, unsignedButton;

	public MySQLColumnFormEditor() {
		super(
				DBMYMUIMessages.getString("editor.column.sectionTitle"), DBMYMUIMessages.getString("editor.column.sectionDesc"), //$NON-NLS-1$ //$NON-NLS-2$
				true);
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {
		// Auto-increment checkbox
		toolkit.createLabel(editor, ""); //$NON-NLS-1$
		autoIncButton = toolkit.createButton(editor,
				DBMYMUIMessages.getString("editor.column.autoinc"), SWT.CHECK); //$NON-NLS-1$

		// Unsigned checkbox
		toolkit.createLabel(editor, DBMYMUIMessages.getString("editor.column.datatype")); //$NON-NLS-1$
		unsignedButton = toolkit.createButton(editor,
				DBMYMUIMessages.getString("editor.column.unqigned"), SWT.CHECK); //$NON-NLS-1$

		final IMySqlModelService modelService = CorePlugin.getService(IMySqlModelService.class);

		toolkit.createLabel(editor, DBMYMUIMessages.getString("editor.column.charset")); //$NON-NLS-1$
		charsetCombo = new CCombo(editor, SWT.BORDER);
		toolkit.adapt(charsetCombo);
		charsetCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		for (String charset : modelService.getCharsetNamesList()) {
			charsetCombo.add(charset);
		}

		toolkit.createLabel(editor, DBMYMUIMessages.getString("editor.column.collation")); //$NON-NLS-1$
		collationCombo = new CCombo(editor, SWT.BORDER);
		toolkit.adapt(collationCombo);
		collationCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

	}

	@Override
	protected void doBindModel(DataBindingContext context) {
		IObservableValue selectionValue = ViewersObservables
				.observeSingleSelection(getSelectionProvider());

		// Binding charset
		IObservableValue widgetValue = WidgetProperties.text().observe(charsetCombo);
		IObservableValue modelValue = PojoObservables.observeDetailValue(selectionValue,
				"characterSet", String.class); //$NON-NLS-1$
		UpdateValueStrategy modelUpdateStrategy = new UpdateValueStrategy();
		modelUpdateStrategy.setConverter(MySQLConverterFactory
				.createNameToCodeCharsetConverter(collationCombo));
		UpdateValueStrategy targetUpdateStrategy = new UpdateValueStrategy();
		targetUpdateStrategy.setConverter(MySQLConverterFactory.createCodeToNameCharsetConverter());
		context.bindValue(widgetValue, modelValue, modelUpdateStrategy, targetUpdateStrategy);

		// Binding autoinc
		widgetValue = WidgetProperties.selection().observe(autoIncButton);
		modelValue = PojoObservables.observeDetailValue(selectionValue,
				"autoIncremented", Boolean.class); //.value(IBasicColumn.class, "name").observe( //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue, null, null);

		// Binding unsigned
		widgetValue = WidgetProperties.selection().observe(unsignedButton);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "datatype.unsigned", //$NON-NLS-1$
				Boolean.class);
		context.bindValue(widgetValue, modelValue, null, null);

		// Binding collation
		widgetValue = WidgetProperties.text().observe(collationCombo);
		modelValue = PojoObservables.observeDetailValue(selectionValue, "collation", String.class); //$NON-NLS-1$
		context.bindValue(widgetValue, modelValue, null, null);
	}

	@Override
	protected void doRefresh() {

	}
}
