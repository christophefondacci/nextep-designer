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
import java.util.List;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.nextep.datadesigner.dbgm.model.IBasicTable;
import com.nextep.datadesigner.model.IModelOriented;
import com.nextep.designer.dbgm.ui.DBGMUIMessages;
import com.nextep.designer.dbgm.ui.factories.ValidatorFactory;
import com.nextep.designer.vcs.ui.editors.base.AbstractFormEditor;

/**
 * @author Christophe Fondacci
 */
public class TableFormEditor extends AbstractFormEditor<IBasicTable> implements
		IModelOriented<IBasicTable> {

	private Text nameText, shortNameText, descText;
	private Button temporaryCheck;
	private final List<ControlDecorationSupport> fieldDecorators = new ArrayList<ControlDecorationSupport>();

	/**
	 * 
	 */
	public TableFormEditor() {
		super(
				MessageFormat.format(DBGMUIMessages.getString("editor.table.properties"), "Table"), MessageFormat.format(DBGMUIMessages //$NON-NLS-1$
										.getString("editor.table.propertiesDesc"), "table"), false); //$NON-NLS-1$
	}

	@Override
	protected void createControls(IManagedForm managedForm, FormToolkit toolkit, Composite editor) {

		editor.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		editor.setLayoutData(gd);

		// Name edition section
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.table.name")); //$NON-NLS-1$
		nameText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Short name edition section
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.table.short")); //$NON-NLS-1$
		shortNameText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		shortNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// Description edition section
		toolkit.createLabel(editor, DBGMUIMessages.getString("editor.table.description")); //$NON-NLS-1$
		descText = toolkit.createText(editor, "", SWT.BORDER); //$NON-NLS-1$
		descText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		toolkit.createLabel(editor, ""); //$NON-NLS-1$
		temporaryCheck = toolkit.createButton(editor,
				DBGMUIMessages.getString("editor.table.temporary"), SWT.CHECK); //$NON-NLS-1$
	}

	@Override
	protected void doBindModel(DataBindingContext context) {

		final IBasicTable table = getModel();
		// Name binding
		IObservableValue widgetValue = WidgetProperties.text(SWT.FocusOut).observe(nameText);
		IObservableValue modelValue = PojoProperties.value(IBasicTable.class, "name") //$NON-NLS-1$
				.observe(table);

		UpdateValueStrategy targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setAfterConvertValidator(ValidatorFactory.createNameValidator(false));
		Binding boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy, null);
		registerControlDecoration(ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT));

		// Short name binding
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(shortNameText);
		modelValue = PojoProperties.value(IBasicTable.class, "shortName").observe(table); //$NON-NLS-1$

		targetModelStrategy = new UpdateValueStrategy();
		targetModelStrategy.setAfterConvertValidator(ValidatorFactory.createNameValidator(true));
		boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy, null);
		registerControlDecoration(ControlDecorationSupport.create(boundValue, SWT.TOP | SWT.LEFT));

		// Description binding
		widgetValue = WidgetProperties.text(SWT.FocusOut).observe(descText);
		modelValue = PojoProperties.value(IBasicTable.class, "description").observe(table); //$NON-NLS-1$

		targetModelStrategy = new UpdateValueStrategy();
		boundValue = context.bindValue(widgetValue, modelValue, targetModelStrategy, null);

		// Binding temporary
		widgetValue = WidgetProperties.selection().observe(temporaryCheck);
		modelValue = PojoProperties.value(IBasicTable.class, "temporary", boolean.class).observe( //$NON-NLS-1$
				table);
		boundValue = context.bindValue(widgetValue, modelValue);
	}

	@Override
	protected void doRefresh() {
	}

	@Override
	public void setModel(IBasicTable model) {
		super.setModel(model);
		if (model != null) {
			setTitle(MessageFormat.format(
					DBGMUIMessages.getString("editor.table.properties"), model.getType().getName())); //$NON-NLS-1$
			setDescription(MessageFormat
					.format(DBGMUIMessages.getString("editor.table.propertiesDesc"), model.getType().getName().toLowerCase())); //$NON-NLS-1$
		}
	}
}
